package com.tterrag.registrate;

import com.google.common.base.Preconditions;
import com.google.common.collect.*;
import com.mojang.serialization.Codec;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.ItemProviderEntry;
import net.minecraft.Util;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.loading.DatagenModLoader;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.*;
import com.tterrag.registrate.builders.*;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.CreativeModeTabModifier;
import com.tterrag.registrate.util.DebugMarkers;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.*;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class AbstractRegistrate<S extends AbstractRegistrate<S>> {
    private static final Logger log = LogManager.getLogger(AbstractRegistrate.class);
    private final Table<ResourceKey<? extends Registry<?>>, String, Registration<?, ?>> registrations = HashBasedTable.create();
    private final Multimap<Pair<String, ResourceKey<? extends Registry<?>>>, NonNullConsumer<?>> registerCallbacks = HashMultimap.create();
    private final Multimap<ResourceKey<? extends Registry<?>>, Runnable> afterRegisterCallbacks = HashMultimap.create();
    private final Set<ResourceKey<? extends Registry<?>>> completedRegistrations = new HashSet();
    private final Table<Pair<String, ResourceKey<? extends Registry<?>>>, ProviderType<?>, Consumer<? extends RegistrateProvider>> datagensByEntry = HashBasedTable.create();
    private final ListMultimap<ProviderType<?>, @NonnullType NonNullConsumer<? extends RegistrateProvider>> datagens = ArrayListMultimap.create();
    private final Multimap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> creativeModeTabModifiers = ArrayListMultimap.create();
    private ResourceKey<CreativeModeTab> defaultCreativeModeTab;
    private final NonNullSupplier<Boolean> doDatagen;
    private final String modid;
    @Nullable
    private String currentName;
    private boolean skipErrors;
    @Nullable
    private RegistrateDataProvider provider;
    private final NonNullSupplier<List<Pair<String, String>>> extraLang;

    public static boolean isDevEnvironment() {
        return FMLEnvironment.naming.equals("mcp");
    }

    protected AbstractRegistrate(String modid) {
        this.defaultCreativeModeTab = CreativeModeTabs.SEARCH;
        this.doDatagen = NonNullSupplier.lazy(DatagenModLoader::isRunningDataGen);
        this.extraLang = NonNullSupplier.lazy(() -> {
            List<Pair<String, String>> ret = new ArrayList();
            this.addDataGenerator(ProviderType.LANG, (prov) -> ret.forEach((p) -> prov.add((String)p.getKey(), (String)p.getValue())));
            return ret;
        });
        this.modid = modid;
    }

    protected final S self() {
        return (S)this;
    }

    public IEventBus getModEventBus() {
        return FMLJavaModLoadingContext.get().getModEventBus();
    }

    protected S registerEventListeners(IEventBus bus) {
        Consumer<RegisterEvent> onRegister = this::onRegister;
        Consumer<RegisterEvent> onRegisterLate = this::onRegisterLate;
        bus.addListener(onRegister);
        bus.addListener(EventPriority.LOWEST, onRegisterLate);
        bus.addListener(this::onBuildCreativeModeTabContents);
        OneTimeEventReceiver.addModListener(this, FMLCommonSetupEvent.class, ($) -> {
            OneTimeEventReceiver.unregister(this, onRegister, RegisterEvent.class);
            OneTimeEventReceiver.unregister(this, onRegisterLate, RegisterEvent.class);
        });
        if ((Boolean)this.doDatagen.get()) {
            OneTimeEventReceiver.addModListener(this, GatherDataEvent.class, this::onData);
        }

        return (S)this.self();
    }

    protected void onRegister(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> type = event.getRegistryKey();
        if (type == null) {
            log.debug(DebugMarkers.REGISTER, "Skipping invalid registry with no supertype: " + event.getRegistryKey().location());
        } else {
            if (!this.registerCallbacks.isEmpty()) {
                this.registerCallbacks.asMap().forEach((k, v) -> log.warn("Found {} unused register callback(s) for entry {} [{}]. Was the entry ever registered?", v.size(), k.getLeft(), ((ResourceKey)k.getRight()).location()));
                this.registerCallbacks.clear();
                if (isDevEnvironment()) {
                    throw new IllegalStateException("Found unused register callbacks, see logs");
                }
            }

            Map<String, Registration<?, ?>> registrationsForType = this.registrations.row(type);
            if (registrationsForType.size() > 0) {
                log.debug(DebugMarkers.REGISTER, "({}) Registering {} known objects of type {}", this.getModid(), registrationsForType.size(), type.location());

                for(Map.Entry<String, AbstractRegistrate<S>.Registration<?, ?>> e : registrationsForType.entrySet()) {
                    try {
                        ((Registration)e.getValue()).register(event);
                        log.debug(DebugMarkers.REGISTER, "Registered {} to registry {}", ((Registration)e.getValue()).getName(), event.getRegistryKey().location());
                    } catch (Exception ex) {
                        ResourceLocation var10000 = ((Registration)e.getValue()).getName();
                        String err = "Unexpected error while registering entry " + var10000 + " to registry " + event.getRegistryKey().location();
                        if (!this.skipErrors) {
                            throw new RuntimeException(err, ex);
                        }

                        log.error(DebugMarkers.REGISTER, err);
                    }
                }
            }

        }
    }

    protected void onRegisterLate(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> type = event.getRegistryKey();
        Collection<Runnable> callbacks = this.afterRegisterCallbacks.get(type);
        callbacks.forEach(Runnable::run);
        callbacks.clear();
        this.completedRegistrations.add(type);
    }

    protected void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        Objects.requireNonNull(event);
        Supplier var10002 = event::getFlags;
        Objects.requireNonNull(event);
        BooleanSupplier var10003 = event::hasPermissions;
        Objects.requireNonNull(event);
        CreativeModeTabModifier modifier = new CreativeModeTabModifier(var10002, var10003, event::accept);
        this.creativeModeTabModifiers.forEach((key, value) -> {
            if (event.getTabKey().equals(key)) {
                value.accept(modifier);
            }

        });
    }

    protected void onData(GatherDataEvent event) {
        event.getGenerator().addProvider(true, this.provider = new RegistrateDataProvider(this, this.modid, event));
    }

    protected String currentName() {
        String name = this.currentName;
        Objects.requireNonNull(name, "Current name not set");
        return name;
    }

    public <R, T extends R> RegistryEntry<T> get(ResourceKey<? extends Registry<R>> type) {
        return this.get(this.currentName(), type);
    }

    public <R, T extends R> RegistryEntry<T> get(String name, ResourceKey<? extends Registry<R>> type) {
        return this.getRegistration(name, type).getDelegate();
    }

    public <R, T extends R> RegistryEntry<T> getOptional(String name, ResourceKey<? extends Registry<R>> type) {
        AbstractRegistrate<S>.Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, type);
        return reg == null ? RegistryEntry.empty() : reg.getDelegate();
    }

    @Nullable
    private <R, T extends R> AbstractRegistrate<S>.Registration<R, T> getRegistrationUnchecked(String name, ResourceKey<? extends Registry<R>> type) {
        return (Registration)this.registrations.get(type, name);
    }

    private <R, T extends R> AbstractRegistrate<S>.Registration<R, T> getRegistration(String name, ResourceKey<? extends Registry<R>> type) {
        AbstractRegistrate<S>.Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, type);
        if (reg != null) {
            return reg;
        } else {
            throw new IllegalArgumentException("Unknown registration " + name + " for type " + type.location());
        }
    }

    public <R> Collection<RegistryEntry<R>> getAll(ResourceKey<? extends Registry<R>> type) {
        return (Collection)this.registrations.row(type).values().stream().map((r) -> r.getDelegate()).collect(Collectors.toList());
    }

    public <R, T extends R> S addRegisterCallback(String name, ResourceKey<? extends Registry<R>> registryType, NonNullConsumer<? super T> callback) {
        AbstractRegistrate<S>.Registration<R, T> reg = this.<R, T>getRegistrationUnchecked(name, registryType);
        if (reg == null) {
            this.registerCallbacks.put(Pair.of(name, registryType), callback);
        } else {
            reg.addRegisterCallback(callback);
        }

        return (S)this.self();
    }

    public <R> S addRegisterCallback(ResourceKey<? extends Registry<R>> registryType, Runnable callback) {
        this.afterRegisterCallbacks.put(registryType, callback);
        return (S)this.self();
    }

    public <R> boolean isRegistered(ResourceKey<? extends Registry<R>> registryType) {
        return this.completedRegistrations.contains(registryType);
    }

    public <P extends RegistrateProvider> Optional<P> getDataProvider(ProviderType<P> type) {
        RegistrateDataProvider provider = this.provider;
        if (provider != null) {
            return provider.getSubProvider(type);
        } else {
            throw new IllegalStateException("Cannot get data provider before datagen is started");
        }
    }

    public <P extends RegistrateProvider, R> S setDataGenerator(Builder<R, ?, ?, ?> builder, ProviderType<? extends P> type, NonNullConsumer<? extends P> cons) {
        return (S)this.setDataGenerator(builder.getName(), builder.getRegistryKey(), type, cons);
    }

    public <P extends RegistrateProvider, R> S setDataGenerator(String entry, ResourceKey<? extends Registry<R>> registryType, ProviderType<? extends P> type, NonNullConsumer<? extends P> cons) {
        if (!(Boolean)this.doDatagen.get()) {
            return (S)this.self();
        } else {
            Consumer<? extends RegistrateProvider> existing = (Consumer)this.datagensByEntry.put(Pair.of(entry, registryType), type, cons);
            if (existing != null) {
                this.datagens.remove(type, existing);
            }

            return (S)this.addDataGenerator(type, cons);
        }
    }

    public <T extends RegistrateProvider> S addDataGenerator(ProviderType<? extends T> type, NonNullConsumer<? extends T> cons) {
        if ((Boolean)this.doDatagen.get()) {
            this.datagens.put(type, cons);
        }

        return (S)this.self();
    }

    public MutableComponent addLang(String type, ResourceLocation id, String localizedName) {
        return this.addRawLang(Util.makeDescriptionId(type, id), localizedName);
    }

    public MutableComponent addLang(String type, ResourceLocation id, String suffix, String localizedName) {
        return this.addRawLang(Util.makeDescriptionId(type, id) + "." + suffix, localizedName);
    }

    public MutableComponent addRawLang(String key, String value) {
        if ((Boolean)this.doDatagen.get()) {
            ((List)this.extraLang.get()).add(Pair.of(key, value));
        }

        return Component.translatable(key);
    }

    private Optional<Pair<String, ResourceKey<? extends Registry<?>>>> getEntryForGenerator(ProviderType<?> type, NonNullConsumer<? extends RegistrateProvider> generator) {
        for(Map.Entry<Pair<String, ResourceKey<? extends Registry<?>>>, Consumer<? extends RegistrateProvider>> e : this.datagensByEntry.column(type).entrySet()) {
            if (e.getValue() == generator) {
                return Optional.of((Pair)e.getKey());
            }
        }

        return Optional.empty();
    }

    public <T extends RegistrateProvider> void genData(ProviderType<? extends T> type, T gen) {
        if ((Boolean)this.doDatagen.get()) {
            this.datagens.get(type).forEach((cons) -> {
                Optional<Pair<String, ResourceKey<? extends Registry<?>>>> entry = null;
                if (log.isEnabled(Level.DEBUG, DebugMarkers.DATA)) {
                    entry = this.getEntryForGenerator(type, cons);
                    if (entry.isPresent()) {
                        log.debug(DebugMarkers.DATA, "Generating data of type {} for entry {} [{}]", RegistrateDataProvider.getTypeName(type), ((Pair)entry.get()).getLeft(), ((ResourceKey)((Pair)entry.get()).getRight()).location());
                    } else {
                        log.debug(DebugMarkers.DATA, "Generating unassociated data of type {} ({})", RegistrateDataProvider.getTypeName(type), type);
                    }
                }

                try {
                    cons.accept(gen);
                } catch (Exception e) {
                    if (entry == null) {
                        entry = this.getEntryForGenerator(type, cons);
                    }

                    Message err;
                    if (entry.isPresent()) {
                        err = log.getMessageFactory().newMessage("Unexpected error while running data generator of type {} for entry {} [{}]", new Object[]{RegistrateDataProvider.getTypeName(type), ((Pair)entry.get()).getLeft(), ((ResourceKey)((Pair)entry.get()).getRight()).location()});
                    } else {
                        err = log.getMessageFactory().newMessage("Unexpected error while running unassociated data generator of type {} ({})", new Object[]{RegistrateDataProvider.getTypeName(type), type});
                    }

                    if (!this.skipErrors) {
                        throw new RuntimeException(err.getFormattedMessage(), e);
                    }

                    log.error(err);
                }

            });
        }
    }

    public S skipErrors(boolean skipErrors) {
        if (skipErrors && !isDevEnvironment()) {
            log.error("Ignoring skipErrors(true) as this is not a development environment!");
        } else {
            this.skipErrors = skipErrors;
        }

        return (S)this.self();
    }

    public S object(String name) {
        this.currentName = name;
        return (S)this.self();
    }

    public S defaultCreativeTab(ResourceKey<CreativeModeTab> creativeModeTab) {
        this.defaultCreativeModeTab = creativeModeTab;
        return (S)this.self();
    }

    public S modifyCreativeModeTab(ResourceKey<CreativeModeTab> creativeModeTab, Consumer<CreativeModeTabModifier> modifier) {
        this.creativeModeTabModifiers.put(creativeModeTab, modifier);
        return (S)this.self();
    }

    public S transform(NonNullUnaryOperator<S> func) {
        return (S)(func.apply(this.self()));
    }

    public <R, T extends R, P, S2 extends Builder<R, T, P, S2>> S2 transform(NonNullFunction<S, S2> func) {
        return (S2)(func.apply(this.self()));
    }

    public <R, T extends R, P, S2 extends Builder<R, T, P, S2>> S2 entry(NonNullBiFunction<String, BuilderCallback, S2> factory) {
        return (S2)this.entry(this.currentName(), (callback) -> (Builder)factory.apply(this.currentName(), callback));
    }

    public <R, T extends R, P, S2 extends Builder<R, T, P, S2>> S2 entry(String name, NonNullFunction<BuilderCallback, S2> factory) {
        return (S2)(factory.apply(this::accept));
    }

    protected <R, T extends R> RegistryEntry<T> accept(String name, ResourceKey<? extends Registry<R>> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> creator, NonNullFunction<DeferredRegister<T>, ? extends RegistryEntry<T>> entryFactory) {
        AbstractRegistrate<S>.Registration<R, T> reg = new Registration<R, T>(new ResourceLocation(this.modid, name), type, creator, entryFactory);
        log.debug(DebugMarkers.REGISTER, "Captured registration for entry {}:{} of type {}", this.getModid(), name, type.location());
        this.registerCallbacks.removeAll(Pair.of(name, type)).forEach((callback) -> reg.addRegisterCallback(callback));
        this.registrations.put(type, name, reg);
        return reg.getDelegate();
    }

    public <R> ResourceKey<Registry<R>> makeRegistry(String name, Supplier<RegistryBuilder<R>> builder) {
        ResourceKey<Registry<R>> registryId = ResourceKey.createRegistryKey(new ResourceLocation(this.getModid(), name));
        OneTimeEventReceiver.addModListener(this, NewRegistryEvent.class, (e) -> e.create(((RegistryBuilder)builder.get()).setName(registryId.location())));
        return registryId;
    }

    public <R> ResourceKey<Registry<R>> makeDatapackRegistry(String name, Codec<R> codec) {
        return this.makeDatapackRegistry(name, codec, (Codec)null);
    }

    public <R> ResourceKey<Registry<R>> makeDatapackRegistry(String name, Codec<R> codec, @Nullable Codec<R> networkCodec) {
        ResourceKey<Registry<R>> registryId = ResourceKey.createRegistryKey(new ResourceLocation(this.getModid(), name));
        OneTimeEventReceiver.addModListener(this, DataPackRegistryEvent.NewRegistry.class, (event) -> event.dataPackRegistry(registryId, codec, networkCodec));
        return registryId;
    }

    public <R, T extends R> RegistryEntry<T> simple(ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return this.simple(this.currentName(), registryType, factory);
    }

    public <R, T extends R> RegistryEntry<T> simple(String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return this.simple(this, name, registryType, factory);
    }

    public <R, T extends R, P> RegistryEntry<T> simple(P parent, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return this.simple(parent, this.currentName(), registryType, factory);
    }

    public <R, T extends R, P> RegistryEntry<T> simple(P parent, String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return this.generic(parent, name, registryType, factory).register();
    }

    public <R, T extends R> NoConfigBuilder<R, T, S> generic(ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return this.generic((Object)this.self(), registryType, factory);
    }

    public <R, T extends R> NoConfigBuilder<R, T, S> generic(String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return this.<R, T, S>generic(this.self(), name, registryType, factory);
    }

    public <R, T extends R, P> NoConfigBuilder<R, T, P> generic(P parent, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return this.<R, T, P>generic(parent, this.currentName(), registryType, factory);
    }

    public <R, T extends R, P> NoConfigBuilder<R, T, P> generic(P parent, String name, ResourceKey<Registry<R>> registryType, NonNullSupplier<T> factory) {
        return (NoConfigBuilder)this.entry(name, (callback) -> new NoConfigBuilder(this, parent, name, callback, registryType, factory));
    }

    public <T extends Item> ItemBuilder<T, S> item(NonNullFunction<Item.Properties, T> factory) {
        return this.item(this.self(), factory);
    }

    public <T extends Item> ItemBuilder<T, S> item(String name, NonNullFunction<Item.Properties, T> factory) {
        return this.<T, S>item(this.self(), name, factory);
    }

    public <T extends Item, P> ItemBuilder<T, P> item(P parent, NonNullFunction<Item.Properties, T> factory) {
        return this.<T, P>item(parent, this.currentName(), factory);
    }

    public <T extends Item, P> ItemBuilder<T, P> item(P parent, String name, NonNullFunction<Item.Properties, T> factory) {
        return (ItemBuilder)this.entry(name, (callback) -> (ItemBuilder)ItemBuilder.create(this, parent, name, callback, factory).transform((builder) -> this.defaultCreativeModeTab == null ? builder : builder.tab(this.defaultCreativeModeTab)));
    }

    public <T extends Block> BlockBuilder<T, S> block(NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return this.block((Object)this.self(), factory);
    }

    public <T extends Block> BlockBuilder<T, S> block(String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return this.<T, S>block(this.self(), name, factory);
    }

    public <T extends Block, P> BlockBuilder<T, P> block(P parent, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return this.<T, P>block(parent, this.currentName(), factory);
    }

    public <T extends Block, P> BlockBuilder<T, P> block(P parent, String name, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return (BlockBuilder)this.entry(name, (callback) -> BlockBuilder.create(this, parent, name, callback, factory));
    }

    public <T extends Entity> EntityBuilder<T, S> entity(EntityType.EntityFactory<T> factory, MobCategory classification) {
        return this.entity((Object)this.self(), factory, classification);
    }

    public <T extends Entity> EntityBuilder<T, S> entity(String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return this.<T, S>entity(this.self(), name, factory, classification);
    }

    public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return this.<T, P>entity(parent, this.currentName(), factory, classification);
    }

    public <T extends Entity, P> EntityBuilder<T, P> entity(P parent, String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return (EntityBuilder)this.entry(name, (callback) -> EntityBuilder.create(this, parent, name, callback, factory, classification));
    }

    public <T extends BlockEntity> BlockEntityBuilder<T, S> blockEntity(BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return this.blockEntity((Object)this.self(), factory);
    }

    public <T extends BlockEntity> BlockEntityBuilder<T, S> blockEntity(String name, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return this.<T, S>blockEntity(this.self(), name, factory);
    }

    public <T extends BlockEntity, P> BlockEntityBuilder<T, P> blockEntity(P parent, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return this.<T, P>blockEntity(parent, this.currentName(), factory);
    }

    public <T extends BlockEntity, P> BlockEntityBuilder<T, P> blockEntity(P parent, String name, BlockEntityBuilder.BlockEntityFactory<T> factory) {
        return (BlockEntityBuilder)this.entry(name, (callback) -> BlockEntityBuilder.create(this, parent, name, callback, factory));
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid() {
        return this.fluid((Object)this.self());
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(FluidBuilder.FluidTypeFactory typeFactory) {

        return this.fluid((Object)this.self(), (FluidBuilder.FluidTypeFactory)typeFactory);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(NonNullSupplier<FluidType> fluidType) {
        return this.fluid((Object)this.self(), (NonNullSupplier)fluidType);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return this.fluid((Object)this.self(), (ResourceLocation)stillTexture, (ResourceLocation)flowingTexture);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory) {
        return this.fluid((Object)this.self(), (ResourceLocation)stillTexture, (ResourceLocation)flowingTexture, typeFactory);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return this.fluid((Object)this.self(), (ResourceLocation)stillTexture, (ResourceLocation)flowingTexture, fluidType);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid((Object)this.self(), (ResourceLocation)stillTexture, (ResourceLocation)flowingTexture, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid((Object)this.self(), (ResourceLocation)stillTexture, flowingTexture, typeFactory, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid((Object)this.self(), (ResourceLocation)stillTexture, flowingTexture, (NonNullSupplier)fluidType, fluidFactory);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(String name) {
        return this.fluid((Object)this.self(), (String)name);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(String name, FluidBuilder.FluidTypeFactory typeFactory) {
        return this.fluid((Object)this.self(), (String)name, typeFactory);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(String name, NonNullSupplier<FluidType> fluidType) {
        return this.fluid((Object)this.self(), (String)name, (NonNullSupplier)fluidType);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return this.fluid((Object)this.self(), (String)name, (ResourceLocation)stillTexture, flowingTexture);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory) {
        return this.fluid((Object)this.self(), (String)name, stillTexture, (ResourceLocation)flowingTexture, typeFactory);
    }

    public FluidBuilder<ForgeFlowingFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return this.fluid((Object)this.self(), (String)name, stillTexture, (ResourceLocation)flowingTexture, fluidType);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid((Object)this.self(), (String)name, stillTexture, (ResourceLocation)flowingTexture, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid(this.self(), name, stillTexture, flowingTexture, typeFactory, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder<T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid(this.self(), name, stillTexture, flowingTexture, (NonNullSupplier)fluidType, fluidFactory);
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent) {
        return this.fluid(parent, this.currentName());
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, FluidBuilder.FluidTypeFactory typeFactory) {
        return this.fluid(parent, this.currentName(), typeFactory);
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, NonNullSupplier<FluidType> fluidType) {
        return this.fluid(parent, this.currentName(), fluidType);
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return this.fluid(parent, this.currentName(), stillTexture, flowingTexture);
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory) {
        return this.fluid(parent, this.currentName(), stillTexture, flowingTexture, typeFactory);
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return this.fluid(parent, this.currentName(), stillTexture, flowingTexture, fluidType);
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid(parent, this.currentName(), stillTexture, flowingTexture, fluidFactory);
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid(parent, this.currentName(), stillTexture, flowingTexture, typeFactory, fluidFactory);
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid(parent, this.currentName(), stillTexture, flowingTexture, fluidType, fluidFactory);
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name) {
        return this.fluid(parent, name, new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_still"), new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_flow"));
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, FluidBuilder.FluidTypeFactory typeFactory) {
        return this.fluid(parent, name, new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_still"), new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_flow"), typeFactory);
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, NonNullSupplier<FluidType> fluidType) {
        return this.fluid(parent, name, new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_still"), new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_flow"), fluidType);
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return (FluidBuilder)this.entry(name, (callback) -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture));
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory) {
        return (FluidBuilder)this.entry(name, (callback) -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, typeFactory));
    }

    public <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return (FluidBuilder)this.entry(name, (callback) -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, fluidType));
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return (FluidBuilder)this.entry(name, (callback) -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, fluidFactory));
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidBuilder.FluidTypeFactory typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return (FluidBuilder)this.entry(name, (callback) -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory));
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return (FluidBuilder)this.entry(name, (callback) -> FluidBuilder.create(this, parent, name, callback, stillTexture, flowingTexture, fluidType, fluidFactory));
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<T, SC, S> menu(MenuBuilder.MenuFactory<T> factory, NonNullSupplier<MenuBuilder.ScreenFactory<T, SC>> screenFactory) {
        return this.menu(this.currentName(), factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<T, SC, S> menu(String name, MenuBuilder.MenuFactory<T> factory, NonNullSupplier<MenuBuilder.ScreenFactory<T, SC>> screenFactory) {
        return this.menu(this.self(), name, factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<T, SC, P> menu(P parent, MenuBuilder.MenuFactory<T> factory, NonNullSupplier<MenuBuilder.ScreenFactory<T, SC>> screenFactory) {
        return this.menu(parent, this.currentName(), factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<T, SC, P> menu(P parent, String name, MenuBuilder.MenuFactory<T> factory, NonNullSupplier<MenuBuilder.ScreenFactory<T, SC>> screenFactory) {
        return (MenuBuilder)this.entry(name, (callback) -> new MenuBuilder(this, parent, name, callback, factory, screenFactory));
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<T, SC, S> menu(MenuBuilder.ForgeMenuFactory<T> factory, NonNullSupplier<MenuBuilder.ScreenFactory<T, SC>> screenFactory) {
        return this.menu(this.currentName(), factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>> MenuBuilder<T, SC, S> menu(String name, MenuBuilder.ForgeMenuFactory<T> factory, NonNullSupplier<MenuBuilder.ScreenFactory<T, SC>> screenFactory) {
        return this.menu(this.self(), name, factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<T, SC, P> menu(P parent, MenuBuilder.ForgeMenuFactory<T> factory, NonNullSupplier<MenuBuilder.ScreenFactory<T, SC>> screenFactory) {
        return this.menu(parent, this.currentName(), factory, screenFactory);
    }

    public <T extends AbstractContainerMenu, SC extends Screen & MenuAccess<T>, P> MenuBuilder<T, SC, P> menu(P parent, String name, MenuBuilder.ForgeMenuFactory<T> factory, NonNullSupplier<MenuBuilder.ScreenFactory<T, SC>> screenFactory) {
        return (MenuBuilder)this.entry(name, (callback) -> new MenuBuilder(this, parent, name, callback, factory, screenFactory));
    }

    public <T extends Enchantment> EnchantmentBuilder<T, S> enchantment(EnchantmentCategory type, EnchantmentBuilder.EnchantmentFactory<T> factory) {
        return this.enchantment((Object)this.self(), type, factory);
    }

    public <T extends Enchantment> EnchantmentBuilder<T, S> enchantment(String name, EnchantmentCategory type, EnchantmentBuilder.EnchantmentFactory<T> factory) {
        return this.<T, S>enchantment(this.self(), name, type, factory);
    }

    public <T extends Enchantment, P> EnchantmentBuilder<T, P> enchantment(P parent, EnchantmentCategory type, EnchantmentBuilder.EnchantmentFactory<T> factory) {
        return this.<T, P>enchantment(parent, this.currentName(), type, factory);
    }

    public <T extends Enchantment, P> EnchantmentBuilder<T, P> enchantment(P parent, String name, EnchantmentCategory type, EnchantmentBuilder.EnchantmentFactory<T> factory) {
        return (EnchantmentBuilder)this.entry(name, (callback) -> EnchantmentBuilder.create(this, parent, name, callback, type, factory));
    }

    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, S> defaultCreativeTab() {
        return this.defaultCreativeTab(this.self());
    }

    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, S> defaultCreativeTab(String name) {
        return this.defaultCreativeTab(this.self(), name);
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(P parent) {
        return this.defaultCreativeTab(parent, this.currentName());
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(P parent, String name) {
        return this.defaultCreativeTab(parent, name, (tab) -> {
        });
    }

    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, S> defaultCreativeTab(Consumer<CreativeModeTab.Builder> config) {
        return this.defaultCreativeTab(this.self(), (Consumer)config);
    }

    public NoConfigBuilder<CreativeModeTab, CreativeModeTab, S> defaultCreativeTab(String name, Consumer<CreativeModeTab.Builder> config) {
        return this.defaultCreativeTab(this.self(), name, config);
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(P parent, Consumer<CreativeModeTab.Builder> config) {
        return this.defaultCreativeTab(parent, this.currentName(), config);
    }

    public <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(P parent, String name, Consumer<CreativeModeTab.Builder> config) {
        this.defaultCreativeModeTab = ResourceKey.create(Registries.CREATIVE_MODE_TAB, new ResourceLocation(this.modid, name));
        return this.<CreativeModeTab, CreativeModeTab, P>generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            CreativeModeTab.Builder builder = CreativeModeTab.builder().icon(() -> (ItemStack)this.getAll(Registries.ITEM).stream().findFirst().map(ItemEntry::cast).map(ItemProviderEntry::asStack).orElse(new ItemStack(Items.AIR))).title(this.addLang("itemGroup", this.defaultCreativeModeTab.location(), RegistrateLangProvider.toEnglishName(name)));
            config.accept(builder);
            return builder.build();
        });
    }

    public String getModid() {
        return this.modid;
    }

    private final class Registration<R, T extends R> {
        private final ResourceLocation name;
        private final ResourceKey<? extends Registry<R>> type;
        private final NonNullSupplier<? extends T> creator;
        private final RegistryEntry<T> delegate;
        private final List<NonNullConsumer<? super T>> callbacks = new ArrayList();

        Registration(ResourceLocation name, ResourceKey<? extends Registry<R>> type, NonNullSupplier<? extends T> creator, NonNullFunction<DeferredRegister<T>, ? extends RegistryEntry<T>> entryFactory) {
            this.name = name;
            this.type = type;
            this.creator = creator.lazy();
            this.delegate = (RegistryEntry)entryFactory.apply(DeferredRegister.create(name, type.location(), AbstractRegistrate.this.getModid()));
        }

        void register(RegisterEvent event) {
            T entry = (T)this.creator.get();
            event.register(this.type, (rh) -> rh.register(this.name, entry));
            this.delegate.updateReference(event);
            this.callbacks.forEach((c) -> c.accept(entry));
            this.callbacks.clear();
        }

        void addRegisterCallback(NonNullConsumer<? super T> callback) {
            Preconditions.checkNotNull(callback, "Callback must not be null");
            this.callbacks.add(callback);
        }

        public ResourceLocation getName() {
            return this.name;
        }

        public ResourceKey<? extends Registry<R>> getType() {
            return this.type;
        }

        public NonNullSupplier<? extends T> getCreator() {
            return this.creator;
        }

        public RegistryEntry<T> getDelegate() {
            return this.delegate;
        }

        public boolean equals(Object o) {
            if (o == this) {
                return true;
            } else if (!(o instanceof Registration)) {
                return false;
            } else {
                AbstractRegistrate<?>.Registration<?, ?> other = (Registration)o;
                Object this$name = this.getName();
                Object other$name = other.getName();
                if (this$name == null) {
                    if (other$name != null) {
                        return false;
                    }
                } else if (!this$name.equals(other$name)) {
                    return false;
                }

                Object this$type = this.getType();
                Object other$type = other.getType();
                if (this$type == null) {
                    if (other$type != null) {
                        return false;
                    }
                } else if (!this$type.equals(other$type)) {
                    return false;
                }

                Object this$creator = this.getCreator();
                Object other$creator = other.getCreator();
                if (this$creator == null) {
                    if (other$creator != null) {
                        return false;
                    }
                } else if (!this$creator.equals(other$creator)) {
                    return false;
                }

                Object this$delegate = this.getDelegate();
                Object other$delegate = other.getDelegate();
                if (this$delegate == null) {
                    if (other$delegate != null) {
                        return false;
                    }
                } else if (!this$delegate.equals(other$delegate)) {
                    return false;
                }

                Object this$callbacks = this.callbacks;
                Object other$callbacks = other.callbacks;
                if (this$callbacks == null) {
                    if (other$callbacks != null) {
                        return false;
                    }
                } else if (!this$callbacks.equals(other$callbacks)) {
                    return false;
                }

                return true;
            }
        }

        public int hashCode() {
            int PRIME = 59;
            int result = 1;
            Object $name = this.getName();
            result = result * 59 + ($name == null ? 43 : $name.hashCode());
            Object $type = this.getType();
            result = result * 59 + ($type == null ? 43 : $type.hashCode());
            Object $creator = this.getCreator();
            result = result * 59 + ($creator == null ? 43 : $creator.hashCode());
            Object $delegate = this.getDelegate();
            result = result * 59 + ($delegate == null ? 43 : $delegate.hashCode());
            Object $callbacks = this.callbacks;
            result = result * 59 + ($callbacks == null ? 43 : $callbacks.hashCode());
            return result;
        }

        public String toString() {
            ResourceLocation var10000 = this.getName();
            return "AbstractRegistrate.Registration(name=" + var10000 + ", type=" + this.getType() + ", creator=" + this.getCreator() + ", delegate=" + this.getDelegate() + ", callbacks=" + this.callbacks + ")";
        }
    }
}

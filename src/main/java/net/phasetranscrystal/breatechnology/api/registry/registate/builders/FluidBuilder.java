package net.phasetranscrystal.breatechnology.api.registry.registate.builders;

import net.minecraft.Util;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.phasetranscrystal.breatechnology.api.registry.registate.BTClientFluidTypeExtensions;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

import javax.annotation.Nullable;

public class FluidBuilder<T extends BaseFlowingFluid, P> extends AbstractBuilder<Fluid, T, P, FluidBuilder<T, P>> {

    // region
    public static <P> FluidBuilder<BaseFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, FluidBuilder::defaultFluidType, BTClientFluidTypeExtensions::new, BaseFlowingFluid.Flowing::new);
    }

    public static <P> FluidBuilder<BaseFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor, ClientExtensionFactory clientExtensionFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, FluidBuilder::defaultFluidType, clientExtensionFactory, BaseFlowingFluid.Flowing::new);
    }

    public static <P> FluidBuilder<BaseFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor, Function<FluidType.Properties, FluidType> typeFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, typeFactory, BTClientFluidTypeExtensions::new, BaseFlowingFluid.Flowing::new);
    }

    public static <P> FluidBuilder<BaseFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor, Function<FluidType.Properties, FluidType> typeFactory, ClientExtensionFactory clientExtensionFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, typeFactory, clientExtensionFactory, BaseFlowingFluid.Flowing::new);
    }

    public static <P> FluidBuilder<BaseFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor, NonNullSupplier<FluidType> fluidType) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, fluidType, BTClientFluidTypeExtensions::new, BaseFlowingFluid.Flowing::new);
    }

    public static <P> FluidBuilder<BaseFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor, NonNullSupplier<FluidType> fluidType, ClientExtensionFactory clientExtensionFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, fluidType, clientExtensionFactory, BaseFlowingFluid.Flowing::new);
    }

    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor,
                                                                            NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, FluidBuilder::defaultFluidType, BTClientFluidTypeExtensions::new, fluidFactory);
    }

    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor,
                                                                            NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory,
                                                                            ClientExtensionFactory clientExtensionFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, FluidBuilder::defaultFluidType, clientExtensionFactory, fluidFactory);
    }

    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor,
                                                                            Function<FluidType.Properties, FluidType> typeFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        FluidBuilder<T, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, typeFactory, BTClientFluidTypeExtensions::new, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor,
                                                                            Function<FluidType.Properties, FluidType> typeFactory, ClientExtensionFactory clientExtensionFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        FluidBuilder<T, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, typeFactory, clientExtensionFactory, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor,
                                                                            NonNullSupplier<FluidType> fluidType, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        FluidBuilder<T, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, fluidType, BTClientFluidTypeExtensions::new, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    public static <T extends BaseFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor,
                                                                            NonNullSupplier<FluidType> fluidType, ClientExtensionFactory clientExtensionFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        FluidBuilder<T, P> ret = new FluidBuilder<>(owner, parent, name, callback, stillTexture, flowingTexture, tintColor, fluidType, clientExtensionFactory, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }
    // endregion

    private final String sourceName, bucketName;

    private final ResourceLocation stillTexture, flowingTexture;
    private int tintColor = 0xFF00FFFF;

    @FunctionalInterface
    public interface ClientExtensionFactory {

        IClientFluidTypeExtensions create(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor);
    }

    private final ClientExtensionFactory clientExtensionFactory;

    private final NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory;

    @Nullable
    private final NonNullSupplier<FluidType> fluidType;

    @Nullable
    private Boolean defaultSource, defaultBlock, defaultBucket;

    private NonNullConsumer<FluidType.Properties> typeProperties = $ -> {};

    private NonNullConsumer<BaseFlowingFluid.Properties> fluidProperties;

    private @NotNull Supplier<RenderType> layer = () -> RenderType.TRANSLUCENT;

    private boolean registerType;

    @Nullable
    private NonNullSupplier<? extends BaseFlowingFluid> source;
    private final List<TagKey<Fluid>> tags = new ArrayList<>();

    public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor, Function<FluidType.Properties, FluidType> typeFactory, ClientExtensionFactory clientExtensionFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
        this.clientExtensionFactory = clientExtensionFactory;
        this.fluidFactory = fluidFactory;
        this.fluidType = NonNullSupplier.lazy(() -> typeFactory.apply(makeTypeProperties()));
        this.registerType = true;

        String bucketName = this.bucketName;
        this.fluidProperties = p -> p.bucket(() -> owner.get(bucketName, Registries.ITEM).get())
                .block(() -> owner.<Block, LiquidBlock>get(name, Registries.BLOCK).get());
    }

    public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor, @org.jetbrains.annotations.Nullable NonNullSupplier<FluidType> fluidType, ClientExtensionFactory clientExtensionFactory, NonNullFunction<BaseFlowingFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, Registries.FLUID);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
        this.clientExtensionFactory = clientExtensionFactory;
        this.fluidFactory = fluidFactory;
        this.fluidType = fluidType;
        this.registerType = false; // Don't register if we have a fluid from outside.

        String bucketName = this.bucketName;
        this.fluidProperties = p -> p.bucket(() -> owner.get(bucketName, Registries.ITEM).get())
                .block(() -> owner.<Block, LiquidBlock>get(name, Registries.BLOCK).get());
    }

    public FluidBuilder<T, P> properties(NonNullConsumer<FluidType.Properties> cons) {
        typeProperties = typeProperties.andThen(cons);
        return this;
    }

    public FluidBuilder<T, P> fluidProperties(NonNullConsumer<BaseFlowingFluid.Properties> cons) {
        fluidProperties = fluidProperties.andThen(cons);
        return this;
    }

    public FluidBuilder<T, P> defaultLang() {
        return lang(f -> f.getFluidType().getDescriptionId(), RegistrateLangProvider.toEnglishName(sourceName));
    }

    public FluidBuilder<T, P> lang(String name) {
        return lang(f -> f.getFluidType().getDescriptionId(), name);
    }

    public FluidBuilder<T, P> defaultSource() {
        if (this.defaultSource != null) {
            throw new IllegalStateException("Cannot set a default source after a custom source has been created");
        }
        this.defaultSource = true;
        return this;
    }

    public FluidBuilder<T, P> source(NonNullFunction<BaseFlowingFluid.Properties, ? extends BaseFlowingFluid> factory) {
        this.defaultSource = false;
        this.source = NonNullSupplier.lazy(() -> factory.apply(makeProperties()));
        return this;
    }

    public FluidBuilder<T, P> defaultBlock() {
        if (this.defaultBlock != null) {
            throw new IllegalStateException("Cannot set a default block after a custom block has been created");
        }
        this.defaultBlock = true;
        return this;
    }

    public BlockBuilder<LiquidBlock, FluidBuilder<T, P>> block() {
        return block(LiquidBlock::new);
    }

    public <B extends LiquidBlock> BlockBuilder<B, FluidBuilder<T, P>> block(NonNullBiFunction<T, BlockBehaviour.Properties, ? extends B> factory) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        final NonNullSupplier<T> supplier = asSupplier();
        final var lightLevel = Lazy.of(() -> fluidType.get().getLightLevel());
        final ToIntFunction<BlockState> lightLevelInt = $ -> lightLevel.get();
        return getOwner().<B, FluidBuilder<T, P>>block(this, sourceName, p -> factory.apply(supplier.get(), p))
                .properties(p -> BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable())
                .properties(p -> p.lightLevel(lightLevelInt))
                .blockstate((ctx, prov) -> prov.simpleBlock(ctx.getEntry(), prov.models().getBuilder(sourceName)
                        .texture("particle", stillTexture)));
    }

    @Beta
    public FluidBuilder<T, P> noBlock() {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        }
        this.defaultBlock = false;
        return this;
    }

    public FluidBuilder<T, P> defaultBucket() {
        if (this.defaultBucket != null) {
            throw new IllegalStateException("Cannot set a default bucket after a custom bucket has been created");
        }
        defaultBucket = true;
        return this;
    }

    public ItemBuilder<BucketItem, FluidBuilder<T, P>> bucket() {
        return bucket(BucketItem::new);
    }

    public <I extends BucketItem> ItemBuilder<I, FluidBuilder<T, P>> bucket(NonNullBiFunction<BaseFlowingFluid, Item.Properties, ? extends I> factory) {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        // TODO: Can we find a way to circumvent this limitation?
        if (source == null) {
            throw new IllegalStateException("Cannot create a bucket before creating a source block");
        }
        return getOwner().<I, FluidBuilder<T, P>>item(this, bucketName, p -> factory.apply(source.get(), p))
                .properties(p -> p.craftRemainder(Items.BUCKET).stacksTo(1))
                .model((ctx, prov) -> prov.generated(ctx::getEntry, ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), "item/" + bucketName)));
    }

    @Beta
    public FluidBuilder<T, P> noBucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        }
        this.defaultBucket = false;
        return this;
    }

    @SafeVarargs
    public final FluidBuilder<T, P> tag(TagKey<Fluid>... tags) {
        FluidBuilder<T, P> ret = this.tag(ProviderType.FLUID_TAGS, tags);
        if (this.tags.isEmpty()) {
            ret.getOwner().<RegistrateTagsProvider<Fluid>, Fluid>setDataGenerator(ret.sourceName, getRegistryKey(), ProviderType.FLUID_TAGS,
                    prov -> this.tags.stream().map(prov::addTag).forEach(p -> p.add(getSource().builtInRegistryHolder().key())));
        }
        this.tags.addAll(Arrays.asList(tags));
        return ret;
    }

    @SafeVarargs
    public final FluidBuilder<T, P> removeTag(TagKey<Fluid>... tags) {
        this.tags.removeAll(Arrays.asList(tags));
        return this.removeTag(ProviderType.FLUID_TAGS, tags);
    }

    private BaseFlowingFluid getSource() {
        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        Preconditions.checkNotNull(source, "Fluid has no source block: " + sourceName);
        return source.get();
    }

    private BaseFlowingFluid.Properties makeProperties() {
        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        BaseFlowingFluid.Properties ret = new BaseFlowingFluid.Properties(fluidType, source == null ? null : source::get, asSupplier());
        fluidProperties.accept(ret);
        return ret;
    }

    private FluidType.Properties makeTypeProperties() {
        FluidType.Properties properties = FluidType.Properties.create();
        Optional<RegistryEntry<Block, Block>> block = getOwner().getOptional(sourceName, Registries.BLOCK);
        this.typeProperties.accept(properties);

        // Force the translation key after the user callback runs
        // This is done because we need to remove the lang data generator if using the block key,
        // and if it was possible to undo this change, it might result in the user translation getting
        // silently lost, as there's no good way to check whether the translation key was changed.
        // TODO improve this?
        if (block.isPresent() && block.get().isBound()) {
            properties.descriptionId(block.get().get().getDescriptionId());
            setData(ProviderType.LANG, NonNullBiConsumer.noop());
        } else {
            properties.descriptionId(Util.makeDescriptionId("fluid", ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), sourceName)));
        }

        return properties;
    }

    public FluidBuilder<T, P> setRenderType(Supplier<RenderType> layer) {
        this.layer = Objects.requireNonNull(layer, "RenderType cannot be null");
        return this;
    }

    @Override
    protected @NotNull T createEntry() {
        return fluidFactory.apply(makeProperties());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public @NotNull FluidEntry<T> register() {
        // Check the fluid has a type.
        if (this.fluidType != null) {
            // Register the type.
            if (this.registerType) {
                var entry = getOwner().simple(this, this.sourceName, NeoForgeRegistries.Keys.FLUID_TYPES, this.fluidType);
                getOwner().getModEventBus().addListener(RegisterClientExtensionsEvent.class, event -> {
                    event.registerFluidType(clientExtensionFactory.create(stillTexture, flowingTexture, tintColor), entry);
                });
            }
        } else {
            throw new IllegalStateException("Fluid must have a type: " + getName());
        }

        if (defaultSource == Boolean.TRUE) {
            source(BaseFlowingFluid.Source::new);
        }
        if (defaultBlock == Boolean.TRUE) {
            block().register();
        }
        if (defaultBucket == Boolean.TRUE) {
            bucket().register();
        }

        NonNullSupplier<? extends BaseFlowingFluid> source = this.source;
        if (source != null) {
            getCallback().accept(sourceName, Registries.FLUID, (FluidBuilder) this, source);
        } else {
            throw new IllegalStateException("Fluid must have a source version: " + getName());
        }

        getOwner().getModEventBus().addListener(FMLClientSetupEvent.class, event -> {
            event.enqueueWork(() -> {
                ItemBlockRenderTypes.setRenderLayer(getEntry(), layer.get());
                ItemBlockRenderTypes.setRenderLayer(getSource(), layer.get());
            });
        });

        return (FluidEntry<T>) super.register();
    }

    @Override
    protected @NotNull RegistryEntry<Fluid, T> createEntryWrapper(@NotNull DeferredHolder<Fluid, T> delegate) {
        return new FluidEntry<>(getOwner(), delegate);
    }

    private static FluidType defaultFluidType(FluidType.Properties properties) {
        return new FluidType(properties);
    }
}

package net.phasetranscrystal.breatechnology.api.registry.registate;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.Registrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.builders.NoConfigBuilder;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.api.definition.MetaBlockDefinition;
import net.phasetranscrystal.breatechnology.api.registry.BTRegistries;
import net.phasetranscrystal.breatechnology.api.utils.FormattingUtil;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class BTRegistrate extends AbstractRegistrate<BTRegistrate> {
    private static final Map<String, BTRegistrate> EXISTING_REGISTRATES = new Object2ObjectOpenHashMap<>();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    protected BTRegistrate(String modid) {
        super(modid);
    }

    public static @NotNull BTRegistrate create(@NotNull String modId) {
        return innerCreate(modId, true);
    }

    @ApiStatus.Internal
    public static BTRegistrate createIgnoringListenerErrors(String modId) {
        return innerCreate(modId, false);
    }

    private static BTRegistrate innerCreate(String modId, boolean strict) {
        if (EXISTING_REGISTRATES.containsKey(modId)) {
            return EXISTING_REGISTRATES.get(modId);
        }
        var registrate = new BTRegistrate(modId);
        Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modId).map(ModContainer::getEventBus);
        if (strict) {
            modEventBus.ifPresentOrElse(registrate::registerEventListeners, () -> {
                String message = "# [GTRegistrate] Failed to register eventListeners for mod " + modId + ", This should be reported to this mod's dev #";
                String hashtags = "#".repeat(message.length());
                BreaTechnology.LOGGER.fatal(hashtags);
                BreaTechnology.LOGGER.fatal(message);
                BreaTechnology.LOGGER.fatal(hashtags);
            });
        } else {
            registrate.registerEventListeners(modEventBus.orElse(BreaTechnology.btModBus));
        }
        EXISTING_REGISTRATES.put(modId, registrate);
        return registrate;
    }

    @Override
    public @NotNull BTRegistrate registerEventListeners(@NotNull IEventBus bus) {
        if (!registered.getAndSet(true)) {
            return (BTRegistrate) super.registerEventListeners(bus);
        }
        return this;
    }

    //TODO:add extra methods to BTRegistrate
    @Nullable
    private RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> currentTab;
    private static final Map<RegistryEntry<?, ?>, @Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab>> TAB_LOOKUP = new IdentityHashMap<>();

    public void creativeModeTab(Supplier<RegistryEntry<CreativeModeTab, ? extends CreativeModeTab>> currentTab) {
        this.currentTab = currentTab.get();
    }

    public void creativeModeTab(RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> currentTab) {
        this.currentTab = currentTab;
    }

    public boolean isInCreativeTab(RegistryEntry<?, ?> entry, RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> tab) {
        return TAB_LOOKUP.get(entry) == tab;
    }

    public void setCreativeTab(RegistryEntry<?, ?> entry, @Nullable RegistryEntry<CreativeModeTab, ? extends CreativeModeTab> tab) {
        TAB_LOOKUP.put(entry, tab);
    }

    protected <R, T extends R> @NotNull RegistryEntry<R, T> accept(@NotNull String name, @NotNull ResourceKey<? extends Registry<R>> type,
                                                                   @NotNull Builder<R, T, ?, ?> builder, @NotNull NonNullSupplier<? extends T> creator,
                                                                   @NotNull NonNullFunction<DeferredHolder<R, T>, ? extends RegistryEntry<R, T>> entryFactory) {
        RegistryEntry<R, T> entry = super.accept(name, type, builder, creator, entryFactory);

        if (this.currentTab != null) {
            TAB_LOOKUP.put(entry, this.currentTab);
        }

        return entry;
    }

    public <P> @NotNull NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> defaultCreativeTab(@NotNull P parent, @NotNull String name,
                                                                                                @NotNull Consumer<CreativeModeTab.Builder> config) {
        return createCreativeModeTab(parent, name, config);
    }

    protected <P> NoConfigBuilder<CreativeModeTab, CreativeModeTab, P> createCreativeModeTab(P parent, String name,
                                                                                             Consumer<CreativeModeTab.Builder> config) {
        return this.generic(parent, name, Registries.CREATIVE_MODE_TAB, () -> {
            var builder = CreativeModeTab.builder()
                    .icon(() -> getAll(Registries.ITEM).stream().findFirst().map(ItemEntry::cast)
                            .map(ItemEntry::asStack).orElse(new ItemStack(Items.AIR)));
            config.accept(builder);
            return builder.build();
        });
    }

    @Override
    public <T extends Item> @NotNull ItemBuilder<T, BTRegistrate> item(@NotNull String name,
                                                                       @NotNull NonNullFunction<Item.Properties, T> factory) {
        return super.item(name, factory).lang(FormattingUtil.toEnglishName(name.replaceAll("\\.", "_")));
    }

    /*

    public <DEFINITION extends MachineDefinition> MachineBuilder<DEFINITION> machine(String name,
                                                                                     Function<ResourceLocation, DEFINITION> definitionFactory,
                                                                                     Function<IMachineBlockEntity, MetaMachine> metaMachine,
                                                                                     BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                                                                                     BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                                                                     TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new MachineBuilder<>(this, name, definitionFactory, metaMachine, blockFactory, itemFactory,
                blockEntityFactory);
    }

    public MachineBuilder<MachineDefinition> machine(String name,
                                                     Function<IMachineBlockEntity, MetaMachine> metaMachine) {
        return new MachineBuilder<>(this, name, MachineDefinition::createDefinition, metaMachine,
                MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public Stream<MachineBuilder<MachineDefinition>> machine(String name,
                                                             BiFunction<IMachineBlockEntity, Integer, MetaMachine> metaMachine,
                                                             int... tiers) {
        return Arrays.stream(tiers)
                .mapToObj(tier -> new MachineBuilder<>(this, name + "." + GTValues.VN[tier].toLowerCase(Locale.ROOT),
                        MachineDefinition::createDefinition, holder -> metaMachine.apply(holder, tier),
                        MetaMachineBlock::new, MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity));
    }

    public MultiblockMachineBuilder multiblock(String name,
                                               Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine,
                                               BiFunction<BlockBehaviour.Properties, MultiblockMachineDefinition, IMachineBlock> blockFactory,
                                               BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                                               TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        return new MultiblockMachineBuilder(this, name, metaMachine, blockFactory, itemFactory,
                blockEntityFactory);
    }

    public MultiblockMachineBuilder multiblock(String name,
                                               Function<IMachineBlockEntity, ? extends MultiblockControllerMachine> metaMachine) {
        return new MultiblockMachineBuilder(this, name, metaMachine, MetaMachineBlock::new,
                MetaMachineItem::new, MetaMachineBlockEntity::createBlockEntity);
    }

    public SoundEntryBuilder sound(String name) {
        return new SoundEntryBuilder(GTCEu.id(name));
    }

    public SoundEntryBuilder sound(ResourceLocation name) {
        return new SoundEntryBuilder(name);
    }

    */
}
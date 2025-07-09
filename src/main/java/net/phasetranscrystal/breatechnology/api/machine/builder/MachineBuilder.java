package net.phasetranscrystal.breatechnology.api.machine.builder;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Tolerate;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.api.blockentity.MetaMachineBlockEntity;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;
import net.phasetranscrystal.breatechnology.api.gui.egitor.EditableMachineUI;
import net.phasetranscrystal.breatechnology.api.item.MetaMachineItem;
import net.phasetranscrystal.breatechnology.api.machine.IMachineBlock;
import net.phasetranscrystal.breatechnology.api.machine.IMachineBlockEntity;
import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;
import net.phasetranscrystal.breatechnology.api.machine.RotationState;
import net.phasetranscrystal.breatechnology.api.machine.feature.IRecipeLogicMachine;
import net.phasetranscrystal.breatechnology.api.recipe.BTRecipeType;
import net.phasetranscrystal.breatechnology.api.recipe.capability.RecipeCapability;
import net.phasetranscrystal.breatechnology.api.recipe.kind.BTRecipe;
import net.phasetranscrystal.breatechnology.api.renderer.BTRendererProvider;
import net.phasetranscrystal.breatechnology.config.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.*;

public class MachineBuilder<DEFINITION extends MetaMachineDefinition<?>> {
    protected final AbstractRegistrate<?> owner;
    protected final String name;
    protected BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory;
    protected final Function<IMachineBlockEntity, MetaMachine> machineFactory;
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory;
    protected final BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory;
    protected final TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory;

    public MachineBuilder(AbstractRegistrate<?> owner, String name,
                          BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory,
                          Function<IMachineBlockEntity, MetaMachine> machineFactory,
                          BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                          BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                          TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        this.owner = owner;
        this.name = name;
        this.definitionFactory = definitionFactory;
        this.machineFactory = machineFactory;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.blockEntityFactory = blockEntityFactory;
    }
    @Nullable
    @Setter
    private Supplier<IRenderer> renderer;
    public MachineBuilder<DEFINITION> modelRenderer(Supplier<ResourceLocation> model) {
        this.renderer = () -> IRenderer.EMPTY;//new MachineRenderer(model.get());
        return this;
    }

    public MachineBuilder<DEFINITION> defaultModelRenderer() {
        return modelRenderer(() -> ResourceLocation.fromNamespaceAndPath(owner.getModid(), "block/" + name));
    }
    /*
    public MachineBuilder<DEFINITION> tieredHullRenderer(ResourceLocation model) {
        return renderer(() -> new TieredHullMachineRenderer(tier, model));
    }

    public MachineBuilder<DEFINITION> overlayTieredHullRenderer(String name) {
        return renderer(() -> new OverlayTieredMachineRenderer(tier,
                ResourceLocation.fromNamespaceAndPath(owner.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder<DEFINITION> overlaySteamHullRenderer(String name) {
        return renderer(() -> new OverlaySteamMachineRenderer(
                ResourceLocation.fromNamespaceAndPath(owner.getModid(), "block/machine/part/" + name)));
    }

    public MachineBuilder<DEFINITION> workableTieredHullRenderer(ResourceLocation workableModel) {
        return renderer(() -> new WorkableTieredHullMachineRenderer(tier, workableModel));
    }

    public MachineBuilder<DEFINITION> simpleGeneratorMachineRenderer(ResourceLocation workableModel) {
        return renderer(() -> new SimpleGeneratorMachineRenderer(tier, workableModel));
    }

    public MachineBuilder<DEFINITION> workableSteamHullRenderer(boolean isHighPressure,
                                                                ResourceLocation workableModel) {
        return renderer(() -> new WorkableSteamMachineRenderer(isHighPressure, workableModel));
    }

    public MachineBuilder<DEFINITION> workableCasingRenderer(ResourceLocation baseCasing,
                                                             ResourceLocation workableModel) {
        return renderer(() -> new WorkableCasingMachineRenderer(baseCasing, workableModel));
    }

    public MachineBuilder<DEFINITION> workableCasingRenderer(ResourceLocation baseCasing,
                                                             ResourceLocation workableModel, boolean tint) {
        return renderer(() -> new WorkableCasingMachineRenderer(baseCasing, workableModel, tint));
    }

    public MachineBuilder<DEFINITION> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel,
                                                                  boolean tint) {
        return renderer(() -> new WorkableSidedCasingMachineRenderer(basePath, overlayModel, tint));
    }

    public MachineBuilder<DEFINITION> sidedWorkableCasingRenderer(String basePath, ResourceLocation overlayModel) {
        return renderer(() -> new WorkableSidedCasingMachineRenderer(basePath, overlayModel));
    }*/
    @Setter
    private VoxelShape shape = Shapes.block();
    @Setter
    private RotationState rotationState = RotationState.ALL;
    @Setter
    boolean enableExtraRotation = true;
    /**
     * Whether this machine can be rotated or face upwards.
     * todo: set to true by default if we manage to rotate the model accordingly
     */
    @Setter
    private boolean hasTESR;
    @Setter
    private boolean renderMultiblockWorldPreview = true;
    @Setter
    private boolean renderMultiblockXEIPreview = true;
    @Setter
    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProp = p -> p;
    @Setter
    private NonNullUnaryOperator<Item.Properties> itemProp = p -> p;
    @Setter
    @Nullable
    private Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    @Setter
    @Nullable
    private Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder;

    @Setter
    private NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister = MetaMachineBlockEntity::onBlockEntityRegister;
    @Getter // getter for KJS
    private BTRecipeType @Nullable [] recipeTypes;
    @Setter
    private Object2IntMap<RecipeCapability<?>> recipeOutputLimits = new Object2IntOpenHashMap<>();
    @Setter
    private int paintingColor = Long.decode(ConfigHolder.INSTANCE.client.defaultPaintingColor).intValue();

    private final List<Component> tooltips = new ArrayList<>();
    @Setter
    @Nullable
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    @Setter
    private boolean alwaysTryModifyRecipe;

    @NotNull
    @Getter
    @Setter
    private BiPredicate<IRecipeLogicMachine, BTRecipe> beforeWorking = (machine, recipe) -> true;
    @NotNull
    @Getter
    @Setter
    private Predicate<IRecipeLogicMachine> onWorking = (machine) -> true;
    @NotNull
    @Getter
    @Setter
    private Consumer<IRecipeLogicMachine> onWaiting = (machine) -> {};
    @NotNull
    @Getter
    @Setter
    private Consumer<IRecipeLogicMachine> afterWorking = (machine) -> {};
    @Getter
    @Setter
    private boolean regressWhenWaiting = true;
    @Setter
    @Nullable
    private Supplier<BlockState> appearance;
    @Getter // getter for KJS
    @Setter
    @Nullable
    private EditableMachineUI editableUI;
    @Getter // getter for KJS
    @Setter
    @Nullable
    private String langValue = null;
    public MachineBuilder<DEFINITION> recipeType(BTRecipeType type) {
        this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        return this;
    }

    @Tolerate
    public MachineBuilder<DEFINITION> recipeTypes(BTRecipeType... types) {
        for (BTRecipeType type : types) {
            this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        }
        return this;
    }

    public MachineBuilder<DEFINITION> appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return this;
    }

    public MachineBuilder<DEFINITION> tooltips(Component... components) {
        tooltips.addAll(Arrays.stream(components).toList());
        return this;
    }

    public MachineBuilder<DEFINITION> conditionalTooltip(Component component, Supplier<Boolean> condition) {
        return conditionalTooltip(component, condition.get());
    }

    public MachineBuilder<DEFINITION> conditionalTooltip(Component component, boolean condition) {
        if (condition)
            tooltips.add(component);
        return this;
    }
    public MachineBuilder<DEFINITION> addOutputLimit(RecipeCapability<?> capability, int limit) {
        this.recipeOutputLimits.put(capability, limit);
        return this;
    }

    public MachineBuilder<DEFINITION> multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                                boolean multiBlockXEIPreview) {
        this.renderMultiblockWorldPreview = multiBlockWorldPreview;
        this.renderMultiblockXEIPreview = multiBlockXEIPreview;
        return this;
    }

    protected DEFINITION createDefinition() {
        return definitionFactory.apply(owner, ResourceLocation.fromNamespaceAndPath(owner.getModid(), name));
    }

    public MachineBuilder<DEFINITION>transform(Consumer<MachineBuilder<DEFINITION>>config){
        config.accept(this);
        return this;
    }

    public @NotNull DEFINITION register() {
        var definition = createDefinition();

        var blockBuilder = BlockBuilderWrapper.makeBlockBuilder(this, definition);
        if (this.blockBuilder != null) {
            this.blockBuilder.accept(blockBuilder);
        }
        var block = blockBuilder.register();

        var itemBuilder = ItemBuilderWrapper.makeItemBuilder(this, block);
        if (this.itemBuilder != null) {
            this.itemBuilder.accept(itemBuilder);
        }
        var item = itemBuilder.register();

        var blockEntityBuilder = owner
                .blockEntity(name, (type, pos, state) -> blockEntityFactory.apply(type, pos, state).self())
                .onRegister(onBlockEntityRegister)
                .validBlock(block);
        if (hasTESR) {
            blockEntityBuilder = blockEntityBuilder.renderer(() -> BTRendererProvider::getOrCreate);
        }
        var blockEntity = blockEntityBuilder.register();
        definition.setRecipeTypes(recipeTypes);
        definition.setBlockSupplier(block);
        definition.setItemSupplier(item);
        definition.setBlockEntityTypeSupplier(blockEntity::get);
        definition.setMachineSupplier(machineFactory);

        definition.setRecipeOutputLimits(recipeOutputLimits);
        definition.setTooltipBuilder((itemStack, components) -> {
            components.addAll(tooltips);
            if (tooltipBuilder != null) tooltipBuilder.accept(itemStack, components);
        });
        definition.setAlwaysTryModifyRecipe(alwaysTryModifyRecipe);
        definition.setBeforeWorking(this.beforeWorking);
        definition.setOnWorking(this.onWorking);
        definition.setOnWaiting(this.onWaiting);
        definition.setAfterWorking(this.afterWorking);
        definition.setRegressWhenWaiting(this.regressWhenWaiting);

        if (renderer == null) {
            renderer = () -> IRenderer.EMPTY;//new MachineRenderer(ResourceLocation.fromNamespaceAndPath(owner.getModid(), "block/machine/" + name));
        }
        if (appearance == null) {
            appearance = block::getDefaultState;
        }
        if (editableUI != null) {
            definition.setEditableUI(editableUI);
        }

        definition.setAppearance(appearance);
        definition.setRenderer(BreaTechnology.isClientSide() ? renderer.get() : IRenderer.EMPTY);
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        definition.setRenderXEIPreview(renderMultiblockXEIPreview);
        definition.setRenderWorldPreview(renderMultiblockWorldPreview);
        definition.setEnableExtraRotation(enableExtraRotation);

        return definition;
    }

    static class BlockBuilderWrapper {

        @SuppressWarnings("removal")
        public static <D extends MetaMachineDefinition<?>> BlockBuilder<Block, MachineBuilder<D>>
        makeBlockBuilder(MachineBuilder<D> builder, D definition) {
            return builder.owner.block(builder, builder.name, prop -> {
                        RotationState.setPreState(builder.rotationState);
                        MetaMachineDefinition.setBuilt(definition);
                        var b = builder.blockFactory.apply(prop, definition);
                        RotationState.clearPreState();
                        MetaMachineDefinition.clearBuilt();
                        return b.self();
                    })
                    .initialProperties(() -> Blocks.DISPENSER)
                    .properties(BlockBehaviour.Properties::noLootTable)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .blockstate(NonNullBiConsumer.noop())
                    .properties(builder.blockProp);

        }
    }

    static class ItemBuilderWrapper {

        public static <D extends MetaMachineDefinition<?>> ItemBuilder<MetaMachineItem, MachineBuilder<D>>
        makeItemBuilder(MachineBuilder<D> builder, BlockEntry<Block> block) {
            return builder.owner.item(builder, builder.name, prop ->
                            builder.itemFactory.apply((IMachineBlock) block.get(), prop))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                    .model(NonNullBiConsumer.noop())
                    .properties(builder.itemProp);
        }
    }
}
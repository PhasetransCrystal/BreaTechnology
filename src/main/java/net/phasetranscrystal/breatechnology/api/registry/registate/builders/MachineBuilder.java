package net.phasetranscrystal.breatechnology.api.registry.registate.builders;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.*;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.*;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
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
import net.phasetranscrystal.breatechnology.api.recipe.modifier.RecipeModifier;
import net.phasetranscrystal.breatechnology.api.registry.BTRegistries;
import net.phasetranscrystal.breatechnology.api.renderer.BTRendererProvider;
import net.phasetranscrystal.breatechnology.config.ConfigHolder;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.*;

@Accessors(chain = true, fluent = true)
public class MachineBuilder<DEFINITION extends MetaMachineDefinition<?>,P>extends AbstractBuilder<MetaMachineDefinition<?>,DEFINITION,P,MachineBuilder<DEFINITION,P>> {

    /// 机器定义数据工厂
    protected BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory;
    /// 机器元数据
    protected final Function<IMachineBlockEntity, MetaMachine> machineFactory;
    /// 机器方块工厂
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory;
    /// 机器方块工厂
    protected final BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory;
    /// 机器实体工厂
    protected final TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory;

    public MachineBuilder(AbstractRegistrate<?> owner, P parent, String name,
                          BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory,
                          BuilderCallback callback,
                          Function<IMachineBlockEntity, MetaMachine> machineFactory,
                          BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                          BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                          TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        super(owner, parent, name, callback, BTRegistries.MACHINE_DEFINITIONS);
        this.definitionFactory = definitionFactory;
        this.machineFactory = machineFactory;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.blockEntityFactory = blockEntityFactory;
    }

    /// 渲染器工厂
    @Nullable
    @Setter
    private Supplier<IRenderer> renderer;

    /// 设置以Model为源的渲染器
    public MachineBuilder<DEFINITION, P> modelRenderer(Supplier<ResourceLocation> model) {
        this.renderer = () -> IRenderer.EMPTY;//new MachineRenderer(model.get());
        return this;
    }

    /// 设置为默认渲染器
    public MachineBuilder<DEFINITION, P> defaultModelRenderer() {
        return modelRenderer(() -> ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), "block/" + getName()));
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
    /// 渲染模型
    @Setter
    private VoxelShape shape = Shapes.block();
    /// 朝向逻辑
    @Setter
    private RotationState rotationState = RotationState.NONE;
    /// 是否允许额外旋转
    @Setter
    boolean enableExtraRotation = false;
    /// 是否使用自定义渲染
    @Setter
    private boolean hasTESR;
    /// 是否渲染多方快的世界预览
    @Setter
    private boolean renderMultiblockWorldPreview = true;
    /// 是否渲染多方快的JEI(EMI,NEI)预览
    @Setter
    private boolean renderMultiblockXEIPreview = true;
    /// 方块属性
    @Setter
    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProp = p -> p;
    /// 物品属性
    @Setter
    private NonNullUnaryOperator<Item.Properties> itemProp = p -> p;
    /// 方块工厂
    @Setter
    @Nullable
    private Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    /// 物品工厂
    @Setter
    @Nullable
    private Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder;
    /// 方块书体注册完成回调
    @Setter
    private NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister = MetaMachineBlockEntity::onBlockEntityRegister;
    /// 机器配方逻辑
    @Getter // getter for KJS
    private BTRecipeType @Nullable [] recipeTypes;
    private RecipeModifier recipeModifier = null;
    /// 机器配方输出限制
    @Setter
    private Object2IntMap<RecipeCapability<?>> recipeOutputLimits = new Object2IntOpenHashMap<>();
    /// 默认绘制颜色
    @Setter
    private int paintingColor = Long.decode(ConfigHolder.INSTANCE.client.defaultPaintingColor).intValue();
    /// 提示栏信息
    private final List<Component> tooltips = new ArrayList<>();
    /// 提示栏构建器
    @Setter
    @Nullable
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    /// 是否坚持处理配方逻辑
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
    private Consumer<IRecipeLogicMachine> onWaiting = (machine) -> {
    };
    @NotNull
    @Getter
    @Setter
    private Consumer<IRecipeLogicMachine> afterWorking = (machine) -> {
    };
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
    /// 机器Lang名称
    @Getter // getter for KJS
    @Setter
    @Nullable
    private String langValue = null;

    /// 添加机器配方
    public MachineBuilder<DEFINITION, P> recipeType(BTRecipeType type) {
        this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        return this;
    }

    /// 添加机器配方
    @Tolerate
    public MachineBuilder<DEFINITION, P> recipeTypes(BTRecipeType... types) {
        for (BTRecipeType type : types) {
            this.recipeTypes = ArrayUtils.add(this.recipeTypes, type);
        }
        return this;
    }

    /// 机器外观逻辑
    public MachineBuilder<DEFINITION, P> appearanceBlock(Supplier<? extends Block> block) {
        appearance = () -> block.get().defaultBlockState();
        return this;
    }

    /// 机器提示词逻辑
    public MachineBuilder<DEFINITION, P> tooltips(Component... components) {
        tooltips.addAll(Arrays.stream(components).toList());
        return this;
    }

    /// 机器提示词逻辑（一定条件下）
    public MachineBuilder<DEFINITION, P> conditionalTooltip(Component component, Supplier<Boolean> condition) {
        return conditionalTooltip(component, condition.get());
    }

    /// 机器提示词逻辑（一定条件下）
    public MachineBuilder<DEFINITION, P> conditionalTooltip(Component component, boolean condition) {
        if (condition)
            tooltips.add(component);
        return this;
    }
    /*
    public MachineBuilder<DEFINITION> abilities(PartAbility... abilities) {
        this.abilities = abilities;
        return this;
    }*/
    /*
    public MachineBuilder<DEFINITION> recipeModifier(RecipeModifier recipeModifier) {
        this.recipeModifier = recipeModifier instanceof RecipeModifierList list ? list :
                new RecipeModifierList(recipeModifier);
        return this;
    }
    public MachineBuilder<DEFINITION> recipeModifier(RecipeModifier recipeModifier, boolean alwaysTryModifyRecipe) {
        this.alwaysTryModifyRecipe = alwaysTryModifyRecipe;
        return this.recipeModifier(recipeModifier);
    }
    public MachineBuilder<DEFINITION> recipeModifiers(RecipeModifier... recipeModifiers) {
        this.recipeModifier = new RecipeModifierList(recipeModifiers);
        return this;
    }
    public MachineBuilder<DEFINITION> recipeModifiers(boolean alwaysTryModifyRecipe,
                                                      RecipeModifier... recipeModifiers) {
        return this.recipeModifier(new RecipeModifierList(recipeModifiers), alwaysTryModifyRecipe);
    }
    public MachineBuilder<DEFINITION> noRecipeModifier() {
        this.recipeModifier = new RecipeModifierList(RecipeModifier.NO_MODIFIER);
        this.alwaysTryModifyRecipe = false;
        return this;
    }
    */

    /// 添加输出限制
    public MachineBuilder<DEFINITION, P> addOutputLimit(RecipeCapability<?> capability, int limit) {
        this.recipeOutputLimits.put(capability, limit);
        return this;
    }

    /// 是否渲染多方快
    public MachineBuilder<DEFINITION, P> multiblockPreviewRenderer(boolean multiBlockWorldPreview,
                                                                   boolean multiBlockXEIPreview) {
        this.renderMultiblockWorldPreview = multiBlockWorldPreview;
        this.renderMultiblockXEIPreview = multiBlockXEIPreview;
        return this;
    }

    /// Builder处理
    public MachineBuilder<DEFINITION, P> transform(Consumer<MachineBuilder<DEFINITION, P>> config) {
        config.accept(this);
        return this;
    }

    private DEFINITION definition;

    @Override
    protected @NonnullType @NotNull DEFINITION createEntry() {
        if (definition == null) {
            definition = definitionFactory.apply(getOwner(), ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), getName()));
        }
        return definition;
    }

    /// 注册
    public RegistryEntry<MetaMachineDefinition<?>, DEFINITION> register() {
        var definition = createEntry();

        var blockBuilder = BlockBuilderWrapper.makeBlockBuilder(this, definition);
        if (this.langValue != null) {
            blockBuilder.lang(langValue);
            definition.setLangValue(langValue);
        }
        if (this.blockBuilder != null) {
            this.blockBuilder.accept(blockBuilder);
        }
        var block = blockBuilder.register();

        var itemBuilder = ItemBuilderWrapper.makeItemBuilder(this, block);
        if (this.itemBuilder != null) {
            this.itemBuilder.accept(itemBuilder);
        }
        var item = itemBuilder.register();

        var blockEntityBuilder = getOwner()
                .blockEntity(getName(), (type, pos, state) -> blockEntityFactory.apply(type, pos, state).self())
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
        definition.setRecipeModifier(recipeModifier);
        definition.setAlwaysTryModifyRecipe(alwaysTryModifyRecipe);
        definition.setBeforeWorking(this.beforeWorking);
        definition.setOnWorking(this.onWorking);
        definition.setOnWaiting(this.onWaiting);
        definition.setAfterWorking(this.afterWorking);
        definition.setRegressWhenWaiting(this.regressWhenWaiting);

        if (renderer == null) {
            renderer = () -> IRenderer.EMPTY;//new MachineRenderer(ResourceLocation.fromNamespaceAndPath(owner.getModid(), "block/machine/" + name));
        }
        if (recipeTypes != null) {
            for (BTRecipeType type : recipeTypes) {
                Objects.requireNonNull(type, "Cannot use null recipe type for machine %s:%s"
                        .formatted(getOwner().getModid(), getName()));
                if (type.getIconSupplier() == null) {
                    type.setIconSupplier(definition::asStack);
                }
            }
        }
        if (appearance == null) {
            appearance = block::getDefaultState;
        }
        if (editableUI != null) {
            definition.setEditableUI(editableUI);
        }
        definition.setAppearance(appearance);
        definition.setEnableExtraRotation(enableExtraRotation);
        definition.setRenderer(BreaTechnology.isClientSide() ? renderer.get() : IRenderer.EMPTY);
        definition.setShape(shape);
        definition.setDefaultPaintingColor(paintingColor);
        definition.setRenderXEIPreview(renderMultiblockXEIPreview);
        definition.setRenderWorldPreview(renderMultiblockWorldPreview);

        return super.register();
    }

    static class BlockBuilderWrapper {

        @SuppressWarnings("removal")
        public static <D extends MetaMachineDefinition<?>, P> BlockBuilder<Block, MachineBuilder<D, P>>
        makeBlockBuilder(MachineBuilder<D, P> builder, D definition) {
            return builder.getOwner().block(builder, builder.getName(), prop -> {
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

        public static <D extends MetaMachineDefinition<?>, P> ItemBuilder<MetaMachineItem, MachineBuilder<D, P>>
        makeItemBuilder(MachineBuilder<D, P> builder, BlockEntry<Block> block) {
            return builder.getOwner().item(builder, builder.getName(), prop ->
                            builder.itemFactory.apply((IMachineBlock) block.get(), prop))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                    .model(NonNullBiConsumer.noop())
                    .properties(builder.itemProp);
        }
    }
}
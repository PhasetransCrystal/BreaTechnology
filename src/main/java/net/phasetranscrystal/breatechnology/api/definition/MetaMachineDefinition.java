package net.phasetranscrystal.breatechnology.api.definition;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.ShapeUtils;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.phasetranscrystal.breatechnology.api.blockentity.MetaMachineBlockEntity;
import net.phasetranscrystal.breatechnology.api.gui.egitor.EditableMachineUI;
import net.phasetranscrystal.breatechnology.api.item.MetaMachineItem;
import net.phasetranscrystal.breatechnology.api.machine.*;
import net.phasetranscrystal.breatechnology.api.machine.feature.IRecipeLogicMachine;
import net.phasetranscrystal.breatechnology.api.recipe.BTRecipeType;
import net.phasetranscrystal.breatechnology.api.recipe.capability.RecipeCapability;
import net.phasetranscrystal.breatechnology.api.recipe.kind.BTRecipe;
import net.phasetranscrystal.breatechnology.api.recipe.modifier.RecipeModifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.*;

/// 基础机器定义信息
public class MetaMachineDefinition<T extends MetaMachineBlockEntity> extends MetaBlockDefinition<T> implements Supplier<IMachineBlock> {
    public MetaMachineDefinition(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }

    @Setter
    private Supplier<? extends Block> blockSupplier;
    @Setter
    private Supplier<? extends MetaMachineItem> itemSupplier;
    @Setter
    private Function<IMachineBlockEntity, MetaMachine> machineSupplier;

    public Block getBlock() {
        return blockSupplier.get();
    }

    public MetaMachineItem getItem() {
        return itemSupplier.get();
    }

    public MetaMachine createMetaMachine(IMachineBlockEntity blockEntity) {
        return machineSupplier.apply(blockEntity);
    }

    public ItemStack asStack() {
        return new ItemStack(getItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(getItem(), count);
    }

    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    public BlockState defaultBlockState() {
        return getBlock().defaultBlockState();
    }

    @Override
    public IMachineBlock get() {
        return (IMachineBlock) blockSupplier.get();
    }

    public String getName() {
        return getId().getPath();
    }

    @Override
    public String toString() {
        return "[Definition: %s]".formatted(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MetaMachineDefinition that = (MetaMachineDefinition) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /// 机器处理配方类型
    @Getter
    @Setter
    private BTRecipeType @Nullable [] recipeTypes;
    /// 默认绘制颜色
    @Getter
    @Setter
    private int defaultPaintingColor;
    /// 配方修饰器
    @Getter
    @Setter
    private RecipeModifier recipeModifier;
    /// 是否坚持修饰配方
    @Getter
    @Setter
    private boolean alwaysTryModifyRecipe;
    @NotNull
    @Getter
    @Setter
    /// 是否允许开始配方
    private BiPredicate<IRecipeLogicMachine, BTRecipe> beforeWorking = (machine, recipe) -> true;
    @NotNull
    @Getter
    @Setter
    /// 是否允许
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

    @Getter
    @Setter
    private IRenderer renderer;
    @Setter
    private VoxelShape shape;

    public VoxelShape getShape(Direction direction) {
        if (shape.isEmpty() || shape == Shapes.block() || direction == Direction.NORTH) return shape;
        return this.cache.computeIfAbsent(direction, dir -> ShapeUtils.rotate(shape, dir));
    }
    @Getter
    @Setter
    private boolean renderWorldPreview;
    @Getter
    @Setter
    private boolean renderXEIPreview;
    private final Map<Direction, VoxelShape> cache = new EnumMap<>(Direction.class);
    @Getter
    @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    @Getter
    @Setter
    private Supplier<BlockState> appearance;
    @Nullable
    @Getter
    @Setter
    private EditableMachineUI editableUI;
    @Getter
    @Setter
    private Object2IntMap<RecipeCapability<?>> recipeOutputLimits = new Object2IntOpenHashMap<>();
    @Getter
    private RotationState rotationState = RotationState.ALL;
    @Getter
    @Setter
    private boolean enableExtraRotation = true;
    @Getter
    private ExtraRotate extraRotate = ExtraRotate.NONE;

    private static ThreadLocal<MetaMachineDefinition> Built = new ThreadLocal<>();

    public static void setBuilt(MetaMachineDefinition definition) {
        Built.set(definition);
    }

    public static MetaMachineDefinition getBuilt() {
        return Built.get();
    }

    public static void clearBuilt() {
        Built.remove();
    }
}
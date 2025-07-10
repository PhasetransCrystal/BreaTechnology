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
    // This is only stored here for KJS use.
    @Getter
    @Setter
    @Nullable
    private String langValue;
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
    /// 是否允许配方进行
    @NotNull
    @Getter
    @Setter
    private BiPredicate<IRecipeLogicMachine, BTRecipe> beforeWorking = (machine, recipe) -> true;
    /// 配方进行是否正常
    @NotNull
    @Getter
    @Setter
    private Predicate<IRecipeLogicMachine> onWorking = (machine) -> true;
    /// 机器无配方进行时回调
    @NotNull
    @Getter
    @Setter
    private Consumer<IRecipeLogicMachine> onWaiting = (machine) -> {
    };
    /// 配方进行完成后回调
    @NotNull
    @Getter
    @Setter
    private Consumer<IRecipeLogicMachine> afterWorking = (machine) -> {
    };
    /// 配方等待时是否允许回滚
    @Getter
    @Setter
    private boolean regressWhenWaiting = true;
    /// 机器方块渲染器
    @Getter
    @Setter
    private IRenderer renderer;
    /// 机器渲染模型
    @Setter
    private VoxelShape shape;

    /// 获取机器渲染模型
    public VoxelShape getShape(Direction direction) {
        if (shape.isEmpty() || shape == Shapes.block() || direction == Direction.NORTH) return shape;
        return this.cache.computeIfAbsent(direction, dir -> ShapeUtils.rotate(shape, dir));
    }

    /// 是否渲染世界预览
    @Getter
    @Setter
    private boolean renderWorldPreview;
    /// 是否渲染JEI(EMI,NEI)预览
    @Getter
    @Setter
    private boolean renderXEIPreview;
    /// 机器渲染缓存
    private final Map<Direction, VoxelShape> cache = new EnumMap<>(Direction.class);
    /// 机器提示信息构建器
    @Getter
    @Setter
    private BiConsumer<ItemStack, List<Component>> tooltipBuilder;
    /// 机器外观工厂
    @Getter
    @Setter
    private Supplier<BlockState> appearance;
    /// 机器UI界面
    @Nullable
    @Getter
    @Setter
    private EditableMachineUI editableUI;
    /// 配方输出限制
    @Getter
    @Setter
    private Object2IntMap<RecipeCapability<?>> recipeOutputLimits = new Object2IntOpenHashMap<>();
    /// 是否允许额外旋转
    @Getter
    @Setter
    private boolean enableExtraRotation = true;

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
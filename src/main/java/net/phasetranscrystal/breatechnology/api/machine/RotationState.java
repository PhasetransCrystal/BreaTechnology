package net.phasetranscrystal.breatechnology.api.machine;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

import java.util.function.Predicate;

/// 朝向限制状态枚举
public enum RotationState implements Predicate<Direction> {

    /// 全部朝向
    ALL(dir -> true, Direction.NORTH, BlockStateProperties.FACING),
    /// 默认朝向
    NONE(dir -> false, Direction.NORTH, DirectionProperty.create("facing", Direction.NORTH)),
    /// 仅限朝上
    Y_AXIS(dir -> dir.getAxis() == Direction.Axis.Y, Direction.UP,
            DirectionProperty.create("facing", Direction.Plane.VERTICAL)),
    /// 水平朝向
    NON_Y_AXIS(dir -> dir.getAxis() != Direction.Axis.Y, Direction.NORTH, BlockStateProperties.HORIZONTAL_FACING);

    final Predicate<Direction> predicate;
    public final Direction defaultDirection;
    public final DirectionProperty property;

    /**
     * 构造函数
     *
     * @param predicate        合法性验证
     * @param defaultDirection 默认朝向
     * @param property         方向性属性
     */
    RotationState(Predicate<Direction> predicate, Direction defaultDirection, DirectionProperty property) {
        this.predicate = predicate;
        this.defaultDirection = defaultDirection;
        this.property = property;
    }

    @Override
    public boolean test(Direction dir) {
        return predicate.test(dir);
    }

    private static final ThreadLocal<RotationState> PRE_STATE = new ThreadLocal<>();

    public static RotationState getPreState() {
        return PRE_STATE.get();
    }

    public static void setPreState(RotationState state) {
        PRE_STATE.set(state);
    }

    public static void clearPreState() {
        PRE_STATE.remove();
    }
}

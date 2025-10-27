package net.phasetranscrystal.breatechnology.api.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;

import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/// 机器方块基本方法接口
public interface IMachineBlock extends EntityBlock, IBlockRendererProvider {

    /// 是否进行Tick的属性
    BooleanProperty SERVER_TICK = BooleanProperty.create("server_tick");
    /// 额外旋转的属性
    EnumProperty<ExtraRotate> EXTRA_ROTATE = EnumProperty.create("extra_rotate", ExtraRotate.class);

    /// 获取机器方块本体
    default Block self() {
        return (Block) this;
    }

    /// 获取机器的定义数据
    MetaMachineDefinition<?> getDefinition();

    /// 获取机器的朝向逻辑
    RotationState getRotationState();

    /// 获取机器着色颜色
    static int colorTinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                           int index) {
        if (level != null && pos != null) {
            var machine = MetaMachine.getMachine(level, pos);
            if (machine != null) {
                return machine.tintColor(index);
            }
        }
        return -1;
    }

    /// 创建新机器方块实体
    @Nullable
    @Override
    default BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return getDefinition().getBlockEntityType().create(pos, state);
    }

    /// 进行tick逻辑
    @Nullable
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(@NotNull Level level, @NotNull BlockState state,
                                                                   @NotNull BlockEntityType<T> blockEntityType) {
        if (blockEntityType == getDefinition().getBlockEntityType()) {
            if (state.getValue(SERVER_TICK) && !level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().serverTick();
                    }
                };
            }
            if (level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().clientTick();
                    }
                };
            }
        }
        return null;
    }
}

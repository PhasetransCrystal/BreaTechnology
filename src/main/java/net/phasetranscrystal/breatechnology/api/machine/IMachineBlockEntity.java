package net.phasetranscrystal.breatechnology.api.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.phasetranscrystal.breatechnology.api.BTValues;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;

import com.lowdragmc.lowdraglib.syncdata.blockentity.IAsyncAutoSyncBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IAutoPersistBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.blockentity.IRPCBlockEntity;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;

/// 机器方块实体基本方法接口
public interface IMachineBlockEntity extends IAsyncAutoSyncBlockEntity, IRPCBlockEntity, IAutoPersistBlockEntity {

    /// 获取方块实体
    default BlockEntity self() {
        return (BlockEntity) this;
    }

    /// 获取方块实体所在的世界
    default Level level() {
        return self().getLevel();
    }

    /// 获取方块实体位置
    default BlockPos pos() {
        return self().getBlockPos();
    }

    /// 通知方块更新
    default void notifyBlockUpdate() {
        if (level() != null) {
            level().updateNeighborsAt(pos(), level().getBlockState(pos()).getBlock());
        }
    }

    /// 处理方块渲染更新
    default void scheduleRenderUpdate() {
        var pos = pos();
        if (level() != null) {
            var state = level().getBlockState(pos);
            if (level().isClientSide) {
                level().sendBlockUpdated(pos, state, state, 1 << 3);
            } else {
                level().blockEvent(pos, state.getBlock(), 1, 0);
            }
        }
    }

    /// 获取机器随机时刻
    default long getOffsetTimer() {
        if (level() == null) return getOffset();
        else if (level().isClientSide()) return BTValues.CLIENT_TIME + getOffset();

        var server = level().getServer();
        if (server != null) return server.getTickCount() + getOffset();
        return getOffset();
    }

    /// 获取机器定义数据
    default MetaMachineDefinition<?> getDefinition() {
        if (self().getBlockState().getBlock() instanceof IMachineBlock machineBlock) {
            return machineBlock.getDefinition();
        } else {
            throw new IllegalStateException("MetaMachineBlockEntity is created for an un available block: " +
                    self().getBlockState().getBlock());
        }
    }

    /// 获取机器元数据
    MetaMachine getMetaMachine();

    /// 获取随机时刻偏移值
    long getOffset();

    MultiManagedStorage getRootStorage();

    @Override
    default void saveCustomPersistedData(CompoundTag tag, boolean forDrop) {
        IAutoPersistBlockEntity.super.saveCustomPersistedData(tag, forDrop);
        getMetaMachine().saveCustomPersistedData(tag, forDrop);
    }

    @Override
    default void loadCustomPersistedData(CompoundTag tag) {
        IAutoPersistBlockEntity.super.loadCustomPersistedData(tag);
        getMetaMachine().loadCustomPersistedData(tag);
    }
}

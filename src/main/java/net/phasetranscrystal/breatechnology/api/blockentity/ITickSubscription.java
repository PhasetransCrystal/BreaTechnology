package net.phasetranscrystal.breatechnology.api.blockentity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.phasetranscrystal.breatechnology.api.machine.TickableSubscription;
import org.jetbrains.annotations.Nullable;

/// Tick事件订阅接口
public interface ITickSubscription {

    /**
     * 用于初始化。要在自动同步后获取级别和属性字段，你可以在
     * {@link BlockEntity#clearRemoved()} 事件中订阅它。
     */
    @Nullable
    TickableSubscription subscribeServerTick(Runnable runnable);

    void unsubscribe(@Nullable TickableSubscription current);

    @Nullable
    default TickableSubscription subscribeServerTick(@Nullable TickableSubscription last, Runnable runnable) {
        if (last == null || !last.isStillSubscribed()) {
            return subscribeServerTick(runnable);
        }
        return last;
    }
}
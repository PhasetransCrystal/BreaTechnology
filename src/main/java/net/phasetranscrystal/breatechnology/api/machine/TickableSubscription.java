package net.phasetranscrystal.breatechnology.api.machine;

import lombok.Getter;

/**
 * 可取消的订阅类，用于管理订阅状态和执行回调操作
 */
public class TickableSubscription {

    // 订阅时绑定的回调函数
    private final Runnable runnable;

    // 订阅状态标记（使用Lombok的@Getter自动生成getter方法）
    @Getter
    private boolean stillSubscribed;

    /**
     * 构造函数
     * 
     * @param runnable 订阅时需要执行的回调函数
     */
    public TickableSubscription(Runnable runnable) {
        this.runnable = runnable;
        this.stillSubscribed = true;  // 初始化时默认处于订阅状态
    }

    /**
     * 执行订阅回调
     * 仅在仍处于订阅状态时才会实际执行回调
     */
    public void run() {
        if (stillSubscribed) {
            runnable.run();
        }
    }

    /**
     * 取消订阅
     * 将订阅状态标记为false，后续调用run()将不再执行回调
     */
    public void unsubscribe() {
        stillSubscribed = false;
    }
}

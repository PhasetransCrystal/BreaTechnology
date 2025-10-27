package net.phasetranscrystal.breatechnology.api.machine.feature;

import net.minecraft.nbt.CompoundTag;

/// 破坏保留数据的机器接口
public interface IDropSaveMachine extends IMachineFeature {

    /**
     * Whether save for breaking.
     */
    default boolean saveBreak() {
        return true;
    }

    /**
     * Whether save for cloning.
     */
    default boolean savePickClone() {
        return true;
    }

    /**
     * Saves the contents of the block entity to a compound tag.
     *
     * @param tag The tag to save to.
     */
    /*
     * @ApiStatus.ScheduledForRemoval(inVersion = "1.9.0")
     * 
     * @Deprecated(forRemoval = true)
     */
    default void saveToItem(CompoundTag tag) {
        self().holder.saveManagedPersistentData(tag, true);
    }

    /**
     * Loads the contents of the block entity from a compound tag.
     */
    default void loadFromItem(CompoundTag tag) {
        self().holder.loadManagedPersistentData(tag);
    }
}

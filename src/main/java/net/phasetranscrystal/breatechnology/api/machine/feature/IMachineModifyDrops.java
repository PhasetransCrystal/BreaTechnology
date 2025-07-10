package net.phasetranscrystal.breatechnology.api.machine.feature;

import net.minecraft.world.item.ItemStack;

import java.util.List;

/// 处理掉落的机器接口
public interface IMachineModifyDrops extends IMachineFeature {

    /**
     * Modify or append drops.
     *
     * @param drops existing drops.
     */
    void onDrops(List<ItemStack> drops);
}

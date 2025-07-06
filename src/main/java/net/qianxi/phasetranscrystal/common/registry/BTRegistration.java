package net.qianxi.phasetranscrystal.common.registry;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.qianxi.phasetranscrystal.BreaTechnology;
import net.qianxi.phasetranscrystal.api.registry.registate.BTRegistrate;

public class BTRegistration {
    public static final BTRegistrate REGISTRATE = BTRegistrate.create(BreaTechnology.MOD_ID);

    static {
        BTRegistration.REGISTRATE.defaultCreativeTab((ResourceKey<CreativeModeTab>) null);
    }

    private BTRegistration() {
    }
}

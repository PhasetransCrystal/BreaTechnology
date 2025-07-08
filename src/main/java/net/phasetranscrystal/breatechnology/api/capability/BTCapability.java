package net.phasetranscrystal.breatechnology.api.capability;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.phasetranscrystal.breatechnology.BreaTechnology;

public class BTCapability {
    public static final BlockCapability<ICoverable, Direction> CAPABILITY_COVERABLE = BlockCapability
            .createSided(BreaTechnology.id("coverable"), ICoverable.class);
}

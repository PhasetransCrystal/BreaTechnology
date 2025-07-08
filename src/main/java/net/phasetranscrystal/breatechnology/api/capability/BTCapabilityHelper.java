package net.phasetranscrystal.breatechnology.api.capability;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class BTCapabilityHelper {
    @Nullable
    public static ICoverable getCoverable(Level level, BlockPos pos, @Nullable Direction side) {
        return level.getCapability(BTCapability.CAPABILITY_COVERABLE, pos, side);
    }
}

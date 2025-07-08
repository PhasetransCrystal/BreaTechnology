package net.phasetranscrystal.breatechnology.api.utils;

import net.minecraft.core.Direction;
import org.jetbrains.annotations.Nullable;

public class BTUtil {
    public static final Direction[] DIRECTIONS = Direction.values();


    @Nullable
    public static Direction determineWrenchingSide(Direction facing, float x, float y, float z) {
        Direction opposite = facing.getOpposite();
        switch (facing) {
            case DOWN, UP -> {
                if (x < 0.25) {
                    if (z < 0.25) return opposite;
                    if (z > 0.75) return opposite;
                    return Direction.WEST;
                }
                if (x > 0.75) {
                    if (z < 0.25) return opposite;
                    if (z > 0.75) return opposite;
                    return Direction.EAST;
                }
                if (z < 0.25) return Direction.NORTH;
                if (z > 0.75) return Direction.SOUTH;
                return facing;
            }
            case NORTH, SOUTH -> {
                if (x < 0.25) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.WEST;
                }
                if (x > 0.75) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.EAST;
                }
                if (y < 0.25) return Direction.DOWN;
                if (y > 0.75) return Direction.UP;
                return facing;
            }
            case WEST, EAST -> {
                if (z < 0.25) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.NORTH;
                }
                if (z > 0.75) {
                    if (y < 0.25) return opposite;
                    if (y > 0.75) return opposite;
                    return Direction.SOUTH;
                }
                if (y < 0.25) return Direction.DOWN;
                if (y > 0.75) return Direction.UP;
                return facing;
            }
        }
        return null;
    }
}

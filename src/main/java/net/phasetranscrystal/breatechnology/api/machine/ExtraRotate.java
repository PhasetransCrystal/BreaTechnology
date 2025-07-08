package net.phasetranscrystal.breatechnology.api.machine;

import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public enum ExtraRotate implements Predicate<ExtraRotate>, StringRepresentable {
    NONE(0, "none"),
    OPPOSITE(180, "opposite"),
    ANTW(270, "anticlockwise"),
    CLOW(90, "clockwise");
    private final int angle;
    private final String name;

    ExtraRotate(int angle, String name) {
        this.angle = angle;
        this.name = name;
    }

    @Override
    public boolean test(ExtraRotate extraRotate) {
        return this.equals(extraRotate);
    }

    private static final ThreadLocal<ExtraRotate> ROTATE = new ThreadLocal<>();

    public static ExtraRotate get() {
        return ROTATE.get();
    }

    public static void set(ExtraRotate extraRotate) {
        ROTATE.set(extraRotate);
    }

    public static void clear() {
        ROTATE.remove();
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }

    public static ExtraRotate transform(Direction direction) {
        return switch (direction) {
            case NORTH -> NONE;
            case SOUTH -> CLOW;
            case EAST -> OPPOSITE;
            case WEST -> ANTW;
            default -> NONE;
        };
    }
}

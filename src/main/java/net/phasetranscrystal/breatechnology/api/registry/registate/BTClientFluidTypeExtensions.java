package net.phasetranscrystal.breatechnology.api.registry.registate;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class BTClientFluidTypeExtensions implements IClientFluidTypeExtensions {
    public static final ResourceLocation UNDERWATER_LOCATION = ResourceLocation.withDefaultNamespace("textures/misc/underwater.png");
    public static final ResourceLocation LIQUID_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    public static final ResourceLocation LIQUID_FLOWING = ResourceLocation.withDefaultNamespace("block/water_flow");
    public static final ResourceLocation LIQUID_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");
    public static final ResourceLocation MELT_STILL = BreaTechnology.id("block/melt_still");
    public static final ResourceLocation MELT_FLOWING = BreaTechnology.id("block/melt_flow");

    public BTClientFluidTypeExtensions(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor) {
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.tintColor = tintColor;
    }

    @Getter
    @Setter
    @Nullable
    private ResourceLocation flowingTexture, stillTexture;
    @Getter
    @Setter
    private int tintColor = 0xffffffff;

    public static class Liquid extends BTClientFluidTypeExtensions {
        public Liquid(int tintColor) {
            super(LIQUID_STILL, LIQUID_FLOWING, tintColor);
        }

        public Liquid(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor) {
            super(stillTexture, flowingTexture, tintColor);
        }

        public ResourceLocation getOverlayTexture() {
            return LIQUID_OVERLAY;
        }

        public ResourceLocation getRenderOverlayTexture(@NotNull Minecraft mc) {
            return UNDERWATER_LOCATION;
        }
    }

    public static class Melt extends BTClientFluidTypeExtensions {
        public Melt(int tintColor) {
            super(MELT_STILL, MELT_FLOWING, tintColor);
        }

        public Melt(ResourceLocation stillTexture, ResourceLocation flowingTexture, int tintColor) {
            super(stillTexture, flowingTexture, tintColor);
        }
    }
}
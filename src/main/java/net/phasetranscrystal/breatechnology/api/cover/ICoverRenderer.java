package net.phasetranscrystal.breatechnology.api.cover;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.model.data.ModelData;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Don't use this as a standalone block renderer, call it from {@link ICoverableRenderer} instead
 */
public interface ICoverRenderer extends IRenderer {

    /**
     * Use
     * {@link #renderCover(List, Direction, RandomSource, CoverBehavior, Direction, BlockPos, BlockAndTintGetter, ModelState)}
     * instead
     */
    @Override
    @Deprecated
    @OnlyIn(Dist.CLIENT)
    default List<BakedQuad> renderModel(BlockAndTintGetter level, BlockPos pos, BlockState state, Direction side,
                                        @NotNull RandomSource rand, @NotNull ModelData data, RenderType renderType) {
        return IRenderer.super.renderModel(level, pos, state, side, rand, data, renderType);
    }

    @OnlyIn(Dist.CLIENT)
    void renderCover(List<BakedQuad> quads, @Nullable Direction side, RandomSource rand,
                     @NotNull CoverBehavior coverBehavior, @Nullable Direction modelFacing, BlockPos pos,
                     BlockAndTintGetter level, ModelState modelState);
}

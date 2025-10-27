package net.phasetranscrystal.breatechnology.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.phasetranscrystal.breatechnology.api.definition.MetaBlockDefinition;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetaAppearanceBlock extends MetaBlock implements IAppearance {

    public MetaAppearanceBlock(Properties properties, MetaBlockDefinition<?> definition) {
        super(properties, definition);
    }

    /// 获取方块外观（BlockState）
    @Override
    public @NotNull BlockState getAppearance(@NotNull BlockState state, @NotNull BlockAndTintGetter level, @NotNull BlockPos pos, @NotNull Direction side,
                                             @Nullable BlockState queryState, @Nullable BlockPos queryPos) {
        var appearance = this.getBlockAppearance(state, level, pos, side, queryState, queryPos);
        return appearance == null ? state : appearance;
    }
}

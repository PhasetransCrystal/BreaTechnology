package net.phasetranscrystal.breatechnology.api.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.phasetranscrystal.breatechnology.api.definition.MetaBlockDefinition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MetaBlock extends Block implements EntityBlock {
    private final MetaBlockDefinition definition;

    public MetaBlock(Properties properties, MetaBlockDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return definition.newBlockEntity(blockPos, blockState);
    }
}

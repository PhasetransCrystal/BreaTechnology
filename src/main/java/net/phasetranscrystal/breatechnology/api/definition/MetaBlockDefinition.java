package net.phasetranscrystal.breatechnology.api.definition;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public class MetaBlockDefinition<T extends BlockEntity> extends MetaDefinition<MetaBlockDefinition<T>> {
    private BlockEntityEntry<T> blockEntityEntry;

    public MetaBlockDefinition(AbstractRegistrate<?> owner, BlockEntityEntry<T> entityEntry) {
        super(owner);
        this.blockEntityEntry = entityEntry;
    }

    public T newBlockEntity(@NotNull BlockPos blockPos, @NotNull BlockState blockState) {
        return blockEntityEntry.create(blockPos, blockState);
    }
}

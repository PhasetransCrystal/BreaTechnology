package com.tterrag.registrate.util.entry;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import com.tterrag.registrate.AbstractRegistrate;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.Optional;

public class BlockEntityEntry<T extends BlockEntity> extends RegistryEntry<BlockEntityType<T>> {
    public BlockEntityEntry(AbstractRegistrate<?> owner, DeferredRegister<BlockEntityType<T>> delegate) {
        super(owner, delegate);
    }

    public T create(BlockPos pos, BlockState state) {
        return (T)((BlockEntityType)this.get()).create(pos, state);
    }

    public boolean is(@Nullable BlockEntity t) {
        return t != null && t.getType() == this.get();
    }

    public Optional<T> get(BlockGetter world, BlockPos pos) {
        return Optional.ofNullable(this.getNullable(world, pos));
    }

    @Nullable
    public T getNullable(BlockGetter world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        return (T)(this.is(be) ? be : null);
    }

    public static <T extends BlockEntity> BlockEntityEntry<T> cast(RegistryEntry<BlockEntityType<T>> entry) {
        return (BlockEntityEntry)RegistryEntry.cast(BlockEntityEntry.class, entry);
    }
}

package com.tterrag.registrate.util.entry;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import com.tterrag.registrate.AbstractRegistrate;
import net.neoforged.neoforge.registries.DeferredRegister;

public class BlockEntry<T extends Block> extends ItemProviderEntry<T> {
    public BlockEntry(AbstractRegistrate<?> owner, DeferredRegister<T> delegate) {
        super(owner, delegate);
    }

    public BlockState getDefaultState() {
        return ((Block)this.get()).defaultBlockState();
    }

    public boolean has(BlockState state) {
        return this.is(state.getBlock());
    }

    public static <T extends Block> BlockEntry<T> cast(RegistryEntry<T> entry) {
        return (BlockEntry)RegistryEntry.cast(BlockEntry.class, entry);
    }
}

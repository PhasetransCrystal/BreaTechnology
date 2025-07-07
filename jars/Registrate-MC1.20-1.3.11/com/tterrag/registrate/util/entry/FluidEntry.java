package com.tterrag.registrate.util.entry;


import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidType;
import com.tterrag.registrate.AbstractRegistrate;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.Optional;

public class FluidEntry<T extends ForgeFlowingFluid> extends RegistryEntry<T> {
    @Nullable
    private final BlockEntry<? extends Block> block;

    public FluidEntry(AbstractRegistrate<?> owner, DeferredRegister<T> delegate) {
        super(owner, delegate);
        BlockEntry<? extends Block> block = null;

        try {
            block = BlockEntry.cast(this.getSibling(ForgeRegistries.BLOCKS));
        } catch (IllegalArgumentException var5) {
        }

        this.block = block;
    }

    public <R> boolean is(R entry) {
        return ((ForgeFlowingFluid)this.get()).isSame((Fluid)entry);
    }

    public <S extends ForgeFlowingFluid> S getSource() {
        return (S)(((ForgeFlowingFluid)this.get()).getSource());
    }

    public FluidType getType() {
        return ((ForgeFlowingFluid)this.get()).getFluidType();
    }

    public <B extends Block> Optional<B> getBlock() {
        return Optional.ofNullable(this.block).map(RegistryEntry::get);
    }

    public <I extends Item> Optional<I> getBucket() {
        return Optional.ofNullable(((ForgeFlowingFluid)this.get()).getBucket());
    }
}

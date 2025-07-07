package com.tterrag.registrate.util.entry;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import com.tterrag.registrate.AbstractRegistrate;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemProviderEntry<T extends ItemLike> extends RegistryEntry<T> implements ItemLike {
    public ItemProviderEntry(AbstractRegistrate<?> owner, DeferredRegister<T> delegate) {
        super(owner, delegate);
    }

    public ItemStack asStack() {
        return new ItemStack(this);
    }

    public ItemStack asStack(int count) {
        return new ItemStack(this, count);
    }

    public boolean isIn(ItemStack stack) {
        return this.is(stack.getItem());
    }

    public boolean is(Item item) {
        return this.asItem() == item;
    }

    public Item asItem() {
        return ((ItemLike)this.get()).asItem();
    }
}
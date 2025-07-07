package com.tterrag.registrate.util.entry;

import net.minecraft.world.item.Item;
import com.tterrag.registrate.AbstractRegistrate;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ItemEntry<T extends Item> extends ItemProviderEntry<T> {
    public ItemEntry(AbstractRegistrate<?> owner, DeferredRegister<T> delegate) {
        super(owner, delegate);
    }

    public static <T extends Item> ItemEntry<T> cast(RegistryEntry<T> entry) {
        return (ItemEntry)RegistryEntry.cast(ItemEntry.class, entry);
    }
}

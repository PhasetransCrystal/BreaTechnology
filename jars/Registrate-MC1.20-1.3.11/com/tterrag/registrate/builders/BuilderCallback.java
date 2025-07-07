package com.tterrag.registrate.builders;

import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

@FunctionalInterface
public interface BuilderCallback {
    <R, T extends R> RegistryEntry<T> accept(String var1, ResourceKey<? extends Registry<R>> var2, Builder<R, T, ?, ?> var3, NonNullSupplier<? extends T> var4, NonNullFunction<RegistryObject<T>, ? extends RegistryEntry<T>> var5);

    default <R, T extends R> RegistryEntry<T> accept(String name, ResourceKey<? extends Registry<R>> type, Builder<R, T, ?, ?> builder, NonNullSupplier<? extends T> factory) {
        return this.accept(name, type, builder, factory, (delegate) -> new RegistryEntry(builder.getOwner(), delegate));
    }
}

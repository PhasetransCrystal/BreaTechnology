package com.tterrag.registrate.util.entry;

import com.tterrag.registrate.util.nullness.NonNullSupplier;

import javax.annotation.Nullable;

public class LazyRegistryEntry<T> implements NonNullSupplier<T> {
    @Nullable
    private NonNullSupplier<? extends RegistryEntry<? extends T>> supplier;
    @Nullable
    private RegistryEntry<? extends T> value;

    public LazyRegistryEntry(NonNullSupplier<? extends RegistryEntry<? extends T>> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        NonNullSupplier<? extends RegistryEntry<? extends T>> supplier = this.supplier;
        if (supplier != null) {
            this.value = (RegistryEntry)supplier.get();
            this.supplier = null;
        }

        return (T)this.value.get();
    }
}

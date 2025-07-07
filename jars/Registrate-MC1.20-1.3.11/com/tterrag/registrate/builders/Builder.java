package com.tterrag.registrate.builders;


import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

public interface Builder<R, T extends R, P, S extends Builder<R, T, P, S>> extends NonNullSupplier<RegistryEntry<T>> {
    RegistryEntry<T> register();

    AbstractRegistrate<?> getOwner();

    P getParent();

    String getName();

    ResourceKey<? extends Registry<R>> getRegistryKey();

    default RegistryEntry<T> get() {
        return this.getOwner().get(this.getName(), this.getRegistryKey());
    }

    default T getEntry() {
        return (T)this.get().get();
    }

    NonNullSupplier<T> asSupplier();

    default <D extends RegistrateProvider> S setData(ProviderType<? extends D> type, NonNullBiConsumer<DataGenContext<R, T>, D> cons) {
        this.getOwner().setDataGenerator(this, type, (prov) -> cons.accept(DataGenContext.from(this), prov));
        return (S)this;
    }

    default <D extends RegistrateProvider> S addMiscData(ProviderType<? extends D> type, NonNullConsumer<? extends D> cons) {
        this.getOwner().addDataGenerator(type, cons);
        return (S)this;
    }

    default S onRegister(NonNullConsumer<? super T> callback) {
        this.getOwner().addRegisterCallback(this.getName(), this.getRegistryKey(), callback);
        return (S)this;
    }

    default <OR> S onRegisterAfter(ResourceKey<? extends Registry<OR>> dependencyType, NonNullConsumer<? super T> callback) {
        return (S)this.onRegister((e) -> {
            if (this.getOwner().isRegistered(dependencyType)) {
                callback.accept(e);
            } else {
                this.getOwner().addRegisterCallback(dependencyType, () -> callback.accept(e));
            }

        });
    }

    default <R2, T2 extends R2, P2, S2 extends Builder<R2, T2, P2, S2>> S2 transform(NonNullFunction<S, S2> func) {
        return (S2)(func.apply(this));
    }

    default P build() {
        this.register();
        return (P)this.getParent();
    }
}
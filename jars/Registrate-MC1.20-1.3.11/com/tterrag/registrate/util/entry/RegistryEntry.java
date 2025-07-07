package com.tterrag.registrate.util.entry;

import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.RegisterEvent;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegistryEntry<T> implements NonNullSupplier<T> {
    private static RegistryEntry<?> EMPTY;
    private final AbstractRegistrate<?> owner;
    @Nullable
    private final DeferredRegister<T> delegate;
    private static final Method _updateReference_Registry;
    private static final Method _updateReference_Event;

    public static <T> RegistryEntry<T> empty() {
        RegistryEntry<T> t = EMPTY;
        return t;
    }

    public RegistryEntry(AbstractRegistrate<?> owner, DeferredRegister<T> delegate) {
        if (EMPTY != null && owner == null) {
            throw new NullPointerException("Owner must not be null");
        } else if (EMPTY != null && delegate == null) {
            throw new NullPointerException("Delegate must not be null");
        } else {
            this.owner = owner;
            this.delegate = delegate;
        }
    }

    /** @deprecated */
    @Deprecated
    public void updateReference(IForgeRegistry<? super T> event) {
        DeferredRegister<T> delegate = this.delegate;

        try {
            _updateReference_Registry.invoke(Objects.requireNonNull(delegate, "Registry entry is empty"), event);
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateReference(RegisterEvent event) {
        DeferredRegister<T> delegate = this.delegate;

        try {
            _updateReference_Event.invoke(Objects.requireNonNull(delegate, "Registry entry is empty"), event);
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public @NonnullType T get() {
        DeferredRegister<T> delegate = this.delegate;
        return (T)Objects.requireNonNull(this.getUnchecked(), () -> delegate == null ? "Registry entry is empty" : "Registry entry not present: " + delegate.getId());
    }

    @Nullable
    public T getUnchecked() {
        DeferredRegister<T> delegate = this.delegate;
        return (T)(delegate == null ? null : delegate.orElse((Object)null));
    }

    public <R, E extends R> RegistryEntry<E> getSibling(ResourceKey<? extends Registry<R>> registryType) {
        return this == EMPTY ? empty() : this.owner.get(this.getId().getPath(), registryType);
    }

    public <R, E extends R> RegistryEntry<E> getSibling(IForgeRegistry<R> registry) {
        return this.getSibling(registry.getRegistryKey());
    }

    public RegistryEntry<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return this.isPresent() && !predicate.test(this.get()) ? empty() : this;
    }

    public <R> boolean is(R entry) {
        return this.get() == entry;
    }

    protected static <E extends RegistryEntry<?>> E cast(Class<? super E> clazz, RegistryEntry<?> entry) {
        if (clazz.isInstance(entry)) {
            return (E)entry;
        } else {
            throw new IllegalArgumentException("Could not convert RegistryEntry: expecting " + clazz + ", found " + entry.getClass());
        }
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof RegistryEntry)) {
            return false;
        } else {
            RegistryEntry<?> other = (RegistryEntry)o;
            if (!other.canEqual(this)) {
                return false;
            } else {
                Object this$delegate = this.delegate;
                Object other$delegate = other.delegate;
                if (this$delegate == null) {
                    if (other$delegate != null) {
                        return false;
                    }
                } else if (!this$delegate.equals(other$delegate)) {
                    return false;
                }

                return true;
            }
        }
    }

    protected boolean canEqual(Object other) {
        return other instanceof RegistryEntry;
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $delegate = this.delegate;
        result = result * 59 + ($delegate == null ? 43 : $delegate.hashCode());
        return result;
    }

    public ResourceLocation getId() {
        return this.delegate.getId();
    }

    public ResourceKey<T> getKey() {
        return this.delegate.getKey();
    }

    public Stream<T> stream() {
        return this.delegate.stream();
    }

    public boolean isPresent() {
        return this.delegate.isPresent();
    }

    public void ifPresent(Consumer<? super T> consumer) {
        this.delegate.ifPresent(consumer);
    }

    public <U> Optional<U> map(Function<? super T, ? extends U> mapper) {
        return this.delegate.map(mapper);
    }

    public <U> Optional<U> flatMap(Function<? super T, Optional<U>> mapper) {
        return this.delegate.flatMap(mapper);
    }

    public <U> Supplier<U> lazyMap(Function<? super T, ? extends U> mapper) {
        return this.delegate.lazyMap(mapper);
    }

    public T orElse(T other) {
        return (T)this.delegate.orElse(other);
    }

    public T orElseGet(Supplier<? extends T> other) {
        return (T)this.delegate.orElseGet(other);
    }

    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        return (T)this.delegate.orElseThrow(exceptionSupplier);
    }

    public Optional<Holder<T>> getHolder() {
        return this.delegate.getHolder();
    }

    static {
        try {
            RegistryEntry<?> ret = new RegistryEntry((AbstractRegistrate)null, (DeferredRegister)ObfuscationReflectionHelper.findMethod(DeferredRegister.class, "empty", new Class[0]).invoke((Object)null));
            EMPTY = ret;
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        _updateReference_Registry = ObfuscationReflectionHelper.findMethod(DeferredRegister.class, "updateReference", new Class[]{IForgeRegistry.class});
        _updateReference_Event = ObfuscationReflectionHelper.findMethod(DeferredRegister.class, "updateReference", new Class[]{RegisterEvent.class});
    }

    private interface Exclusions<T> {
        T get();

        DeferredRegister<T> filter(Predicate<? super T> var1);

        void updateReference(IForgeRegistry<? extends T> var1);
    }
}

package com.tterrag.registrate.providers;


import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;

public final class DataGenContext<R, E extends R> implements NonNullSupplier<E> {
    private final NonNullSupplier<E> entry;
    private final String name;
    private final ResourceLocation id;

    public @NonnullType E getEntry() {
        return (E)this.entry.get();
    }

    /** @deprecated */
    @Deprecated
    public static <R, E extends R> DataGenContext<R, E> from(Builder<R, E, ?, ?> builder, ResourceKey<? extends Registry<R>> type) {
        return from(builder);
    }

    public static <R, E extends R> DataGenContext<R, E> from(Builder<R, E, ?, ?> builder) {
        return new DataGenContext<R, E>(NonNullSupplier.of(builder.getOwner().get(builder.getName(), builder.getRegistryKey())), builder.getName(), ResourceLocation.fromNamespaceAndPath(builder.getOwner().getModid(), builder.getName()));
    }

    public DataGenContext(NonNullSupplier<E> entry, String name, ResourceLocation id) {
        this.entry = entry;
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public ResourceLocation getId() {
        return this.id;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (!(o instanceof DataGenContext)) {
            return false;
        } else {
            DataGenContext<?, ?> other = (DataGenContext)o;
            Object this$entry = this.getEntry();
            Object other$entry = other.getEntry();
            if (this$entry == null) {
                if (other$entry != null) {
                    return false;
                }
            } else if (!this$entry.equals(other$entry)) {
                return false;
            }

            Object this$name = this.getName();
            Object other$name = other.getName();
            if (this$name == null) {
                if (other$name != null) {
                    return false;
                }
            } else if (!this$name.equals(other$name)) {
                return false;
            }

            Object this$id = this.getId();
            Object other$id = other.getId();
            if (this$id == null) {
                if (other$id != null) {
                    return false;
                }
            } else if (!this$id.equals(other$id)) {
                return false;
            }

            return true;
        }
    }

    public int hashCode() {
        int PRIME = 59;
        int result = 1;
        Object $entry = this.getEntry();
        result = result * 59 + ($entry == null ? 43 : $entry.hashCode());
        Object $name = this.getName();
        result = result * 59 + ($name == null ? 43 : $name.hashCode());
        Object $id = this.getId();
        result = result * 59 + ($id == null ? 43 : $id.hashCode());
        return result;
    }

    public String toString() {
        Object var10000 = this.getEntry();
        return "DataGenContext(entry=" + var10000 + ", name=" + this.getName() + ", id=" + this.getId() + ")";
    }

    public E get() {
        return (E)this.entry.get();
    }

    public NonNullSupplier<E> lazy() {
        return this.entry.lazy();
    }
}

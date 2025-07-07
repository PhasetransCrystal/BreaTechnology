package com.tterrag.registrate.util.entry;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import com.tterrag.registrate.AbstractRegistrate;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;

public class EntityEntry<T extends Entity> extends RegistryEntry<EntityType<T>> {
    public EntityEntry(AbstractRegistrate<?> owner, DeferredRegister<EntityType<T>> delegate) {
        super(owner, delegate);
    }

    @Nullable
    public T create(Level world) {
        return (T)((EntityType)this.get()).create(world);
    }

    public boolean is(Entity t) {
        return t != null && t.getType() == this.get();
    }

    public static <T extends Entity> EntityEntry<T> cast(RegistryEntry<EntityType<T>> entry) {
        return (EntityEntry)RegistryEntry.cast(EntityEntry.class, entry);
    }
}

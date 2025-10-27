package net.phasetranscrystal.breatechnology.api.material.type;

import net.minecraft.resources.ResourceLocation;
import net.phasetranscrystal.breatechnology.api.definition.MetaDefinition;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.Getter;
import lombok.Setter;

/// 基础材质类
public class MetaMaterial<S extends MetaMaterial<S>> extends MetaDefinition<S> {

    public MetaMaterial(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }

    @Getter
    @Setter
    private int color = 0xFFFFFFFF;
    @Getter
    @Setter
    private int secondaryColor = 0xFFFFFFFF;

    private static final ThreadLocal<MetaMaterial<?>> STATE = new ThreadLocal<>();

    private static MetaMaterial<?> getInstance() {
        return STATE.get();
    }

    public static void setInstance(MetaMaterial<?> instance) {
        STATE.set(instance);
    }

    public static void clearInstance() {
        STATE.remove();
    }
}

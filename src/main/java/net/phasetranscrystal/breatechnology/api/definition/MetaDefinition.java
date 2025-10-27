package net.phasetranscrystal.breatechnology.api.definition;

import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.Getter;

import java.util.function.Consumer;

/// 基础定义信息
public class MetaDefinition<S extends MetaDefinition<S>> {

    @Getter
    private final ResourceLocation id;
    @Getter
    private final AbstractRegistrate<?> owner;

    public S Self() {
        return (S) this;
    }

    protected MetaDefinition(AbstractRegistrate<?> owner, ResourceLocation id) {
        this.owner = owner;
        this.id = id;
    }

    public S transform(Consumer<S> transformer) {
        transformer.accept((S) this);
        return (S) this;
    }
}

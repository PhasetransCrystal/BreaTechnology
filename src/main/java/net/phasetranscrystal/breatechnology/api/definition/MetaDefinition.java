package net.phasetranscrystal.breatechnology.api.definition;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.Getter;

import java.util.function.Consumer;

public class MetaDefinition<S extends MetaDefinition<S>> {
    @Getter
    private final AbstractRegistrate<?> owner;

    protected MetaDefinition(AbstractRegistrate<?> owner) {
        this.owner = owner;
    }

    public S transform(Consumer<S> transformer) {
        transformer.accept((S) this);
        return (S) this;
    }
}
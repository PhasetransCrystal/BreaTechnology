package com.tterrag.registrate.builders;


import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Registry;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateLangProvider;
import com.tterrag.registrate.providers.RegistrateTagsProvider;
import com.tterrag.registrate.util.entry.LazyRegistryEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Stream;

public abstract class AbstractBuilder<R, T extends R, P, S extends AbstractBuilder<R, T, P, S>> implements Builder<R, T, P, S> {
    private final AbstractRegistrate<?> owner;
    private final P parent;
    private final String name;
    private final BuilderCallback callback;
    private final ResourceKey<Registry<R>> registryKey;
    private final Multimap<ProviderType<? extends RegistrateTagsProvider<?>>, TagKey<?>> tagsByType = HashMultimap.create();
    private final LazyRegistryEntry<T> safeSupplier = new LazyRegistryEntry(this);

    protected abstract @NonnullType T createEntry();

    public RegistryEntry<T> register() {
        return this.callback.accept(this.name, this.registryKey, this, this::createEntry, this::createEntryWrapper);
    }

    protected RegistryEntry<T> createEntryWrapper(DeferredRegister<T> delegate) {
        return new RegistryEntry<>(this.getOwner(), delegate);
    }

    public NonNullSupplier<T> asSupplier() {
        return this.safeSupplier;
    }

    @SafeVarargs
    public final <TP extends TagsProvider<R> & RegistrateTagsProvider<R>> S tag(ProviderType<? extends TP> type, TagKey<R>... tags) {
        if (!this.tagsByType.containsKey(type)) {
            this.setData(type, (ctx, prov) -> {
                Stream<TagKey<?>> var10000 = this.tagsByType.get(type).stream().map((t) -> t);
                RegistrateTagsProvider var10001 = (RegistrateTagsProvider)prov;
                Objects.requireNonNull((RegistrateTagsProvider)prov);
                var10000.map(var10001::addTag).forEach((b) -> b.add(TagEntry.element(ResourceLocation.fromNamespaceAndPath(this.getOwner().getModid(), this.getName()))));
            });
        }

        this.tagsByType.putAll(type, Arrays.asList(tags));
        return (S)this;
    }

    @SafeVarargs
    public final <TP extends TagsProvider<R> & RegistrateTagsProvider<R>> S removeTag(ProviderType<TP> type, TagKey<R>... tags) {
        if (this.tagsByType.containsKey(type)) {
            for(TagKey<R> tag : tags) {
                this.tagsByType.remove(type, tag);
            }
        }

        return (S)this;
    }

    public S lang(NonNullFunction<T, String> langKeyProvider) {
        return (S)this.lang(langKeyProvider, (NonNullBiFunction)((p, t) -> p.getAutomaticName(t, this.getRegistryKey())));
    }

    public S lang(NonNullFunction<T, String> langKeyProvider, String name) {
        return (S)this.lang(langKeyProvider, (NonNullBiFunction)((p, s) -> name));
    }

    private S lang(NonNullFunction<T, String> langKeyProvider, NonNullBiFunction<RegistrateLangProvider, NonNullSupplier<? extends T>, String> localizedNameProvider) {
        return (S)(this.setData(ProviderType.LANG, (ctx, prov) -> {
            String var10001 = (String)langKeyProvider.apply(ctx.getEntry());
            Objects.requireNonNull(ctx);
            prov.add(var10001, (String)localizedNameProvider.apply(prov, ctx::getEntry));
        }));
    }

    public AbstractBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<Registry<R>> registryKey) {
        this.owner = owner;
        this.parent = parent;
        this.name = name;
        this.callback = callback;
        this.registryKey = registryKey;
    }

    public AbstractRegistrate<?> getOwner() {
        return this.owner;
    }

    public P getParent() {
        return this.parent;
    }

    public String getName() {
        return this.name;
    }

    protected BuilderCallback getCallback() {
        return this.callback;
    }

    public ResourceKey<Registry<R>> getRegistryKey() {
        return this.registryKey;
    }
}

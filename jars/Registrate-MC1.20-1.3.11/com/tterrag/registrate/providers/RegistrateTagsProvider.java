package com.tterrag.registrate.providers;


import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.neoforged.fml.LogicalSide;
import com.tterrag.registrate.AbstractRegistrate;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

public interface RegistrateTagsProvider<T> extends RegistrateProvider {
    TagsProvider.TagAppender<T> addTag(TagKey<T> var1);

    public static class Impl<T> extends TagsProvider<T> implements RegistrateTagsProvider<T> {
        private final AbstractRegistrate<?> owner;
        private final ProviderType<? extends Impl<T>> type;
        private final String name;

        public Impl(AbstractRegistrate<?> owner, ProviderType<? extends Impl<T>> type, String name, PackOutput packOutput, ResourceKey<? extends Registry<T>> registryIn, CompletableFuture<HolderLookup.Provider> registriesLookup, ExistingFileHelper existingFileHelper) {
            super(packOutput, registryIn, registriesLookup, owner.getModid(), existingFileHelper);
            this.owner = owner;
            this.type = type;
            this.name = name;
        }

        public String getName() {
            return "Tags (%s)".formatted(this.name);
        }

        protected void addTags(HolderLookup.Provider provider) {
            this.owner.genData(this.type, this);
        }

        public LogicalSide getSide() {
            return LogicalSide.SERVER;
        }

        public TagsProvider.TagAppender<T> addTag(TagKey<T> tag) {
            return super.tag(tag);
        }
    }

    public static class IntrinsicImpl<T> extends IntrinsicHolderTagsProvider<T> implements RegistrateTagsProvider<T> {
        private final AbstractRegistrate<?> owner;
        private final ProviderType<? extends IntrinsicImpl<T>> type;
        private final String name;

        public IntrinsicImpl(AbstractRegistrate<?> owner, ProviderType<? extends IntrinsicImpl<T>> type, String name, PackOutput packOutput, ResourceKey<? extends Registry<T>> registryIn, CompletableFuture<HolderLookup.Provider> registriesLookup, Function<T, ResourceKey<T>> keyExtractor, ExistingFileHelper existingFileHelper) {
            super(packOutput, registryIn, registriesLookup, keyExtractor, owner.getModid(), existingFileHelper);
            this.owner = owner;
            this.type = type;
            this.name = name;
        }

        public String getName() {
            return "Tags (%s)".formatted(this.name);
        }

        protected void addTags(HolderLookup.Provider provider) {
            this.owner.genData(this.type, this);
        }

        public LogicalSide getSide() {
            return LogicalSide.SERVER;
        }

        public IntrinsicHolderTagsProvider.IntrinsicTagAppender<T> addTag(TagKey<T> tag) {
            return super.tag(tag);
        }
    }
}

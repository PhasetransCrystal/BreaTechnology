package com.tterrag.registrate.builders;


import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.loot.RegistrateEntityLootTables;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.EntityEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class EntityBuilder<T extends Entity, P> extends AbstractBuilder<EntityType<?>, EntityType<T>, P, EntityBuilder<T, P>> {
    private final NonNullSupplier<EntityType.Builder<T>> builder;
    private NonNullConsumer<EntityType.Builder<T>> builderCallback = ($) -> {
    };
    @Nullable
    private NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer;
    private boolean attributesConfigured;
    private boolean spawnConfigured;

    public static <T extends Entity, P> EntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
        return (new EntityBuilder<T, P>(owner, parent, name, callback, factory, classification)).defaultLang();
    }

    protected EntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EntityType.EntityFactory<T> factory, MobCategory classification) {
        super(owner, parent, name, callback, Keys.ENTITY_TYPES);
        this.builder = () -> EntityType.Builder.of(factory, classification);
    }

    public EntityBuilder<T, P> properties(NonNullConsumer<EntityType.Builder<T>> cons) {
        this.builderCallback = this.builderCallback.andThen(cons);
        return this;
    }

    public EntityBuilder<T, P> renderer(NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer) {
        if (this.renderer == null) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerRenderer);
        }

        this.renderer = renderer;
        return this;
    }

    protected void registerRenderer() {
        OneTimeEventReceiver.addModListener(this.getOwner(), EntityRenderersEvent.RegisterRenderers.class, (evt) -> {
            NonNullSupplier<NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>>> renderer = this.renderer;
            if (renderer != null) {
                try {
                    NonNullFunction<EntityRendererProvider.Context, EntityRenderer<? super T>> provider = (NonNullFunction)renderer.get();
                    EntityType var10001 = (EntityType)this.getEntry();
                    Objects.requireNonNull(provider);
                    evt.registerEntityRenderer(var10001, provider::apply);
                } catch (Exception e) {
                    throw new IllegalStateException("Failed to register renderer for Entity " + this.get().getId(), e);
                }
            }

        });
    }

    public EntityBuilder<T, P> attributes(Supplier<AttributeSupplier.Builder> attributes) {
        if (this.attributesConfigured) {
            throw new IllegalStateException("Cannot configure attributes more than once");
        } else {
            this.attributesConfigured = true;
            OneTimeEventReceiver.addModListener(this.getOwner(), EntityAttributeCreationEvent.class, (e) -> e.put((EntityType)this.getEntry(), ((AttributeSupplier.Builder)attributes.get()).build()));
            return this;
        }
    }

    public EntityBuilder<T, P> spawnPlacement(SpawnPlacements.Type type, Heightmap.Types heightmap, SpawnPlacements.SpawnPredicate<T> predicate) {
        if (this.spawnConfigured) {
            throw new IllegalStateException("Cannot configure spawn placement more than once");
        } else {
            this.spawnConfigured = true;
            this.onRegister((t) -> SpawnPlacements.register(t, type, heightmap, predicate));
            return this;
        }
    }

    /** @deprecated */
    @Deprecated
    public EntityBuilder<T, P> defaultSpawnEgg(int primaryColor, int secondaryColor) {
        return (EntityBuilder)this.spawnEgg(primaryColor, secondaryColor).build();
    }

    /** @deprecated */
    @Deprecated
    public ItemBuilder<? extends SpawnEggItem, EntityBuilder<T, P>> spawnEgg(int primaryColor, int secondaryColor) {
        NonNullSupplier<EntityType<T>> sup = this.asSupplier();
        return this.getOwner().item(this, this.getName() + "_spawn_egg", (p) -> new ForgeSpawnEggItem(sup, primaryColor, secondaryColor, p)).tab(CreativeModeTabs.SPAWN_EGGS).model((ctx, prov) -> prov.withExistingParent(ctx.getName(), new ResourceLocation("item/template_spawn_egg")));
    }

    public EntityBuilder<T, P> defaultLang() {
        return (EntityBuilder)this.lang(EntityType::getDescriptionId);
    }

    public EntityBuilder<T, P> lang(String name) {
        return (EntityBuilder)this.lang(EntityType::getDescriptionId, name);
    }

    public EntityBuilder<T, P> loot(NonNullBiConsumer<RegistrateEntityLootTables, EntityType<T>> cons) {
        return (EntityBuilder)this.setData(ProviderType.LOOT, (ctx, prov) -> prov.addLootAction(RegistrateLootTableProvider.LootType.ENTITY, (tb) -> cons.accept(tb, (EntityType)ctx.getEntry())));
    }

    @SafeVarargs
    public final EntityBuilder<T, P> tag(TagKey<EntityType<?>>... tags) {
        return (EntityBuilder)this.tag(ProviderType.ENTITY_TAGS, tags);
    }

    protected EntityType<T> createEntry() {
        EntityType.Builder<T> builder = (EntityType.Builder)this.builder.get();
        this.builderCallback.accept(builder);
        return builder.build(this.getName());
    }

    /** @deprecated */
    @Deprecated
    protected void injectSpawnEggType(EntityType<T> entry) {
    }

    protected RegistryEntry<EntityType<T>> createEntryWrapper(DeferredRegister<EntityType<T>> delegate) {
        return new EntityEntry(this.getOwner(), delegate);
    }

    public EntityEntry<T> register() {
        return (EntityEntry)super.register();
    }
}

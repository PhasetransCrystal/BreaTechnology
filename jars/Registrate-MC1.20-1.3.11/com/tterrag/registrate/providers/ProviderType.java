package com.tterrag.registrate.providers;


import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.nullness.FieldsAreNonnullByDefault;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@FunctionalInterface
@FieldsAreNonnullByDefault
@ParametersAreNonnullByDefault
public interface ProviderType<T extends RegistrateProvider> {
    ProviderType<RegistrateRecipeProvider> RECIPE = register("recipe", (NonNullBiFunction)((p, e) -> new RegistrateRecipeProvider(p, e.getGenerator().getPackOutput())));
    ProviderType<RegistrateAdvancementProvider> ADVANCEMENT = register("advancement", (NonNullBiFunction)((p, e) -> new RegistrateAdvancementProvider(p, e.getGenerator().getPackOutput(), e.getLookupProvider())));
    ProviderType<RegistrateLootTableProvider> LOOT = register("loot", (NonNullBiFunction)((p, e) -> new RegistrateLootTableProvider(p, e.getGenerator().getPackOutput())));
    ProviderType<RegistrateTagsProvider.IntrinsicImpl<Block>> BLOCK_TAGS = register("tags/block", (NonNullFunction)((type) -> (p, e) -> new RegistrateTagsProvider.IntrinsicImpl(p, type, "blocks", e.getGenerator().getPackOutput(), Registries.BLOCK, e.getLookupProvider(), (block) -> block.builtInRegistryHolder().key(), e.getExistingFileHelper())));
    ProviderType<RegistrateItemTagsProvider> ITEM_TAGS = registerDelegate("tags/item", (type) -> (p, e, existing) -> new RegistrateItemTagsProvider(p, type, "items", e.getGenerator().getPackOutput(), e.getLookupProvider(), ((TagsProvider)existing.get(BLOCK_TAGS)).contentsGetter(), e.getExistingFileHelper()));
    ProviderType<RegistrateTagsProvider.IntrinsicImpl<Fluid>> FLUID_TAGS = register("tags/fluid", (NonNullFunction)((type) -> (p, e) -> new RegistrateTagsProvider.IntrinsicImpl(p, type, "fluids", e.getGenerator().getPackOutput(), Registries.FLUID, e.getLookupProvider(), (fluid) -> fluid.builtInRegistryHolder().key(), e.getExistingFileHelper())));
    ProviderType<RegistrateTagsProvider.IntrinsicImpl<EntityType<?>>> ENTITY_TAGS = register("tags/entity", (NonNullFunction)((type) -> (p, e) -> new RegistrateTagsProvider.IntrinsicImpl(p, type, "entity_types", e.getGenerator().getPackOutput(), Registries.ENTITY_TYPE, e.getLookupProvider(), (entityType) -> entityType.builtInRegistryHolder().key(), e.getExistingFileHelper())));
    ProviderType<RegistrateGenericProvider> GENERIC_SERVER = register("registrate_generic_server_provider", (NonNullFunction)((providerType) -> (registrate, event) -> new RegistrateGenericProvider(registrate, event, LogicalSide.SERVER, providerType)));
    ProviderType<RegistrateBlockstateProvider> BLOCKSTATE = register("blockstate", (NonNullBiFunction)((p, e) -> new RegistrateBlockstateProvider(p, e.getGenerator().getPackOutput(), e.getExistingFileHelper())));
    ProviderType<RegistrateItemModelProvider> ITEM_MODEL = register("item_model", (ProviderType)((p, e, existing) -> new RegistrateItemModelProvider(p, e.getGenerator().getPackOutput(), ((RegistrateBlockstateProvider)existing.get(BLOCKSTATE)).getExistingFileHelper())));
    ProviderType<RegistrateLangProvider> LANG = register("lang", (NonNullBiFunction)((p, e) -> new RegistrateLangProvider(p, e.getGenerator().getPackOutput())));
    ProviderType<RegistrateGenericProvider> GENERIC_CLIENT = register("registrate_generic_client_provider", (NonNullFunction)((providerTYpe) -> (registrate, event) -> new RegistrateGenericProvider(registrate, event, LogicalSide.CLIENT, providerTYpe)));

    T create(AbstractRegistrate<?> var1, GatherDataEvent var2, Map<ProviderType<?>, RegistrateProvider> var3);

    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> registerDelegate(String name, final NonNullUnaryOperator<ProviderType<T>> type) {
        ProviderType<T> ret = new ProviderType<T>() {
            public T create(@Nonnull AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing) {
                return (T)((ProviderType)type.apply(this)).create(parent, event, existing);
            }
        };
        return register(name, ret);
    }

    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> register(String name, final NonNullFunction<ProviderType<T>, NonNullBiFunction<AbstractRegistrate<?>, GatherDataEvent, T>> type) {
        ProviderType<T> ret = new ProviderType<T>() {
            public T create(@Nonnull AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing) {
                return (T)((RegistrateProvider)((NonNullBiFunction)type.apply(this)).apply(parent, event));
            }
        };
        return register(name, ret);
    }

    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> register(String name, final NonNullBiFunction<AbstractRegistrate<?>, GatherDataEvent, T> type) {
        ProviderType<T> ret = new ProviderType<T>() {
            public T create(AbstractRegistrate<?> parent, GatherDataEvent event, Map<ProviderType<?>, RegistrateProvider> existing) {
                return (T)((RegistrateProvider)type.apply(parent, event));
            }
        };
        return register(name, ret);
    }

    @Nonnull
    static <T extends RegistrateProvider> ProviderType<T> register(String name, ProviderType<T> type) {
        RegistrateDataProvider.TYPES.put(name, type);
        return type;
    }
}

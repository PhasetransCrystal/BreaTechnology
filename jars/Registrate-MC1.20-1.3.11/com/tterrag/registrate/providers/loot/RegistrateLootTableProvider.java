package com.tterrag.registrate.providers.loot;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.data.loot.packs.VanillaLootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class RegistrateLootTableProvider extends LootTableProvider implements RegistrateProvider {
    private static final Map<String, LootType<?>> LOOT_TYPES = new HashMap();
    private final AbstractRegistrate<?> parent;
    private final Multimap<LootType<?>, Consumer<? super RegistrateLootTables>> specialLootActions = HashMultimap.create();
    private final Multimap<LootContextParamSet, Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>> lootActions = HashMultimap.create();
    private final Set<RegistrateLootTables> currentLootCreators = new HashSet();
    private static final BiMap<ResourceLocation, LootContextParamSet> SET_REGISTRY = (BiMap) ObfuscationReflectionHelper.getPrivateValue(LootContextParamSets.class, (Object)null, "REGISTRY");

    public RegistrateLootTableProvider(AbstractRegistrate<?> parent, PackOutput packOutput) {
        super(packOutput, Set.of(), VanillaLootTableProvider.create(packOutput).getTables());
        this.parent = parent;
    }

    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationresults) {
        this.currentLootCreators.forEach((c) -> c.validate(map, validationresults));
    }

    public <T extends RegistrateLootTables> void addLootAction(LootType<T> type, NonNullConsumer<T> action) {
        this.specialLootActions.put(type, action);
    }

    public void addLootAction(LootContextParamSet set, Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> action) {
        this.lootActions.put(set, action);
    }

    private Supplier<LootTableSubProvider> getLootCreator(AbstractRegistrate<?> parent, LootType<?> type) {
        return () -> {
            RegistrateLootTables creator = type.getLootCreator(parent, (cons) -> this.specialLootActions.get(type).forEach((c) -> c.accept(cons)));
            this.currentLootCreators.add(creator);
            return creator;
        };
    }

    public List<SubProviderEntry> getTables() {
        this.parent.genData(ProviderType.LOOT, this);
        this.currentLootCreators.clear();
        ImmutableList.Builder<LootTableProvider.SubProviderEntry> builder = ImmutableList.builder();

        for(LootType<?> type : LOOT_TYPES.values()) {
            builder.add(new LootTableProvider.SubProviderEntry(this.getLootCreator(this.parent, type), type.getLootSet()));
        }

        for(LootContextParamSet set : SET_REGISTRY.values()) {
            builder.add(new LootTableProvider.SubProviderEntry(() -> (callback) -> this.lootActions.get(set).forEach((a) -> a.accept(callback)), set));
        }

        return builder.build();
    }

    public interface LootType<T extends RegistrateLootTables> {
        LootType<RegistrateBlockLootTables> BLOCK = register("block", LootContextParamSets.BLOCK, RegistrateBlockLootTables::new);
        LootType<RegistrateEntityLootTables> ENTITY = register("entity", LootContextParamSets.ENTITY, RegistrateEntityLootTables::new);

        T getLootCreator(AbstractRegistrate<?> var1, Consumer<T> var2);

        LootContextParamSet getLootSet();

        static <T extends RegistrateLootTables> LootType<T> register(String name, final LootContextParamSet set, final NonNullBiFunction<AbstractRegistrate, Consumer<T>, T> factory) {
            LootType<T> type = new LootType<T>() {
                public T getLootCreator(AbstractRegistrate<?> parent, Consumer<T> callback) {
                    return (T)((RegistrateLootTables)factory.apply(parent, callback));
                }

                public LootContextParamSet getLootSet() {
                    return set;
                }
            };
            RegistrateLootTableProvider.LOOT_TYPES.put(name, type);
            return type;
        }
    }
}

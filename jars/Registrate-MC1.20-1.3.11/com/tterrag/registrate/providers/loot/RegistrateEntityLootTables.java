package com.tterrag.registrate.providers.loot;


import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.packs.VanillaEntityLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.FrogVariant;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import com.tterrag.registrate.AbstractRegistrate;

import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class RegistrateEntityLootTables extends VanillaEntityLoot implements RegistrateLootTables {
    private final AbstractRegistrate<?> parent;
    private final Consumer<RegistrateEntityLootTables> callback;

    public void generate() {
        this.callback.accept(this);
    }

    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return this.parent.getAll(Registries.ENTITY_TYPE).stream().map(Supplier::get);
    }

    public static LootTable.Builder createSheepTable(ItemLike p_249422_) {
        return EntityLootSubProvider.createSheepTable(p_249422_);
    }

    public boolean canHaveLootTable(EntityType<?> p_249029_) {
        return super.canHaveLootTable(p_249029_);
    }

    public LootItemCondition.Builder killedByFrogVariant(FrogVariant p_249403_) {
        return super.killedByFrogVariant(p_249403_);
    }

    public void add(EntityType<?> p_248740_, LootTable.Builder p_249440_) {
        super.add(p_248740_, p_249440_);
    }

    public void add(EntityType<?> p_252130_, ResourceLocation p_251706_, LootTable.Builder p_249357_) {
        super.add(p_252130_, p_251706_, p_249357_);
    }

    public RegistrateEntityLootTables(AbstractRegistrate<?> parent, Consumer<RegistrateEntityLootTables> callback) {
        this.parent = parent;
        this.callback = callback;
    }
}

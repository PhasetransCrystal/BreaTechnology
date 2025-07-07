package com.tterrag.registrate.builders;


import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.enchantment.Enchantment;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonnullType;

import java.util.Arrays;
import java.util.EnumSet;

public class EnchantmentBuilder<T extends Enchantment, P> extends AbstractBuilder<Enchantment, T, P, EnchantmentBuilder<T, P>> {
    private Enchantment.Rarity rarity;
    private final EnchantmentCategory type;
    private EnumSet<EquipmentSlot> slots;
    private final EnchantmentFactory<T> factory;

    public static <T extends Enchantment, P> EnchantmentBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EnchantmentCategory type, EnchantmentFactory<T> factory) {
        return (new EnchantmentBuilder<T, P>(owner, parent, name, callback, type, factory)).defaultLang();
    }

    protected EnchantmentBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, EnchantmentCategory type, EnchantmentFactory<T> factory) {
        super(owner, parent, name, callback, Keys.ENCHANTMENTS);
        this.rarity = Rarity.COMMON;
        this.slots = EnumSet.noneOf(EquipmentSlot.class);
        this.factory = factory;
        this.type = type;
    }

    public EnchantmentBuilder<T, P> rarity(Enchantment.Rarity rarity) {
        this.rarity = rarity;
        return this;
    }

    public EnchantmentBuilder<T, P> addArmorSlots() {
        return this.addSlots(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
    }

    public EnchantmentBuilder<T, P> addSlots(EquipmentSlot... slots) {
        this.slots.addAll(Arrays.asList(slots));
        return this;
    }

    public EnchantmentBuilder<T, P> defaultLang() {
        return (EnchantmentBuilder)this.lang(Enchantment::getDescriptionId);
    }

    public EnchantmentBuilder<T, P> lang(String name) {
        return (EnchantmentBuilder)this.lang(Enchantment::getDescriptionId, name);
    }

    protected @NonnullType T createEntry() {
        return this.factory.create(this.rarity, this.type, (EquipmentSlot[])this.slots.toArray(new EquipmentSlot[0]));
    }

    @FunctionalInterface
    public interface EnchantmentFactory<T extends Enchantment> {
        T create(Enchantment.Rarity var1, EnchantmentCategory var2, EquipmentSlot... var3);
    }
}

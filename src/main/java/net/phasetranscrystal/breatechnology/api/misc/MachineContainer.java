package net.phasetranscrystal.breatechnology.api.misc;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.INBTSerializable;
import org.jetbrains.annotations.UnknownNullability;

import java.util.Set;
import java.util.function.Predicate;

public class MachineContainer implements Container {
    @Override
    public int getContainerSize() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public ItemStack getItem(int i) {
        return null;
    }

    @Override
    public ItemStack removeItem(int i, int i1) {
        return null;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return null;
    }

    @Override
    public void setItem(int i, ItemStack itemStack) {

    }

    @Override
    public int getMaxStackSize() {
        return Container.super.getMaxStackSize();
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return Container.super.getMaxStackSize(stack);
    }

    @Override
    public void setChanged() {

    }

    @Override
    public boolean stillValid(Player player) {
        return false;
    }

    @Override
    public void startOpen(Player player) {
        Container.super.startOpen(player);
    }

    @Override
    public void stopOpen(Player player) {
        Container.super.stopOpen(player);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return Container.super.canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItem(Container target, int slot, ItemStack stack) {
        return Container.super.canTakeItem(target, slot, stack);
    }

    @Override
    public int countItem(Item item) {
        return Container.super.countItem(item);
    }

    @Override
    public boolean hasAnyOf(Set<Item> set) {
        return Container.super.hasAnyOf(set);
    }

    @Override
    public boolean hasAnyMatching(Predicate<ItemStack> predicate) {
        return Container.super.hasAnyMatching(predicate);
    }

    @Override
    public void clearContent() {

    }
}

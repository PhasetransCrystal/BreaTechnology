package net.phasetranscrystal.breatechnology.integration.xei.handlers.item;


import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.phasetranscrystal.breatechnology.integration.xei.entry.item.ItemStackList;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CycleItemStackHandler implements IItemHandlerModifiable {

    private final List<ItemStackList> stacks;

    public CycleItemStackHandler(List<List<ItemStack>> stacks) {
        this.stacks = new ArrayList<>();
        for (var list : stacks) {
            this.stacks.add(ItemStackList.of(list));
        }
    }

    @Override
    public int getSlots() {
        return stacks.size();
    }

    @NotNull
    @Override
    public ItemStack getStackInSlot(int i) {
        List<ItemStack> stackList = stacks.get(i).getStacks();
        return stackList == null || stackList.isEmpty() ? ItemStack.EMPTY :
                stackList.get(Math.abs((int) (System.currentTimeMillis() / 1000) % stackList.size()));
    }

    @Override
    public void setStackInSlot(int index, @NotNull ItemStack stack) {
        if (index >= 0 && index < stacks.size()) {
            stacks.set(index, ItemStackList.of(stack));
        }
    }

    public ItemStackList getStackList(int i) {
        return stacks.get(i);
    }

    @NotNull
    @Override
    public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        return stack;
    }

    @NotNull
    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int i) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return true;
    }
}
package com.tterrag.registrate.util.entry;


import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.inventory.MenuType;
import com.tterrag.registrate.AbstractRegistrate;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Consumer;

public class MenuEntry<T extends AbstractContainerMenu> extends RegistryEntry<MenuType<T>> {
    public MenuEntry(AbstractRegistrate<?> owner, DeferredRegister<MenuType<T>> delegate) {
        super(owner, delegate);
    }

    public T create(int windowId, Inventory playerInv) {
        return (T)((MenuType)this.get()).create(windowId, playerInv);
    }

    public MenuConstructor asProvider() {
        return (window, playerinv, $) -> this.create(window, playerinv);
    }

    public void open(ServerPlayer player, Component displayName) {
        this.open(player, displayName, this.asProvider());
    }

    public void open(ServerPlayer player, Component displayName, Consumer<FriendlyByteBuf> extraData) {
        this.open(player, displayName, this.asProvider(), extraData);
    }

    public void open(ServerPlayer player, Component displayName, MenuConstructor provider) {

        NetworkHooks.openScreen(player, new SimpleMenuProvider(provider, displayName));
    }

    public void open(ServerPlayer player, Component displayName, MenuConstructor provider, Consumer<FriendlyByteBuf> extraData) {
        NetworkHooks.openScreen(player, new SimpleMenuProvider(provider, displayName), extraData);
    }
}

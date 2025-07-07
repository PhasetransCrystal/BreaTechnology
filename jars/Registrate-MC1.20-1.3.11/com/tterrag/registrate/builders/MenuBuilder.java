package com.tterrag.registrate.builders;


import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.api.distmarker.Dist;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.MenuEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;

public class MenuBuilder<T extends AbstractContainerMenu, S extends Screen & MenuAccess<T>, P> extends AbstractBuilder<MenuType<?>, MenuType<T>, P, MenuBuilder<T, S, P>> {
    private final ForgeMenuFactory<T> factory;
    private final NonNullSupplier<ScreenFactory<T, S>> screenFactory;

    public MenuBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, MenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
        this(owner, parent, name, callback, (ForgeMenuFactory)((type, windowId, inv, $) -> factory.create(type, windowId, inv)), screenFactory);
    }

    public MenuBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ForgeMenuFactory<T> factory, NonNullSupplier<ScreenFactory<T, S>> screenFactory) {
        super(owner, parent, name, callback, Keys.MENU_TYPES);
        this.factory = factory;
        this.screenFactory = screenFactory;
    }

    protected @NonnullType MenuType<T> createEntry() {
        ForgeMenuFactory<T> factory = this.factory;
        NonNullSupplier<MenuType<T>> supplier = this.asSupplier();
        MenuType<T> ret = IForgeMenuType.create((windowId, inv, buf) -> factory.create((MenuType)supplier.get(), windowId, inv, buf));
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> {
            ScreenFactory<T, S> screenFactory = (ScreenFactory)this.screenFactory.get();
            MenuScreens.register(ret, (type, inv, displayName) -> screenFactory.create(type, inv, displayName));
        });
        return ret;
    }

    protected RegistryEntry<MenuType<T>> createEntryWrapper(DeferredRegister<MenuType<T>> delegate) {
        return new MenuEntry(this.getOwner(), delegate);
    }

    public MenuEntry<T> register() {
        return (MenuEntry)super.register();
    }

    public interface ForgeMenuFactory<T extends AbstractContainerMenu> {
        T create(MenuType<T> var1, int var2, Inventory var3, @Nullable FriendlyByteBuf var4);
    }

    public interface MenuFactory<T extends AbstractContainerMenu> {
        T create(MenuType<T> var1, int var2, Inventory var3);
    }

    public interface ScreenFactory<M extends AbstractContainerMenu, T extends Screen & MenuAccess<M>> {
        T create(M var1, Inventory var2, Component var3);
    }
}

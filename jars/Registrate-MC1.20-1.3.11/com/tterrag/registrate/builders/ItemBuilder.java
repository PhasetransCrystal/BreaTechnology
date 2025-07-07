package com.tterrag.registrate.builders;


import com.google.common.collect.Maps;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateItemModelProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.CreativeModeTabModifier;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.ItemEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemBuilder<T extends Item, P> extends AbstractBuilder<Item, T, P, ItemBuilder<T, P>> {
    private final NonNullFunction<Item.Properties, T> factory;
    private NonNullSupplier<Item.Properties> initialProperties = Item.Properties::new;
    private NonNullFunction<Item.Properties, Item.Properties> propertiesCallback = NonNullUnaryOperator.identity();
    @Nullable
    private NonNullSupplier<Supplier<ItemColor>> colorHandler;
    private Map<ResourceKey<CreativeModeTab>, NonNullBiConsumer<DataGenContext<Item, T>, CreativeModeTabModifier>> creativeModeTabs = Maps.newLinkedHashMap();

    public static <T extends Item, P> ItemBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Item.Properties, T> factory) {
        return (new ItemBuilder<T, P>(owner, parent, name, callback, factory)).defaultModel().defaultLang();
    }

    protected ItemBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<Item.Properties, T> factory) {
        super(owner, parent, name, callback, Keys.ITEMS);
        this.factory = factory;
        this.onRegister((item) -> {
            this.creativeModeTabs.forEach((creativeModeTab, consumer) -> owner.modifyCreativeModeTab(creativeModeTab, (modifier) -> consumer.accept(DataGenContext.from(this), modifier)));
            this.creativeModeTabs.clear();
        });
    }

    public ItemBuilder<T, P> properties(NonNullUnaryOperator<Item.Properties> func) {
        this.propertiesCallback = this.propertiesCallback.andThen(func);
        return this;
    }

    public ItemBuilder<T, P> initialProperties(NonNullSupplier<Item.Properties> properties) {
        this.initialProperties = properties;
        return this;
    }

    /** @deprecated */
    @Deprecated
    public ItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab, Consumer<CreativeModeTabModifier> modifier) {
        return this.tab(tab, (NonNullBiConsumer)(($, m) -> modifier.accept(m)));
    }

    public ItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab, NonNullBiConsumer<DataGenContext<Item, T>, CreativeModeTabModifier> modifier) {
        this.creativeModeTabs.put(tab, modifier);
        return this;
    }

    public ItemBuilder<T, P> tab(ResourceKey<CreativeModeTab> tab) {
        return this.tab(tab, (NonNullBiConsumer)((item, modifier) -> modifier.accept(item)));
    }

    public ItemBuilder<T, P> removeTab(ResourceKey<CreativeModeTab> tab) {
        this.creativeModeTabs.remove(tab);
        return this;
    }

    public ItemBuilder<T, P> color(NonNullSupplier<Supplier<ItemColor>> colorHandler) {
        if (this.colorHandler == null) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> this::registerItemColor);
        }

        this.colorHandler = colorHandler;
        return this;
    }

    protected void registerItemColor() {
        OneTimeEventReceiver.addModListener(this.getOwner(), RegisterColorHandlersEvent.Item.class, (e) -> {
            NonNullSupplier<Supplier<ItemColor>> colorHandler = this.colorHandler;
            if (colorHandler != null) {
                e.register((ItemColor)((Supplier)colorHandler.get()).get(), new ItemLike[]{(ItemLike)this.getEntry()});
            }

        });
    }

    public ItemBuilder<T, P> defaultModel() {
        return this.model((ctx, prov) -> {
            Objects.requireNonNull(ctx);
            prov.generated(ctx::getEntry);
        });
    }

    public ItemBuilder<T, P> model(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateItemModelProvider> cons) {
        return (ItemBuilder)this.setData(ProviderType.ITEM_MODEL, cons);
    }

    public ItemBuilder<T, P> defaultLang() {
        return (ItemBuilder)this.lang(Item::getDescriptionId);
    }

    public ItemBuilder<T, P> lang(String name) {
        return (ItemBuilder)this.lang(Item::getDescriptionId, name);
    }

    public ItemBuilder<T, P> recipe(NonNullBiConsumer<DataGenContext<Item, T>, RegistrateRecipeProvider> cons) {
        return (ItemBuilder)this.setData(ProviderType.RECIPE, cons);
    }

    @SafeVarargs
    public final ItemBuilder<T, P> tag(TagKey<Item>... tags) {
        return (ItemBuilder)this.tag(ProviderType.ITEM_TAGS, tags);
    }

    protected T createEntry() {
        Item.Properties properties = (Item.Properties)this.initialProperties.get();
        properties = (Item.Properties)this.propertiesCallback.apply(properties);
        return (T)(this.factory.apply(properties));
    }

    protected RegistryEntry<T> createEntryWrapper(DeferredRegister<T> delegate) {
        return new ItemEntry(this.getOwner(), delegate);
    }

    public RegistryEntry<T> register() {
        return (ItemEntry)super.register();
    }
}

package com.tterrag.registrate.builders;


import com.google.common.base.Preconditions;
import com.google.gson.JsonElement;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.providers.loot.RegistrateBlockLootTables;
import com.tterrag.registrate.providers.loot.RegistrateLootTableProvider;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.*;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class BlockBuilder<T extends Block, P> extends AbstractBuilder<Block, T, P, BlockBuilder<T, P>> {
    private final NonNullFunction<BlockBehaviour.Properties, T> factory;
    private NonNullSupplier<BlockBehaviour.Properties> initialProperties;
    private NonNullFunction<BlockBehaviour.Properties, BlockBehaviour.Properties> propertiesCallback = NonNullUnaryOperator.identity();
    private List<Supplier<Supplier<RenderType>>> renderLayers = new ArrayList(1);
    @Nullable
    private NonNullSupplier<Supplier<BlockColor>> colorHandler;

    public static <T extends Block, P> BlockBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<BlockBehaviour.Properties, T> factory) {
        return (new BlockBuilder<T, P>(owner, parent, name, callback, factory, () -> BlockBehaviour.Properties.of())).defaultBlockstate().defaultLoot().defaultLang();
    }

    protected BlockBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, NonNullFunction<BlockBehaviour.Properties, T> factory, NonNullSupplier<BlockBehaviour.Properties> initialProperties) {
        super(owner, parent, name, callback, Keys.BLOCKS);
        this.factory = factory;
        this.initialProperties = initialProperties;
    }

    public BlockBuilder<T, P> properties(NonNullUnaryOperator<BlockBehaviour.Properties> func) {
        this.propertiesCallback = this.propertiesCallback.andThen(func);
        return this;
    }

    public BlockBuilder<T, P> initialProperties(NonNullSupplier<? extends Block> block) {
        this.initialProperties = () -> BlockBehaviour.Properties.copy((BlockBehaviour)block.get());
        return this;
    }

    /** @deprecated */
    @Deprecated(
            forRemoval = true
    )
    public BlockBuilder<T, P> addLayer(Supplier<Supplier<RenderType>> layer) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(((Supplier)layer.get()).get()), "Invalid block layer: " + layer));
        if (this.renderLayers.isEmpty()) {
            this.onRegister(this::registerLayers);
        }

        this.renderLayers.add(layer);
        return this;
    }

    protected void registerLayers(T entry) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> OneTimeEventReceiver.addModListener(this.getOwner(), FMLClientSetupEvent.class, ($) -> {
            if (this.renderLayers.size() == 1) {
                RenderType layer = (RenderType)((Supplier)((Supplier)this.renderLayers.get(0)).get()).get();
                ItemBlockRenderTypes.setRenderLayer(entry, layer);
            } else if (this.renderLayers.size() > 1) {
                Set<RenderType> layers = (Set)this.renderLayers.stream().map((s) -> (RenderType)((Supplier)s.get()).get()).collect(Collectors.toSet());
                Objects.requireNonNull(layers);
                ItemBlockRenderTypes.setRenderLayer(entry, layers::contains);
            }

        }));
    }

    public BlockBuilder<T, P> simpleItem() {
        return (BlockBuilder)this.item().build();
    }

    public ItemBuilder<BlockItem, BlockBuilder<T, P>> item() {
        return this.item(BlockItem::new);
    }

    public <I extends Item> ItemBuilder<I, BlockBuilder<T, P>> item(NonNullBiFunction<? super T, Item.Properties, ? extends I> factory) {
        NonNullSupplier<T> sup = this.asSupplier();
        return ((ItemBuilder)this.getOwner().item(this, this.getName(), (p) -> (Item)factory.apply(this.getEntry(), p)).setData(ProviderType.LANG, NonNullBiConsumer.noop())).model((ctx, prov) -> {
            Optional<String> model = this.getOwner().getDataProvider(ProviderType.BLOCKSTATE).flatMap((p) -> p.getExistingVariantBuilder((Block)this.getEntry())).map((b) -> (BlockStateProvider.ConfiguredModelList)b.getModels().get(b.partialState())).map(BlockStateProvider.ConfiguredModelList::toJSON).filter(JsonElement::isJsonObject).map((j) -> j.getAsJsonObject().get("model")).map(JsonElement::getAsString);
            if (model.isPresent()) {
                prov.withExistingParent(ctx.getName(), (String)model.get());
            } else {
                prov.blockItem(sup);
            }

        });
    }

    public <BE extends BlockEntity> BlockBuilder<T, P> simpleBlockEntity(BlockEntityBuilder.BlockEntityFactory<BE> factory) {
        return (BlockBuilder)this.blockEntity(factory).build();
    }

    public <BE extends BlockEntity> BlockEntityBuilder<BE, BlockBuilder<T, P>> blockEntity(BlockEntityBuilder.BlockEntityFactory<BE> factory) {
        return this.getOwner().blockEntity(this, this.getName(), factory).validBlock(this.asSupplier());
    }

    public BlockBuilder<T, P> color(NonNullSupplier<Supplier<BlockColor>> colorHandler) {
        if (this.colorHandler == null) {
            DistExecutor.runWhenOn(Dist.CLIENT, () -> this::registerBlockColor);
        }

        this.colorHandler = colorHandler;
        return this;
    }

    protected void registerBlockColor() {
        OneTimeEventReceiver.addModListener(this.getOwner(), RegisterColorHandlersEvent.Block.class, (e) -> {
            NonNullSupplier<Supplier<BlockColor>> colorHandler = this.colorHandler;
            if (colorHandler != null) {
                e.register((BlockColor)((Supplier)colorHandler.get()).get(), new Block[]{(Block)this.getEntry()});
            }

        });
    }

    public BlockBuilder<T, P> defaultBlockstate() {
        return this.blockstate((ctx, prov) -> prov.simpleBlock((Block)ctx.getEntry()));
    }

    public BlockBuilder<T, P> blockstate(NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> cons) {
        return (BlockBuilder)this.setData(ProviderType.BLOCKSTATE, cons);
    }

    public BlockBuilder<T, P> defaultLang() {
        return (BlockBuilder)this.lang(Block::getDescriptionId);
    }

    public BlockBuilder<T, P> lang(String name) {
        return (BlockBuilder)this.lang(Block::getDescriptionId, name);
    }

    public BlockBuilder<T, P> defaultLoot() {
        return this.loot(RegistrateBlockLootTables::dropSelf);
    }

    public BlockBuilder<T, P> loot(NonNullBiConsumer<RegistrateBlockLootTables, T> cons) {
        return (BlockBuilder)this.setData(ProviderType.LOOT, (ctx, prov) -> prov.addLootAction(RegistrateLootTableProvider.LootType.BLOCK, (tb) -> {
            if (!((Block)ctx.getEntry()).getLootTable().equals(BuiltInLootTables.EMPTY)) {
                cons.accept(tb, (Block)ctx.getEntry());
            }

        }));
    }

    public BlockBuilder<T, P> recipe(NonNullBiConsumer<DataGenContext<Block, T>, RegistrateRecipeProvider> cons) {
        return (BlockBuilder)this.setData(ProviderType.RECIPE, cons);
    }

    @SafeVarargs
    public final BlockBuilder<T, P> tag(TagKey<Block>... tags) {
        return (BlockBuilder)this.tag(ProviderType.BLOCK_TAGS, tags);
    }

    protected T createEntry() {
        BlockBehaviour.Properties properties = (BlockBehaviour.Properties)this.initialProperties.get();
        properties = (BlockBehaviour.Properties)this.propertiesCallback.apply(properties);
        return (T)(this.factory.apply(properties));
    }

    protected RegistryEntry<T> createEntryWrapper(DeferredRegister<T> delegate) {
        return new BlockEntry(this.getOwner(), delegate);
    }

    public BlockEntry<T> register() {
        return (BlockEntry)super.register();
    }
}


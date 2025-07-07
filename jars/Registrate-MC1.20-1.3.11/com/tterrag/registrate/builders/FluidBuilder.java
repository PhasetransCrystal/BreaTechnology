package com.tterrag.registrate.builders;


import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import net.minecraft.Util;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.FluidEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullBiFunction;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

public class FluidBuilder<T extends ForgeFlowingFluid, P> extends AbstractBuilder<Fluid, T, P, FluidBuilder<T, P>> {
    private final String sourceName;
    private final String bucketName;
    private final ResourceLocation stillTexture;
    private final ResourceLocation flowingTexture;
    private final NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory;
    @Nullable
    private final NonNullSupplier<FluidType> fluidType;
    @Nullable
    private Boolean defaultSource;
    @Nullable
    private Boolean defaultBlock;
    @Nullable
    private Boolean defaultBucket;
    private NonNullConsumer<FluidType.Properties> typeProperties = ($) -> {
    };
    private NonNullConsumer<ForgeFlowingFluid.Properties> fluidProperties;
    @Nullable
    private Supplier<RenderType> layer = null;
    private boolean registerType;
    @Nullable
    private NonNullSupplier<? extends ForgeFlowingFluid> source;
    private final List<TagKey<Fluid>> tags = new ArrayList();

    public static <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, FluidBuilder::defaultFluidType, ForgeFlowingFluid.Flowing::new);
    }

    public static <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, ForgeFlowingFluid.Flowing::new);
    }

    public static <P> FluidBuilder<ForgeFlowingFluid.Flowing, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, ForgeFlowingFluid.Flowing::new);
    }

    public static <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return create(owner, parent, name, callback, stillTexture, flowingTexture, FluidBuilder::defaultFluidType, fluidFactory);
    }

    public static <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        FluidBuilder<T, P> ret = (new FluidBuilder<T, P>(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory)).defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    public static <T extends ForgeFlowingFluid, P> FluidBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        FluidBuilder<T, P> ret = (new FluidBuilder<T, P>(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, fluidFactory)).defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactory typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, Keys.FLUIDS);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidFactory = fluidFactory;
        this.fluidType = NonNullSupplier.lazy(() -> typeFactory.create(this.makeTypeProperties(), this.stillTexture, this.flowingTexture));
        this.registerType = true;
        String bucketName = this.bucketName;
        this.fluidProperties = (p) -> p.bucket(() -> (Item)owner.get(bucketName, Keys.ITEMS).get()).block(() -> (LiquidBlock)owner.get(name, Keys.BLOCKS).get());
    }

    public FluidBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, Keys.FLUIDS);
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidFactory = fluidFactory;
        this.fluidType = fluidType;
        this.registerType = false;
        String bucketName = this.bucketName;
        this.fluidProperties = (p) -> p.bucket(() -> (Item)owner.get(bucketName, Keys.ITEMS).get()).block(() -> (LiquidBlock)owner.get(name, Keys.BLOCKS).get());
    }

    public FluidBuilder<T, P> properties(NonNullConsumer<FluidType.Properties> cons) {
        this.typeProperties = this.typeProperties.andThen(cons);
        return this;
    }

    public FluidBuilder<T, P> fluidProperties(NonNullConsumer<ForgeFlowingFluid.Properties> cons) {
        this.fluidProperties = this.fluidProperties.andThen(cons);
        return this;
    }

    public FluidBuilder<T, P> defaultLang() {
        return (FluidBuilder)this.lang((f) -> f.getFluidType().getDescriptionId(), RegistrateLangProvider.toEnglishName(this.sourceName));
    }

    public FluidBuilder<T, P> lang(String name) {
        return (FluidBuilder)this.lang((f) -> f.getFluidType().getDescriptionId(), name);
    }

    public FluidBuilder<T, P> renderType(Supplier<RenderType> layer) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> Preconditions.checkArgument(RenderType.chunkBufferLayers().contains(layer.get()), "Invalid render type: " + layer));
        if (this.layer == null) {
            this.onRegister(this::registerRenderType);
        }

        this.layer = layer;
        return this;
    }

    protected void registerRenderType(T entry) {
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> OneTimeEventReceiver.addModListener(this.getOwner(), FMLClientSetupEvent.class, ($) -> {
            if (this.layer != null) {
                RenderType layer = (RenderType)this.layer.get();
                ItemBlockRenderTypes.setRenderLayer(entry, layer);
                ItemBlockRenderTypes.setRenderLayer(this.getSource(), layer);
            }

        }));
    }

    public FluidBuilder<T, P> defaultSource() {
        if (this.defaultSource != null) {
            throw new IllegalStateException("Cannot set a default source after a custom source has been created");
        } else {
            this.defaultSource = true;
            return this;
        }
    }

    public FluidBuilder<T, P> source(NonNullFunction<ForgeFlowingFluid.Properties, ? extends ForgeFlowingFluid> factory) {
        this.defaultSource = false;
        this.source = NonNullSupplier.lazy(() -> (ForgeFlowingFluid)factory.apply(this.makeProperties()));
        return this;
    }

    public FluidBuilder<T, P> defaultBlock() {
        if (this.defaultBlock != null) {
            throw new IllegalStateException("Cannot set a default block after a custom block has been created");
        } else {
            this.defaultBlock = true;
            return this;
        }
    }

    public BlockBuilder<LiquidBlock, FluidBuilder<T, P>> block() {
        return this.block(LiquidBlock::new);
    }

    public <B extends LiquidBlock> BlockBuilder<B, FluidBuilder<T, P>> block(NonNullBiFunction<NonNullSupplier<? extends T>, BlockBehaviour.Properties, ? extends B> factory) {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        } else {
            this.defaultBlock = false;
            NonNullSupplier<T> supplier = this.asSupplier();
            Lazy<Integer> lightLevel = Lazy.of(() -> ((FluidType)this.fluidType.get()).getLightLevel());
            ToIntFunction<BlockState> lightLevelInt = ($) -> (Integer)lightLevel.get();
            return this.getOwner().block(this, this.sourceName, (p) -> (LiquidBlock)factory.apply(supplier, p)).properties((p) -> BlockBehaviour.Properties.copy(Blocks.WATER).noLootTable()).properties((p) -> p.lightLevel(lightLevelInt)).blockstate((ctx, prov) -> prov.simpleBlock((Block)ctx.getEntry(), ((BlockModelBuilder)prov.models().getBuilder(this.sourceName)).texture("particle", this.stillTexture)));
        }
    }

    @Beta
    public FluidBuilder<T, P> noBlock() {
        if (this.defaultBlock == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to block/noBlock per builder allowed");
        } else {
            this.defaultBlock = false;
            return this;
        }
    }

    public FluidBuilder<T, P> defaultBucket() {
        if (this.defaultBucket != null) {
            throw new IllegalStateException("Cannot set a default bucket after a custom bucket has been created");
        } else {
            this.defaultBucket = true;
            return this;
        }
    }

    public ItemBuilder<BucketItem, FluidBuilder<T, P>> bucket() {
        return this.bucket(BucketItem::new);
    }

    public <I extends BucketItem> ItemBuilder<I, FluidBuilder<T, P>> bucket(NonNullBiFunction<Supplier<? extends ForgeFlowingFluid>, Item.Properties, ? extends I> factory) {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        } else {
            this.defaultBucket = false;
            NonNullSupplier<? extends ForgeFlowingFluid> source = this.source;
            if (source == null) {
                throw new IllegalStateException("Cannot create a bucket before creating a source block");
            } else {
                return this.getOwner().item(this, this.bucketName, (p) -> {
                    Objects.requireNonNull(source);
                    return (BucketItem)factory.apply(source::get, p);
                }).properties((p) -> p.craftRemainder(Items.BUCKET).stacksTo(1)).model((ctx, prov) -> {
                    Objects.requireNonNull(ctx);
                    prov.generated(ctx::getEntry, new ResourceLocation[]{new ResourceLocation(this.getOwner().getModid(), "item/" + this.bucketName)});
                });
            }
        }
    }

    @Beta
    public FluidBuilder<T, P> noBucket() {
        if (this.defaultBucket == Boolean.FALSE) {
            throw new IllegalStateException("Only one call to bucket/noBucket per builder allowed");
        } else {
            this.defaultBucket = false;
            return this;
        }
    }

    @SafeVarargs
    public final FluidBuilder<T, P> tag(TagKey<Fluid>... tags) {
        FluidBuilder<T, P> ret = (FluidBuilder)this.tag(ProviderType.FLUID_TAGS, tags);
        if (this.tags.isEmpty()) {
            ret.getOwner().setDataGenerator(ret.sourceName, this.getRegistryKey(), ProviderType.FLUID_TAGS, (prov) -> {
                Stream var10000 = this.tags.stream();
                Objects.requireNonNull(prov);
                var10000.map(prov::addTag).forEach((p) -> p.add(this.getSource().builtInRegistryHolder().key()));
            });
        }

        this.tags.addAll(Arrays.asList(tags));
        return ret;
    }

    @SafeVarargs
    public final FluidBuilder<T, P> removeTag(TagKey<Fluid>... tags) {
        this.tags.removeAll(Arrays.asList(tags));
        return (FluidBuilder)this.removeTag(ProviderType.FLUID_TAGS, tags);
    }

    private ForgeFlowingFluid getSource() {
        NonNullSupplier<? extends ForgeFlowingFluid> source = this.source;
        Preconditions.checkNotNull(source, "Fluid has no source block: " + this.sourceName);
        return (ForgeFlowingFluid)source.get();
    }

    private ForgeFlowingFluid.Properties makeProperties() {
        NonNullSupplier<? extends ForgeFlowingFluid> source = this.source;
        ForgeFlowingFluid.Properties var10000 = new ForgeFlowingFluid.Properties;
        NonNullSupplier<FluidType> var10002 = this.fluidType;
        Supplier var10003;
        if (source == null) {
            var10003 = null;
        } else {
            Objects.requireNonNull(source);
            var10003 = source::get;
        }

        var10000.<init>(var10002, var10003, this.asSupplier());
        ForgeFlowingFluid.Properties ret = var10000;
        this.fluidProperties.accept(ret);
        return ret;
    }

    private FluidType.Properties makeTypeProperties() {
        FluidType.Properties properties = net.minecraftforge.fluids.FluidType.Properties.create();
        RegistryEntry<Block> block = this.getOwner().getOptional(this.sourceName, Keys.BLOCKS);
        this.typeProperties.accept(properties);
        if (block.isPresent()) {
            properties.descriptionId(((Block)block.get()).getDescriptionId());
            this.setData(ProviderType.LANG, NonNullBiConsumer.noop());
        } else {
            properties.descriptionId(Util.makeDescriptionId("fluid", new ResourceLocation(this.getOwner().getModid(), this.sourceName)));
        }

        return properties;
    }

    protected T createEntry() {
        return (T)(this.fluidFactory.apply(this.makeProperties()));
    }

    public FluidEntry<T> register() {
        if (this.fluidType != null) {
            if (this.registerType) {
                this.getOwner().simple(this, this.sourceName, NeoForgeRegistries.Keys.FLUID_TYPES, this.fluidType);
            }

            if (this.defaultSource == Boolean.TRUE) {
                this.source(ForgeFlowingFluid.Source::new);
            }

            if (this.defaultBlock == Boolean.TRUE) {
                this.block().register();
            }

            if (this.defaultBucket == Boolean.TRUE) {
                this.bucket().register();
            }

            NonNullSupplier<? extends ForgeFlowingFluid> source = this.source;
            if (source != null) {
                BuilderCallback var10000 = this.getCallback();
                String var10001 = this.sourceName;
                ResourceKey var10002 = Keys.FLUIDS;
                Objects.requireNonNull(source);
                var10000.accept(var10001, var10002, this, source::get);
                return (FluidEntry)super.register();
            } else {
                throw new IllegalStateException("Fluid must have a source version: " + this.getName());
            }
        } else {
            throw new IllegalStateException("Fluid must have a type: " + this.getName());
        }
    }

    protected RegistryEntry<T> createEntryWrapper(DeferredRegister<T> delegate) {
        return new FluidEntry(this.getOwner(), delegate);
    }

    private static FluidType defaultFluidType(FluidType.Properties properties, final ResourceLocation stillTexture, final ResourceLocation flowingTexture) {
        return new FluidType(properties) {
            public void initializeClient(Consumer<net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
                consumer.accept(new IClientFluidTypeExtensions() {
                    public ResourceLocation getStillTexture() {
                        return stillTexture;
                    }

                    public ResourceLocation getFlowingTexture() {
                        return flowingTexture;
                    }
                });
            }
        };
    }

    @FunctionalInterface
    public interface FluidTypeFactory {
        FluidType create(FluidType.Properties var1, ResourceLocation var2, ResourceLocation var3);
    }
}

package com.tterrag.registrate.builders;


import com.mojang.datafixers.types.Type;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.neoforged.neoforge.registries.DeferredRegister;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class BlockEntityBuilder<T extends BlockEntity, P> extends AbstractBuilder<BlockEntityType<?>, BlockEntityType<T>, P, BlockEntityBuilder<T, P>> {
    private final BlockEntityFactory<T> factory;
    private final Set<NonNullSupplier<? extends Block>> validBlocks = new HashSet();
    @Nullable
    private NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> renderer;

    public static <T extends BlockEntity, P> BlockEntityBuilder<T, P> create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
        return new BlockEntityBuilder<T, P>(owner, parent, name, callback, factory);
    }

    protected BlockEntityBuilder(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, BlockEntityFactory<T> factory) {
        super(owner, parent, name, callback, Keys.BLOCK_ENTITY_TYPES);
        this.factory = factory;
    }

    public BlockEntityBuilder<T, P> validBlock(NonNullSupplier<? extends Block> block) {
        this.validBlocks.add(block);
        return this;
    }

    @SafeVarargs
    public final BlockEntityBuilder<T, P> validBlocks(NonNullSupplier<? extends Block>... blocks) {
        Arrays.stream(blocks).forEach(this::validBlock);
        return this;
    }

    public BlockEntityBuilder<T, P> renderer(NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> renderer) {
        if (this.renderer == null) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> this::registerRenderer);
        }

        this.renderer = renderer;
        return this;
    }

    protected void registerRenderer() {
        OneTimeEventReceiver.addModListener(this.getOwner(), FMLClientSetupEvent.class, ($) -> {
            NonNullSupplier<NonNullFunction<BlockEntityRendererProvider.Context, BlockEntityRenderer<? super T>>> renderer = this.renderer;
            if (renderer != null) {
                BlockEntityType var10000 = (BlockEntityType)this.getEntry();
                NonNullFunction var10001 = (NonNullFunction)renderer.get();
                Objects.requireNonNull(var10001);
                BlockEntityRenderers.register(var10000, var10001::apply);
            }

        });
    }

    protected BlockEntityType<T> createEntry() {
        BlockEntityFactory<T> factory = this.factory;
        NonNullSupplier<BlockEntityType<T>> supplier = this.asSupplier();
        return BlockEntityType.Builder.of((pos, state) -> factory.create((BlockEntityType)supplier.get(), pos, state), (Block[])this.validBlocks.stream().map(NonNullSupplier::get).toArray((x$0) -> new Block[x$0])).build((Type)null);
    }

    protected RegistryEntry<BlockEntityType<T>> createEntryWrapper(DeferredRegister<BlockEntityType<T>> delegate) {
        return new BlockEntityEntry(this.getOwner(), delegate);
    }

    public BlockEntityEntry<T> register() {
        return (BlockEntityEntry)super.register();
    }

    public interface BlockEntityFactory<T extends BlockEntity> {
        T create(BlockEntityType<T> var1, BlockPos var2, BlockState var3);
    }
}

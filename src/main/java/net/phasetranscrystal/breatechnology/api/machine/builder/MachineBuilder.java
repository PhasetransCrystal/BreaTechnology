package net.phasetranscrystal.breatechnology.api.machine.builder;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.BlockBuilder;
import com.tterrag.registrate.builders.ItemBuilder;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.BlockEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullUnaryOperator;
import lombok.Setter;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.phasetranscrystal.breatechnology.api.blockentity.MetaMachineBlockEntity;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;
import net.phasetranscrystal.breatechnology.api.item.MetaMachineItem;
import net.phasetranscrystal.breatechnology.api.machine.IMachineBlock;
import net.phasetranscrystal.breatechnology.api.machine.IMachineBlockEntity;
import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;
import net.phasetranscrystal.breatechnology.api.machine.RotationState;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class MachineBuilder<DEFINITION extends MetaMachineDefinition<?>> {
    protected final AbstractRegistrate<?> owner;
    protected final String name;
    protected BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory;
    protected final Function<IMachineBlockEntity, MetaMachine> machineFactory;
    protected final BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory;
    protected final BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory;
    protected final TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory;

    public MachineBuilder(AbstractRegistrate<?> owner, String name,
                          BiFunction<AbstractRegistrate<?>, ResourceLocation, DEFINITION> definitionFactory,
                          Function<IMachineBlockEntity, MetaMachine> machineFactory,
                          BiFunction<BlockBehaviour.Properties, DEFINITION, IMachineBlock> blockFactory,
                          BiFunction<IMachineBlock, Item.Properties, MetaMachineItem> itemFactory,
                          TriFunction<BlockEntityType<?>, BlockPos, BlockState, IMachineBlockEntity> blockEntityFactory) {
        this.owner = owner;
        this.name = name;
        this.definitionFactory = definitionFactory;
        this.machineFactory = machineFactory;
        this.blockFactory = blockFactory;
        this.itemFactory = itemFactory;
        this.blockEntityFactory = blockEntityFactory;
    }

    @Setter
    private RotationState rotationState = RotationState.ALL;

    protected DEFINITION createDefinition() {
        return definitionFactory.apply(owner, ResourceLocation.fromNamespaceAndPath(owner.getModid(), name));
    }

    @Setter
    private NonNullUnaryOperator<BlockBehaviour.Properties> blockProp = p -> p;
    @Setter
    private NonNullUnaryOperator<Item.Properties> itemProp = p -> p;
    @Setter
    @Nullable
    private Consumer<BlockBuilder<? extends Block, ?>> blockBuilder;
    @Setter
    @Nullable
    private Consumer<ItemBuilder<? extends MetaMachineItem, ?>> itemBuilder;
    @Setter
    private NonNullConsumer<BlockEntityType<BlockEntity>> onBlockEntityRegister = MetaMachineBlockEntity::onBlockEntityRegister;

    public @NotNull DEFINITION register() {
        var definition = createDefinition();

        var blockBuilder = BlockBuilderWrapper.makeBlockBuilder(this, definition);
        if (this.blockBuilder != null) {
            this.blockBuilder.accept(blockBuilder);
        }
        var block = blockBuilder.register();

        var itemBuilder = ItemBuilderWrapper.makeItemBuilder(this, block);
        if (this.itemBuilder != null) {
            this.itemBuilder.accept(itemBuilder);
        }
        var item = itemBuilder.register();

        var blockEntityBuilder = owner.blockEntity(name, (type, pos, state) -> blockEntityFactory.apply(type, pos, state).self())
                .onRegister(onBlockEntityRegister)
                .validBlock(block);

        var blockEntity = blockEntityBuilder.register();
        definition.setRecipeTypes(null);
        definition.setBlockSupplier(block);
        definition.setItemSupplier(item);
        definition.setBlockEntityTypeSupplier(blockEntity::get);
        definition.setMachineSupplier(machineFactory);

        return definition;
    }

    static class BlockBuilderWrapper {

        @SuppressWarnings("removal")
        public static <D extends MetaMachineDefinition<?>> BlockBuilder<Block, MachineBuilder<D>>
        makeBlockBuilder(MachineBuilder<D> builder, D definition) {
            return builder.owner.block(builder, builder.name, prop -> {
                        RotationState.setPreState(builder.rotationState);
                        MetaMachineDefinition.setBuilt(definition);
                        var b = builder.blockFactory.apply(prop, definition);
                        RotationState.clearPreState();
                        MetaMachineDefinition.clearBuilt();
                        return b.self();
                    })
                    .initialProperties(() -> Blocks.DISPENSER)
                    .properties(BlockBehaviour.Properties::noLootTable)
                    .addLayer(() -> RenderType::cutoutMipped)
                    .blockstate(NonNullBiConsumer.noop())
                    .properties(builder.blockProp);

        }
    }

    static class ItemBuilderWrapper {

        public static <D extends MetaMachineDefinition<?>> ItemBuilder<MetaMachineItem, MachineBuilder<D>>
        makeItemBuilder(MachineBuilder<D> builder, BlockEntry<Block> block) {
            return builder.owner.item(builder, builder.name, prop ->
                            builder.itemFactory.apply((IMachineBlock) block.get(), prop))
                    .setData(ProviderType.LANG, NonNullBiConsumer.noop()) // do not gen any lang keys
                    .model(NonNullBiConsumer.noop())
                    .properties(builder.itemProp);
        }
    }
}
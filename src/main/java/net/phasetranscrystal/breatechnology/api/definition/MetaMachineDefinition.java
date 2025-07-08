package net.phasetranscrystal.breatechnology.api.definition;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.entry.BlockEntityEntry;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.phasetranscrystal.breatechnology.api.blockentity.MetaMachineBlockEntity;
import net.phasetranscrystal.breatechnology.api.item.MetaMachineItem;
import net.phasetranscrystal.breatechnology.api.machine.IMachineBlockEntity;
import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;
import net.phasetranscrystal.breatechnology.api.machine.RotationState;
import net.phasetranscrystal.breatechnology.api.recipe.BTRecipeType;
import org.jetbrains.annotations.Nullable;

import java.util.function.*;

/// 基础机器定义信息
public class MetaMachineDefinition<T extends MetaMachineBlockEntity> extends MetaBlockDefinition<T> {
    public MetaMachineDefinition(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }

    @Setter
    private Supplier<? extends Block> blockSupplier;
    @Setter
    private Supplier<? extends MetaMachineItem> itemSupplier;
    @Setter
    private Function<IMachineBlockEntity, MetaMachine> machineSupplier;

    public Block getBlock() {
        return blockSupplier.get();
    }

    public MetaMachineItem getItem() {
        return itemSupplier.get();
    }

    public MetaMachine createMetaMachine(IMachineBlockEntity blockEntity) {
        return machineSupplier.apply(blockEntity);
    }

    public ItemStack asStack() {
        return new ItemStack(getItem());
    }

    public ItemStack asStack(int count) {
        return new ItemStack(getItem(), count);
    }

    public String getDescriptionId() {
        return getBlock().getDescriptionId();
    }

    public BlockState defaultBlockState() {
        return getBlock().defaultBlockState();
    }

    @Getter
    @Setter
    private BTRecipeType @Nullable [] recipeTypes;

    @Getter
    private RotationState rotationState = RotationState.ALL;

    public String getName() {
        return getId().getPath();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        var that = (MetaMachineDefinition<?>) o;

        return getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }


    @Override
    public String toString() {
        return "[Definition: %s]".formatted(getId());
    }

    private static ThreadLocal<MetaMachineDefinition> Built = new ThreadLocal<>();

    public static void setBuilt(MetaMachineDefinition definition) {
        Built.set(definition);
    }

    public static MetaMachineDefinition getBuilt() {
        return Built.get();
    }

    public static void clearBuilt() {
        Built.remove();
    }
}
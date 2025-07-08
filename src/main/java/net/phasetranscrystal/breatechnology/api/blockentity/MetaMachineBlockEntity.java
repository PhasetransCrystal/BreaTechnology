package net.phasetranscrystal.breatechnology.api.blockentity;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import com.lowdragmc.lowdraglib.syncdata.managed.MultiManagedStorage;
import icyllis.modernui.fragment.Fragment;
import icyllis.modernui.mc.neoforge.MenuScreenFactory;
import lombok.Getter;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.phasetranscrystal.breatechnology.api.BTValues;
import net.phasetranscrystal.breatechnology.api.machine.IMachineBlockEntity;
import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/// 机器方块的方块实体类
public class MetaMachineBlockEntity extends MetaBlockEntity implements IMachineBlockEntity {
    public final MultiManagedStorage managedStorage = new MultiManagedStorage();
    @Getter
    public final MetaMachine metaMachine;
    private final long offset = BTValues.RNG.nextInt(20);

    public MetaMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        this.metaMachine = getDefinition().createMetaMachine(this);
    }

    public static MetaMachineBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos,
                                                           BlockState blockState) {
        return new MetaMachineBlockEntity(type, pos, blockState);
    }

    public static void onBlockEntityRegister(BlockEntityType<BlockEntity> type) {
    }

    @Override
    public @NotNull MultiManagedStorage getRootStorage() {
        return managedStorage;
    }

    @Override
    public boolean triggerEvent(int id, int para) {
        if (id == 1) { // chunk re render
            if (level != null && level.isClientSide) {
                scheduleRenderUpdate();
            }
            return true;
        }
        return false;
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput componentInput) {
        super.applyImplicitComponents(componentInput);
        metaMachine.applyImplicitComponents(new ExDataComponentInput() {
            @Override
            public @Nullable <T> T get(DataComponentType<T> component) {
                return componentInput.get(component);
            }

            @Override
            public <T> T getOrDefault(DataComponentType<? extends T> component, T defaultValue) {
                return componentInput.getOrDefault(component, defaultValue);
            }
        });
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        metaMachine.collectImplicitComponents(components);
    }

    @Override
    public long getOffset() {
        return offset;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        metaMachine.onUnload();
    }

    @Override
    public void clearRemoved() {
        super.clearRemoved();
        metaMachine.onLoad();
    }

    @Override
    public void setChanged() {
        if (getLevel() != null) {
            getLevel().blockEntityChanged(getBlockPos());
        }
    }

    /**
     * Extending interface to make {@link BlockEntity.DataComponentInput} public as it's protected by default.
     */
    public interface ExDataComponentInput extends BlockEntity.DataComponentInput {
    }
}
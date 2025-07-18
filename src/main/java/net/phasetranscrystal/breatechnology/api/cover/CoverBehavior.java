package net.phasetranscrystal.breatechnology.api.cover;

import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.phasetranscrystal.breatechnology.api.capability.ICoverable;
import net.phasetranscrystal.breatechnology.api.gui.fancy.IFancyConfigurator;
import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;
import net.phasetranscrystal.breatechnology.api.machine.feature.multiblock.IMultiController;
import net.phasetranscrystal.breatechnology.api.transfer.fluid.IFluidHandlerModifiable;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents cover instance attached on the specific side of meta tile entity
 * Cover filters out interaction and logic of meta tile entity
 */
public abstract class CoverBehavior implements IEnhancedManaged{
    public static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(CoverBehavior.class);

    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    public final CoverDefinition coverDefinition;
    public final ICoverable coverHolder;
    public final Direction attachedSide;
    @Getter
    @Persisted
    @DescSynced
    protected ItemStack attachItem = ItemStack.EMPTY;
    @Getter
    @Persisted
    protected int redstoneSignalOutput = 0;

    public CoverBehavior(CoverDefinition definition, ICoverable coverHolder, Direction attachedSide) {
        this.coverDefinition = definition;
        this.coverHolder = coverHolder;
        this.attachedSide = attachedSide;
    }
    //////////////////////////////////////
    // ***** Initialization ******//
    //////////////////////////////////////
    @Override
    public void scheduleRenderUpdate() {
        coverHolder.scheduleRenderUpdate();
    }

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        var level = coverHolder.getLevel();
        if (level != null && !level.isClientSide && level.getServer() != null) {
            level.getServer().execute(coverHolder::markDirty);
        }
    }
    /**
     * Called on server side to check whether cover can be attached to given cover holder.
     * it will be called before {@link CoverBehavior#onAttached(ItemStack, ServerPlayer)}
     *
     * @return true if cover can be attached, false otherwise
     */
    @MustBeInvokedByOverriders
    public boolean canAttach() {
        var machine = MetaMachine.getMachine(coverHolder.getLevel(), coverHolder.getPos());
        return machine == null || !machine.hasFrontFacing() || coverHolder.getFrontFacing() != attachedSide ||
                machine instanceof IMultiController;
    }

    /**
     * Will be called on server side after the cover attachment to the machine
     * Cover can change it's internal state here and return initial data as nbt.
     *
     * @param itemStack the item cover was attached from
     */
    public void onAttached(ItemStack itemStack, ServerPlayer player) {
        attachItem = itemStack.copy();
        attachItem.setCount(1);
    }

    public void onLoad() {}

    public void onUnload() {}
    //////////////////////////////////////
    // ********** Misc ***********//
    //////////////////////////////////////
    public ItemStack getPickItem() {
        return attachItem;
    }

    /**
     * Append additional drops. It doesn't include itself.
     */
    public List<ItemStack> getAdditionalDrops() {
        return new ArrayList<>();
    }

    /**
     * Called prior to cover removing on the server side
     * Will also be called during machine dismantling, as machine loses installed covers after that
     */
    public void onRemoved() {}

    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {}

    public void setRedstoneSignalOutput(int redstoneSignalOutput) {
        if (this.redstoneSignalOutput == redstoneSignalOutput) return;
        this.redstoneSignalOutput = redstoneSignalOutput;
        coverHolder.notifyBlockUpdate();
        coverHolder.markDirty();
    }
    public boolean canConnectRedstone() {
        return false;
    }

    //////////////////////////////////////
    // ******* Rendering ********//
    //////////////////////////////////////
    /**
     * @return If the pipe this is placed on and a pipe on the other side should be able to connect
     */
    public boolean canPipePassThrough() {
        return true;
    }

    public boolean shouldRenderPlate() {
        return true;
    }

    public ICoverRenderer getCoverRenderer() {
        return coverDefinition.getCoverRenderer();
    }

    public @Nullable IFancyConfigurator getConfigurator() {
        return null;
    }

    /**
     * get Appearance. same as IBlockExtension.getAppearance() / IFabricBlock.getAppearance()
     */
    @Nullable
    public BlockState getAppearance(BlockState sourceState, BlockPos sourcePos) {
        return null;
    }

    //////////////////////////////////////
    // ******* Capabilities *******//
    //////////////////////////////////////

    @Nullable
    public IItemHandlerModifiable getItemHandlerCap(IItemHandlerModifiable defaultValue) {
        return defaultValue;
    }

    @Nullable
    public IFluidHandlerModifiable getFluidHandlerCap(IFluidHandlerModifiable defaultValue) {
        return defaultValue;
    }
}
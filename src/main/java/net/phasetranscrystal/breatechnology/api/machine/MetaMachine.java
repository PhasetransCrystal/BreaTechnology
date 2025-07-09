package net.phasetranscrystal.breatechnology.api.machine;

import com.lowdragmc.lowdraglib.syncdata.IEnhancedManaged;
import com.lowdragmc.lowdraglib.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib.syncdata.field.FieldManagedStorage;
import com.lowdragmc.lowdraglib.syncdata.field.ManagedFieldHolder;
import com.lowdragmc.lowdraglib.utils.DummyWorld;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.api.block.IAppearance;
import net.phasetranscrystal.breatechnology.api.block.MetaMachineBlock;
import net.phasetranscrystal.breatechnology.api.blockentity.IPaintable;
import net.phasetranscrystal.breatechnology.api.blockentity.ITickSubscription;
import net.phasetranscrystal.breatechnology.api.blockentity.MetaMachineBlockEntity;
import net.phasetranscrystal.breatechnology.api.cover.CoverBehavior;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;
import net.phasetranscrystal.breatechnology.api.machine.feature.IRedstoneSignalMachine;
import net.phasetranscrystal.breatechnology.api.machine.trait.MachineTrait;
import net.phasetranscrystal.breatechnology.common.machine.owner.MachineOwner;
import net.phasetranscrystal.breatechnology.common.machine.owner.PlayerOwner;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/// 机器基本方法
public class MetaMachine implements IEnhancedManaged, ITickSubscription, IAppearance , IPaintable, IRedstoneSignalMachine {//},IToolable, IToolGridHighLight, IFancyTooltip, {
    protected static final ManagedFieldHolder MANAGED_FIELD_HOLDER = new ManagedFieldHolder(MetaMachine.class);
    @Getter
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    @Setter
    @Getter
    @Persisted
    @DescSynced
    @Nullable
    private UUID ownerUUID;
    @Getter
    public final IMachineBlockEntity holder;
    @Getter
    @DescSynced
    @Persisted(key = "cover")
    protected final MachineCoverContainer coverContainer;
    @Getter
    @Setter
    @Persisted
    @DescSynced
    @RequireRerender
    private int paintingColor = -1;
    @Getter
    protected final List<MachineTrait> traits;
    private final List<TickableSubscription> serverTicks;
    private final List<TickableSubscription> waitingToAdd;

    public MetaMachine(IMachineBlockEntity holder) {
        this.holder = holder;
        this.coverContainer = new MachineCoverContainer(this);
        this.traits = new ArrayList<>();
        this.serverTicks = new ArrayList<>();
        this.waitingToAdd = new ArrayList<>();
        // bind sync storage
        this.holder.getRootStorage().attach(getSyncStorage());
    }
    //////////////////////////////////////
    // ***** Initialization ******//

    /// ///////////////////////////////////

    @Override
    public ManagedFieldHolder getFieldHolder() {
        return MANAGED_FIELD_HOLDER;
    }

    @Override
    public void onChanged() {
        var level = getLevel();
        if (level != null && !level.isClientSide && level.getServer() != null) {
            level.getServer().execute(this::markDirty);
        }
    }

    public @Nullable Level getLevel() {
        return holder.level();
    }

    public BlockPos getPos() {
        return holder.pos();
    }

    public BlockState getBlockState() {
        return holder.getSelf().getBlockState();
    }

    public boolean isRemote() {
        return getLevel() == null ? BreaTechnology.isClientThread() : getLevel().isClientSide;
    }

    public void notifyBlockUpdate() {
        holder.notifyBlockUpdate();
    }

    public void scheduleRenderUpdate() {
        holder.scheduleRenderUpdate();
    }

    public void scheduleNeighborShapeUpdate() {
        Level level = getLevel();
        BlockPos pos = getPos();

        if (level == null || pos == null)
            return;

        level.getBlockState(pos).updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    public long getOffsetTimer() {
        return holder.getOffsetTimer();
    }

    public void markDirty() {
        holder.getSelf().setChanged();
    }

    public boolean isInValid() {
        return holder.getSelf().isRemoved();
    }

    public void onUnload() {
        traits.forEach(MachineTrait::onMachineUnLoad);
        coverContainer.onUnload();
        for (TickableSubscription serverTick : serverTicks) {
            serverTick.unsubscribe();
        }
        serverTicks.clear();
    }

    public void onLoad() {
        traits.forEach(MachineTrait::onMachineLoad);
        coverContainer.onLoad();
    }

    /**
     * Use for data not able to be saved with the SyncData system, like optional mod compatiblity in internal machines.
     *
     * @param tag     the CompoundTag to load data from
     * @param forDrop if the save is done for dropping the machine as an item.
     */
    public void saveCustomPersistedData(@NotNull CompoundTag tag, boolean forDrop) {
        for (MachineTrait trait : this.getTraits()) {
            trait.saveCustomPersistedData(tag, forDrop);
        }
    }

    public void loadCustomPersistedData(@NotNull CompoundTag tag) {
        for (MachineTrait trait : this.getTraits()) {
            trait.loadCustomPersistedData(tag);
        }
    }

    public void applyImplicitComponents(MetaMachineBlockEntity.ExDataComponentInput componentInput) {
    }

    public void collectImplicitComponents(DataComponentMap.Builder components) {
    }

    //////////////////////////////////////
    // ***** Tickable Manager ****//
    //////////////////////////////////////

    /**
     * For initialization. To get level and property fields after auto sync, you can subscribe it in {@link #onLoad()}
     * event.
     */
    @Nullable
    public TickableSubscription subscribeServerTick(Runnable runnable) {
        if (!isRemote()) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
            var blockState = getBlockState();
            if (!blockState.getValue(IMachineBlock.SERVER_TICK)) {
                if (getLevel() instanceof ServerLevel serverLevel) {
                    blockState = blockState.setValue(IMachineBlock.SERVER_TICK, true);
                    holder.getSelf().setBlockState(blockState);
                    serverLevel.getServer().tell(new TickTask(0, () -> {
                        if (!isInValid()) {
                            serverLevel.setBlockAndUpdate(getPos(),
                                    getBlockState().setValue(IMachineBlock.SERVER_TICK, true));
                        }
                    }));
                }
            }
            return subscription;
        } else if (getLevel() instanceof DummyWorld) {
            var subscription = new TickableSubscription(runnable);
            waitingToAdd.add(subscription);
            return subscription;
        }
        return null;
    }

    public void unsubscribe(@Nullable TickableSubscription current) {
        if (current != null) {
            current.unsubscribe();
        }
    }

    public final void serverTick() {
        executeTick();
        if (serverTicks.isEmpty() && waitingToAdd.isEmpty() && !isInValid()) {
            getLevel().setBlockAndUpdate(getPos(), getBlockState().setValue(IMachineBlock.SERVER_TICK, false));
        }
    }

    public boolean isFirstDummyWorldTick = true;

    @OnlyIn(Dist.CLIENT)
    public void clientTick() {
        if (getLevel() instanceof DummyWorld) {
            if (isFirstDummyWorldTick) {
                isFirstDummyWorldTick = false;
                onLoad();
            }
            executeTick();
        }
    }

    private void executeTick() {
        if (!waitingToAdd.isEmpty()) {
            serverTicks.addAll(waitingToAdd);
            waitingToAdd.clear();
        }
        var iter = serverTicks.iterator();
        while (iter.hasNext()) {
            var tickable = iter.next();
            if (tickable.isStillSubscribed()) {
                tickable.run();
            }
            if (isInValid()) break;
            if (!tickable.isStillSubscribed()) {
                iter.remove();
            }
        }
    }

    //////////////////////////////////////
    // ********** MISC ***********//

    /// ///////////////////////////////////

    @Nullable
    public static MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof IMachineBlockEntity machineBlockEntity) {
            return machineBlockEntity.getMetaMachine();
        }
        return null;
    }

    /**
     * All traits should be initialized while MetaMachine is creating. you cannot add them on the fly.
     */
    public void attachTraits(MachineTrait trait) {
        traits.add(trait);
    }

    public void clearInventory(IItemHandlerModifiable inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            if (!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                Block.popResource(getLevel(), getPos(), stackInSlot);
            }
        }
    }

    public MetaMachineDefinition getDefinition() {
        return holder.getDefinition();
    }

    /**
     * Called to obtain list of AxisAlignedBB used for collision testing, highlight rendering
     * and ray tracing this meta tile entity's block in world
     */
    public void addCollisionBoundingBox(List<VoxelShape> collisionList) {
        collisionList.add(Shapes.block());
    }

    public boolean canSetIoOnSide(@Nullable Direction direction) {
        return !hasFrontFacing() || getFrontFacing() != direction;
    }

    public static @NotNull Direction getFrontFacing(@Nullable MetaMachine machine) {
        return machine == null ? Direction.NORTH : machine.getFrontFacing();
    }

    public Direction getFrontFacing() {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock machineBlock) {
            return machineBlock.getFrontFacing(blockState);
        }
        return Direction.NORTH;
    }

    public final boolean hasFrontFacing() {
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock machineBlock) {
            return machineBlock.getRotationState() != RotationState.NONE;
        }
        return false;
    }

    public boolean isFacingValid(Direction facing) {
        if (enableExtraRotation()) {
            return true;
        }
        if (hasFrontFacing() && facing == getFrontFacing()) return false;
        var coverContainer = getCoverContainer();
        if (coverContainer.hasCover(facing)) {
            // noinspection DataFlowIssue
            var coverDefinition = coverContainer.getCoverAtSide(facing).coverDefinition;
            var behaviour = coverDefinition.createCoverBehavior(coverContainer, getFrontFacing());
            if (!behaviour.canAttach()) {
                return false;
            }
        }
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock metaMachineBlock) {
            return metaMachineBlock.rotationState.test(facing);
        }
        return false;
    }

    public static @NotNull ExtraRotate getUpwardFacing(@Nullable MetaMachine machine) {
        return machine == null || !machine.enableExtraRotation() ? ExtraRotate.NONE :
                machine.getBlockState().getValue(IMachineBlock.EXTRA_ROTATE);
    }

    public ExtraRotate getUpwardsFacing() {
        return this.enableExtraRotation() ? this.getBlockState().getValue(IMachineBlock.EXTRA_ROTATE) : ExtraRotate.NONE;
    }

    public void setUpwardsFacing(@NotNull ExtraRotate upwardsFacing) {
        if (!getDefinition().isEnableExtraRotation()) {
            return;
        }
        var blockState = getBlockState();
        if (blockState.getBlock() instanceof MetaMachineBlock &&
                blockState.getValue(IMachineBlock.EXTRA_ROTATE) != upwardsFacing) {
            getLevel().setBlockAndUpdate(getPos(),
                    blockState.setValue(IMachineBlock.EXTRA_ROTATE, upwardsFacing));
            if (getLevel() != null && !getLevel().isClientSide) {
                notifyBlockUpdate();
                markDirty();
            }
        }
    }

    public void onRotated(Direction oldFacing, Direction newFacing) {
    }

    public boolean enableExtraRotation() {
        return getDefinition().isEnableExtraRotation();
    }

    public int tintColor(int index) {
        // index < -100 => emission if shimmer is installed.
        if (index == 1 || index == -111) {
            return getRealColor();
        }
        return -1;
    }

    public void onNeighborChanged(Block block, BlockPos fromPos, boolean isMoving) {
        coverContainer.onNeighborChanged(block, fromPos, isMoving);
    }

    public void animateTick(RandomSource random) {
    }

    @Override
    @NotNull
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side,
                                         @Nullable BlockState sourceState, BlockPos sourcePos) {
        var appearance = getCoverContainer().getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
        if (appearance != null) return appearance;
        /*
        if (this instanceof IMultiPart part && part.isFormed()) {
            appearance = part.getFormedAppearance(sourceState, sourcePos, side);
            if (appearance != null) return appearance;
        }
        */
        return (BlockState) getDefinition().getAppearance().get();
    }

    @Override
    public int getOutputSignal(@Nullable Direction side) {
        if (side == null) return 0;

        // For some reason, Minecraft requests the output signal from the opposite side...
        CoverBehavior cover = getCoverContainer().getCoverAtSide(side.getOpposite());
        if (cover == null) return 0;

        return cover.getRedstoneSignalOutput();
    }

    @Override
    public boolean canConnectRedstone(@Nullable Direction side) {
        if (side == null) return false;

        // For some reason, Minecraft requests the output signal from the opposite side...
        CoverBehavior cover = getCoverContainer().getCoverAtSide(side);
        if (cover == null) return false;

        return cover.canConnectRedstone();
    }

    //////////////////////////////////////
    // ****** Ownership ********//

    /// ///////////////////////////////////

    public @Nullable MachineOwner getOwner() {
        return MachineOwner.getOwner(ownerUUID);
    }

    public @Nullable PlayerOwner getPlayerOwner() {
        return MachineOwner.getPlayerOwner(ownerUUID);
    }

    @Override
    public int getDefaultPaintingColor() {
        return getDefinition().getDefaultPaintingColor();
    }

}
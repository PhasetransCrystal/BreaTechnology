package net.phasetranscrystal.breatechnology.api.block;

import com.lowdragmc.lowdraglib.client.renderer.IRenderer;
import com.lowdragmc.lowdraglib.utils.LocalizationUtils;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;
import net.phasetranscrystal.breatechnology.api.machine.*;
import net.phasetranscrystal.breatechnology.api.machine.feature.*;
import net.phasetranscrystal.breatechnology.api.utils.BTUtil;
import net.phasetranscrystal.breatechnology.common.machine.owner.MachineOwner;
import net.phasetranscrystal.breatechnology.data.menus.BTMenus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

/// 机器方块的基础类
public class MetaMachineBlock extends MetaAppearanceBlock implements IMachineBlock {

    @Getter
    public final RotationState rotationState;

    public MetaMachineBlock(Properties properties, MetaMachineDefinition<?> definition) {
        super(properties, definition);
        this.rotationState = RotationState.getPreState();
        if (rotationState != RotationState.NONE) {
            BlockState defaultState = this.defaultBlockState().setValue(rotationState.property, rotationState.defaultDirection);
            if (definition.isEnableExtraRotation()) {
                defaultState.setValue(EXTRA_ROTATE, ExtraRotate.NONE);
            }
            registerDefaultState(defaultState);
        }
    }

    @Override
    public MetaMachineDefinition<?> getDefinition() {
        return (MetaMachineDefinition<?>) super.getDefinition();
    }

    /// 添加State的属性
    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        builder.add(SERVER_TICK);
        var rotationState = RotationState.getPreState();
        if (rotationState != RotationState.NONE) {
            builder.add(rotationState.property);
            if (MetaMachineDefinition.getBuilt().isEnableExtraRotation()) {
                builder.add(EXTRA_ROTATE);
            }
        }
    }

    /// 获取机器元数据
    @Nullable
    public MetaMachine getMachine(BlockGetter level, BlockPos pos) {
        return MetaMachine.getMachine(level, pos);
    }

    /// 获取机器渲染器
    @Nullable
    @Override
    public IRenderer getRenderer(BlockState state) {
        return getDefinition().getRenderer();
    }

    /// 获取机器渲染类型
    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return getRotationState() == RotationState.NONE ? getDefinition().getShape(Direction.NORTH) :
                getDefinition().getShape(pState.getValue(getRotationState().property));
    }

    /// tick逻辑
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        var machine = getMachine(level, pos);
        if (machine != null) {
            machine.animateTick(random);
        }
    }

    /// 放置方块时初始化
    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity player, ItemStack pStack) {
        if (!pLevel.isClientSide) {
            var machine = getMachine(pLevel, pPos);
            if (machine != null) {
                if (player instanceof ServerPlayer sPlayer) {
                    machine.setOwnerUUID(sPlayer.getUUID());
                    machine.markDirty();
                }
            }
            if (machine instanceof IDropSaveMachine dropSaveMachine) {
                CustomData tag = pStack.get(DataComponents.BLOCK_ENTITY_DATA);
                if (tag != null) {
                    dropSaveMachine.loadFromItem(tag.copyTag());
                }
            }
            if (machine instanceof IMachineLife machineLife) {
                machineLife.onMachinePlaced(player, pStack);
            }
        }
    }

    /// 放置方块事件钩子
    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        // needed to trigger block updates so machines connect to open cables properly.
        level.updateNeighbourForOutputSignal(pos, this);
    }

    ///  设置BlockState在放置方块时
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var rotationState = getRotationState();
        var player = context.getPlayer();
        var blockPos = context.getClickedPos();
        var state = defaultBlockState();
        if (player != null && rotationState != RotationState.NONE) {

            if (rotationState == RotationState.Y_AXIS) {
                state = state.setValue(rotationState.property, Direction.UP);
            } else {
                state = state.setValue(rotationState.property, player.getDirection().getOpposite());
            }

            Vec3 pos = player.position();
            if (Math.abs(pos.x - (double) ((float) blockPos.getX() + 0.5F)) < 2.0D &&
                    Math.abs(pos.z - (double) ((float) blockPos.getZ() + 0.5F)) < 2.0D) {
                double d0 = pos.y + (double) player.getEyeHeight();
                if (d0 - (double) blockPos.getY() > 2.0D && rotationState.test(Direction.UP)) {
                    state = state.setValue(rotationState.property, Direction.UP);
                }
                if ((double) blockPos.getY() - d0 > 0.0D && rotationState.test(Direction.DOWN)) {
                    state = state.setValue(rotationState.property, Direction.DOWN);
                }
            }
            if (getDefinition().isEnableExtraRotation()) {
                Direction frontFacing = state.getValue(rotationState.property);
                if (frontFacing == Direction.UP) {
                    state = state.setValue(IMachineBlock.EXTRA_ROTATE, ExtraRotate.transform(player.getDirection()));
                } else if (frontFacing == Direction.DOWN) {
                    state = state.setValue(IMachineBlock.EXTRA_ROTATE, ExtraRotate.transform(player.getDirection().getOpposite()));
                }
            }
        }
        return state;
    }

    /// 获取机器的旋转方向
    public Direction getFrontFacing(BlockState state) {
        return getRotationState() == RotationState.NONE ? Direction.NORTH : state.getValue(getRotationState().property);
    }

    @Override
    public @NotNull ItemStack getCloneItemStack(@NotNull BlockState state, @NotNull HitResult target, @NotNull LevelReader level, @NotNull BlockPos pos, @NotNull Player player) {
        ItemStack itemStack = super.getCloneItemStack(state, target, level, pos, player);
        if (getMachine(level, pos) instanceof IDropSaveMachine dropSaveMachine && dropSaveMachine.savePickClone()) {
            CompoundTag tag = itemStack.getOrDefault(DataComponents.BLOCK_ENTITY_DATA, CustomData.EMPTY).copyTag();
            // TODO remove in future version.
            dropSaveMachine.saveToItem(tag);
            itemStack.set(DataComponents.BLOCK_ENTITY_DATA, CustomData.of(tag));
        }
        return itemStack;
    }

    /// 添加覆盖层提示
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Item.TooltipContext level, @NotNull List<Component> tooltip,
                                @NotNull TooltipFlag flag) {
        getDefinition().getTooltipBuilder().accept(stack, tooltip);
        String mainKey = String.format("%s.machine.%s.tooltip", getDefinition().getId().getNamespace(), getDefinition().getId().getPath());
        if (LocalizationUtils.exist(mainKey)) {
            tooltip.add(1, Component.translatable(mainKey));
        }
    }

    /// 触发事件
    @Override
    public boolean triggerEvent(@NotNull BlockState pState, Level pLevel, @NotNull BlockPos pPos, int pId, int pParam) {
        BlockEntity tile = pLevel.getBlockEntity(pPos);
        if (tile != null) {
            return tile.triggerEvent(pId, pParam);
        }
        return false;
    }

    /// 旋转方块
    @Override
    public @NotNull BlockState rotate(@NotNull BlockState pState, @NotNull Rotation pRotation) {
        if (this.rotationState == RotationState.NONE) {
            return pState;
        }
        return pState.setValue(this.rotationState.property, pRotation.rotate(pState.getValue(this.rotationState.property)));
    }

    /// 获取掉落表
    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.Builder builder) {
        BlockEntity tileEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        var drops = super.getDrops(state, builder);
        if (tileEntity instanceof IMachineBlockEntity holder) {
            var machine = holder.getMetaMachine();
            if (machine instanceof IMachineModifyDrops machineModifyDrops) {
                machineModifyDrops.onDrops(drops);
            }
        }
        return drops;
    }

    /// 方块被移除事件钩子
    @Override
    public void onRemove(BlockState pState, @NotNull Level pLevel, @NotNull BlockPos pPos, @NotNull BlockState pNewState, boolean pIsMoving) {
        // 检查当前方块是否有方块实体
        if (pState.hasBlockEntity()) {
            // 情况1：新方块与旧方块类型不同（方块被替换）
            if (!pState.is(pNewState.getBlock())) {
                // 获取当前位置的机器元数据
                MetaMachine machine = getMachine(pLevel, pPos);
                // 如果机器实现了IMachineLife接口，调用其移除回调
                if (machine instanceof IMachineLife machineLife) {
                    machineLife.onMachineRemoved();
                }
                // 如果机器存在，移除所有方向的覆盖物
                if (machine != null) {
                    for (Direction direction : BTUtil.DIRECTIONS) {
                        machine.getCoverContainer().removeCover(direction, null);
                    }
                }
                // 更新相邻方块的输出信号并移除方块实体
                pLevel.updateNeighbourForOutputSignal(pPos, this);
                pLevel.removeBlockEntity(pPos);
            }
            // 情况2：方块类型相同但朝向不同（仅旋转）
            else if (rotationState != RotationState.NONE) {
                // 获取新旧朝向值
                var oldFacing = pState.getValue(rotationState.property);
                var newFacing = pNewState.getValue(rotationState.property);
                // 如果朝向发生变化
                if (newFacing != oldFacing) {
                    // 获取机器实例并调用旋转回调
                    var machine = getMachine(pLevel, pPos);
                    if (machine != null) {
                        machine.onRotated(oldFacing, newFacing);
                    }
                }
            }
        }
    }

    /// 使用物品点击事件
    @Override
    protected @NotNull ItemInteractionResult useItemOn(@NotNull ItemStack stack, @NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos,
                                                       @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        var machine = getMachine(level, pos);
        /// 是否打开UI
        boolean shouldOpenUi = true;

        if (machine instanceof IInteractedMachine interactedMachine) {
            var result = interactedMachine.onUseWithItem(stack, state, level, pos, player, hand, hit);
            if (result.result() != InteractionResult.PASS) return result;
        }
        if (shouldOpenUi && machine instanceof IUIMachine uiMachine &&
                MachineOwner.canOpenOwnerMachine(player, machine)) {
            return uiMachine.tryToOpenUI(player, hand, hit);
        }
        return shouldOpenUi ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION : ItemInteractionResult.CONSUME;
    }

    /// 空手点击事件
    @Override
    protected InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        var machine = getMachine(level, pos);
        if (machine instanceof IUIMachine uiMachine &&
                MachineOwner.canOpenOwnerMachine(player, machine)) {
            return uiMachine.tryToOpenUI(player, InteractionHand.MAIN_HAND, hitResult).result();
        }
        return super.useWithoutItem(state, level, pos, player, hitResult);
    }

    /// 是否能连接红石
    public boolean canConnectRedstone(BlockGetter level, BlockPos pos, @Nullable Direction side) {
        return getMachine(level, pos).canConnectRedstone(side);
    }

    /// 活动红石信号强度
    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getMachine(level, pos).getOutputSignal(direction);
    }

    /// 获得指定方向红石信号强度
    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getMachine(level, pos).getOutputDirectSignal(direction);
    }

    /// 模拟输出信号强度
    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getMachine(level, pos).getAnalogOutputSignal();
    }

    /// 通知周围方块更新
    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
                                boolean isMoving) {
        var machine = getMachine(level, pos);
        if (machine != null) {
            machine.onNeighborChanged(block, fromPos, isMoving);
        }
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
    }

    /// 获取重制后方块外观（BlockState）
    @Nullable
    @Override
    public BlockState getBlockAppearance(BlockState state, BlockAndTintGetter level, BlockPos pos, Direction side, @Nullable BlockState sourceState, BlockPos sourcePos) {
        var machine = getMachine(level, pos);
        if (machine != null) {
            return machine.getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
        }
        return super.getBlockAppearance(state, level, pos, side, sourceState, sourcePos);
    }
}
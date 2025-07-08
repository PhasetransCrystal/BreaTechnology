package net.phasetranscrystal.breatechnology.api.machine;

import appeng.api.AECapabilities;
import appeng.api.networking.IInWorldGridNodeHost;
import com.lowdragmc.lowdraglib.client.renderer.IBlockRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.api.definition.MetaDefinition;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;
import net.phasetranscrystal.breatechnology.api.machine.trait.MachineTrait;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/// 机器方块基本方法接口
public interface IMachineBlock extends EntityBlock, IBlockRendererProvider {
    BooleanProperty SERVER_TICK = BooleanProperty.create("server_tick");
    EnumProperty<ExtraRotate> EXTRA_ROTATE = EnumProperty.create("extra_rotate", ExtraRotate.class);
    //DirectionProperty UPWARDS_FACING_PROPERTY = DirectionProperty.create("upwards_facing", Direction.Plane.HORIZONTAL);

    default Block self() {
        return (Block) this;
    }

    MetaMachineDefinition getDefinition();

    RotationState getRotationState();
    static int colorTinted(BlockState blockState, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos,
                           int index) {
        if (level != null && pos != null) {
            var machine = MetaMachine.getMachine(level, pos);
            if (machine != null) {
                return machine.tintColor(index);
            }
        }
        return -1;
    }
    @Nullable
    @Override
    default BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getDefinition().getBlockEntityType().create(pos, state);
    }
    @Nullable
    @Override
    default <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                   BlockEntityType<T> blockEntityType) {
        if (blockEntityType == getDefinition().getBlockEntityType()) {
            if (state.getValue(SERVER_TICK) && !level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().serverTick();
                    }
                };
            }
            if (level.isClientSide) {
                return (pLevel, pPos, pState, pTile) -> {
                    if (pTile instanceof IMachineBlockEntity metaMachine) {
                        metaMachine.getMetaMachine().clientTick();
                    }
                };
            }
        }
        return null;
    }

    default void attachCapabilities(RegisterCapabilitiesEvent event) {
        if (BreaTechnology.Mods.isAE2Loaded()) {
            event.registerBlock(AECapabilities.IN_WORLD_GRID_NODE_HOST, (level, pos, state, blockEntity, side) -> {
                if (blockEntity instanceof IMachineBlockEntity machine) {
                    if (machine.getMetaMachine() instanceof IInWorldGridNodeHost nodeHost) {
                        return nodeHost;
                    }
                    var list = getCapabilitiesFromTraits(machine.getMetaMachine().getTraits(), null,
                            IInWorldGridNodeHost.class);
                    if (!list.isEmpty()) {
                        // TODO wrap list in the future (or not.)
                        return list.getFirst();
                    }
                }
                return null;
            }, this.self());
        }
    }
    static <T> List<T> getCapabilitiesFromTraits(List<MachineTrait> traits, @Nullable Direction accessSide,
                                                 Class<T> capability) {
        if (traits.isEmpty()) return Collections.emptyList();
        List<T> list = new ArrayList<>();
        for (MachineTrait trait : traits) {
            if (trait.hasCapability(accessSide) && capability.isInstance(trait)) {
                list.add(capability.cast(trait));
            }
        }
        return list;
    }
}

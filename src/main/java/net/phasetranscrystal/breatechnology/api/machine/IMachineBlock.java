package net.phasetranscrystal.breatechnology.api.machine;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.phasetranscrystal.breatechnology.api.definition.MetaDefinition;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;

/// 机器方块基本方法接口
public interface IMachineBlock extends EntityBlock {
    BooleanProperty SERVER_TICK = BooleanProperty.create("server_tick");
    //DirectionProperty UPWARDS_FACING_PROPERTY = DirectionProperty.create("upwards_facing", Direction.Plane.HORIZONTAL);

    default Block self() {
        return (Block) this;
    }

    MetaMachineDefinition getDefinition();
}

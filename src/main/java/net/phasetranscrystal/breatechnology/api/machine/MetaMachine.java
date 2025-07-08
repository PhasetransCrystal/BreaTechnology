package net.phasetranscrystal.breatechnology.api.machine;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.UUID;

/// 机器基本方法
public class MetaMachine {
    @Setter
    @Getter
    @Nullable
    private UUID ownerUUID;
    @Getter
    public final IMachineBlockEntity holder;

    public MetaMachine(IMachineBlockEntity holder) {
        this.holder = holder;
        //this.coverContainer = new MachineCoverContainer(this);
        //this.traits = new ArrayList<>();
        //this.serverTicks = new ArrayList<>();
        //this.waitingToAdd = new ArrayList<>();
        // bind sync storage
        //this.holder.getRootStorage().attach(getSyncStorage());
    }
}

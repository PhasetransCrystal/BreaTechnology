package net.phasetranscrystal.breatechnology.api.machine.feature;

import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;

/// 机器额外能力接口
public interface IMachineFeature {

    default MetaMachine self() {
        return (MetaMachine) this;
    }
}
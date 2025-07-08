package net.phasetranscrystal.breatechnology.api.machine.feature;

import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;

public interface IMachineFeature {

    default MetaMachine self() {
        return (MetaMachine) this;
    }
}
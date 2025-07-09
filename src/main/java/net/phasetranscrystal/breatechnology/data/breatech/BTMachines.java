package net.phasetranscrystal.breatechnology.data.breatech;

import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;
import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;
import net.phasetranscrystal.breatechnology.api.registry.BTRegistries;
import net.phasetranscrystal.breatechnology.data.misc.BTCreativeModeTabs;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTMachines {
    static {
        REGISTRATE.creativeModeTab(() -> BTCreativeModeTabs.MACHINE_TAB);
    }

    public static MetaMachineDefinition<?> TEST_MACHINE = REGISTRATE.machine("test_machine", MetaMachine::new)
            .register();

    public static void init() {
    }
}
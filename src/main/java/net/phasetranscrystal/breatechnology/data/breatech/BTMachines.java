package net.phasetranscrystal.breatechnology.data.breatech;

import net.minecraft.network.chat.Component;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;
import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;
import net.phasetranscrystal.breatechnology.api.machine.RotationState;
import net.phasetranscrystal.breatechnology.data.misc.BTCreativeModeTabs;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTMachines {

    static {
        REGISTRATE.creativeModeTab(() -> BTCreativeModeTabs.MACHINE_TAB);
    }

    public static RegistryEntry<MetaMachineDefinition<?>, MetaMachineDefinition<?>> TEST_MACHINE = REGISTRATE.machine("test_machine", MetaMachine::new)
            .rotationState(RotationState.ALL)
            .enableExtraRotation(true)
            .tooltips(Component.translatable("tooltip.breatech.test_machine"))
            .register();

    public static void init() {}
}

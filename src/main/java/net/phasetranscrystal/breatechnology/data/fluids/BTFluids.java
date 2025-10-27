package net.phasetranscrystal.breatechnology.data.fluids;

import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.phasetranscrystal.breatechnology.api.registry.registate.BTClientFluidTypeExtensions;
import net.phasetranscrystal.breatechnology.data.misc.BTCreativeModeTabs;

import com.tterrag.registrate.util.entry.FluidEntry;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTFluids {

    static {
        REGISTRATE.creativeModeTab(() -> BTCreativeModeTabs.TEST_TAB);
    }

    public static FluidEntry<BaseFlowingFluid.Flowing> TEST_FLUID1 = REGISTRATE.fluid("test_fluid1", BTClientFluidTypeExtensions.LIQUID_STILL, BTClientFluidTypeExtensions.LIQUID_FLOWING, 0xFFf7c173, BTClientFluidTypeExtensions.Liquid::new)
            .lang("test_fluid1")
            .register();
    public static FluidEntry<BaseFlowingFluid.Flowing> TEST_FLUID2 = REGISTRATE.fluid("test_fluid2", BTClientFluidTypeExtensions.MELT_STILL, BTClientFluidTypeExtensions.MELT_FLOWING, 0xFFf7c173, BTClientFluidTypeExtensions.Melt::new)
            .lang("test_fluid2")
            .register();

    public static void init() {}
}

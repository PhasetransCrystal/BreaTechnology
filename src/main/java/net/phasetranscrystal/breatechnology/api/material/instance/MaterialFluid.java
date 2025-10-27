package net.phasetranscrystal.breatechnology.api.material.instance;

import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.phasetranscrystal.breatechnology.api.material.tag.MaterialTagInfo;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;

import lombok.Getter;

public abstract class MaterialFluid extends BaseFlowingFluid implements IMaterialInstance {

    @FunctionalInterface
    public interface MaterialFluidFactory {

        MaterialFluid create(MetaMaterial<?> definition, MaterialTagInfo instanceType, Properties properties);
    }

    @Getter
    private final MetaMaterial<?> definition;
    @Getter
    private final MaterialTagInfo instanceType;

    protected MaterialFluid(MetaMaterial<?> definition, MaterialTagInfo instanceType, Properties properties) {
        super(properties);
        this.definition = definition;
        this.instanceType = instanceType;
    }

    public static class Source extends MaterialFluid {

        public Source(MetaMaterial<?> definition, MaterialTagInfo instanceType, Properties properties) {
            super(definition, instanceType, properties);
        }

        public int getAmount(FluidState state) {
            return 8;
        }

        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static class Flowing extends MaterialFluid {

        public Flowing(MetaMaterial<?> definition, MaterialTagInfo instanceType, Properties properties) {
            super(definition, instanceType, properties);
            this.registerDefaultState((FluidState) ((FluidState) this.getStateDefinition().any()).setValue(LEVEL, 7));
        }

        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(new Property[] { LEVEL });
        }

        public int getAmount(FluidState state) {
            return (Integer) state.getValue(LEVEL);
        }

        public boolean isSource(FluidState state) {
            return false;
        }
    }
}

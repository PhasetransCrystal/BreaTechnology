package net.phasetranscrystal.breatechnology.api.material.instance;

import net.minecraft.world.item.BucketItem;
import net.minecraft.world.level.material.Fluid;
import net.phasetranscrystal.breatechnology.api.material.tag.MaterialTagInfo;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;

import lombok.Getter;

public class MaterialBucketItem extends BucketItem implements IMaterialInstance {

    @FunctionalInterface
    public interface MaterialBucketFactory {

        MaterialBucketItem create(MetaMaterial<?> definition, MaterialTagInfo instanceType, Fluid fluid, Properties properties);
    }

    @Getter
    private final MetaMaterial<?> definition;
    @Getter
    private final MaterialTagInfo instanceType;

    public MaterialBucketItem(MetaMaterial<?> definition, MaterialTagInfo instanceType, Fluid fluid, Properties properties) {
        super(fluid, properties);
        this.definition = definition;
        this.instanceType = instanceType;
    }
}

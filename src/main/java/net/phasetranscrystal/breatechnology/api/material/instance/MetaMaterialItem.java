package net.phasetranscrystal.breatechnology.api.material.instance;

import lombok.Getter;
import net.minecraft.world.item.Item;
import net.phasetranscrystal.breatechnology.api.material.tag.MaterialTagInfo;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;

public class MetaMaterialItem extends Item implements IMaterialInstance {
    @Getter
    private final MetaMaterial<?> definition;
    @Getter
    private final MaterialTagInfo instanceType;

    public MetaMaterialItem(MetaMaterial<?> definition, MaterialTagInfo instanceType, Properties properties) {
        super(properties);
        this.instanceType = instanceType;
        this.definition = definition;
    }
}

package net.phasetranscrystal.breatechnology.api.material.instance;

import net.minecraft.world.level.block.Block;
import net.phasetranscrystal.breatechnology.api.material.tag.MaterialTagInfo;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;

import lombok.Getter;

public class MetaMaterialBlock extends Block implements IMaterialInstance {

    @Getter
    private final MetaMaterial<?> definition;
    @Getter
    private final MaterialTagInfo instanceType;

    public MetaMaterialBlock(MetaMaterial<?> definition, MaterialTagInfo instanceType, Properties properties) {
        super(properties);
        this.definition = definition;
        this.instanceType = instanceType;
    }
}

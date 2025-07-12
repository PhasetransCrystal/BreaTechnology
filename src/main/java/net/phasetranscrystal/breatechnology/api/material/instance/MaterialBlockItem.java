package net.phasetranscrystal.breatechnology.api.material.instance;

import lombok.Getter;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.phasetranscrystal.breatechnology.api.material.tag.MaterialTagInfo;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;

public class MaterialBlockItem extends BlockItem implements IMaterialInstance {
    @FunctionalInterface
    public interface MaterialBlockItemFactory {
        MaterialBlockItem create(MetaMaterial<?> definition, MaterialTagInfo instanceType, Block block, Properties properties);
    }

    @Getter
    private final MetaMaterial<?> definition;
    @Getter
    private final MaterialTagInfo instanceType;

    public MaterialBlockItem(MetaMaterial<?> definition, MaterialTagInfo instanceType, Block block, Properties properties) {
        super(block, properties);
        this.definition = definition;
        this.instanceType = instanceType;
    }
}

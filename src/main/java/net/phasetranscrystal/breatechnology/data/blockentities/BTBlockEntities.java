package net.phasetranscrystal.breatechnology.data.blockentities;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.phasetranscrystal.breatechnology.api.blockentity.MetaBlockEntity;
import net.phasetranscrystal.breatechnology.data.blocks.BTBlocks;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTBlockEntities {
    public static BlockEntityEntry<MetaBlockEntity> META_BLOCK_ENTITY = REGISTRATE
            .blockEntity("meta_block_entity", MetaBlockEntity::new)
            .validBlock(BTBlocks.TEST_ENTITY_BLOCK)
            .register();

    public static void init() {

    }
}

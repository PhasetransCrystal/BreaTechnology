package net.phasetranscrystal.breatechnology.data.blocks;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.world.level.block.Block;
import net.phasetranscrystal.breatechnology.api.block.MetaBlock;
import net.phasetranscrystal.breatechnology.data.breatech.BTDefinitions;
import net.phasetranscrystal.breatechnology.data.misc.BTCreativeModeTabs;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTBlocks {
    static {
        REGISTRATE.creativeModeTab(() -> BTCreativeModeTabs.TEST_TAB);
    }

    public static RegistryEntry<Block, Block> TEST_BLOCK = REGISTRATE.block("test_block", Block::new)
            .simpleItem()
            .lang("Test Block")
            .register();
    public static RegistryEntry<Block, MetaBlock> TEST_ENTITY_BLOCK = REGISTRATE.block("test_entity_block",
                    prop -> new MetaBlock(prop, BTDefinitions.META_BLOCK_DEFINITION))
            .simpleItem()
            .lang("Test Block")
            .register();

    public static void init() {
    }
}

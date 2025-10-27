package net.phasetranscrystal.breatechnology.data.blocks;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.data.misc.BTCreativeModeTabs;

import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.RegistryEntry;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTBlocks {

    static {
        REGISTRATE.creativeModeTab(() -> BTCreativeModeTabs.TEST_TAB);
    }

    public static RegistryEntry<Block, Block> TEST_BLOCK = REGISTRATE.block("test_block", Block::new)
            .simpleItem()
            .properties(prop -> prop.strength(4.0F, 7.0F).lightLevel(state -> 4))
            .setData(ProviderType.BLOCKSTATE, (ctx, type) -> {
                type.simpleBlockWithItem(ctx.get(), new ModelFile.ExistingModelFile(BreaTechnology.id("block/tm"), type.models().existingFileHelper));
            })
            .tag(BlockTags.NEEDS_IRON_TOOL, BlockTags.MINEABLE_WITH_AXE, BlockTags.MINEABLE_WITH_PICKAXE)
            .lang("Test Block")
            .register();

    public static void init() {}
}

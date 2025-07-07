package net.phasetranscrystal.breatechnology.data.items;

import com.tterrag.registrate.util.entry.RegistryEntry;
import lombok.Getter;
import net.minecraft.world.item.Item;
import net.phasetranscrystal.breatechnology.data.misc.BTCreativeModeTabs;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

@Getter
public class BTItems {
    static {
        REGISTRATE.creativeModeTab(() -> BTCreativeModeTabs.TEST_TAB);
    }

    public static RegistryEntry<Item, Item> TEST_ITEM = REGISTRATE.item("test_item", Item::new)
            .lang("Test Item")
            .register();

    public static void init() {
    }
}

package net.phasetranscrystal.breatechnology.data.menus;

import com.tterrag.registrate.util.entry.MenuEntry;
import net.phasetranscrystal.breatechnology.test.TestMenu;
import net.phasetranscrystal.breatechnology.test.TestScreen;
import org.apache.commons.compress.utils.ExactMath;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTMenus {
    public static MenuEntry<TestMenu> TestMenu = REGISTRATE.menu("example_menu", TestMenu::new, () -> TestScreen::new).register();

    public static void init() {
    }
}

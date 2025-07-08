package net.phasetranscrystal.breatechnology.data.textures;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.phasetranscrystal.breatechnology.api.registry.BTRegistries;

public class GuiTextures {
    static {
        BTRegistries.GUI_TEXTURES.unfreeze();
    }
    public static ResourceTexture BACKGROUND = registerTexture("background", "breatechnology:textures/gui/base/background.png");

    public static void init() {
    }

    private static ResourceTexture getTexture(String location) {
        return new ResourceTexture(location);
    }

    public static ResourceTexture registerTexture(String name, String location) {
        return BTRegistries.GUI_TEXTURES.register(name, getTexture(location));
    }

    public static ResourceTexture registerTexture(String name, ResourceTexture texture) {
        return BTRegistries.GUI_TEXTURES.register(name, texture);
    }
}
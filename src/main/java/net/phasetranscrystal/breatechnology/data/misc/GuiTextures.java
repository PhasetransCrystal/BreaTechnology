package net.phasetranscrystal.breatechnology.data.misc;

import net.phasetranscrystal.breatechnology.api.registry.BTRegistries;

import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;

public class GuiTextures {

    public static ResourceTexture SCENE;
    public static ResourceTexture VANILLA_BUTTON;
    public static ResourceTexture SLOT;

    static {
        BTRegistries.GUI_TEXTURES.unfreeze();
    }

    public static ResourceTexture BACKGROUND = registerTexture("background", "breatechnology:textures/gui/base/background.png");

    public static void init() {}

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

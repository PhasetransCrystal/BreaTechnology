package net.phasetranscrystal.breatechnology.api.capability.recipe;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import lombok.Getter;
import net.phasetranscrystal.breatechnology.api.gui.widget.EnumSelectorWidget;

/**
 * The capability can be input or output or both
 */
public enum IO implements EnumSelectorWidget.SelectableEnum {

    IN("breatechnology.io.import", "import"),
    OUT("breatechnology.io.export", "export"),
    BOTH("breatechnology.io.both", "both"),
    NONE("breatechnology.io.none", "none");

    @Getter
    public final String tooltip;
    @Getter
    public final IGuiTexture icon;

    IO(String tooltip, String textureName) {
        this.tooltip = tooltip;
        this.icon = new ResourceTexture("gtceu:textures/gui/icon/io_mode/" + textureName + ".png");
    }

    public boolean support(IO io) {
        if (io == this) return true;
        if (io == NONE) return false;
        return this == BOTH;
    }
}

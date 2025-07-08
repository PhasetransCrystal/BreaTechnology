package net.phasetranscrystal.breatechnology.api.cover;

import com.lowdragmc.lowdraglib.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.phasetranscrystal.breatechnology.api.gui.widget.EnumSelectorWidget;

public enum ManualIOMode implements EnumSelectorWidget.SelectableEnum {

    DISABLED("disabled"),
    FILTERED("filtered"),
    UNFILTERED("unfiltered");

    public static final ManualIOMode[] VALUES = values();

    public final String localeName;

    ManualIOMode(String localeName) {
        this.localeName = localeName;
    }

    @Override
    public String getTooltip() {
        return "cover.universal.manual_import_export.mode." + localeName;
    }

    @Override
    public IGuiTexture getIcon() {
        return new ResourceTexture("gtceu:textures/gui/icon/manual_io_mode/" + localeName + ".png");
    }
}

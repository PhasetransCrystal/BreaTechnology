package net.phasetranscrystal.breatechnology.config;

import net.phasetranscrystal.breatechnology.BreaTechnology;

import dev.toma.configuration.Configuration;
import dev.toma.configuration.config.Config;
import dev.toma.configuration.config.Configurable;
import dev.toma.configuration.config.format.ConfigFormats;
import org.jetbrains.annotations.ApiStatus;

@Config(id = BreaTechnology.MOD_ID)
public class ConfigHolder {

    public static ConfigHolder INSTANCE;
    private static final Object LOCK = new Object();

    @ApiStatus.Internal
    public static dev.toma.configuration.config.ConfigHolder<ConfigHolder> INTERNAL_INSTANCE;

    public static void init() {
        synchronized (LOCK) {
            if (INSTANCE == null || INTERNAL_INSTANCE == null) {
                INTERNAL_INSTANCE = Configuration.registerConfig(ConfigHolder.class, ConfigFormats.YAML);
                INSTANCE = INTERNAL_INSTANCE.getConfigInstance();
            }
        }
    }

    @Configurable
    public ClientConfigs client = new ClientConfigs();

    public static class ClientConfigs {

        @Configurable
        @Configurable.Comment({ "The default color to overlay onto machines.",
                "#FFFFFF is no coloring (default).",
                "#D2DCFF is the classic blue from GT5." })
        @Configurable.StringPattern(value = "#[0-9a-fA-F]{1,6}")
        @Configurable.Gui.ColorValue
        public String defaultPaintingColor = "#FFFFFF";
    }

    @Configurable
    public ServerConfigs server = new ServerConfigs();

    public static class ServerConfigs {}

    @Configurable
    public MachineConfigs machines = new MachineConfigs();

    public static class MachineConfigs {

        @Configurable
        @Configurable.Comment({ "Whether ONLY owners can open a machine gui", "Default: false" })
        public boolean onlyOwnerGUI = false;
        @Configurable
        @Configurable.Comment({ "Whether ONLY owners can break a machine", "Default: false" })
        public boolean onlyOwnerBreak = false;
        @Configurable
        @Configurable.Comment({ "Minimum op level to bypass the ownership checks", "Default: 2" })
        @Configurable.Range(min = 0, max = 4)
        public int ownerOPBypass = 2;
    }
}

package com.phasetranscrystal.breatechnology;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

import com.phasetranscrystal.breatechnology.client.ClientProxy;
import com.phasetranscrystal.breatechnology.common.CommonProxy;
import com.tterrag.registrate.util.RegistrateDistExecutor;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(BreaTechnology.MOD_ID)
public class BreaTechnology {

    public static final Logger LOGGER = LogManager.getLogger(BreaTechnology.MOD_ID);
    public static final String MOD_ID = "breatechnology";
    public static final String NAME = "舟尘科技";
    @Getter
    private static ModContainer modContainer;
    @Getter
    private static IEventBus modEventBus;

    public BreaTechnology(ModContainer container, IEventBus modEventBus) {
        BreaTechnology.modContainer = container;
        BreaTechnology.modEventBus = modEventBus;
        RegistrateDistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);
    }
}

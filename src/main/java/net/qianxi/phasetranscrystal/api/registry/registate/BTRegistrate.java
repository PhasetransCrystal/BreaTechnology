package net.qianxi.phasetranscrystal.api.registry.registate;

import com.tterrag.registrate.Registrate;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.qianxi.phasetranscrystal.BreaTechnology;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class BTRegistrate extends Registrate {
    private static final Map<String, BTRegistrate> EXISTING_REGISTRATES = new Object2ObjectOpenHashMap<>();

    private final AtomicBoolean registered = new AtomicBoolean(false);

    protected BTRegistrate(String modid) {
        super(modid);
    }

    public static BTRegistrate create(String modId) {
        return innerCreate(modId, true);
    }

    @ApiStatus.Internal
    public static BTRegistrate createIgnoringListenerErrors(String modId) {
        return innerCreate(modId, false);
    }

    private static BTRegistrate innerCreate(String modId, boolean strict) {
        if (EXISTING_REGISTRATES.containsKey(modId)) {
            return EXISTING_REGISTRATES.get(modId);
        }
        var registrate = new BTRegistrate(modId);
        Optional<IEventBus> modEventBus = ModList.get().getModContainerById(modId).map(ModContainer::getEventBus);
        if (strict) {
            modEventBus.ifPresentOrElse(registrate::registerEventListeners, () -> {
                String message = "# [GTRegistrate] Failed to register eventListeners for mod " + modId + ", This should be reported to this mod's dev #";
                String hashtags = "#".repeat(message.length());
                BreaTechnology.LOGGER.fatal(hashtags);
                BreaTechnology.LOGGER.fatal(message);
                BreaTechnology.LOGGER.fatal(hashtags);
            });
        } else {
            registrate.registerEventListeners(modEventBus.orElse(BreaTechnology.btModBus));
        }
        EXISTING_REGISTRATES.put(modId, registrate);
        return registrate;
    }

    @Override
    public BTRegistrate registerEventListeners(IEventBus bus) {
        if (!registered.getAndSet(true)) {
            return (BTRegistrate) super.registerEventListeners(bus);
        }
        return this;
    }
}
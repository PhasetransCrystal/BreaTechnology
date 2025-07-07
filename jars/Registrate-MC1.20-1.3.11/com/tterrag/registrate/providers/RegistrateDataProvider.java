package com.tterrag.registrate.providers;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.DebugMarkers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RegistrateDataProvider implements DataProvider {
    private static final Logger log = LogManager.getLogger(RegistrateDataProvider.class);
    static final BiMap<String, ProviderType<?>> TYPES = HashBiMap.create();
    private final String mod;
    private final Map<ProviderType<?>, RegistrateProvider> subProviders = new LinkedHashMap();
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;

    @Nullable
    public static String getTypeName(ProviderType<?> type) {
        return (String)TYPES.inverse().get(type);
    }

    public RegistrateDataProvider(AbstractRegistrate<?> parent, String modid, GatherDataEvent event) {
        this.mod = modid;
        this.registriesLookup = event.getLookupProvider();
        EnumSet<LogicalSide> sides = EnumSet.noneOf(LogicalSide.class);
        if (event.includeServer()) {
            sides.add(LogicalSide.SERVER);
        }

        if (event.includeClient()) {
            sides.add(LogicalSide.CLIENT);
        }

        log.debug(DebugMarkers.DATA, "Gathering providers for sides: {}", sides);
        Map<ProviderType<?>, RegistrateProvider> known = new HashMap();

        for(String id : TYPES.keySet()) {
            ProviderType<?> type = (ProviderType)TYPES.get(id);
            RegistrateProvider prov = type.create(parent, event, known);
            known.put(type, prov);
            if (sides.contains(prov.getSide())) {
                log.debug(DebugMarkers.DATA, "Adding provider for type: {}", id);
                this.subProviders.put(type, prov);
            }
        }

    }

    public CompletableFuture<?> run(CachedOutput cache) {
        return this.registriesLookup.thenCompose((provider) -> {
            ArrayList<CompletableFuture<?>> list = Lists.newArrayList();

            for(Map.Entry<ProviderType<?>, RegistrateProvider> e : this.subProviders.entrySet()) {
                log.debug(DebugMarkers.DATA, "Generating data for type: {}", getTypeName((ProviderType)e.getKey()));
                list.add(((RegistrateProvider)e.getValue()).run(cache));
            }

            return CompletableFuture.allOf((CompletableFuture[])list.toArray((x$0) -> new CompletableFuture[x$0]));
        });
    }

    public String getName() {
        String modid = this.mod;
        return "Registrate Provider for " + modid + " [" + (String)this.subProviders.values().stream().map(DataProvider::getName).collect(Collectors.joining(", ")) + "]";
    }

    public <P extends RegistrateProvider> Optional<P> getSubProvider(ProviderType<P> type) {
        return Optional.ofNullable((RegistrateProvider)this.subProviders.get(type));
    }
}

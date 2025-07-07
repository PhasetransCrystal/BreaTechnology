package com.tterrag.registrate.providers;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.fml.LogicalSide;
import com.tterrag.registrate.AbstractRegistrate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class RegistrateAdvancementProvider implements RegistrateProvider, Consumer<Advancement> {
    private static final Logger log = LogManager.getLogger(RegistrateAdvancementProvider.class);
    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
    private final AbstractRegistrate<?> owner;
    private final PackOutput packOutput;
    private final CompletableFuture<HolderLookup.Provider> registriesLookup;
    private final List<CompletableFuture<?>> advancementsToSave = Lists.newArrayList();
    @Nullable
    private CachedOutput cache;
    private Set<ResourceLocation> seenAdvancements = new HashSet();

    public RegistrateAdvancementProvider(AbstractRegistrate<?> owner, PackOutput packOutputIn, CompletableFuture<HolderLookup.Provider> registriesLookupIn) {
        this.owner = owner;
        this.packOutput = packOutputIn;
        this.registriesLookup = registriesLookupIn;
    }

    public LogicalSide getSide() {
        return LogicalSide.SERVER;
    }

    public MutableComponent title(String category, String name, String title) {
        return this.owner.addLang("advancements", ResourceLocation.fromNamespaceAndPath(category, name), "title", title);
    }

    public MutableComponent desc(String category, String name, String desc) {
        return this.owner.addLang("advancements", ResourceLocation.fromNamespaceAndPath(category, name), "description", desc);
    }

    public CompletableFuture<?> run(CachedOutput cache) {
        return this.registriesLookup.thenCompose((lookup) -> {
            this.advancementsToSave.clear();

            try {
                this.cache = cache;
                this.seenAdvancements.clear();
                this.owner.genData(ProviderType.ADVANCEMENT, this);
            } finally {
                this.cache = null;
            }

            return CompletableFuture.allOf((CompletableFuture[])this.advancementsToSave.toArray((x$0) -> new CompletableFuture[x$0]));
        });
    }

    public void accept(@Nullable Advancement t) {
        CachedOutput cache = this.cache;
        if (cache == null) {
            throw new IllegalStateException("Cannot accept advancements outside of act");
        } else {
            Objects.requireNonNull(t, "Cannot accept a null advancement");
            Path path = this.packOutput.getOutputFolder();
            if (!this.seenAdvancements.add(t.getId())) {
                throw new IllegalStateException("Duplicate advancement " + t.getId());
            } else {
                Path path1 = getPath(path, t);
                this.advancementsToSave.add(DataProvider.saveStable(cache, t.deconstruct().serializeToJson(), path1));
            }
        }
    }

    private static Path getPath(Path pathIn, Advancement advancementIn) {
        String var10001 = advancementIn.getId().getNamespace();
        return pathIn.resolve("data/" + var10001 + "/advancements/" + advancementIn.getId().getPath() + ".json");
    }

    public String getName() {
        return "Advancements";
    }
}

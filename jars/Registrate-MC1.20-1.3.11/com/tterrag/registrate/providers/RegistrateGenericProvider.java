package com.tterrag.registrate.providers;


import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import com.tterrag.registrate.AbstractRegistrate;
import org.apache.commons.compress.utils.Lists;
import org.jetbrains.annotations.ApiStatus;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class RegistrateGenericProvider implements RegistrateProvider {
    private final AbstractRegistrate<?> registrate;
    private final PackOutput output;
    private final CompletableFuture<HolderLookup.Provider> registries;
    private final ExistingFileHelper existingFileHelper;
    private final LogicalSide side;
    private final ProviderType<RegistrateGenericProvider> providerType;
    private final List<Generator> generators = Lists.newArrayList();

    @ApiStatus.Internal
    RegistrateGenericProvider(AbstractRegistrate<?> registrate, GatherDataEvent event, LogicalSide side, ProviderType<RegistrateGenericProvider> providerType) {
        this.registrate = registrate;
        this.side = side;
        this.providerType = providerType;
        this.output = event.getGenerator().getPackOutput();
        this.registries = event.getLookupProvider();
        this.existingFileHelper = event.getExistingFileHelper();
    }

    public RegistrateGenericProvider add(Generator generator) {
        this.generators.add(generator);
        return this;
    }

    public LogicalSide getSide() {
        return this.side;
    }

    public CompletableFuture<?> run(CachedOutput cache) {
        this.generators.clear();
        GeneratorData data = new GeneratorData(this.output, this.registries, this.existingFileHelper);
        this.registrate.genData(this.providerType, this);
        return CompletableFuture.allOf((CompletableFuture[])this.generators.stream().map((generator) -> generator.generate(data)).map((provider) -> provider.run(cache)).toArray((x$0) -> new CompletableFuture[x$0]));
    }

    public String getName() {
        return "generic_%s_provider".formatted(this.side.name().toLowerCase(Locale.ROOT));
    }

    public static record GeneratorData(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
        public GeneratorData(PackOutput output, CompletableFuture<HolderLookup.Provider> registries, ExistingFileHelper existingFileHelper) {
            this.output = output;
            this.registries = registries;
            this.existingFileHelper = existingFileHelper;
        }

        public PackOutput output() {
            return this.output;
        }

        public CompletableFuture<HolderLookup.Provider> registries() {
            return this.registries;
        }

        public ExistingFileHelper existingFileHelper() {
            return this.existingFileHelper;
        }
    }

    @FunctionalInterface
    public interface Generator {
        DataProvider generate(GeneratorData var1);
    }
}

package com.tterrag.registrate.providers;

import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.IGeneratedBlockState;
import net.neoforged.neoforge.client.model.generators.MultiPartBlockStateBuilder;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import com.tterrag.registrate.AbstractRegistrate;

import java.util.Optional;

public class RegistrateBlockstateProvider extends BlockStateProvider implements RegistrateProvider {
    private final AbstractRegistrate<?> parent;

    public RegistrateBlockstateProvider(AbstractRegistrate<?> parent, PackOutput packOutput, ExistingFileHelper exFileHelper) {
        super(packOutput, parent.getModid(), exFileHelper);
        this.parent = parent;
    }

    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    protected void registerStatesAndModels() {
        this.parent.genData(ProviderType.BLOCKSTATE, this);
    }

    public String getName() {
        return "Blockstates";
    }

    ExistingFileHelper getExistingFileHelper() {
        return this.models().existingFileHelper;
    }

    public Optional<VariantBlockStateBuilder> getExistingVariantBuilder(Block block) {
        return Optional.ofNullable((IGeneratedBlockState)this.registeredBlocks.get(block)).filter((b) -> b instanceof VariantBlockStateBuilder).map((b) -> (VariantBlockStateBuilder)b);
    }

    public Optional<MultiPartBlockStateBuilder> getExistingMultipartBuilder(Block block) {
        return Optional.ofNullable((IGeneratedBlockState)this.registeredBlocks.get(block)).filter((b) -> b instanceof MultiPartBlockStateBuilder).map((b) -> (MultiPartBlockStateBuilder)b);
    }
}

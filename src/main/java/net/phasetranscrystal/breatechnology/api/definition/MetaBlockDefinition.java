package net.phasetranscrystal.breatechnology.api.definition;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.phasetranscrystal.breatechnology.api.blockentity.MetaBlockEntity;

import com.tterrag.registrate.AbstractRegistrate;
import lombok.Setter;

import java.util.function.Supplier;

/// 基础方块定义信息类
public class MetaBlockDefinition<T extends MetaBlockEntity> extends MetaDefinition<MetaBlockDefinition<T>> {

    @Setter
    private Supplier<BlockEntityType<? extends BlockEntity>> blockEntityTypeSupplier;

    public MetaBlockDefinition(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }

    public BlockEntityType<? extends BlockEntity> getBlockEntityType() {
        return blockEntityTypeSupplier.get();
    }
}

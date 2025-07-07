package net.phasetranscrystal.breatechnology.data.breatech;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.phasetranscrystal.breatechnology.api.blockentity.MetaBlockEntity;
import net.phasetranscrystal.breatechnology.api.definition.MetaBlockDefinition;
import net.phasetranscrystal.breatechnology.api.registry.BTRegistries;
import net.phasetranscrystal.breatechnology.data.blockentities.BTBlockEntities;

import java.util.function.Function;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTDefinitions {
    static {
        BTRegistries.BlockDefinitions.unfreeze();
    }

    public static MetaBlockDefinition<MetaBlockEntity> META_BLOCK_DEFINITION = registerMetaBlockDefinition("meta_block_definition",
            owner -> new MetaBlockDefinition<>(owner, BTBlockEntities.META_BLOCK_ENTITY));

    public static void init() {
    }

    // TODO: register extra information for BT registrar
    public static <R extends BlockEntity, T extends MetaBlockDefinition<R>> T registerMetaBlockDefinition(String name, Function<AbstractRegistrate<?>, T> factory) {
        var definition = factory.apply(REGISTRATE);
        BTRegistries.BlockDefinitions.register(ResourceLocation.fromNamespaceAndPath(REGISTRATE.getModid(), name), definition);
        return definition;
    }
}

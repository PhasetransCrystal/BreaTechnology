package net.phasetranscrystal.breatechnology.api.material.type;

import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.AbstractRegistrate;

/// 复合材料类型
public class CompositeMaterial extends MetaMaterial<CompositeMaterial> {

    public CompositeMaterial(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }
}

package net.phasetranscrystal.breatechnology.api.material.type;

import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.AbstractRegistrate;

/// 合金材料类
public class AlloyMaterial extends MetaMaterial<AlloyMaterial> {

    public AlloyMaterial(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }
}

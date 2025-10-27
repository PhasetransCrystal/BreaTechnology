package net.phasetranscrystal.breatechnology.api.material.type;

import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.AbstractRegistrate;

/// 有机材料类
public class OrganicMaterial extends MetaMaterial<OrganicMaterial> {

    public OrganicMaterial(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }
}

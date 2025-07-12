package net.phasetranscrystal.breatechnology.api.material.type;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.resources.ResourceLocation;

/// 无机非金属材料类
public class CeramicsMaterial extends MetaMaterial<CeramicsMaterial> {
    public CeramicsMaterial(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }
}

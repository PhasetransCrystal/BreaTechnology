package net.phasetranscrystal.breatechnology.api.material.type;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.resources.ResourceLocation;

/// 复合材料类型
public class CompositeMaterial extends MetaMaterial<CompositeMaterial> {
    public CompositeMaterial(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }
}

package net.phasetranscrystal.breatechnology.api.material.type;

import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.resources.ResourceLocation;

/// 基本元素材料类
public class ElementMaterial extends MetaMaterial<ElementMaterial> {
    public ElementMaterial(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }
}

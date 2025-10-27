package net.phasetranscrystal.breatechnology.api.material.type;

import net.minecraft.resources.ResourceLocation;

import com.tterrag.registrate.AbstractRegistrate;

/// 基本元素材料类
public class ElementMaterial extends MetaMaterial<ElementMaterial> {

    public ElementMaterial(AbstractRegistrate<?> owner, ResourceLocation id) {
        super(owner, id);
    }
}

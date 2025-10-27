package net.phasetranscrystal.breatechnology.data.materials;

import net.phasetranscrystal.breatechnology.api.material.instance.MetaMaterialBlock;
import net.phasetranscrystal.breatechnology.api.material.instance.MetaMaterialItem;
import net.phasetranscrystal.breatechnology.api.material.tag.MaterialTagInfo;
import net.phasetranscrystal.breatechnology.api.material.type.ElementMaterial;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;
import net.phasetranscrystal.breatechnology.data.misc.BTCreativeModeTabs;

import com.tterrag.registrate.util.entry.RegistryEntry;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTMaterials {

    // 材料物品类型
    public static MaterialTagInfo Ingot = new MaterialTagInfo("ingot").materialAmount(144).generateItem(true);
    public static MaterialTagInfo Dust = new MaterialTagInfo("dust").materialAmount(144).generateItem(true);
    public static MaterialTagInfo Block = new MaterialTagInfo("block").materialAmount(144 * 9).generateBlock(true);
    public static MaterialTagInfo Liquid = new MaterialTagInfo("liquid").materialAmount(1000).generateLiquidFluid(true);
    public static MaterialTagInfo Melt = new MaterialTagInfo("melt").materialAmount(1000).generateMeltFluid(true);

    static {
        REGISTRATE.creativeModeTab(() -> BTCreativeModeTabs.MATERIAL_TAB);
    }

    // 材料类型
    public static RegistryEntry<MetaMaterial<?>, ElementMaterial> Iron = REGISTRATE.material("gal", ElementMaterial::new)
            .addInstanceItem(MetaMaterialItem::new, Ingot, Dust)
            .addInstanceBlock(MetaMaterialBlock::new, Block)
            .addInstanceFluid(Liquid, Melt)
            .color(0xffffff00).secondaryColor(0xffffff00)
            .register();

    public static void init() {}
}

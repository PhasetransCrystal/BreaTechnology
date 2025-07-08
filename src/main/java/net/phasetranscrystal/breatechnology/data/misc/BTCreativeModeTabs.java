package net.phasetranscrystal.breatechnology.data.misc;

import com.tterrag.registrate.util.entry.RegistryEntry;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.api.registry.registate.BTRegistrate;
import org.jetbrains.annotations.NotNull;

import static net.phasetranscrystal.breatechnology.common.registry.BTRegistration.REGISTRATE;

public class BTCreativeModeTabs {
    public static RegistryEntry<CreativeModeTab, CreativeModeTab> TEST_TAB = REGISTRATE.defaultCreativeTab("test_tab",
                    builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("test_tab", REGISTRATE))
                            .icon(Items.AIR::getDefaultInstance)
                            .title(REGISTRATE.addLang("itemGroup", BreaTechnology.id("test_tab"), BreaTechnology.NAME + "TestTab"))
                            .build())
            .register();
    public static RegistryEntry<CreativeModeTab,CreativeModeTab>MACHINE_TAB=REGISTRATE.defaultCreativeTab("machine_tab",
            builder -> builder.displayItems(new RegistrateDisplayItemsGenerator("machine_tab", REGISTRATE))
                    .icon(Items.AIR::getDefaultInstance)
                    .title(REGISTRATE.addLang("itemGroup", BreaTechnology.id("machine_tab"), BreaTechnology.NAME + "Machine Tab"))
                    .build())
            .register();

    public static void init() {
    }

    public static class RegistrateDisplayItemsGenerator implements CreativeModeTab.DisplayItemsGenerator {

        public final String name;
        public final BTRegistrate registrate;

        public RegistrateDisplayItemsGenerator(String name, BTRegistrate registrate) {
            this.name = name;
            this.registrate = registrate;
        }

        @Override
        public void accept(@NotNull CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
                           @NotNull CreativeModeTab.Output output) {
            var tab = registrate.get(name, Registries.CREATIVE_MODE_TAB);
            for (var entry : registrate.getAll(Registries.BLOCK)) {
                if (!registrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get().asItem();
                if (item == Items.AIR)
                    continue;
            }
            for (var entry : registrate.getAll(Registries.ITEM)) {
                if (!registrate.isInCreativeTab(entry, tab))
                    continue;
                Item item = entry.get();
                switch (item) {
                    /*
                    case IComponentItem componentItem -> {
                        NonNullList<ItemStack> list = NonNullList.create();
                        componentItem.fillItemCategory(tab.get(), list);
                        list.forEach(output::accept);
                    }
                    case IGTTool tool -> {
                        NonNullList<ItemStack> list = NonNullList.create();
                        tool.definition$fillItemCategory(tab.get(), list);
                        list.forEach(output::accept);
                    }
                    case LampBlockItem lamp -> {
                        NonNullList<ItemStack> list = NonNullList.create();
                        lamp.fillItemCategory(tab.get(), list);
                        list.forEach(output::accept);
                    }*/
                    default -> output.accept(item);
                }
            }
        }
    }
}

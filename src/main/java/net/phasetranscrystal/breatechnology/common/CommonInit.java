package net.phasetranscrystal.breatechnology.common;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.event.lifecycle.InterModProcessEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;
import net.phasetranscrystal.breatechnology.data.blockentities.BTBlockEntities;
import net.phasetranscrystal.breatechnology.data.blocks.BTBlocks;
import net.phasetranscrystal.breatechnology.data.breatech.BTMachines;
import net.phasetranscrystal.breatechnology.data.items.BTItems;
import net.phasetranscrystal.breatechnology.data.menus.BTMenus;
import net.phasetranscrystal.breatechnology.data.misc.BTCreativeModeTabs;

public class CommonInit {
    public static void init(IEventBus modEventBus) {
        modEventBus.register(CommonInit.class);

        initMaterials();

        BTCreativeModeTabs.init();
        BTBlocks.init();

        BTBlockEntities.init();

        BTMachines.init();

        BTItems.init();

        BTMenus.init();
    }

    private static void initMaterials() {

    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public static void loadComplete(FMLLoadCompleteEvent event) {
    }

    @SubscribeEvent
    public static void interModProcess(InterModProcessEvent event) {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
    }

    @SubscribeEvent
    public static void registerDataPackRegistries(DataPackRegistryEvent.NewRegistry event) {
    }

    @SubscribeEvent
    public static void registerPackFinders(AddPackFindersEvent event) {
    }

    @SubscribeEvent
    public static void addValidBlocksToBETypes(BlockEntityTypeAddBlocksEvent event) {
    }
}
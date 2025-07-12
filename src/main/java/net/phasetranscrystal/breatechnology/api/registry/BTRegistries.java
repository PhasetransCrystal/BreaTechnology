package net.phasetranscrystal.breatechnology.api.registry;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.lowdragmc.lowdraglib.gui.texture.ResourceTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.api.cover.CoverDefinition;
import net.phasetranscrystal.breatechnology.api.definition.MetaMachineDefinition;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;
import org.jetbrains.annotations.ApiStatus;

public class BTRegistries {
    public static final ResourceKey<Registry<MetaMaterial<?>>> MATERIALS =
            ResourceKey.createRegistryKey(BreaTechnology.id("material"));
    public static final ResourceKey<Registry<MetaMachineDefinition<?>>> MACHINE_DEFINITIONS =
            ResourceKey.createRegistryKey(BreaTechnology.id("machine_definitions"));

    public static final BTRegistry.String<ResourceTexture> GUI_TEXTURES = new BTRegistry.String<>(BreaTechnology.id("gui_textures"));
    public static final BTRegistry.RL<CoverDefinition> COVERS = new BTRegistry.RL<>(BreaTechnology.id("covers"));

    private static final Table<Registry<?>, ResourceLocation, Object> TO_REGISTER = HashBasedTable.create();


    /// 注册到全局注册表
    public static <V, T extends V> T register(Registry<V> registry, ResourceLocation name, T value) {
        TO_REGISTER.put(registry, name, value);
        return value;
    }

    // ignore the generics and hope the registered objects are still correctly typed :3
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void actuallyRegister(RegisterEvent event) {
        for (Registry reg : TO_REGISTER.rowKeySet()) {
            event.register(reg.key(), helper -> {
                TO_REGISTER.row(reg).forEach(helper::register);
            });
        }
    }

    public static void init(IEventBus eventBus) {
        eventBus.addListener(BTRegistries::actuallyRegister);
    }

    private static final RegistryAccess BLANK = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    private static RegistryAccess FROZEN = BLANK;

    /**
     * You shouldn't call it, you should probably not even look at it just to be extra safe
     *
     * @param registryAccess the new value to set to the frozen registry access
     */
    @ApiStatus.Internal
    public static void updateFrozenRegistry(RegistryAccess registryAccess) {
        FROZEN = registryAccess;
    }

    public static RegistryAccess builtinRegistry() {
        if (FROZEN == BLANK && BreaTechnology.isClientThread()) {
            if (Minecraft.getInstance().getConnection() != null) {
                return Minecraft.getInstance().getConnection().registryAccess();
            }
        }
        return FROZEN;
    }
}
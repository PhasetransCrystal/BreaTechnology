package net.phasetranscrystal.breatechnology.api.gui.egitor;

import com.lowdragmc.lowdraglib.gui.editor.configurator.IConfigurableWidget;
import com.lowdragmc.lowdraglib.gui.editor.data.Resources;
import com.lowdragmc.lowdraglib.gui.widget.WidgetGroup;
import com.lowdragmc.lowdraglib.utils.Position;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;

import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.api.machine.MetaMachine;
import net.phasetranscrystal.breatechnology.api.registry.BTRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class EditableMachineUI implements IEditableUI<WidgetGroup, MetaMachine> {

    @Getter
    final String groupName;
    @Getter
    final ResourceLocation uiPath;
    final Supplier<WidgetGroup> widgetSupplier;
    final BiConsumer<WidgetGroup, MetaMachine> binder;
    @Nullable
    private CompoundTag customUICache;

    public EditableMachineUI(String groupName, ResourceLocation uiPath, Supplier<WidgetGroup> widgetSupplier,
                             BiConsumer<WidgetGroup, MetaMachine> binder) {
        this.groupName = groupName;
        this.uiPath = uiPath;
        this.widgetSupplier = widgetSupplier;
        this.binder = binder;
    }

    public WidgetGroup createDefault() {
        return widgetSupplier.get();
    }

    public void setupUI(WidgetGroup template, MetaMachine machine) {
        binder.accept(template, machine);
    }

    //////////////////////////////////////
    // ******** GUI *********//
    //////////////////////////////////////

    @Nullable
    public WidgetGroup createCustomUI() {
        if (hasCustomUI()) {
            var nbt = getCustomUI();
            var group = new WidgetGroup();
            IConfigurableWidget.deserializeNBT(group, nbt.getCompound("root"),
                    Resources.fromNBT(nbt.getCompound("resources")), false, BTRegistries.builtinRegistry());
            group.setSelfPosition(new Position(0, 0));
            return group;
        }
        return null;
    }

    public CompoundTag getCustomUI() {
        if (this.customUICache == null) {
            ResourceManager resourceManager = null;
            if (BreaTechnology.isClientSide()) {
                resourceManager = Minecraft.getInstance().getResourceManager();
            } else if (BreaTechnology.getMinecraftServer() != null) {
                resourceManager = BreaTechnology.getMinecraftServer().getResourceManager();
            }
            if (resourceManager == null) {
                this.customUICache = new CompoundTag();
            } else {
                try {
                    var resource = resourceManager
                            .getResourceOrThrow(ResourceLocation.fromNamespaceAndPath(uiPath.getNamespace(),
                                    "ui/machine/%s.mui".formatted(uiPath.getPath())));
                    try (InputStream inputStream = resource.open()) {
                        try (DataInputStream dataInputStream = new DataInputStream(inputStream);) {
                            this.customUICache = NbtIo.read(dataInputStream, NbtAccounter.unlimitedHeap());
                        }
                    }
                } catch (Exception e) {
                    this.customUICache = new CompoundTag();
                }
                if (this.customUICache == null) {
                    this.customUICache = new CompoundTag();
                }
            }
        }
        return this.customUICache;
    }

    public boolean hasCustomUI() {
        return !getCustomUI().isEmpty();
    }

    public void reloadCustomUI() {
        this.customUICache = null;
    }
}

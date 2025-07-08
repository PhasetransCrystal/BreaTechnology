package net.phasetranscrystal.breatechnology.api.gui.factory;

import com.lowdragmc.lowdraglib.gui.factory.UIFactory;
import com.lowdragmc.lowdraglib.gui.modular.ModularUI;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.api.capability.BTCapabilityHelper;
import net.phasetranscrystal.breatechnology.api.cover.CoverBehavior;
import net.phasetranscrystal.breatechnology.api.cover.IUICover;

public class CoverUIFactory extends UIFactory<CoverBehavior> {

    public static final CoverUIFactory INSTANCE = new CoverUIFactory();

    public CoverUIFactory() {
        super(BreaTechnology.id("cover"));
    }

    @Override
    protected ModularUI createUITemplate(CoverBehavior holder, Player entityPlayer) {
        if (holder instanceof IUICover cover) {
            return cover.createUI(entityPlayer);
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    protected CoverBehavior readHolderFromSyncData(RegistryFriendlyByteBuf syncData) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return null;
        var pos = syncData.readBlockPos();
        var side = syncData.readEnum(Direction.class);
        var coverable = BTCapabilityHelper.getCoverable(world, pos, side);
        if (coverable != null) {
            return coverable.getCoverAtSide(side);
        }
        return null;
    }

    @Override
    protected void writeHolderToSyncData(RegistryFriendlyByteBuf syncData, CoverBehavior holder) {
        syncData.writeBlockPos(holder.coverHolder.getPos());
        syncData.writeEnum(holder.attachedSide);
    }
}

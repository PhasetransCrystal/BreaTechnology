package net.phasetranscrystal.breatechnology.api.renderer;

import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.lowdragmc.lowdraglib.client.renderer.ATESRRendererProvider;
import org.jetbrains.annotations.Nullable;

@OnlyIn(Dist.CLIENT)
public class BTRendererProvider extends ATESRRendererProvider<BlockEntity> {

    private static BTRendererProvider INSTANCE;

    private BTRendererProvider(BlockEntityRendererProvider.Context context) {
        // ModelBellows.INSTANCE = new ModelBellows(context);
        // ModelHungryChest.INSTANCE = new ModelHungryChest(context);
    }

    public static BTRendererProvider getOrCreate(BlockEntityRendererProvider.Context context) {
        if (INSTANCE == null) {
            INSTANCE = new BTRendererProvider(context);
        }
        return INSTANCE;
    }

    @Nullable
    public static BTRendererProvider getInstance() {
        return INSTANCE;
    }
}

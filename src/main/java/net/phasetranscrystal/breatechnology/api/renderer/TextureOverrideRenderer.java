package net.phasetranscrystal.breatechnology.api.renderer;

import com.lowdragmc.lowdraglib.client.model.ModelFactory;
import lombok.Getter;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.phasetranscrystal.breatechnology.BreaTechnology;
import net.phasetranscrystal.breatechnology.api.renderer.model.SpriteOverrider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Getter
public class TextureOverrideRenderer extends CTMModelRenderer {

    @NotNull
    protected Map<String, ResourceLocation> override;
    @Nullable
    protected Supplier<Map<String, ResourceLocation>> overrideSupplier;
    @OnlyIn(Dist.CLIENT)
    protected Map<ModelState, BakedModel> bakedModelCache;

    public TextureOverrideRenderer(ResourceLocation model, @NotNull Map<String, ResourceLocation> override) {
        super(model);
        this.override = override;
        if (BreaTechnology.isClientSide()) {
            registerEvent();
        }
    }

    

    public TextureOverrideRenderer(ResourceLocation model,
                                   @NotNull Supplier<Map<String, ResourceLocation>> overrideSupplier) {
        super(model);
        this.override = Collections.emptyMap();
        this.overrideSupplier = overrideSupplier;
        if (BreaTechnology.isClientSide()) {
            registerEvent();
        }
    }

    public TextureOverrideRenderer(ResourceLocation model) {
        super(model);
        this.override = Collections.emptyMap();
        if (BreaTechnology.isClientSide()) {
            registerEvent();
        }
    }

    @Override
    public void initRenderer() {
        if (BreaTechnology.isClientSide()) {
            this.bakedModelCache = new ConcurrentHashMap<>();
        }
        super.initRenderer();
    }

    public void setTextureOverride(Map<String, ResourceLocation> override) {
        this.override = override;
    }

    @Nullable
    @OnlyIn(Dist.CLIENT)
    protected BakedModel getItemBakedModel() {
        if (itemModel == null) {
            var model = getModel();
            if (model instanceof BlockModel blockModel && blockModel.getRootModel() == ModelBakery.GENERATION_MARKER) {
                // fabric doesn't help us to fix vanilla bakery, so we have to do it ourselves
                model = ModelFactory.ITEM_MODEL_GENERATOR.generateBlockModel(new SpriteOverrider(override), blockModel);
            }
            itemModel = model.bake(
                    ModelFactory.getModelBaker(),
                    new SpriteOverrider(override),
                    BlockModelRotation.X0_Y0);
        }
        return itemModel;
    }

    @SuppressWarnings("removal")
    @Override
    @OnlyIn(Dist.CLIENT)
    public BakedModel getRotatedModel(Direction frontFacing) {
        return blockModels.computeIfAbsent(frontFacing, facing -> getModel().bake(
                ModelFactory.getModelBaker(),
                new SpriteOverrider(override),
                ModelFactory.getRotation(facing)));
    }

    @OnlyIn(Dist.CLIENT)
    public BakedModel getRotatedModel(ModelState modelState) {
        return bakedModelCache.computeIfAbsent(modelState, state -> getModel().bake(
                ModelFactory.getModelBaker(),
                new SpriteOverrider(override),
                modelState));
    }

    @SuppressWarnings("deprecation")
    @Override
    @OnlyIn(Dist.CLIENT)
    public void onPrepareTextureAtlas(ResourceLocation atlasName, Consumer<ResourceLocation> register) {
        super.onPrepareTextureAtlas(atlasName, register);
        if (atlasName.equals(TextureAtlas.LOCATION_BLOCKS)) { // prepare for override.
            if (bakedModelCache != null) {
                bakedModelCache.clear();
            }
            if (overrideSupplier != null) override = overrideSupplier.get();
            for (ResourceLocation value : override.values()) {
                register.accept(value);
            }
        }
    }

    @Override
    public void updateModelWithoutReloadingResource(ResourceLocation modelLocation) {
        super.updateModelWithoutReloadingResource(modelLocation);
        if (BreaTechnology.isClientSide()) {
            if (bakedModelCache != null) {
                bakedModelCache.clear();
            }
        }
    }
}

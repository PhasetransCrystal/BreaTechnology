package com.tterrag.registrate.providers;


import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;

public class RegistrateItemModelProvider extends ItemModelProvider implements RegistrateProvider {
    private final AbstractRegistrate<?> parent;

    public RegistrateItemModelProvider(AbstractRegistrate<?> parent, PackOutput packOutput, ExistingFileHelper existingFileHelper) {
        super(packOutput, parent.getModid(), existingFileHelper);
        this.parent = parent;
    }

    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    protected void registerModels() {
        this.parent.genData(ProviderType.ITEM_MODEL, this);
    }

    public String getName() {
        return "Item models";
    }

    public String modid(NonNullSupplier<? extends ItemLike> item) {
        return ForgeRegistries.ITEMS.getKey(((ItemLike)item.get()).asItem()).getNamespace();
    }

    public String name(NonNullSupplier<? extends ItemLike> item) {
        return ForgeRegistries.ITEMS.getKey(((ItemLike)item.get()).asItem()).getPath();
    }

    public ResourceLocation itemTexture(NonNullSupplier<? extends ItemLike> item) {
        String var10001 = this.name(item);
        return this.modLoc("item/" + var10001);
    }

    public ItemModelBuilder blockItem(NonNullSupplier<? extends ItemLike> block) {
        return this.blockItem(block, "");
    }

    public ItemModelBuilder blockItem(NonNullSupplier<? extends ItemLike> block, String suffix) {
        String var10001 = this.name(block);
        String var10004 = this.modid(block);
        String var10005 = this.name(block);
        return (ItemModelBuilder)this.withExistingParent(var10001, ResourceLocation.fromNamespaceAndPath(var10004, "block/" + var10005 + suffix));
    }

    public ItemModelBuilder blockWithInventoryModel(NonNullSupplier<? extends ItemLike> block) {
        String var10001 = this.name(block);
        String var10004 = this.modid(block);
        String var10005 = this.name(block);
        return (ItemModelBuilder)this.withExistingParent(var10001, ResourceLocation.fromNamespaceAndPath(var10004, "block/" + var10005 + "_inventory"));
    }

    public ItemModelBuilder blockSprite(NonNullSupplier<? extends ItemLike> block) {
        String var10003 = this.name(block);
        return this.blockSprite(block, this.modLoc("block/" + var10003));
    }

    public ItemModelBuilder blockSprite(NonNullSupplier<? extends ItemLike> block, ResourceLocation texture) {
        return this.generated(() -> ((ItemLike)block.get()).asItem(), texture);
    }

    public ItemModelBuilder generated(NonNullSupplier<? extends ItemLike> item) {
        return this.generated(item, this.itemTexture(item));
    }

    public ItemModelBuilder generated(NonNullSupplier<? extends ItemLike> item, ResourceLocation... layers) {
        ItemModelBuilder ret = (ItemModelBuilder)((ItemModelBuilder)this.getBuilder(this.name(item))).parent(new ModelFile.UncheckedModelFile("item/generated"));

        for(int i = 0; i < layers.length; ++i) {
            ret = (ItemModelBuilder)ret.texture("layer" + i, layers[i]);
        }

        return ret;
    }

    public ItemModelBuilder handheld(NonNullSupplier<? extends ItemLike> item) {
        return this.handheld(item, this.itemTexture(item));
    }

    public ItemModelBuilder handheld(NonNullSupplier<? extends ItemLike> item, ResourceLocation texture) {
        return (ItemModelBuilder)((ItemModelBuilder)this.withExistingParent(this.name(item), "item/handheld")).texture("layer0", texture);
    }
}

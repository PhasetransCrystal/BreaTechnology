package net.phasetranscrystal.breatechnology.api.registry.registate.builders;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.phasetranscrystal.breatechnology.api.material.instance.*;
import net.phasetranscrystal.breatechnology.api.material.tag.MaterialTagInfo;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;
import net.phasetranscrystal.breatechnology.api.registry.BTRegistries;
import net.phasetranscrystal.breatechnology.api.registry.registate.BTClientFluidTypeExtensions;

import com.ibm.icu.impl.Pair;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonnullType;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

@Accessors(chain = true, fluent = true)
public class MaterialBuilder<MATERIAL extends MetaMaterial<?>, P> extends AbstractBuilder<MetaMaterial<?>, MATERIAL, P, MaterialBuilder<MATERIAL, P>> {

    public static <MATERIAL extends MetaMaterial<?>, P> MaterialBuilder<MATERIAL, P> create(AbstractRegistrate<?> owner, P parent, String name,
                                                                                            BuilderCallback callback,
                                                                                            BiFunction<AbstractRegistrate<?>, ResourceLocation, MATERIAL> materialFactory) {
        return new MaterialBuilder<>(owner, parent, name, callback, materialFactory);
    }

    public MaterialBuilder(AbstractRegistrate<?> owner, P parent, String name,
                           BuilderCallback callback,
                           BiFunction<AbstractRegistrate<?>, ResourceLocation, MATERIAL> materialFactory) {
        super(owner, parent, name, callback, BTRegistries.MATERIALS);
        this.materialFactory = materialFactory;
    }

    private final BiFunction<AbstractRegistrate<?>, ResourceLocation, MATERIAL> materialFactory;

    private final Set<MaterialTagInfo> types = new HashSet<>();

    private final Map<MaterialTagInfo, TriFunction<MATERIAL, MaterialTagInfo, Item.Properties, MetaMaterialItem>> instanceItems = new HashMap<>();

    public MaterialBuilder<MATERIAL, P> addInstanceItem(MaterialTagInfo... instanceTypes) {
        return addInstanceItem(MetaMaterialItem::new, instanceTypes);
    }

    public MaterialBuilder<MATERIAL, P> addInstanceItem(TriFunction<MATERIAL, MaterialTagInfo, Item.Properties, MetaMaterialItem> factory, MaterialTagInfo... instanceTypes) {
        Arrays.asList(instanceTypes).forEach(type -> {
            if (types.contains(type)) {
                throw new IllegalArgumentException("Instance type " + type + " already exists");
            }
            if (!type.generateItem() | type.generateBlock()) {
                return;
                // throw new IllegalArgumentException("Instance type " + type + " cannot generate both item and block");
            }
            types.add(type);
            instanceItems.put(type, factory);
        });
        return this;
    }

    private final Map<MaterialTagInfo, Pair<TriFunction<MATERIAL, MaterialTagInfo, BlockBehaviour.Properties, MetaMaterialBlock>, MaterialBlockItem.MaterialBlockItemFactory>> instanceBlocks = new HashMap<>();

    public MaterialBuilder<MATERIAL, P> addInstanceBlock(MaterialTagInfo... instanceTypes) {
        return addInstanceBlock(MetaMaterialBlock::new, MaterialBlockItem::new, instanceTypes);
    }

    public MaterialBuilder<MATERIAL, P> addInstanceBlock(TriFunction<MATERIAL, MaterialTagInfo, BlockBehaviour.Properties, MetaMaterialBlock> blockFactory, MaterialTagInfo... instanceTypes) {
        return addInstanceBlock(blockFactory, MaterialBlockItem::new, instanceTypes);
    }

    public MaterialBuilder<MATERIAL, P> addInstanceBlock(TriFunction<MATERIAL, MaterialTagInfo, BlockBehaviour.Properties, MetaMaterialBlock> blockFactory, MaterialBlockItem.MaterialBlockItemFactory blockItemFactory, MaterialTagInfo... instanceTypes) {
        Arrays.asList(instanceTypes).forEach(type -> {
            if (types.contains(type)) {
                throw new IllegalArgumentException("Instance type " + type + " already exists");
            }
            if (!type.generateBlock()) {
                return;
                // throw new IllegalArgumentException("Instance type " + type + " cannot generate block");
            }
            types.add(type);
            instanceBlocks.put(type, Pair.of(blockFactory, blockItemFactory));
        });
        return this;
    }

    private record MaterialFluidFactoryEntry(MaterialFluid.MaterialFluidFactory flowing,
                                             MaterialBucketItem.MaterialBucketFactory bucket) {}

    private final Map<MaterialTagInfo, MaterialFluidFactoryEntry> instanceFluids = new HashMap<>();

    public MaterialBuilder<MATERIAL, P> addInstanceFluid(MaterialTagInfo... instanceTypes) {
        return addInstanceFluid(MaterialFluid.Flowing::new,
                MaterialBucketItem::new,
                instanceTypes);
    }

    public MaterialBuilder<MATERIAL, P> addInstanceFluid(MaterialFluid.MaterialFluidFactory flowing,
                                                         MaterialTagInfo... instanceTypes) {
        return addInstanceFluid(flowing, MaterialBucketItem::new, instanceTypes);
    }

    public MaterialBuilder<MATERIAL, P> addInstanceFluid(MaterialFluid.MaterialFluidFactory flowing,
                                                         MaterialBucketItem.MaterialBucketFactory bucket,
                                                         MaterialTagInfo... instanceTypes) {
        Arrays.asList(instanceTypes).forEach(type -> {
            if (types.contains(type)) {
                throw new IllegalArgumentException("Instance type " + type + " already exists");
            }
            if (!type.generateFluid()) {
                return;
                // throw new IllegalArgumentException("Instance type " + type + " cannot generate fluid");
            }
            types.add(type);
            instanceFluids.put(type, new MaterialFluidFactoryEntry(flowing, bucket));
        });
        return this;
    }

    @Getter
    @Setter
    private int color = 0xFFFFFFFF;
    @Getter
    @Setter
    private int secondaryColor = 0xFFFFFFFF;

    private MATERIAL material;

    @Override
    protected @NonnullType MATERIAL createEntry() {
        if (material == null) {
            material = materialFactory.apply(getOwner(), ResourceLocation.fromNamespaceAndPath(getOwner().getModid(), getName()));
        }
        return material;
    }

    @Override
    public @NotNull RegistryEntry<MetaMaterial<?>, MATERIAL> register() {
        var definiton = createEntry();

        definiton.setColor(color);
        definiton.setSecondaryColor(secondaryColor);

        MetaMaterial.setInstance(definiton);

        instanceItems.forEach((type, factory) -> {
            getOwner().item(this, type.idPattern().formatted(getName()), prop -> factory.apply(definiton, type, type.generateItemProperties(prop))).register();
        });
        instanceBlocks.forEach((type, pair) -> {
            var bf = pair.first;
            var bif = pair.second;
            getOwner().block(this, type.idPattern().formatted(getName()), prop -> bf.apply(definiton, type, prop))
                    .item((block, prop) -> bif.create(definiton, type, block, prop)).build()
                    .register();
        });
        instanceFluids.forEach((type, entry) -> {
            var name = type.idPattern().formatted(getName());
            if (type.generateMeltFluid()) {
                getOwner().entry(name, callback -> FluidBuilder.create(getOwner(), this, type.idPattern().formatted(getName()),
                        callback, BTClientFluidTypeExtensions.MELT_STILL, BTClientFluidTypeExtensions.MELT_FLOWING, definiton.getSecondaryColor(),
                        (FluidBuilder.ClientExtensionFactory) BTClientFluidTypeExtensions.Melt::new)).register();
            } else if (type.generateLiquidFluid()) {
                getOwner().entry(name, callback -> FluidBuilder.create(getOwner(), this, type.idPattern().formatted(getName()),
                        callback, BTClientFluidTypeExtensions.LIQUID_STILL, BTClientFluidTypeExtensions.LIQUID_FLOWING, definiton.getColor(),
                        (FluidBuilder.ClientExtensionFactory) BTClientFluidTypeExtensions.Liquid::new)).register();
            }
        });

        MetaMaterial.clearInstance();

        return super.register();
    }
}

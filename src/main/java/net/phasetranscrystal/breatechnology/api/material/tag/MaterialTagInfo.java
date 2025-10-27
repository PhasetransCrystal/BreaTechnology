package net.phasetranscrystal.breatechnology.api.material.tag;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.phasetranscrystal.breatechnology.api.material.type.MetaMaterial;
import net.phasetranscrystal.breatechnology.api.utils.FormattingUtil;
import net.phasetranscrystal.breatechnology.api.utils.MemoizedSupplier;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

@SuppressWarnings("unused")
@Accessors(chain = true, fluent = true)
public class MaterialTagInfo {

    protected static final Map<String, MaterialTagInfo> TagInfoMap = new HashMap<>();

    public static MaterialTagInfo get(String name) {
        return TagInfoMap.get(name);
    }

    public static final MaterialTagInfo EMPTY = new MaterialTagInfo("null");

    public boolean isEmpty() {
        return this == EMPTY;
    }

    /// 名称
    @Getter
    private final String name;
    /// ID格式化字符串
    @Getter
    @Setter
    private String idPattern;
    /// Lang格式化字符串
    @Setter
    @Getter
    public String langValue;
    @Nullable
    @Getter
    @Setter
    private BiConsumer<MetaMaterial, List<Component>> tooltip;
    /// 分量，-1表示无该属性
    @Getter
    @Setter
    private long materialAmount = -1;
    /// 最大堆叠数量
    @Getter
    @Setter
    private int maxStackSize = 64;

    public MaterialTagInfo(String name) {
        this.name = name;
        String lowerCaseUnder = FormattingUtil.toLowerCaseUnder(name);
        this.idPattern = "%s_" + lowerCaseUnder;
        this.langValue = "%s " + FormattingUtil.toEnglishName(lowerCaseUnder);
        TagInfoMap.put(name, this);
    }

    @Setter
    @Getter
    private boolean generateItem;
    @Setter
    private UnaryOperator<Item.Properties> itemProperties = UnaryOperator.identity();

    public Item.Properties generateItemProperties(Item.Properties properties) {
        return itemProperties.apply(properties);
    }

    @Setter
    @Getter
    private boolean generateBlock;

    public record BlockProperties(Supplier<Supplier<RenderType>> renderType,
                                  UnaryOperator<BlockBehaviour.Properties> properties) {}

    @Getter
    @Setter
    private BlockProperties blockProperties = new BlockProperties(() -> RenderType::translucent, UnaryOperator.identity());
    @Setter
    @Getter
    private boolean generateLiquidFluid;
    @Setter
    @Getter
    private boolean generateMeltFluid;

    public boolean generateFluid() {
        return generateLiquidFluid || generateMeltFluid;
    }

    /// 注册时忽略的物品
    private final Map<MetaMaterial<?>, Supplier<? extends ItemLike>[]> ignoredMaterials = new HashMap<>();

    public boolean isIgnored(MetaMaterial<?> material) {
        return ignoredMaterials.containsKey(material);
    }

    @SafeVarargs
    public final void setIgnored(MetaMaterial<?> material, Supplier<? extends ItemLike>... items) {
        ignoredMaterials.put(material, items);
        if (items.length > 0) {

        }
    }

    @SuppressWarnings("unchecked")
    public void setIgnored(MetaMaterial<?> material, ItemLike... items) {
        // go through setIgnoredBlock to wrap if this is a block prefix
        if (this.generateBlock()) {
            this.setIgnoredBlock(material,
                    Arrays.stream(items).filter(Block.class::isInstance).map(Block.class::cast).toArray(Block[]::new));
        } else {
            this.setIgnored(material,
                    Arrays.stream(items).map(item -> (Supplier<ItemLike>) () -> item).toArray(Supplier[]::new));
        }
    }

    @SuppressWarnings("unchecked")
    public void setIgnoredBlock(MetaMaterial<?> material, Block... items) {
        this.setIgnored(material, Arrays.stream(items).map(block -> new MemoizedSupplier<>(() -> block))
                .toArray(Supplier[]::new));
    }

    @SuppressWarnings("unchecked")
    public void setIgnored(MetaMaterial<?> material) {
        this.ignoredMaterials.put(material, new Supplier[0]);
    }

    public void removeIgnored(MetaMaterial<?> material) {
        ignoredMaterials.remove(material);
    }

    public Map<MetaMaterial<?>, Supplier<? extends ItemLike>[]> getIgnored() {
        return new HashMap<>(ignoredMaterials);
    }
}

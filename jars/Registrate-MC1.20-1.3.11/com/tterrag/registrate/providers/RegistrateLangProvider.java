package com.tterrag.registrate.providers;


import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.PackOutput;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.data.LanguageProvider;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import com.tterrag.registrate.util.nullness.NonnullType;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class RegistrateLangProvider extends LanguageProvider implements RegistrateProvider {
    private final AbstractRegistrate<?> owner;
    private final AccessibleLanguageProvider upsideDown;
    private static final String NORMAL_CHARS = "abcdefghijklmnñopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_,;.?!/\\'";
    private static final String UPSIDE_DOWN_CHARS = "ɐqɔpǝɟbɥıظʞןɯuuodbɹsʇnʌʍxʎzⱯᗺƆᗡƎℲ⅁HIſʞꞀWNOԀὉᴚS⟘∩ΛMXʎZ0ƖᄅƐㄣϛ9ㄥ86‾'؛˙¿¡/\\,";

    public RegistrateLangProvider(AbstractRegistrate<?> owner, PackOutput packOutput) {
        super(packOutput, owner.getModid(), "en_us");
        this.owner = owner;
        this.upsideDown = new AccessibleLanguageProvider(packOutput, owner.getModid(), "en_ud");
    }

    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    public String getName() {
        return "Lang (en_us/en_ud)";
    }

    protected void addTranslations() {
        this.owner.genData(ProviderType.LANG, this);
    }

    public static final String toEnglishName(String internalName) {
        return (String) Arrays.stream(internalName.toLowerCase(Locale.ROOT).split("_")).map(StringUtils::capitalize).collect(Collectors.joining(" "));
    }

    public <T> String getAutomaticName(NonNullSupplier<? extends T> sup, ResourceKey<Registry<T>> registry) {
        return toEnglishName(((Registry) BuiltInRegistries.REGISTRY.get(registry)).getKey(sup.get()).getPath());
    }

    public void addBlock(NonNullSupplier<? extends Block> block) {
        this.addBlock(block, this.getAutomaticName(block, Registries.BLOCK));
    }

    public void addBlockWithTooltip(NonNullSupplier<? extends Block> block, String tooltip) {
        this.addBlock(block);
        this.addTooltip(block, tooltip);
    }

    public void addBlockWithTooltip(NonNullSupplier<? extends Block> block, String name, String tooltip) {
        this.addBlock(block, name);
        this.addTooltip(block, tooltip);
    }

    public void addItem(NonNullSupplier<? extends Item> item) {
        this.addItem(item, this.getAutomaticName(item, Registries.ITEM));
    }

    public void addItemWithTooltip(NonNullSupplier<? extends Item> block, String name, List<@NonnullType String> tooltip) {
        this.addItem(block, name);
        this.addTooltip(block, tooltip);
    }

    public void addTooltip(NonNullSupplier<? extends ItemLike> item, String tooltip) {
        this.add(((ItemLike)item.get()).asItem().getDescriptionId() + ".desc", tooltip);
    }

    public void addTooltip(NonNullSupplier<? extends ItemLike> item, List<@NonnullType String> tooltip) {
        for(int i = 0; i < tooltip.size(); ++i) {
            this.add(((ItemLike)item.get()).asItem().getDescriptionId() + ".desc." + i, (String)tooltip.get(i));
        }

    }

    public void add(CreativeModeTab tab, String name) {
        ComponentContents contents = tab.getDisplayName().getContents();
        if (contents instanceof TranslatableContents lang) {
            this.add(lang.getKey(), name);
        } else {
            throw new IllegalArgumentException("Creative tab does not have a translatable name: " + tab.getDisplayName());
        }
    }

    public void addEntityType(NonNullSupplier<? extends EntityType<?>> entity) {
        this.addEntityType(entity, this.getAutomaticName(entity, Registries.ENTITY_TYPE));
    }

    private String toUpsideDown(String normal) {
        char[] ud = new char[normal.length()];

        for(int i = 0; i < normal.length(); ++i) {
            char c = normal.charAt(i);
            if (c != '%') {
                int lookup = "abcdefghijklmnñopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_,;.?!/\\'".indexOf(c);
                if (lookup >= 0) {
                    c = "ɐqɔpǝɟbɥıظʞןɯuuodbɹsʇnʌʍxʎzⱯᗺƆᗡƎℲ⅁HIſʞꞀWNOԀὉᴚS⟘∩ΛMXʎZ0ƖᄅƐㄣϛ9ㄥ86‾'؛˙¿¡/\\,".charAt(lookup);
                }

                ud[normal.length() - 1 - i] = c;
            } else {
                String fmtArg;
                for(fmtArg = ""; Character.isDigit(c) || c == '%' || c == '$' || c == 's' || c == 'd'; c = i == normal.length() ? 0 : normal.charAt(i)) {
                    fmtArg = fmtArg + c;
                    ++i;
                }

                --i;

                for(int j = 0; j < fmtArg.length(); ++j) {
                    ud[normal.length() - 1 - i + j] = fmtArg.charAt(j);
                }
            }
        }

        return new String(ud);
    }

    public void add(String key, String value) {
        super.add(key, value);
        this.upsideDown.add(key, this.toUpsideDown(value));
    }

    public CompletableFuture<?> run(CachedOutput cache) {
        return CompletableFuture.allOf(super.run(cache), this.upsideDown.run(cache));
    }

    static {
        if ("abcdefghijklmnñopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_,;.?!/\\'".length() != "ɐqɔpǝɟbɥıظʞןɯuuodbɹsʇnʌʍxʎzⱯᗺƆᗡƎℲ⅁HIſʞꞀWNOԀὉᴚS⟘∩ΛMXʎZ0ƖᄅƐㄣϛ9ㄥ86‾'؛˙¿¡/\\,".length()) {
            throw new AssertionError("Char maps do not match in length!");
        }
    }

    private static class AccessibleLanguageProvider extends LanguageProvider {
        public AccessibleLanguageProvider(PackOutput packOutput, String modid, String locale) {
            super(packOutput, modid, locale);
        }

        public void add(@Nullable String key, @Nullable String value) {
            super.add(key, value);
        }

        protected void addTranslations() {
        }
    }
}

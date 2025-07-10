package net.phasetranscrystal.breatechnology.api.recipe;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.phasetranscrystal.breatechnology.api.recipe.kind.BTRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class BTRecipeType implements RecipeType<BTRecipe> {
    @Setter
    @Getter
    @Nullable
    private Supplier<ItemStack> iconSupplier;
}

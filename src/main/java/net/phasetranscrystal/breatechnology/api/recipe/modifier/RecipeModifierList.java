package net.phasetranscrystal.breatechnology.api.recipe.modifier;

/**
 * Represents a list of RecipeModifiers that should be applied in order
 */
public final class RecipeModifierList implements RecipeModifier {

    private final RecipeModifier[] modifiers;

    public RecipeModifierList(RecipeModifier... modifiers) {
        this.modifiers = modifiers;
    }
}

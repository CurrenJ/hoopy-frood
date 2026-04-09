package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.recipe.NoRemainderShapelessRecipe;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeUnlockAdvancementBuilder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

/**
 * Builder for {@link NoRemainderShapelessRecipe} — a shapeless recipe that fully
 * consumes all inputs without returning crafting remainders (e.g. empty buckets).
 */
public class NoRemainderShapelessRecipeBuilder implements RecipeBuilder {
    private final HolderGetter<Item> items;
    private final RecipeCategory category;
    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients = new ArrayList<>();
    private final RecipeUnlockAdvancementBuilder advancementBuilder = new RecipeUnlockAdvancementBuilder();
    private @Nullable String group;

    private NoRemainderShapelessRecipeBuilder(HolderGetter<Item> items, RecipeCategory category, ItemStackTemplate result) {
        this.items = items;
        this.category = category;
        this.result = result;
    }

    public static NoRemainderShapelessRecipeBuilder shapeless(HolderGetter<Item> items, RecipeCategory category, ItemLike item) {
        return shapeless(items, category, item, 1);
    }

    public static NoRemainderShapelessRecipeBuilder shapeless(HolderGetter<Item> items, RecipeCategory category, ItemLike item, int count) {
        return new NoRemainderShapelessRecipeBuilder(items, category, new ItemStackTemplate(item.asItem(), count));
    }

    public NoRemainderShapelessRecipeBuilder requires(ItemLike item) {
        return requires(item, 1);
    }

    public NoRemainderShapelessRecipeBuilder requires(ItemLike item, int count) {
        for (int i = 0; i < count; i++) {
            this.ingredients.add(Ingredient.of(item));
        }
        return this;
    }

    public NoRemainderShapelessRecipeBuilder requires(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        return this;
    }

    @Override
    public NoRemainderShapelessRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.advancementBuilder.unlockedBy(name, criterion);
        return this;
    }

    @Override
    public NoRemainderShapelessRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public ResourceKey<Recipe<?>> defaultId() {
        return RecipeBuilder.getDefaultRecipeId(this.result);
    }

    @Override
    public void save(RecipeOutput output, ResourceKey<Recipe<?>> id) {
        NoRemainderShapelessRecipe recipe = new NoRemainderShapelessRecipe(
            RecipeBuilder.createCraftingCommonInfo(true),
            RecipeBuilder.createCraftingBookInfo(this.category, this.group),
            this.result,
            this.ingredients
        );
        output.accept(id, recipe, this.advancementBuilder.build(output, id, this.category));
    }
}

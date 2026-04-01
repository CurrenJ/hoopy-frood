package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;

import java.util.concurrent.CompletableFuture;

public class RecipeProvider extends VanillaRecipeProvider {
    protected RecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @Override
    protected void buildRecipes() {
        HolderGetter<Item> items = this.registries.lookupOrThrow(Registries.ITEM);

        ShapedRecipeBuilder recipeBuilder = ShapedRecipeBuilder.shaped(items, RecipeCategory.BUILDING_BLOCKS, HoopyFroodTutBlocks.BROWN_BRICKS.get())
                .pattern("##")
                .pattern("##")
                .define('#', HoopyFroodItems.BROWN_BRICK.get())
                .unlockedBy("has_brown_brick", has(HoopyFroodItems.BROWN_BRICK.get()));
        ResourceKey<Recipe<?>> recipeKey = recipeBuilder.defaultId();
        recipeBuilder.save(this.output, recipeKey);

        generateRecipes(HoopyFroodTutBlocks.BROWN_BRICKS_FAMILY.get(), FeatureFlagSet.of());

        // Disposable Caterpillar: surround a dirt block with 4 slime balls
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get())
                .pattern(" S ")
                .pattern("SDS")
                .pattern(" S ")
                .define('S', Items.SLIME_BALL)
                .define('D', Items.DIRT)
                .unlockedBy("has_slime_ball", has(Items.SLIME_BALL))
                .save(this.output);
    }

    public static class Runner extends VanillaRecipeProvider.Runner {
        public Runner(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
            super(packOutput, registries);
        }

        protected net.minecraft.data.recipes.RecipeProvider createRecipeProvider(HolderLookup.Provider registries, RecipeOutput output) {
            return new RecipeProvider(registries, output);
        }

        public String getName() {
            return "Hoopy Frood Recipes";
        }
    }
}

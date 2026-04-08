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
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
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
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get(), 16)
                .requires(Items.PISTON)
                .requires(Items.REDSTONE)
                .requires(Items.IRON_PICKAXE)
                .unlockedBy("has_iron_pickaxe", has(Items.IRON_PICKAXE))
                .save(this.output);

        // Begging Item Scrabbler: surround a chest with 4 iron nuggets in cardinal positions
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get())
                .pattern(" N ")
                .pattern("NCN")
                .pattern(" N ")
                .define('N', Items.IRON_NUGGET)
                .define('C', Items.CHEST)
                .unlockedBy("has_iron_nugget", has(Items.IRON_NUGGET))
                .save(this.output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, HoopyFroodTutBlocks.INFINITE_IMPROBABILITY_DRIVE.get())
                .pattern("RIR")
                .pattern("IDI")
                .pattern("RIR")
                .define('R', Items.REDSTONE)
                .define('I', Items.IRON_INGOT)
                .define('D', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(this.output);

        // Somebody Else's Problem Field: eye of ender surrounded by 8 glass panes
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, HoopyFroodTutBlocks.SOMEBODY_ELSES_PROBLEM_FIELD.get())
                .pattern("GGG")
                .pattern("GEG")
                .pattern("GGG")
                .define('G', Items.GLASS)
                .define('E', Items.ENDER_EYE)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(this.output);

        // Banishing Bin: Eye of Ender at centre, Blaze Rod below it, Iron Bars + Copper + Obsidian on sides
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, HoopyFroodTutBlocks.BANISHING_BIN.get())
                .pattern("ICI")
                .pattern("OEO")
                .pattern("IBI")
                .define('I', Items.IRON_BARS)
                .define('C', Items.COPPER_INGOT)
                .define('O', Items.OBSIDIAN)
                .define('E', Items.ENDER_EYE)
                .define('B', Items.BLAZE_ROD)
                .unlockedBy("has_ender_eye", has(Items.ENDER_EYE))
                .save(this.output);

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, HoopyFroodItems.PERIL_SENSITIVE_SUNGLASSES.get())
                .pattern("GGG")
                .pattern("G G")
                .pattern("   ")
                .define('G', Items.TINTED_GLASS)
                .unlockedBy("has_glass", has(Items.GLASS))
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

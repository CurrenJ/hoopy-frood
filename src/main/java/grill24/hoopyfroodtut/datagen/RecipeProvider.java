package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.HoopyFroodTut;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import grill24.hoopyfroodtut.recipe.CaterpillarAddCountRecipe;
import grill24.hoopyfroodtut.recipe.CaterpillarCombineRecipe;
import grill24.hoopyfroodtut.recipe.CaterpillarSetFlagRecipe;
import grill24.hoopyfroodtut.recipe.SuperEnchantedBookRecipe;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.packs.VanillaRecipeProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
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

        // Pulse Latch: comparator centre, iron ingots in corners, redstone on cardinal sides
        ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.PULSE_LATCH.get())
                .pattern("RSR")
                .pattern("SPS")
                .define('S', Items.STONE)
                .define('P', Items.STICKY_PISTON)
                .define('R', Items.REPEATER)
                .unlockedBy("has_repeater", has(Items.REPEATER))
                .save(this.output);

        // Sluggish Pulse Latch: craft a Pulse Latch with Soul Sand
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get())
                .requires(HoopyFroodTutBlocks.PULSE_LATCH)
                .requires(Items.SOUL_SAND)
                .unlockedBy("has_pulse_latch", has(HoopyFroodTutBlocks.PULSE_LATCH.get()))
                .save(this.output);

        // Release Latch: Pulse Latch + Redstone Torch (invert the trigger edge)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.RELEASE_LATCH.get())
                .requires(HoopyFroodTutBlocks.PULSE_LATCH)
                .requires(Items.REDSTONE_TORCH)
                .unlockedBy("has_pulse_latch", has(HoopyFroodTutBlocks.PULSE_LATCH.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "release_latch")));

        // Sluggish Release Latch: Release Latch + Soul Sand
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH.get())
                .requires(HoopyFroodTutBlocks.RELEASE_LATCH)
                .requires(Items.SOUL_SAND)
                .unlockedBy("has_release_latch", has(HoopyFroodTutBlocks.RELEASE_LATCH.get()))
                .save(this.output);

        // Redstone Clock: clock item centre, comparator below, iron ingots corners, redstone sides
        ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.REDSTONE_CLOCK.get())
                .pattern("DS")
                .pattern("SR")
                .pattern("CD")
                .define('S', Items.STONE)
                .define('R', Items.REPEATER)
                .define('D', Items.REDSTONE)
                .define('C', Items.COMPARATOR)
                .unlockedBy("has_comparator", has(Items.COMPARATOR))
                .save(this.output);

        // Proximity Sensor: comparator centre, ender pearls on sides, iron ingots corners, redstone
        ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.PROXIMITY_SENSOR.get())
                .pattern("IRI")
                .pattern("ESE")
                .pattern("IRI")
                .define('I', Items.SCULK)
                .define('R', Items.REDSTONE)
                .define('S', Items.SCULK_SENSOR)
                .define('E', Items.ENDER_PEARL)
                .unlockedBy("has_comparator", has(Items.COMPARATOR))
                .save(this.output);

        // Proximity Sensor: comparator centre, ender pearls on sides, iron ingots corners, redstone
        ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.INVERTER.get())
                .pattern("DST")
                .pattern("SSS")
                .define('T', Items.REDSTONE_TORCH)
                .define('D', Items.REDSTONE)
                .define('S', Items.STONE)
                .unlockedBy("has_redstone_torch", has(Items.REDSTONE_TORCH))
                .save(this.output);

        // Sluggish Redstone Clock: craft a Redstone Clock with Soul Sand
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK.get())
                .requires(HoopyFroodTutBlocks.REDSTONE_CLOCK)
                .requires(Items.SOUL_SAND)
                .unlockedBy("has_redstone_clock", has(HoopyFroodTutBlocks.REDSTONE_CLOCK.get()))
                .save(this.output);

        // Ejector: dispenser wrapped in redstone dust, with an observer and comparator
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, HoopyFroodTutBlocks.EJECTOR.get())
                .requires(Blocks.DISPENSER)
                .requires(HoopyFroodTutBlocks.REDSTONE_CLOCK)
                .unlockedBy("has_dispenser", has(Items.DISPENSER))
                .save(this.output);

        // Expeller: dropper + redstone clock
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, HoopyFroodTutBlocks.EXPELLER.get())
                .requires(Blocks.DROPPER)
                .requires(HoopyFroodTutBlocks.REDSTONE_CLOCK)
                .unlockedBy("has_dropper", has(Items.DROPPER))
                .save(this.output);

        // Balancer Node: two hoppers, redstone dust, and surrounded by iron ingots
        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, HoopyFroodTutBlocks.BALANCER_NODE.get())
                .pattern("III")
                .pattern("HRH")
                .pattern("III")
                .define('I', Items.IRON_INGOT)
                .define('H', Items.HOPPER)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(this.output);

        // Balancer Range Extender: two hoppers, redstone dust, 1 gold block
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, HoopyFroodItems.BALANCER_RANGE_EXTENDER.get())
                .requires(Items.HOPPER)
                .requires(Items.HOPPER)
                .requires(Items.REDSTONE)
                .requires(Items.GOLD_BLOCK)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(this.output);

        // Disposable Caterpillar — one recipe per pickaxe tier.
        // Higher-tier pickaxes yield more caterpillars and/or more charges per caterpillar.
        // Baseline: iron → 64 caterpillars × 1 charge = 64 total mining ops.
        addCaterpillarRecipe(items, Items.WOODEN_PICKAXE,    "wooden",    32,  1);
        addCaterpillarRecipe(items, Items.STONE_PICKAXE,     "stone",    64,  1);
        addCaterpillarRecipe(items, Items.IRON_PICKAXE,      "iron",     64,  4);
        addCaterpillarRecipe(items, Items.GOLDEN_PICKAXE,    "golden",   64,  8);
        addCaterpillarRecipe(items, Items.DIAMOND_PICKAXE,   "diamond",  64,  16);
        addCaterpillarRecipe(items, Items.NETHERITE_PICKAXE, "netherite", 64, 32);

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

        // Wobbly Water Bucket: 4 water buckets fully consumed (no empty bucket remainder)
        NoRemainderShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, HoopyFroodItems.WOBBLY_WATER_BUCKET.get())
                .requires(Items.WATER_BUCKET, 4)
                .unlockedBy("has_bucket", has(Items.WATER_BUCKET))
                .save(this.output);

        // Wobbly Lava Bucket: Wobbly Water Bucket + Lava Bucket, fully consumed
        NoRemainderShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, HoopyFroodItems.WOBBLY_LAVA_BUCKET.get())
                .requires(HoopyFroodItems.WOBBLY_WATER_BUCKET.get())
                .requires(Items.LAVA_BUCKET)
                .unlockedBy("has_wobbly_water_bucket", has(HoopyFroodItems.WOBBLY_WATER_BUCKET.get()))
                .save(this.output);

        // Wobbly Slime Bucket: Wobbly Water Bucket + Slime Block
        NoRemainderShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, HoopyFroodItems.WOBBLY_SLIME_BUCKET.get())
                .requires(HoopyFroodItems.WOBBLY_WATER_BUCKET.get())
                .requires(Items.SLIME_BLOCK)
                .unlockedBy("has_wobbly_water_bucket", has(HoopyFroodItems.WOBBLY_WATER_BUCKET.get()))
                .save(this.output);

        // Wobbly Honey Bucket: Wobbly Water Bucket + Honey Block
        NoRemainderShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, HoopyFroodItems.WOBBLY_HONEY_BUCKET.get())
                .requires(HoopyFroodItems.WOBBLY_WATER_BUCKET.get())
                .requires(Items.HONEY_BLOCK)
                .unlockedBy("has_wobbly_water_bucket", has(HoopyFroodItems.WOBBLY_WATER_BUCKET.get()))
                .save(this.output);

        // Wobbly Magma Bucket: Wobbly Water Bucket + Magma Block
        NoRemainderShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, HoopyFroodItems.WOBBLY_MAGMA_BUCKET.get())
                .requires(HoopyFroodItems.WOBBLY_WATER_BUCKET.get())
                .requires(Items.MAGMA_BLOCK)
                .unlockedBy("has_wobbly_water_bucket", has(HoopyFroodItems.WOBBLY_WATER_BUCKET.get()))
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

        // Caterpillar custom recipes — shapeless, no advancement, registered directly.
        saveCaterpillarRecipe("caterpillar_combine",
                new CaterpillarCombineRecipe(CraftingBookCategory.MISC));
        saveCaterpillarRecipe("caterpillar_add_torches",
                new CaterpillarAddCountRecipe(Ingredient.of(Items.TORCH),
                        HoopyFroodDataComponents.CATERPILLAR_TORCHES.get(), 0));
        saveCaterpillarRecipe("caterpillar_add_cobweb",
                new CaterpillarSetFlagRecipe(Ingredient.of(Items.COBWEB),
                        HoopyFroodDataComponents.CATERPILLAR_IMMOBILE.get()));
        saveCaterpillarRecipe("caterpillar_add_nether_star",
                new CaterpillarSetFlagRecipe(Ingredient.of(Items.NETHER_STAR),
                        HoopyFroodDataComponents.CATERPILLAR_UNDYING.get()));

        // ---- Scaffolded Redstone Components ----
        // [base] + Scaffolding → [scaffolded] (shapeless)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_INVERTER.get())
                .requires(HoopyFroodTutBlocks.INVERTER)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_inverter", has(HoopyFroodTutBlocks.INVERTER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_inverter")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_REPEATER.get())
                .requires(Items.REPEATER)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_repeater", has(Items.REPEATER))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_repeater")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR.get())
                .requires(Items.COMPARATOR)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_comparator", has(Items.COMPARATOR))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_comparator")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH.get())
                .requires(HoopyFroodTutBlocks.PULSE_LATCH)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_pulse_latch", has(HoopyFroodTutBlocks.PULSE_LATCH.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_pulse_latch")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_RELEASE_LATCH.get())
                .requires(HoopyFroodTutBlocks.RELEASE_LATCH)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_release_latch", has(HoopyFroodTutBlocks.RELEASE_LATCH.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_release_latch")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_RELEASE_LATCH.get())
                .requires(HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_sluggish_release_latch", has(HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_sluggish_release_latch")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH.get())
                .requires(HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_sluggish_pulse_latch", has(HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_sluggish_pulse_latch")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK.get())
                .requires(HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_sluggish_redstone_clock", has(HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_sluggish_redstone_clock")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK.get())
                .requires(HoopyFroodTutBlocks.REDSTONE_CLOCK)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_redstone_clock", has(HoopyFroodTutBlocks.REDSTONE_CLOCK.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_redstone_clock")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_DUST.get())
                .requires(Items.REDSTONE)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_redstone_dust")));

        // ---- Sturdy Pistons ----
        // Sturdy Piston: iron ingots top, cobblestone sides, piston center, redstone bottom
        ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.STURDY_PISTON.get())
                .pattern("III")
                .pattern("CPC")
                .pattern("CRC")
                .define('I', Items.IRON_INGOT)
                .define('C', Items.COBBLESTONE)
                .define('P', Items.PISTON)
                .define('R', Items.REDSTONE)
                .unlockedBy("has_piston", has(Items.PISTON))
                .save(this.output);

        // Sticky Sturdy Piston: Sturdy Piston + Slime Ball (shapeless)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.STICKY_STURDY_PISTON.get())
                .requires(HoopyFroodTutBlocks.STURDY_PISTON)
                .requires(Items.SLIME_BALL)
                .unlockedBy("has_sturdy_piston", has(HoopyFroodTutBlocks.STURDY_PISTON.get()))
                .save(this.output);

        // Reverse: [scaffolded] alone → [base] + Scaffolding (shapeless, 1 ingredient)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.INVERTER.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_INVERTER)
                .unlockedBy("has_scaffolded_inverter", has(HoopyFroodTutBlocks.SCAFFOLDED_INVERTER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_inverter_reverse")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, Items.REPEATER)
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_REPEATER)
                .unlockedBy("has_scaffolded_repeater", has(HoopyFroodTutBlocks.SCAFFOLDED_REPEATER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_repeater_reverse")));

        // Note: reverse gives back base + scaffolding as two outputs — but shapeless only supports one result.
        // We give back only the base item; Scaffolding is consumed (treat as cost of in-world conversion only).
        // For crafting, return base + separate scaffolding recipe.

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, Items.COMPARATOR)
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR)
                .unlockedBy("has_scaffolded_comparator", has(HoopyFroodTutBlocks.SCAFFOLDED_COMPARATOR.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_comparator_reverse")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.PULSE_LATCH.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH)
                .unlockedBy("has_scaffolded_pulse_latch", has(HoopyFroodTutBlocks.SCAFFOLDED_PULSE_LATCH.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_pulse_latch_reverse")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.RELEASE_LATCH.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_RELEASE_LATCH)
                .unlockedBy("has_scaffolded_release_latch", has(HoopyFroodTutBlocks.SCAFFOLDED_RELEASE_LATCH.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_release_latch_reverse")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SLUGGISH_RELEASE_LATCH.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_RELEASE_LATCH)
                .unlockedBy("has_scaffolded_sluggish_release_latch", has(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_RELEASE_LATCH.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_sluggish_release_latch_reverse")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH)
                .unlockedBy("has_scaffolded_sluggish_pulse_latch", has(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_PULSE_LATCH.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_sluggish_pulse_latch_reverse")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SLUGGISH_REDSTONE_CLOCK.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK)
                .unlockedBy("has_scaffolded_sluggish_redstone_clock", has(HoopyFroodTutBlocks.SCAFFOLDED_SLUGGISH_REDSTONE_CLOCK.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_sluggish_redstone_clock_reverse")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.REDSTONE_CLOCK.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK)
                .unlockedBy("has_scaffolded_redstone_clock", has(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_CLOCK.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_redstone_clock_reverse")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, Items.REDSTONE)
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_DUST)
                .unlockedBy("has_scaffolded_redstone_dust", has(HoopyFroodTutBlocks.SCAFFOLDED_REDSTONE_DUST.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_redstone_dust_reverse")));

        // ---- Angled Repeaters ----
        // Initial craft: Repeater + Redstone Dust → Left Angled Repeater (shaped)
        ShapedRecipeBuilder.shaped(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get())
                .pattern("RD ")
                .define('R', Items.REPEATER)
                .define('D', Items.REDSTONE)
                .unlockedBy("has_repeater", has(Items.REPEATER))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "left_angled_repeater")));

        // Flip: left ↔ right (shapeless, 1 ingredient, delay resets)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER.get())
                .requires(HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER)
                .unlockedBy("has_left_angled_repeater", has(HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "left_to_right_angled_repeater")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get())
                .requires(HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER)
                .unlockedBy("has_right_angled_repeater", has(HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "right_to_left_angled_repeater")));

        // Scaffolded angled: [angled] + Scaffolding → [scaffolded angled]
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER.get())
                .requires(HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_left_angled_repeater", has(HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_left_angled_repeater")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER.get())
                .requires(HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER)
                .requires(Items.SCAFFOLDING)
                .unlockedBy("has_right_angled_repeater", has(HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_right_angled_repeater")));

        // Scaffolded angled flip: [scaffolded left] ↔ [scaffolded right]
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER)
                .unlockedBy("has_scaffolded_left_angled_repeater", has(HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_left_to_right_angled_repeater")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER)
                .unlockedBy("has_scaffolded_right_angled_repeater", has(HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_right_to_left_angled_repeater")));

        // Reverse scaffolded angled: [scaffolded angled] alone → [angled] + Scaffolding
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.LEFT_ANGLED_REPEATER.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER)
                .unlockedBy("has_scaffolded_left_angled_repeater", has(HoopyFroodTutBlocks.SCAFFOLDED_LEFT_ANGLED_REPEATER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_left_angled_repeater_reverse")));

        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.RIGHT_ANGLED_REPEATER.get())
                .requires(HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER)
                .unlockedBy("has_scaffolded_right_angled_repeater", has(HoopyFroodTutBlocks.SCAFFOLDED_RIGHT_ANGLED_REPEATER.get()))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "scaffolded_right_angled_repeater_reverse")));

        // Inert TNT: deactivate regular TNT with a slime ball (absorbs the concussive force)
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.INERT_TNT.get())
                .requires(Blocks.TNT)
                .requires(Items.MILK_BUCKET)
                .unlockedBy("has_tnt", has(Items.TNT))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "inert_tnt_from_milk_bucket")));
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.REDSTONE, HoopyFroodTutBlocks.INERT_TNT.get())
                .requires(Blocks.TNT)
                .requires(Items.HONEY_BOTTLE)
                .unlockedBy("has_tnt", has(Items.TNT))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "inert_tnt_from_honey_bottle")));

        // Super Enchanted Book: max-level enchanted book + netherite block → +1 level
        saveSuperEnchantedBookRecipe("super_enchanted_book",
                new SuperEnchantedBookRecipe(
                        new Recipe.CommonInfo(true),
                        Ingredient.of(Items.ENCHANTED_BOOK),
                        Optional.of(Ingredient.of(Items.NETHERITE_BLOCK))));

        ShapedRecipeBuilder.shaped(items, RecipeCategory.MISC, HoopyFroodTutBlocks.LAVA_NEUTRALIZER.get())
                .pattern("GNG")
                .pattern("GBG")
                .pattern("GGG")
                .define('G', Items.GOLD_BLOCK)
                .define('N', Items.NETHERITE_INGOT)
                .define('B', Items.BUCKET)
                .unlockedBy("has_bucket", has(Items.BUCKET))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "lava_neutralizer")));
    }

    /** Adds a Disposable Caterpillar crafting recipe for a specific pickaxe tier. */
    private void addCaterpillarRecipe(HolderGetter<Item> items, Item pickaxe, String tier, int count, int charges) {
        DataComponentPatch patch = charges > 1
                ? DataComponentPatch.builder()
                        .set(HoopyFroodDataComponents.CATERPILLAR_CHARGES.get(), charges)
                        .build()
                : DataComponentPatch.EMPTY;
        ItemStackTemplate result = new ItemStackTemplate(
                HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get().asItem(), count, patch);
        ShapelessRecipeBuilder.shapeless(items, RecipeCategory.MISC, result)
                .requires(Items.PISTON)
                .requires(Items.REDSTONE)
                .requires(pickaxe)
                .unlockedBy("has_" + tier + "_pickaxe", has(pickaxe))
                .save(this.output, ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                "disposable_caterpillar_" + tier)));
    }

    /** Saves a caterpillar custom recipe (no unlock advancement needed). */
    private void saveCaterpillarRecipe(String name, Recipe<?> recipe) {
        this.output.accept(
                ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, name)),
                recipe,
                null
        );
    }

    /** Saves a super enchanted book smithing recipe (no unlock advancement needed). */
    private void saveSuperEnchantedBookRecipe(String name, Recipe<?> recipe) {
        this.output.accept(
                ResourceKey.create(Registries.RECIPE,
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, name)),
                recipe,
                null
        );
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

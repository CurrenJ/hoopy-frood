package grill24.hoopyfroodtut.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import grill24.hoopyfroodtut.core.HoopyFroodRecipeSerializers;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.NormalCraftingRecipe;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.ShapelessCraftingRecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.RecipeMatcher;

/**
 * A shapeless crafting recipe that suppresses all crafting remainder items.
 * Use this when the inputs have craftRemainder set (e.g. water/lava buckets)
 * and the recipe should fully consume them without returning empty buckets.
 */
public class NoRemainderShapelessRecipe extends NormalCraftingRecipe {

    // Max 9 ingredients (3x3 grid). Hardcoded since ShapedRecipePattern.maxWidth/maxHeight are package-private.
    public static final MapCodec<NoRemainderShapelessRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            CraftingRecipe.CraftingBookInfo.MAP_CODEC.forGetter(o -> o.bookInfo),
            ItemStackTemplate.CODEC.fieldOf("result").forGetter(o -> o.result),
            Ingredient.CODEC.listOf(1, 9).fieldOf("ingredients").forGetter(o -> o.ingredients)
        ).apply(i, NoRemainderShapelessRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, NoRemainderShapelessRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
        CraftingRecipe.CraftingBookInfo.STREAM_CODEC, o -> o.bookInfo,
        ItemStackTemplate.STREAM_CODEC, o -> o.result,
        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), o -> o.ingredients,
        NoRemainderShapelessRecipe::new
    );

    private final ItemStackTemplate result;
    private final List<Ingredient> ingredients;
    private final boolean isSimple;

    public NoRemainderShapelessRecipe(
            Recipe.CommonInfo commonInfo,
            CraftingRecipe.CraftingBookInfo bookInfo,
            ItemStackTemplate result,
            List<Ingredient> ingredients) {
        super(commonInfo, bookInfo);
        this.result = result;
        this.ingredients = ingredients;
        this.isSimple = ingredients.stream().allMatch(Ingredient::isSimple);
    }

    @Override
    public RecipeSerializer<NoRemainderShapelessRecipe> getSerializer() {
        return HoopyFroodRecipeSerializers.NO_REMAINDER_SHAPELESS.get();
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.create(this.ingredients);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.ingredientCount() != this.ingredients.size()) {
            return false;
        } else if (!isSimple) {
            var nonEmptyItems = new ArrayList<ItemStack>(input.ingredientCount());
            for (var item : input.items())
                if (!item.isEmpty())
                    nonEmptyItems.add(item);
            return RecipeMatcher.findMatches(nonEmptyItems, this.ingredients) != null;
        } else {
            return input.size() == 1 && this.ingredients.size() == 1
                ? this.ingredients.getFirst().test(input.getItem(0))
                : input.stackedContents().canCraft(this, null);
        }
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return this.result.create();
    }

    /** Returns empty stacks for every slot — suppresses all crafting remainder items. */
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput input) {
        return NonNullList.withSize(input.size(), ItemStack.EMPTY);
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
            new ShapelessCraftingRecipeDisplay(
                this.ingredients.stream().map(Ingredient::display).toList(),
                new SlotDisplay.ItemStackSlotDisplay(this.result),
                new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE)
            )
        );
    }
}

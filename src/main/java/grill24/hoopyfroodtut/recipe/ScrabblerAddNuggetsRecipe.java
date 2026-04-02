package grill24.hoopyfroodtut.recipe;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import grill24.hoopyfroodtut.core.HoopyFroodRecipeSerializers;
import grill24.hoopyfroodtut.item.BeggingItemScrabblerItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Crafting recipe that loads metallic nuggets (iron or gold) into a Begging Item Scrabbler.
 * <p>
 * Place exactly one Begging Item Scrabbler plus any number of iron or gold nuggets (≥ 1)
 * anywhere in the crafting grid to add those nuggets to the scrabbler's fuel supply.
 * Each nugget provides one block of movement.
 * <p>
 * Example: a scrabbler with 4 nuggets + 8 iron nuggets → scrabbler with 12 nuggets.
 */
public class ScrabblerAddNuggetsRecipe extends CustomRecipe {

    public static final MapCodec<ScrabblerAddNuggetsRecipe> CODEC =
            CraftingBookCategory.CODEC
                    .optionalFieldOf("category", CraftingBookCategory.MISC)
                    .xmap(ScrabblerAddNuggetsRecipe::new, CustomRecipe::category);

    public static final StreamCodec<RegistryFriendlyByteBuf, ScrabblerAddNuggetsRecipe> STREAM_CODEC =
            CraftingBookCategory.STREAM_CODEC
                    .<RegistryFriendlyByteBuf>cast()
                    .map(ScrabblerAddNuggetsRecipe::new, CustomRecipe::category);

    public ScrabblerAddNuggetsRecipe(CraftingBookCategory category) {
        super();
    }

    /** Valid when: exactly 1 scrabbler + at least 1 iron or gold nugget, all other slots empty. */
    @Override
    public boolean matches(CraftingInput input, Level level) {
        int scrabblers = 0;
        int nuggets = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(HoopyFroodItems.BEGGING_ITEM_SCRABBLER_ITEM.get())) scrabblers++;
            else if (stack.is(Items.IRON_NUGGET) || stack.is(Items.GOLD_NUGGET)) nuggets++;
            else return false;
        }
        return scrabblers == 1 && nuggets >= 1;
    }

    /** Returns the scrabbler with its nugget count increased by the number of nuggets in the grid. */
    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack scrabbler = ItemStack.EMPTY;
        int addedNuggets = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(HoopyFroodItems.BEGGING_ITEM_SCRABBLER_ITEM.get())) {
                scrabbler = stack;
            } else if (stack.is(Items.IRON_NUGGET) || stack.is(Items.GOLD_NUGGET)) {
                addedNuggets++;
            }
        }
        ItemStack result = scrabbler.copyWithCount(1);
        int existing = BeggingItemScrabblerItem.getNuggets(result);
        result.set(HoopyFroodDataComponents.SCRABBLER_NUGGETS.get(), existing + addedNuggets);
        return result;
    }

    @Override
    public RecipeSerializer<ScrabblerAddNuggetsRecipe> getSerializer() {
        return HoopyFroodRecipeSerializers.SCRABBLER_ADD_NUGGETS.get();
    }
}

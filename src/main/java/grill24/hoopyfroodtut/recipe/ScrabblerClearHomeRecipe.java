package grill24.hoopyfroodtut.recipe;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import grill24.hoopyfroodtut.core.HoopyFroodRecipeSerializers;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Crafting recipe that clears the home position from a Begging Item Scrabbler.
 * <p>
 * Place exactly one Begging Item Scrabbler (with a home set) alone in the crafting
 * grid to produce an identical scrabbler with the home component removed.
 * Nuggets and any carried items encoded on the item are preserved.
 */
public class ScrabblerClearHomeRecipe extends CustomRecipe {

    public static final MapCodec<ScrabblerClearHomeRecipe> CODEC =
            CraftingBookCategory.CODEC
                    .optionalFieldOf("category", CraftingBookCategory.MISC)
                    .xmap(ScrabblerClearHomeRecipe::new, CustomRecipe::category);

    public static final StreamCodec<RegistryFriendlyByteBuf, ScrabblerClearHomeRecipe> STREAM_CODEC =
            CraftingBookCategory.STREAM_CODEC
                    .<RegistryFriendlyByteBuf>cast()
                    .map(ScrabblerClearHomeRecipe::new, CustomRecipe::category);

    public ScrabblerClearHomeRecipe(CraftingBookCategory category) {
        super();
    }

    /** Valid when: exactly 1 scrabbler that has a home set, all other slots empty. */
    @Override
    public boolean matches(CraftingInput input, Level level) {
        int scrabblers = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(HoopyFroodItems.BEGGING_ITEM_SCRABBLER_ITEM.get())
                    && stack.has(HoopyFroodDataComponents.SCRABBLER_HOME.get())) {
                scrabblers++;
            } else {
                return false;
            }
        }
        return scrabblers == 1;
    }

    /** Returns the scrabbler with the home component removed; all other data preserved. */
    @Override
    public ItemStack assemble(CraftingInput input) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (!stack.isEmpty() && stack.is(HoopyFroodItems.BEGGING_ITEM_SCRABBLER_ITEM.get())) {
                ItemStack result = stack.copyWithCount(1);
                result.remove(HoopyFroodDataComponents.SCRABBLER_HOME.get());
                return result;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public RecipeSerializer<ScrabblerClearHomeRecipe> getSerializer() {
        return HoopyFroodRecipeSerializers.SCRABBLER_CLEAR_HOME.get();
    }
}

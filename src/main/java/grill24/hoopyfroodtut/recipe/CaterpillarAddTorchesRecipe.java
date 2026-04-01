package grill24.hoopyfroodtut.recipe;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import grill24.hoopyfroodtut.core.HoopyFroodRecipeSerializers;
import grill24.hoopyfroodtut.item.DisposableCaterpillarItem;
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
 * Crafting recipe that loads torches into a Disposable Caterpillar.
 * <p>
 * Place exactly one Disposable Caterpillar plus any number of torches (≥ 1)
 * anywhere in the crafting grid to add those torches to the caterpillar's
 * torch supply. The caterpillar will then place one torch every 12 blocks
 * it travels.
 * <p>
 * Example: a caterpillar with 5 torch charges + 3 torches → caterpillar with 8.
 */
public class CaterpillarAddTorchesRecipe extends CustomRecipe {

    public static final MapCodec<CaterpillarAddTorchesRecipe> CODEC =
            CraftingBookCategory.CODEC
                    .optionalFieldOf("category", CraftingBookCategory.MISC)
                    .xmap(CaterpillarAddTorchesRecipe::new, CustomRecipe::category);

    public static final StreamCodec<RegistryFriendlyByteBuf, CaterpillarAddTorchesRecipe> STREAM_CODEC =
            CraftingBookCategory.STREAM_CODEC
                    .<RegistryFriendlyByteBuf>cast()
                    .map(CaterpillarAddTorchesRecipe::new, CustomRecipe::category);

    public CaterpillarAddTorchesRecipe(CraftingBookCategory category) {
        super();
    }

    /** Valid when: exactly 1 caterpillar + at least 1 torch, all other slots empty. */
    @Override
    public boolean matches(CraftingInput input, Level level) {
        int caterpillars = 0;
        int torches = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get())) caterpillars++;
            else if (stack.is(Items.TORCH)) torches++;
            else return false;
        }
        return caterpillars == 1 && torches >= 1;
    }

    /** Returns the caterpillar with its torch supply increased by the number of torches in the grid. */
    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack caterpillar = ItemStack.EMPTY;
        int addedTorches = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get())) {
                caterpillar = stack;
            } else if (stack.is(Items.TORCH)) {
                addedTorches++;
            }
        }
        ItemStack result = caterpillar.copyWithCount(1);
        int existing = DisposableCaterpillarItem.getTorches(result);
        result.set(HoopyFroodDataComponents.CATERPILLAR_TORCHES.get(), existing + addedTorches);
        return result;
    }

    @Override
    public RecipeSerializer<CaterpillarAddTorchesRecipe> getSerializer() {
        return HoopyFroodRecipeSerializers.CATERPILLAR_ADD_TORCHES.get();
    }
}

package grill24.hoopyfroodtut.recipe;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.core.HoopyFroodDataComponents;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import grill24.hoopyfroodtut.core.HoopyFroodRecipeSerializers;
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
 * Crafting recipe that makes a Disposable Caterpillar "stuck" (immobile).
 * <p>
 * Place exactly one Disposable Caterpillar and one cobweb anywhere in the
 * crafting grid to produce an immobile caterpillar. An immobile caterpillar
 * still mines the block it faces on each redstone pulse and ticks down its
 * charge counter, but never advances forward — useful for static farming.
 */
public class CaterpillarAddCobwebRecipe extends CustomRecipe {

    public static final MapCodec<CaterpillarAddCobwebRecipe> CODEC =
            CraftingBookCategory.CODEC
                    .optionalFieldOf("category", CraftingBookCategory.MISC)
                    .xmap(CaterpillarAddCobwebRecipe::new, CustomRecipe::category);

    public static final StreamCodec<RegistryFriendlyByteBuf, CaterpillarAddCobwebRecipe> STREAM_CODEC =
            CraftingBookCategory.STREAM_CODEC
                    .<RegistryFriendlyByteBuf>cast()
                    .map(CaterpillarAddCobwebRecipe::new, CustomRecipe::category);

    public CaterpillarAddCobwebRecipe(CraftingBookCategory category) {
        super();
    }

    /** Valid when: exactly 1 caterpillar + exactly 1 cobweb, all other slots empty. */
    @Override
    public boolean matches(CraftingInput input, Level level) {
        int caterpillars = 0;
        int cobwebs = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get())) caterpillars++;
            else if (stack.is(Items.COBWEB)) cobwebs++;
            else return false;
        }
        return caterpillars == 1 && cobwebs == 1;
    }

    /** Returns the caterpillar with its immobile flag set to true. */
    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack caterpillar = ItemStack.EMPTY;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get())) {
                caterpillar = stack;
                break;
            }
        }
        ItemStack result = caterpillar.copyWithCount(1);
        result.set(HoopyFroodDataComponents.CATERPILLAR_IMMOBILE.get(), true);
        return result;
    }

    @Override
    public RecipeSerializer<CaterpillarAddCobwebRecipe> getSerializer() {
        return HoopyFroodRecipeSerializers.CATERPILLAR_ADD_COBWEB.get();
    }
}

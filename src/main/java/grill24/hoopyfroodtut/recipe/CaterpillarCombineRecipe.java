package grill24.hoopyfroodtut.recipe;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import grill24.hoopyfroodtut.core.HoopyFroodRecipeSerializers;
import grill24.hoopyfroodtut.item.DisposableCaterpillarItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Crafting recipe that combines any number of Disposable Caterpillar items (≥ 2)
 * placed anywhere in the crafting grid into a single item whose charge count equals
 * the sum of all inputs' charges.
 * <p>
 * Example: a 3-charge item + a 2-charge item → one 5-charge item.
 * <p>
 * The recipe type is {@code hoopyfroodtut:caterpillar_combine} and requires no
 * specific shape — caterpillars can occupy any slots as long as every non-empty
 * slot contains a Disposable Caterpillar.
 */
public class CaterpillarCombineRecipe extends CustomRecipe {

    /**
     * JSON codec: reads/writes an optional "category" field and wraps it into
     * the recipe via xmap.
     */
    public static final MapCodec<CaterpillarCombineRecipe> CODEC =
            CraftingBookCategory.CODEC
                    .optionalFieldOf("category", CraftingBookCategory.MISC)
                    .xmap(CaterpillarCombineRecipe::new, CustomRecipe::category);

    /**
     * Network codec: encodes/decodes only the category.
     * CraftingBookCategory.STREAM_CODEC is StreamCodec&lt;ByteBuf, CraftingBookCategory&gt;;
     * cast() widens the buffer type to RegistryFriendlyByteBuf (safe since RFBB extends ByteBuf).
     */
    public static final StreamCodec<RegistryFriendlyByteBuf, CaterpillarCombineRecipe> STREAM_CODEC =
            CraftingBookCategory.STREAM_CODEC
                    .<RegistryFriendlyByteBuf>cast()
                    .map(CaterpillarCombineRecipe::new, CustomRecipe::category);

    public CaterpillarCombineRecipe(CraftingBookCategory category) {
        super();
    }

    /**
     * Valid when: all non-empty slots contain a Disposable Caterpillar item, and
     * there are at least two such items (combining a single item would be a no-op).
     */
    @Override
    public boolean matches(CraftingInput input, Level level) {
        int count = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (!stack.is(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get())) return false;
            count++;
        }
        return count >= 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        return DisposableCaterpillarItem.merge(
                input.items().stream()
                        .filter(s -> s.is(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get()))
                        .toList()
        );
    }

    @Override
    public RecipeSerializer<CaterpillarCombineRecipe> getSerializer() {
        return HoopyFroodRecipeSerializers.CATERPILLAR_COMBINE.get();
    }
}

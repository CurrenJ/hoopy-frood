package grill24.hoopyfroodtut.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import grill24.hoopyfroodtut.core.HoopyFroodRecipeSerializers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Generic crafting recipe that sets a boolean data component on a Disposable Caterpillar.
 * <p>
 * Configured in JSON with two fields:
 * <ul>
 *   <li>{@code ingredient} — the item to combine with the caterpillar (e.g. cobweb, nether star)</li>
 *   <li>{@code component} — the {@code DataComponentType<Boolean>} to set to {@code true}</li>
 * </ul>
 * <p>
 * Example JSON:
 * <pre>{@code
 * {
 *   "type": "hoopyfroodtut:caterpillar_set_flag",
 *   "ingredient": {"item": "minecraft:cobweb"},
 *   "component": "hoopyfroodtut:caterpillar_immobile"
 * }
 * }</pre>
 */
public class CaterpillarSetFlagRecipe extends CustomRecipe {

    private final Ingredient ingredient;
    @SuppressWarnings("rawtypes")
    private final DataComponentType component;

    @SuppressWarnings("unchecked")
    public static final MapCodec<CaterpillarSetFlagRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
        inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            Identifier.CODEC.xmap(
                id -> BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(id),
                comp -> BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(comp)
            ).fieldOf("component").forGetter(r -> r.component)
        ).apply(inst, CaterpillarSetFlagRecipe::new)
    );

    @SuppressWarnings("unchecked")
    public static final StreamCodec<RegistryFriendlyByteBuf, CaterpillarSetFlagRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
        Identifier.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().map(
            id -> BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(id),
            comp -> BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(comp)
        ), r -> r.component,
        CaterpillarSetFlagRecipe::new
    );

    public CaterpillarSetFlagRecipe(Ingredient ingredient, DataComponentType<?> component) {
        super();
        this.ingredient = ingredient;
        this.component = component;
    }

    /** Valid when: exactly 1 caterpillar + exactly 1 matching ingredient, all other slots empty. */
    @Override
    public boolean matches(CraftingInput input, Level level) {
        int caterpillars = 0;
        int matches = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            if (stack.is(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get())) caterpillars++;
            else if (ingredient.test(stack)) matches++;
            else return false;
        }
        return caterpillars == 1 && matches == 1;
    }

    /** Returns the caterpillar with the configured boolean component set to true. */
    @SuppressWarnings("unchecked")
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
        result.set(component, true);
        return result;
    }

    @Override
    public RecipeSerializer<CaterpillarSetFlagRecipe> getSerializer() {
        return HoopyFroodRecipeSerializers.CATERPILLAR_SET_FLAG.get();
    }
}

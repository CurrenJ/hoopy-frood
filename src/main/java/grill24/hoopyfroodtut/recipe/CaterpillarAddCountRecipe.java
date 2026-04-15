package grill24.hoopyfroodtut.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import grill24.hoopyfroodtut.core.HoopyFroodRecipeSerializers;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Generic crafting recipe that increments an integer data component on a Disposable
 * Caterpillar by counting how many matching ingredient items are in the grid.
 * <p>
 * Configured in JSON with three fields:
 * <ul>
 *   <li>{@code ingredient} — the item to combine with the caterpillar (e.g. torch)</li>
 *   <li>{@code component} — the {@code DataComponentType<Integer>} to increment</li>
 *   <li>{@code default_value} — (optional, default 0) the baseline when the component is absent</li>
 * </ul>
 * <p>
 * Example JSON:
 * <pre>{@code
 * {
 *   "type": "hoopyfroodtut:caterpillar_add_count",
 *   "ingredient": {"item": "minecraft:torch"},
 *   "component": "hoopyfroodtut:caterpillar_torches"
 * }
 * }</pre>
 */
public class CaterpillarAddCountRecipe extends CustomRecipe {

    private final Ingredient ingredient;
    @SuppressWarnings("rawtypes")
    private final DataComponentType component;
    private final int defaultValue;

    @SuppressWarnings("unchecked")
    public static final MapCodec<CaterpillarAddCountRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
        inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(r -> r.ingredient),
            Identifier.CODEC.xmap(
                id -> BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(id),
                comp -> BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(comp)
            ).fieldOf("component").forGetter(r -> r.component),
            Codec.INT.optionalFieldOf("default_value", 0).forGetter(r -> r.defaultValue)
        ).apply(inst, CaterpillarAddCountRecipe::new)
    );

    @SuppressWarnings("unchecked")
    public static final StreamCodec<RegistryFriendlyByteBuf, CaterpillarAddCountRecipe> STREAM_CODEC = StreamCodec.composite(
        Ingredient.CONTENTS_STREAM_CODEC, r -> r.ingredient,
        Identifier.STREAM_CODEC.<RegistryFriendlyByteBuf>cast().map(
            id -> BuiltInRegistries.DATA_COMPONENT_TYPE.getValue(id),
            comp -> BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(comp)
        ), r -> r.component,
        ByteBufCodecs.INT, r -> r.defaultValue,
        CaterpillarAddCountRecipe::new
    );

    public CaterpillarAddCountRecipe(Ingredient ingredient, DataComponentType<?> component, int defaultValue) {
        super();
        this.ingredient = ingredient;
        this.component = component;
        this.defaultValue = defaultValue;
    }

    /** Valid when: exactly 1 caterpillar + at least 1 matching ingredient, all other slots empty. */
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
        return caterpillars == 1 && matches >= 1;
    }

    /** Returns the caterpillar with the configured integer component incremented by the ingredient count. */
    @SuppressWarnings("unchecked")
    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack caterpillar = ItemStack.EMPTY;
        int added = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.is(HoopyFroodItems.DISPOSABLE_CATERPILLAR_ITEM.get())) {
                caterpillar = stack;
            } else if (ingredient.test(stack)) {
                added++;
            }
        }
        ItemStack result = caterpillar.copyWithCount(1);
        int existing = result.getOrDefault(component, defaultValue);
        result.set(component, existing + added);
        return result;
    }

    @Override
    public RecipeSerializer<CaterpillarAddCountRecipe> getSerializer() {
        return HoopyFroodRecipeSerializers.CATERPILLAR_ADD_COUNT.get();
    }
}

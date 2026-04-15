package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.recipe.CaterpillarAddCountRecipe;
import grill24.hoopyfroodtut.recipe.CaterpillarCombineRecipe;
import grill24.hoopyfroodtut.recipe.CaterpillarSetFlagRecipe;
import grill24.hoopyfroodtut.recipe.NoRemainderShapelessRecipe;
import grill24.hoopyfroodtut.recipe.ScrabblerAddNuggetsRecipe;
import grill24.hoopyfroodtut.recipe.ScrabblerClearHomeRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HoopyFroodRecipeSerializers {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, HoopyFroodTut.MODID);

    /** Serializer for the caterpillar charge-combining recipe. */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CaterpillarCombineRecipe>>
            CATERPILLAR_COMBINE = RECIPE_SERIALIZERS.register("caterpillar_combine",
                    () -> new RecipeSerializer<>(CaterpillarCombineRecipe.CODEC, CaterpillarCombineRecipe.STREAM_CODEC));

    /** Serializer for recipes that increment an integer data component on a caterpillar (e.g. add torches). */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CaterpillarAddCountRecipe>>
            CATERPILLAR_ADD_COUNT = RECIPE_SERIALIZERS.register("caterpillar_add_count",
                    () -> new RecipeSerializer<>(CaterpillarAddCountRecipe.CODEC, CaterpillarAddCountRecipe.STREAM_CODEC));

    /** Serializer for recipes that set a boolean data component on a caterpillar (e.g. stuck, undying). */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CaterpillarSetFlagRecipe>>
            CATERPILLAR_SET_FLAG = RECIPE_SERIALIZERS.register("caterpillar_set_flag",
                    () -> new RecipeSerializer<>(CaterpillarSetFlagRecipe.CODEC, CaterpillarSetFlagRecipe.STREAM_CODEC));

    /** Serializer for loading metallic nuggets (fuel) into a Begging Item Scrabbler. */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ScrabblerAddNuggetsRecipe>>
            SCRABBLER_ADD_NUGGETS = RECIPE_SERIALIZERS.register("scrabbler_add_nuggets",
                    () -> new RecipeSerializer<>(ScrabblerAddNuggetsRecipe.CODEC, ScrabblerAddNuggetsRecipe.STREAM_CODEC));

    /** Serializer for clearing the home position from a Begging Item Scrabbler. */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ScrabblerClearHomeRecipe>>
            SCRABBLER_CLEAR_HOME = RECIPE_SERIALIZERS.register("scrabbler_clear_home",
                    () -> new RecipeSerializer<>(ScrabblerClearHomeRecipe.CODEC, ScrabblerClearHomeRecipe.STREAM_CODEC));

    /** Serializer for shapeless recipes that fully consume their inputs with no crafting remainder. */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<NoRemainderShapelessRecipe>>
            NO_REMAINDER_SHAPELESS = RECIPE_SERIALIZERS.register("no_remainder_shapeless",
                    () -> new RecipeSerializer<>(NoRemainderShapelessRecipe.MAP_CODEC, NoRemainderShapelessRecipe.STREAM_CODEC));
}

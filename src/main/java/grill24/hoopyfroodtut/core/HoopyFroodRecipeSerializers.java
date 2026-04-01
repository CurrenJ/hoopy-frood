package grill24.hoopyfroodtut.core;

import grill24.hoopyfroodtut.recipe.CaterpillarAddTorchesRecipe;
import grill24.hoopyfroodtut.recipe.CaterpillarCombineRecipe;
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

    /** Serializer for adding torches to a caterpillar item. */
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CaterpillarAddTorchesRecipe>>
            CATERPILLAR_ADD_TORCHES = RECIPE_SERIALIZERS.register("caterpillar_add_torches",
                    () -> new RecipeSerializer<>(CaterpillarAddTorchesRecipe.CODEC, CaterpillarAddTorchesRecipe.STREAM_CODEC));
}

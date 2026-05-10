package grill24.hoopyfroodtut.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import grill24.hoopyfroodtut.core.HoopyFroodRecipeSerializers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.item.crafting.display.SmithingRecipeDisplay;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Optional;

public class SuperEnchantedBookRecipe extends SimpleSmithingRecipe {

    public static final MapCodec<SuperEnchantedBookRecipe> MAP_CODEC = RecordCodecBuilder.mapCodec(
        i -> i.group(
            Recipe.CommonInfo.MAP_CODEC.forGetter(o -> o.commonInfo),
            Ingredient.CODEC.fieldOf("base").forGetter(o -> o.base),
            Ingredient.CODEC.optionalFieldOf("addition").forGetter(o -> o.addition)
        ).apply(i, SuperEnchantedBookRecipe::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, SuperEnchantedBookRecipe> STREAM_CODEC = StreamCodec.composite(
        Recipe.CommonInfo.STREAM_CODEC, o -> o.commonInfo,
        Ingredient.CONTENTS_STREAM_CODEC, o -> o.base,
        Ingredient.OPTIONAL_CONTENTS_STREAM_CODEC, o -> o.addition,
        SuperEnchantedBookRecipe::new
    );

    private final Ingredient base;
    private final Optional<Ingredient> addition;

    public SuperEnchantedBookRecipe(Recipe.CommonInfo commonInfo, Ingredient base, Optional<Ingredient> addition) {
        super(commonInfo);
        this.base = base;
        this.addition = addition;
    }

    @Override
    public RecipeSerializer<SuperEnchantedBookRecipe> getSerializer() {
        return HoopyFroodRecipeSerializers.SUPER_ENCHANTED_BOOK.get();
    }

    @Override
    public Optional<Ingredient> templateIngredient() {
        return Optional.empty();
    }

    @Override
    public Ingredient baseIngredient() {
        return this.base;
    }

    @Override
    public Optional<Ingredient> additionIngredient() {
        return this.addition;
    }

    @Override
    public boolean matches(SmithingRecipeInput input, Level level) {
        if (!super.matches(input, level)) {
            return false;
        }

        ItemStack baseStack = input.base();
        if (!baseStack.is(Items.ENCHANTED_BOOK)) {
            return false;
        }

        ItemEnchantments enchantments = baseStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.size() != 1) {
            return false;
        }

        var entry = enchantments.entrySet().iterator().next();
        int currentLevel = entry.getIntValue();
        int maxLevel = entry.getKey().value().definition().maxLevel();
        return currentLevel == maxLevel;
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput input) {
        ItemStack baseStack = input.base();
        ItemEnchantments enchantments = baseStack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            return ItemStack.EMPTY;
        }

        var entry = enchantments.entrySet().iterator().next();
        var enchantment = entry.getKey();
        int currentLevel = entry.getIntValue();

        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(enchantments);
        mutable.set(enchantment, currentLevel + 1);
        ItemEnchantments upgraded = mutable.toImmutable();

        ItemStack result = new ItemStack(Items.ENCHANTED_BOOK);
        result.set(DataComponents.STORED_ENCHANTMENTS, upgraded);
        result.set(DataComponents.ITEM_NAME, Component.literal("Super Enchanted Book"));

        return result;
    }

    @Override
    protected PlacementInfo createPlacementInfo() {
        return PlacementInfo.createFromOptionals(List.of(
            Optional.empty(),
            Optional.of(this.base),
            this.addition
        ));
    }

    @Override
    public List<RecipeDisplay> display() {
        return List.of(
            new SmithingRecipeDisplay(
                Ingredient.optionalIngredientToDisplay(Optional.empty()),
                this.base.display(),
                Ingredient.optionalIngredientToDisplay(this.addition),
                new SlotDisplay.ItemSlotDisplay(Items.ENCHANTED_BOOK),
                new SlotDisplay.ItemSlotDisplay(Items.SMITHING_TABLE)
            )
        );
    }
}

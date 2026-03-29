package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import grill24.hoopyfroodtut.core.HoopyFroodTut;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;

public class BlockModelProvider extends ModelProvider {
    public BlockModelProvider(PackOutput output) {
        super(output, HoopyFroodTut.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createTrivialCube(HoopyFroodTutBlocks.BROWN_BRICKS.get());
        // ALTERNATIVELY, the above live in equivalent to doing it manually like this:
//        Block brownBricksBlock = HoopyFroodTut.BROWN_BRICKS.get();
//        Identifier brownBricksId = ModelTemplates.CUBE_ALL.create(brownBricksBlock, TextureMapping.cube(brownBricksBlock), blockModels.modelOutput);
//        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(brownBricksBlock, BlockModelGenerators.plainVariant(brownBricksId)));

        itemModels.generateFlatItem(HoopyFroodItems.BROWN_BRICK.get(), ModelTemplates.FLAT_ITEM);
        // ALTERNATIVELY, the above is live in equivalent to doing it manually like this:
//        Item brownBrickItem = HoopyFroodTut.BROWN_BRICK.get();
//        Identifier brownBrickId = ModelTemplates.FLAT_ITEM.create(brownBrickItem, TextureMapping.layer0(brownBrickItem), itemModels.modelOutput);
//        itemModels.itemModelOutput.accept(HoopyFroodTut.BROWN_BRICK.get(), ItemModelUtils.plainModel(brownBrickId));
    }
}

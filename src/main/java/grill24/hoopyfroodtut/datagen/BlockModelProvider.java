package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.block.BalancerNode;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import grill24.hoopyfroodtut.core.HoopyFroodTut;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;

public class BlockModelProvider extends ModelProvider {
    public BlockModelProvider(PackOutput output) {
        super(output, HoopyFroodTut.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
//        blockModels.createTrivialCube(HoopyFroodTutBlocks.BROWN_BRICKS.get());
        // ALTERNATIVELY, the above live in equivalent to doing it manually like this:
//        Block brownBricksBlock = HoopyFroodTut.BROWN_BRICKS.get();
//        Identifier brownBricksId = ModelTemplates.CUBE_ALL.create(brownBricksBlock, TextureMapping.cube(brownBricksBlock), blockModels.modelOutput);
//        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(brownBricksBlock, BlockModelGenerators.plainVariant(brownBricksId)));

        blockModels.family(HoopyFroodTutBlocks.BROWN_BRICKS.get()).generateFor(HoopyFroodTutBlocks.BROWN_BRICKS_FAMILY.get());

        itemModels.generateFlatItem(HoopyFroodItems.BROWN_BRICK.get(), ModelTemplates.FLAT_ITEM);
        itemModels.generateFlatItem(HoopyFroodItems.BALANCER_RANGE_EXTENDER.get(), ModelTemplates.FLAT_ITEM);
        // ALTERNATIVELY, the above is live in equivalent to doing it manually like this:
//        Item brownBrickItem = HoopyFroodTut.BROWN_BRICK.get();
//        Identifier brownBrickId = ModelTemplates.FLAT_ITEM.create(brownBrickItem, TextureMapping.layer0(brownBrickItem), itemModels.modelOutput);
//        itemModels.itemModelOutput.accept(HoopyFroodTut.BROWN_BRICK.get(), ItemModelUtils.plainModel(brownBrickId));

        registerBalancerNode(blockModels);
    }

    /**
     * Generates the blockstate JSON for the Balancer Node.
     * <p>
     * The blockstate combines two independent property dispatches:
     * <ul>
     *   <li><b>POWERED</b> – selects between the "off" and "on" model files (different textures).</li>
     *   <li><b>FACING</b> – rotates the chosen model to face the correct direction via the
     *       pre-built {@link BlockModelGenerators#ROTATION_FACING} dispatch.</li>
     * </ul>
     * The MC variant system multiplies the two dispatches to produce all 12 combinations
     * (6 directions × 2 power states) automatically.
     * <p>
     * Model files ({@code balancer_node_off.json} / {@code balancer_node_on.json}) are
     * hand-authored in {@code src/main/resources/} so they are not generated here —
     * only the blockstate JSON is emitted by datagen.
     * <p>
     * Rotation convention (matches {@link BlockModelGenerators#ROTATION_FACING}; base model faces NORTH):
     * <pre>
     *   NORTH  → no rotation
     *   SOUTH  → y=180
     *   EAST   → y=90
     *   WEST   → y=270
     *   UP     → x=270
     *   DOWN   → x=90
     * </pre>
     */
    private static void registerBalancerNode(BlockModelGenerators blockModels) {
        MultiVariant offModel = BlockModelGenerators.plainVariant(
                Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/balancer_node_off"));
        MultiVariant onModel = BlockModelGenerators.plainVariant(
                Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/balancer_node_on"));

        // Step 1: initial dispatch selects the model based on POWERED.
        // Step 2: ROTATION_FACING mutates the selected variant to face the right direction.
        //         This works because BalancerNode.FACING == BlockStateProperties.FACING (same object).
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(HoopyFroodTutBlocks.BALANCER_NODE.get())
                        .with(PropertyDispatch.initial(BalancerNode.POWERED)
                                .select(false, offModel)
                                .select(true,  onModel))
                        .with(BlockModelGenerators.ROTATION_FACING)
        );
    }
}

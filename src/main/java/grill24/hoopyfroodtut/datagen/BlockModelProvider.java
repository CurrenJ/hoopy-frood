package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.block.BalancerNode;
import grill24.hoopyfroodtut.block.BeggingItemScrabbler;
import grill24.hoopyfroodtut.block.DisposableCaterpillar;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import grill24.hoopyfroodtut.core.HoopyFroodTut;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.ItemModelUtils;
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
        itemModels.itemModelOutput.accept(
                HoopyFroodItems.PERIL_SENSITIVE_SUNGLASSES.get(),
                ItemModelUtils.plainModel(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/peril_sensitive_sunglasses")));
        // ALTERNATIVELY, the above is live in equivalent to doing it manually like this:
//        Item brownBrickItem = HoopyFroodTut.BROWN_BRICK.get();
//        Identifier brownBrickId = ModelTemplates.FLAT_ITEM.create(brownBrickItem, TextureMapping.layer0(brownBrickItem), itemModels.modelOutput);
//        itemModels.itemModelOutput.accept(HoopyFroodTut.BROWN_BRICK.get(), ItemModelUtils.plainModel(brownBrickId));

        registerBalancerNode(blockModels);
        registerDisposableCaterpillar(blockModels);
        registerBeggingItemScrabbler(blockModels);
        registerInfiniteImprobabilityDrive(blockModels);
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
     * hand-authored in {@code src/main/resources/} so they are not generated here.
     * Only the blockstate JSON is datagen.
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
    /**
     * Generates the blockstate JSON for the Disposable Caterpillar.
     * <p>
     * Combines two independent dispatches:
     * <ul>
     *   <li><b>TRIGGERED</b> – selects between the idle (lime) and active (yellow) model.</li>
     *   <li><b>FACING</b> – rotates the chosen model via the pre-built
     *       {@link BlockModelGenerators#ROTATION_FACING} dispatch (all 6 directions).</li>
     * </ul>
     * Model files are hand-authored in {@code src/main/resources/}; only the blockstate
     * JSON is datagen-generated.
     */
    private static void registerDisposableCaterpillar(BlockModelGenerators blockModels) {
        MultiVariant idleModel = BlockModelGenerators.plainVariant(
                Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/disposable_caterpillar"));
        MultiVariant activeModel = BlockModelGenerators.plainVariant(
                Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/disposable_caterpillar_active"));

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get())
                        .with(PropertyDispatch.initial(DisposableCaterpillar.TRIGGERED)
                                .select(false, idleModel)
                                .select(true,  activeModel))
                        .with(BlockModelGenerators.ROTATION_FACING)
        );
    }

    /**
     * Generates the blockstate JSON for the Begging Item Scrabbler.
     * <p>
     * The block uses {@link net.minecraft.world.level.block.RenderShape#INVISIBLE} so the
     * blockstate model is never rendered in the world — it is only referenced for the item
     * model in inventory. A single-model blockstate handles all FACING values.
     */
    private static void registerBeggingItemScrabbler(BlockModelGenerators blockModels) {
        MultiVariant model = BlockModelGenerators.plainVariant(
                Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/begging_item_scrabbler"));

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(HoopyFroodTutBlocks.BEGGING_ITEM_SCRABBLER.get())
                        .with(PropertyDispatch.initial(BeggingItemScrabbler.FACING)
                                .select(net.minecraft.core.Direction.NORTH, model)
                                .select(net.minecraft.core.Direction.SOUTH, model)
                                .select(net.minecraft.core.Direction.EAST,  model)
                                .select(net.minecraft.core.Direction.WEST,  model)
                                .select(net.minecraft.core.Direction.UP,    model)
                                .select(net.minecraft.core.Direction.DOWN,  model))
        );
    }

    /**
     * Generates the blockstate JSON for the Infinite Improbability Drive.
     * <p>
     * The block has no state properties, so a single static model variant covers all
     * states. The block itself uses {@link net.minecraft.world.level.block.RenderShape#INVISIBLE}
     * in-world — the blockstate model is only consulted for the inventory item icon.
     * The machine geometry is rendered by {@link grill24.hoopyfroodtut.client.renderer.InfiniteImprobabilityDriveRenderer}.
     */
    private static void registerInfiniteImprobabilityDrive(BlockModelGenerators blockModels) {
        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(
                        HoopyFroodTutBlocks.INFINITE_IMPROBABILITY_DRIVE.get(),
                        BlockModelGenerators.plainVariant(
                                Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                                        "block/infinite_improbability_drive"))));
    }

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

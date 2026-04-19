package grill24.hoopyfroodtut.datagen;

import grill24.hoopyfroodtut.block.BalancerNode;
import grill24.hoopyfroodtut.block.BanishingBin;
import grill24.hoopyfroodtut.block.BeggingItemScrabbler;
import grill24.hoopyfroodtut.block.DisposableCaterpillar;
import grill24.hoopyfroodtut.block.Ejector;
import grill24.hoopyfroodtut.block.Inverter;
import grill24.hoopyfroodtut.block.ProximitySensor;
import grill24.hoopyfroodtut.block.SomebodyElsesProblemField;
import grill24.hoopyfroodtut.block.WobblyWater;
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
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import grill24.hoopyfroodtut.core.HoopyFroodItems;

public class BlockModelProvider extends ModelProvider {
    public BlockModelProvider(PackOutput output) {
        super(output, HoopyFroodTut.MODID);
    }

    // PulseLatch, RedstoneClock, and Inverter have hand-authored blockstate JSONs in src/main/resources;
    // exclude them from datagen validation so runData doesn't complain about missing definitions.
    @Override
    protected java.util.stream.Stream<? extends Holder<Block>> getKnownBlocks() {
        return super.getKnownBlocks().filter(h -> {
            Block b = h.value();
            return b != HoopyFroodTutBlocks.PULSE_LATCH.get()
                    && b != HoopyFroodTutBlocks.SLUGGISH_PULSE_LATCH.get()
                    && b != HoopyFroodTutBlocks.REDSTONE_CLOCK.get()
                    && b != HoopyFroodTutBlocks.INVERTER.get();
        });
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
        itemModels.generateFlatItem(HoopyFroodItems.MAGIC_MIRROR.get(), ModelTemplates.FLAT_ITEM);
        itemModels.itemModelOutput.accept(
                HoopyFroodItems.PERIL_SENSITIVE_SUNGLASSES.get(),
                ItemModelUtils.plainModel(
                        Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/peril_sensitive_sunglasses")));
        // ALTERNATIVELY, the above is live in equivalent to doing it manually like this:
//        Item brownBrickItem = HoopyFroodTut.BROWN_BRICK.get();
//        Identifier brownBrickId = ModelTemplates.FLAT_ITEM.create(brownBrickItem, TextureMapping.layer0(brownBrickItem), itemModels.modelOutput);
//        itemModels.itemModelOutput.accept(HoopyFroodTut.BROWN_BRICK.get(), ItemModelUtils.plainModel(brownBrickId));

        registerBalancerNode(blockModels);
        registerEjector(blockModels);
        registerProximitySensor(blockModels, itemModels);
        // PulseLatch, RedstoneClock, and Inverter blockstates are hand-authored; only register item models.
        itemModels.itemModelOutput.accept(HoopyFroodItems.PULSE_LATCH_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/pulse_latch_1tick")));
        itemModels.itemModelOutput.accept(HoopyFroodItems.SLUGGISH_PULSE_LATCH_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/sluggish_pulse_latch_1tick")));
        itemModels.itemModelOutput.accept(HoopyFroodItems.REDSTONE_CLOCK_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/redstone_clock_1tick")));
        itemModels.itemModelOutput.accept(HoopyFroodItems.INVERTER_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/inverter")));
        registerDisposableCaterpillar(blockModels);
        registerBeggingItemScrabbler(blockModels);
        registerInfiniteImprobabilityDrive(blockModels);
        registerSomebodyElsesProblemField(blockModels, itemModels);
        registerWobblyWater(blockModels, itemModels);
        registerWobblyWaterBuckets(itemModels);
        registerBanishingBin(blockModels, itemModels);
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

    /**
     * Generates the blockstate JSON for the Somebody Else's Problem Field.
     * <p>
     * The block uses {@link net.minecraft.world.level.block.RenderShape#INVISIBLE} so the
     * blockstate model is never rendered in the world — it is only referenced for the item
     * model in inventory. Both POWERED states map to the same static full model.
     */
    private static void registerSomebodyElsesProblemField(
            BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier modelId = Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID,
                "block/somebody_elses_problem_field");

        MultiVariant model = BlockModelGenerators.plainVariant(modelId);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(HoopyFroodTutBlocks.SOMEBODY_ELSES_PROBLEM_FIELD.get())
                        .with(PropertyDispatch.initial(SomebodyElsesProblemField.POWERED)
                                .select(false, model)
                                .select(true,  model)));

        itemModels.itemModelOutput.accept(
                HoopyFroodItems.SOMEBODY_ELSES_PROBLEM_FIELD_ITEM.get(),
                ItemModelUtils.plainModel(modelId));
    }

    /**
     * Generates the blockstate JSON for Wobbly Water.
     * <p>
     * The block uses {@link RenderShape#INVISIBLE} so the blockstate model is only consulted
     * for the inventory item icon; in-world rendering is handled entirely by the BER.
     * The single static variant points at {@code wobbly_water.json}, a basin shape with water top.
     * The item model points at {@code wobbly_water_base.json} (same standalone model used by the BER).
     */
    private static void registerWobblyWater(
            BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier blockModelId = Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/wobbly_water");
        Identifier itemModelId  = Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/wobbly_water_base");
        MultiVariant model = BlockModelGenerators.plainVariant(blockModelId);

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(HoopyFroodTutBlocks.WOBBLY_WATER.get())
                        .with(PropertyDispatch.initial(WobblyWater.SURFACE_TEXTURE)
                                .select(WobblyWater.SurfaceTexture.WATER, model)
                                .select(WobblyWater.SurfaceTexture.LAVA,  model)
                                .select(WobblyWater.SurfaceTexture.SLIME, model)
                                .select(WobblyWater.SurfaceTexture.HONEY, model)
                                .select(WobblyWater.SurfaceTexture.MAGMA, model)));

        itemModels.itemModelOutput.accept(
                HoopyFroodItems.WOBBLY_WATER_ITEM.get(),
                ItemModelUtils.plainModel(itemModelId));
    }

    /**
     * Generates the blockstate JSON for the Banishing Bin.
     * The block uses RenderShape.INVISIBLE so the blockstate model is only needed for the
     * inventory item icon; in-world rendering is handled by the BER.
     */
    /**
     * Registers item models for all Wobbly Water bucket variants.
     * Water and lava reuse vanilla bucket models; slime/honey/magma use custom models
     * defined in src/main/resources that overlay vanilla block textures on the bucket shape.
     */
    private static void registerWobblyWaterBuckets(ItemModelGenerators itemModels) {
        itemModels.itemModelOutput.accept(
                HoopyFroodItems.WOBBLY_WATER_BUCKET.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath("minecraft", "item/water_bucket")));

        itemModels.itemModelOutput.accept(
                HoopyFroodItems.WOBBLY_LAVA_BUCKET.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath("minecraft", "item/lava_bucket")));

        itemModels.itemModelOutput.accept(
                HoopyFroodItems.WOBBLY_SLIME_BUCKET.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "item/wobbly_slime_bucket")));

        itemModels.itemModelOutput.accept(
                HoopyFroodItems.WOBBLY_HONEY_BUCKET.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "item/wobbly_honey_bucket")));

        itemModels.itemModelOutput.accept(
                HoopyFroodItems.WOBBLY_MAGMA_BUCKET.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "item/wobbly_magma_bucket")));
    }

    private static void registerBanishingBin(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        Identifier modelId = Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/banishing_bin_base");
        MultiVariant model = BlockModelGenerators.plainVariant(modelId);

        blockModels.blockStateOutput.accept(
                BlockModelGenerators.createSimpleBlock(
                        HoopyFroodTutBlocks.BANISHING_BIN.get(), model));

        itemModels.itemModelOutput.accept(
                HoopyFroodItems.BANISHING_BIN_ITEM.get(),
                ItemModelUtils.plainModel(modelId));
    }

    private static void registerProximitySensor(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        MultiVariant inactiveModel = BlockModelGenerators.plainVariant(
                Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/proximity_sensor"));
        MultiVariant activeModel = BlockModelGenerators.plainVariant(
                Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/proximity_sensor_active"));

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(HoopyFroodTutBlocks.PROXIMITY_SENSOR.get())
                        .with(PropertyDispatch.initial(ProximitySensor.ACTIVE)
                                .select(false, inactiveModel)
                                .select(true,  activeModel))
        );

        itemModels.itemModelOutput.accept(
                HoopyFroodItems.PROXIMITY_SENSOR_ITEM.get(),
                ItemModelUtils.plainModel(Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/proximity_sensor")));
    }

    private static void registerEjector(BlockModelGenerators blockModels) {
        MultiVariant model = BlockModelGenerators.plainVariant(
                Identifier.fromNamespaceAndPath(HoopyFroodTut.MODID, "block/ejector"));

        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(HoopyFroodTutBlocks.EJECTOR.get())
                        .with(PropertyDispatch.initial(Ejector.TRIGGERED)
                                .select(false, model)
                                .select(true,  model))
                        .with(BlockModelGenerators.ROTATION_FACING)
        );
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

package grill24.hoopyfroodtut.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import grill24.hoopyfroodtut.block.DisposableCaterpillar;
import grill24.hoopyfroodtut.blockentity.DisposableCaterpillarBlockEntity;
import grill24.hoopyfroodtut.blockentity.MovingBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodTut;
import grill24.hoopyfroodtut.core.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.List;

public class DisposableCaterpillarRenderer implements BlockEntityRenderer<DisposableCaterpillarBlockEntity, DisposableCaterpillarRenderState> {

    private static final float AMPLITUDE = 1f / 48f;
    private static final float BREATHE_AMPLITUDE = 0.05f;
    private static final float ANIMATION_SPEED = 0.5f;
    private static final float PHASE_STEP = (float) (Math.PI / 3);


    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> FULL_MODEL_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ": disposable_caterpillar_full");

    public static final StandaloneModelKey<BlockStateModelPart> BASE_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ": disposable_caterpillar_active");
    public static final StandaloneModelKey<BlockStateModelPart>[] SEGMENT_KEYS = new StandaloneModelKey[10];
    public static final StandaloneModelKey<BlockStateModelPart> LEGS_KEY;

    static {
        for (int i = 0; i < 10; i++) {
            final int index = i;
            SEGMENT_KEYS[i] = new StandaloneModelKey<>(
                    () -> HoopyFroodTut.MODID + ": disposable_caterpillar_seg" + index);
        }
        LEGS_KEY = new StandaloneModelKey<>(
                () -> HoopyFroodTut.MODID + ": disposable_caterpillar_legs");
    }

    public DisposableCaterpillarRenderer(BlockEntityRendererProvider.Context context) {}

    /**
     * Applies the same rotation the blockstate JSON uses for each facing direction,
     * centred on the block's midpoint so the model stays in place.
     */
    private static void applyFacingRotation(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(270));
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case DOWN  -> poseStack.mulPose(Axis.XP.rotationDegrees(270));
            case UP    -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            default    -> {} // NORTH — no rotation
        }
        poseStack.translate(-0.5, -0.5, -0.5);
    }

    /**
     * Returns the world direction the caterpillar's belly (leg side) faces,
     * accounting for the blockstate rotation applied to the model.
     */
    private static Direction getBellyDirection(Direction facing) {
        return switch (facing) {
            case DOWN -> Direction.NORTH;
            case UP -> Direction.SOUTH;
            default -> Direction.DOWN;
        };
    }

    @Override
    public DisposableCaterpillarRenderState createRenderState() {
        return new DisposableCaterpillarRenderState();
    }

    @Override
    public void extractRenderState(DisposableCaterpillarBlockEntity blockEntity, DisposableCaterpillarRenderState renderState, float partialTick, Vec3 cameraPos, ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        BlockState blockState = blockEntity.getBlockState();
        renderState.facing = blockState.getValue(DisposableCaterpillar.FACING);
        renderState.isActive = blockState.getValue(DisposableCaterpillar.TRIGGERED);

        int advanceTimestamp = blockEntity.getAdvanceTimestamp();
        float forwardProgress = 0f;
        if (advanceTimestamp > 0) {
            long elapsedTime = blockEntity.getLevel().getGameTime() - advanceTimestamp;
            forwardProgress = Math.min(elapsedTime + partialTick, blockEntity.getAdvanceForwardDuration()) / blockEntity.getAdvanceForwardDuration(); // progress from 0 to 1 over ADVANCE_FORWARD_DURATION ticks
            forwardProgress = Util.easeInOutCubic(forwardProgress);
        }
        renderState.forwardOffset = forwardProgress;

        Level level = blockEntity.getLevel();
        if (level != null) {
            renderState.animTime = (level.getGameTime() + partialTick) * ANIMATION_SPEED;

            Direction bellyDir = getBellyDirection(renderState.facing);
            BlockPos bellyPos = blockEntity.getBlockPos().relative(bellyDir);
            renderState.hasSturdy = level.getBlockState(bellyPos).isFaceSturdy(level, bellyPos, bellyDir.getOpposite());
        }
    }

    @Override
    public void submit(DisposableCaterpillarRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        var modelManager = Minecraft.getInstance().getModelManager();

        poseStack.pushPose();
        applyFacingRotation(poseStack, state.facing);
        // Move forward in the facing direction by state.forwardOffset, so the caterpillar can extend out of the block when active
        poseStack.translate(0, 0, -state.forwardOffset);

        if (state.isActive) {
            BlockStateModelPart activeBasePart = modelManager.getStandaloneModel(BASE_KEY);
            if (activeBasePart != null) {
                submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                        List.of(activeBasePart), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }


            for (int i = 0; i < 10; i++) {
                BlockStateModelPart segPart = modelManager.getStandaloneModel(SEGMENT_KEYS[i]);

                if (segPart == null) continue;

                float phase = state.animTime + i * PHASE_STEP;
                float yOffset = (i == 0) ? 0 : (float) (Math.sin(phase) * AMPLITUDE);
                float breatheScale = (i == 0) ? 1f : 1f + (float) (Math.sin(phase) * BREATHE_AMPLITUDE);

                poseStack.pushPose();
                // Translate to segment center (accounting for bob), scale X/Y to breathe, then restore origin
                poseStack.translate(0.5, 0.5 + yOffset, 0.5);
                poseStack.scale(breatheScale, breatheScale, 1f);
                poseStack.translate(-0.5, -0.5, -0.5);
                submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                        List.of(segPart), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        } else {
            BlockStateModelPart inactiveFullModel = modelManager.getStandaloneModel(FULL_MODEL_KEY);
            if (inactiveFullModel != null) {
                submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                        List.of(inactiveFullModel), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }
        }

        if (state.hasSturdy) {
            BlockStateModelPart legsPart = modelManager.getStandaloneModel(LEGS_KEY);
            if (legsPart != null) {
                submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                        List.of(legsPart), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            }
        }

        poseStack.popPose();
    }
}

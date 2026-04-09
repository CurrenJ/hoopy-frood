package grill24.hoopyfroodtut.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.block.BalancerNode;
import grill24.hoopyfroodtut.blockentity.BalancerNodeBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.List;

/**
 * Renders the Balancer Node's three probe elements as independently spinning parts.
 *
 * <p>Spin-up is sequential: elem 0 (back plate) reaches full speed first, then
 * elem 1 (middle, opposite spin), then elem 2 (front tip). Spin-down reverses the
 * order. All rotation angles are accumulated delta-per-frame to eliminate cumulative
 * game-time error from long frames or chunk reloads.
 *
 * <p>Activity is driven by the POWERED block state, which the server sets via the
 * configurable {@code balancerActivityCooldown} (ticks after last transfer).
 */
public class BalancerNodeRenderer
        implements BlockEntityRenderer<BalancerNodeBlockEntity, BalancerNodeRenderState> {

    /** Rotation speed at full spin: 0.5 rev/s → π/20 rad/tick at 20 TPS. */
    private static final float SPIN_SPEED = (float) (Math.PI / 20.0);

    /** Ticks for each element to ramp from 0 → full speed (or full → 0). */
    private static final float SPIN_UP_DURATION = 20f;

    /** Total spin-time range: 3 elements × ramp-up duration. */
    private static final float TOTAL_SPIN_TIME = 3f * SPIN_UP_DURATION;

    /** Element Z-centres in block space [0, 1], used as pivot for the spin rotation. */
    private static final float ELEM0_Z = (15f + 16f) / 32f; // 0.96875
    private static final float ELEM1_Z = (13f + 15f) / 32f; // 0.875
    private static final float ELEM2_Z = (9f + 13f)  / 32f; // 0.6875

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> ELEM0_KEY =
            new StandaloneModelKey<>(() -> HoopyFroodTut.MODID + ":balancer_node_elem0");
    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> ELEM1_KEY =
            new StandaloneModelKey<>(() -> HoopyFroodTut.MODID + ":balancer_node_elem1");
    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> ELEM2_KEY =
            new StandaloneModelKey<>(() -> HoopyFroodTut.MODID + ":balancer_node_elem2");

    public BalancerNodeRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public BalancerNodeRenderState createRenderState() {
        return new BalancerNodeRenderState();
    }

    @Override
    public void extractRenderState(
            BalancerNodeBlockEntity blockEntity,
            BalancerNodeRenderState state,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);

        BlockState blockState = blockEntity.getBlockState();
        state.facing = blockState.getValue(BalancerNode.FACING);

        Level level = blockEntity.getLevel();
        if (level == null) return;

        // Raw game time in ticks (float, interpolated). Used only for delta calculation.
        float gameTime = level.getGameTime() + partialTick;
        float dt = (blockEntity.clientLastAnimTime >= 0f)
                ? gameTime - blockEntity.clientLastAnimTime
                : 0f;
        blockEntity.clientLastAnimTime = gameTime;

        // Drive animation from the POWERED block state (updated by server via cooldown).
        boolean isActive = blockState.getValue(BalancerNode.POWERED);

        // Advance or retreat the unified spin-time accumulator.
        if (isActive) {
            blockEntity.clientSpinTime = Math.min(TOTAL_SPIN_TIME, blockEntity.clientSpinTime + dt);
        } else {
            blockEntity.clientSpinTime = Math.max(0f, blockEntity.clientSpinTime - dt);
        }

        // Derive per-element speed progress from consecutive bands of spin-time.
        float t = blockEntity.clientSpinTime;
        float prog0 = Util.easeInOutCubic(Math.min(1f, t / SPIN_UP_DURATION));
        float prog1 = Util.easeInOutCubic(Math.min(1f, Math.max(0f, (t - SPIN_UP_DURATION)       / SPIN_UP_DURATION)));
        float prog2 = Util.easeInOutCubic(Math.min(1f, Math.max(0f, (t - 2f * SPIN_UP_DURATION)  / SPIN_UP_DURATION)));

        // Accumulate rotation angles (delta-time, avoids cumulative game-time error).
        blockEntity.clientPhase0 += dt * SPIN_SPEED * prog0;
        blockEntity.clientPhase1 -= dt * SPIN_SPEED * prog1; // opposite spin direction
        blockEntity.clientPhase2 += dt * SPIN_SPEED * prog2;

        state.phase0 = blockEntity.clientPhase0;
        state.phase1 = blockEntity.clientPhase1;
        state.phase2 = blockEntity.clientPhase2;
    }

    @Override
    public void submit(
            BalancerNodeRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector nodes,
            CameraRenderState camera) {
        var mm = Minecraft.getInstance().getModelManager();

        poseStack.pushPose();
        applyFacingRotation(poseStack, state.facing);

        submitElement(poseStack, nodes, state, mm.getStandaloneModel(ELEM0_KEY), ELEM0_Z, state.phase0);
        submitElement(poseStack, nodes, state, mm.getStandaloneModel(ELEM1_KEY), ELEM1_Z, state.phase1);
        submitElement(poseStack, nodes, state, mm.getStandaloneModel(ELEM2_KEY), ELEM2_Z, state.phase2);

        poseStack.popPose();
    }

    /**
     * Submits one element model part, rotating it about its own Z-axis centre.
     * The Z axis (after facing rotation) aligns with the protrusion direction of the node.
     */
    private static void submitElement(
            PoseStack poseStack,
            SubmitNodeCollector nodes,
            BalancerNodeRenderState state,
            BlockStateModelPart model,
            float zCenter,
            float angle) {
        if (model == null) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, zCenter);
        poseStack.mulPose(Axis.ZP.rotation(angle));
        poseStack.translate(-0.5, -0.5, -zCenter);
        nodes.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                List.of(model), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    /**
     * Replicates the inverse of the ROTATION_FACING blockstate rotation so that the
     * model's protrusion axis aligns with the world-space facing direction.
     *
     * <p>Blockstate JSON applies a forward rotation to vertices; the PoseStack
     * coordinate-frame rotation needs the inverse to produce the same visual result.
     */
    private static void applyFacingRotation(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5, 0.5, 0.5);
        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST  -> poseStack.mulPose(Axis.YP.rotationDegrees(270)); // blockstate y:90
            case WEST  -> poseStack.mulPose(Axis.YP.rotationDegrees(90));  // blockstate y:270
            case DOWN  -> poseStack.mulPose(Axis.XP.rotationDegrees(270)); // blockstate x:90
            case UP    -> poseStack.mulPose(Axis.XP.rotationDegrees(90));  // blockstate x:270
            default    -> {}                                                // NORTH — no rotation
        }
        poseStack.translate(-0.5, -0.5, -0.5);
    }
}

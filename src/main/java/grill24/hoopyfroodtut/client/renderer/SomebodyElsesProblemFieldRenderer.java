package grill24.hoopyfroodtut.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import grill24.hoopyfroodtut.blockentity.SomebodyElsesProblemFieldBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodTut;
import grill24.hoopyfroodtut.core.Util;
import org.joml.Quaternionf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.List;

/**
 * Renders the Somebody Else's Problem Field with a three-ring concentric animation.
 *
 * <p>On activation (redstone signal), the rings smoothly rise to staggered heights
 * (inner highest, outer lowest) over {@value #ANIM_DURATION} animation-time units.
 * Four floater cubes simultaneously rise above the rings and begin orbiting the
 * block centre. All transitions ease in-and-out cubically and reverse cleanly on
 * deactivation.
 */
public class SomebodyElsesProblemFieldRenderer
        implements BlockEntityRenderer<SomebodyElsesProblemFieldBlockEntity, SomebodyElsesProblemFieldRenderState> {

    // Animation time unit: 1 unit = 1/ANIMATION_SPEED game ticks = 1 real second at 20 TPS.
    private static final float ANIMATION_SPEED = 0.02f;

    /** Duration of the rise/fall transition in animation-time units (= seconds). */
    private static final float ANIM_DURATION = 2f;

    // Target Y offsets when fully active (in blocks, i.e. 1/16 = one MC pixel).
    private static final float INNER_RING_TARGET_Y  = 7f / 16f;
    private static final float MID_RING_TARGET_Y    = 6f / 16f;
    private static final float OUTER_RING_TARGET_Y  = 5f / 16f;
    private static final float FLOATER_TARGET_Y     = 12f / 16f;

    /** Maximum orbit radius from the block centre when fully active. */
    private static final float FLOATER_ORBIT_RADIUS = 3.5f / 16f;

    /**
     * Orbit speed in radians per animation-time unit (≈ radians per second).
     * 2 rad/s gives roughly one full revolution every π ≈ 3.1 seconds.
     */
    private static final float ORBIT_SPEED = 2.0f;

    /**
     * Natural XZ centre of the floater model cube in block space (7.5 / 16).
     * Used to compute the translation needed to reach the desired orbit position.
     */
    private static final float FLOATER_NATURAL_CENTER = 7.5f / 16f;

    /** Y centre of the floater model cube in block space (0.5 / 16). */
    private static final float FLOATER_Y_CENTER = 0.5f / 16f;

    /** Floater tumble speeds (rad/anim-time) per axis — prime-ish ratios keep the motion aperiodic. */
    private static final float FLOATER_SPIN_Y = 4.0f;
    private static final float FLOATER_SPIN_X = 2.7f;
    private static final float FLOATER_SPIN_Z = 1.9f;

    /** Peak vertical bob displacement for each ring (in blocks). */
    private static final float BOB_AMPLITUDE = 1.5f / 16f;
    /** Bob oscillation frequency in radians per animation-time unit. */
    private static final float BOB_SPEED = 1.5f;

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> INNER_RING_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":sep_field_inner_ring");

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> MID_RING_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":sep_field_mid_ring");

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> OUTER_RING_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":sep_field_outer_ring");

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> FLOATER_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":sep_field_floater");

    public SomebodyElsesProblemFieldRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public SomebodyElsesProblemFieldRenderState createRenderState() {
        return new SomebodyElsesProblemFieldRenderState();
    }

    @Override
    public void extractRenderState(
            SomebodyElsesProblemFieldBlockEntity blockEntity,
            SomebodyElsesProblemFieldRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        Level level = blockEntity.getLevel();
        if (level == null) return;

        boolean isActive = blockEntity.isActive();
        float lastToggleTime = blockEntity.getLastToggleTime();
        float currentAnimTime = (level.getGameTime() + partialTick) * ANIMATION_SPEED;

        float activeProg;
        if (lastToggleTime < 0f) {
            // Never been toggled — snap to current state with no transition.
            activeProg = isActive ? 1f : 0f;
        } else {
            float elapsed = currentAnimTime - lastToggleTime * ANIMATION_SPEED;
            float t = Math.min(1f, Math.max(0f, elapsed / ANIM_DURATION));
            t = Util.easeInOutCubic(t);
            // IN transition: t goes 0→1; OUT transition: 1→0.
            activeProg = isActive ? t : 1f - t;
        }

        renderState.ringProgress = activeProg;
        // Orbit angle advances continuously regardless of active state so the
        // floaters resume orbiting smoothly if the field is re-activated.
        renderState.orbitAngle = currentAnimTime * ORBIT_SPEED;
    }

    @Override
    public void submit(
            SomebodyElsesProblemFieldRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {

        var mc = Minecraft.getInstance();
        float prog = state.ringProgress;
        float animTime = state.orbitAngle / ORBIT_SPEED;

        float innerY  = INNER_RING_TARGET_Y  * prog;
        float midY    = MID_RING_TARGET_Y    * prog;
        float outerY  = OUTER_RING_TARGET_Y  * prog;
        float floaterY = FLOATER_TARGET_Y    * prog;
        float orbitR  = FLOATER_ORBIT_RADIUS * prog;

        // Per-ring bob: sine wave scaled by prog so it settles flat when inactive.
        // Phases are spaced 120° apart for aF gentle ripple across the three rings.
        float bobBase  = animTime * BOB_SPEED;
        float innerBob = BOB_AMPLITUDE * (float) Math.sin(bobBase)                        * prog;
        float midBob   = BOB_AMPLITUDE * (float) Math.sin(bobBase + Math.PI * 2.0 / 3.0) * prog;
        float outerBob = BOB_AMPLITUDE * (float) Math.sin(bobBase + Math.PI * 4.0 / 3.0) * prog;

        // --- Outer ring (lowest endpoint) ---
        BlockStateModelPart outerRing = mc.getModelManager().getStandaloneModel(OUTER_RING_KEY);
        if (outerRing != null) {
            poseStack.pushPose();
            poseStack.translate(0f, outerY + outerBob, 0f);
            submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                    List.of(outerRing), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // --- Mid ring ---
        BlockStateModelPart midRing = mc.getModelManager().getStandaloneModel(MID_RING_KEY);
        if (midRing != null) {
            poseStack.pushPose();
            poseStack.translate(0f, midY + midBob, 0f);
            submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                    List.of(midRing), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // --- Inner ring (highest endpoint) ---
        BlockStateModelPart innerRing = mc.getModelManager().getStandaloneModel(INNER_RING_KEY);
        if (innerRing != null) {
            poseStack.pushPose();
            poseStack.translate(0f, innerY + innerBob, 0f);
            submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                    List.of(innerRing), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // --- 4 orbiting floaters ---
        BlockStateModelPart floater = mc.getModelManager().getStandaloneModel(FLOATER_KEY);
        if (floater != null) {
            for (int i = 0; i < 4; i++) {
                float phase = i * (float) (Math.PI * 0.5);
                float angle = state.orbitAngle + phase;
                float targetX = 0.5f + orbitR * (float) Math.cos(angle);
                float targetZ = 0.5f + orbitR * (float) Math.sin(angle);

                // Full-axis tumble: each axis uses a different speed so the rotation is aperiodic.
                // Phase offset staggers each floater. All angles scale to 0 with prog.
                float tau = (float) Math.TAU;
                float spinY = ((animTime * FLOATER_SPIN_Y + phase)          % tau) * prog;
                float spinX = ((animTime * FLOATER_SPIN_X + phase * 0.618f) % tau) * prog;
                float spinZ = ((animTime * FLOATER_SPIN_Z + phase * 1.272f) % tau) * prog;

                poseStack.pushPose();
                poseStack.translate(targetX - FLOATER_NATURAL_CENTER, floaterY, targetZ - FLOATER_NATURAL_CENTER);
                poseStack.rotateAround(
                        new Quaternionf().rotationYXZ(spinY, spinX, spinZ),
                        FLOATER_NATURAL_CENTER, FLOATER_Y_CENTER, FLOATER_NATURAL_CENTER);
                submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                        List.of(floater), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                poseStack.popPose();
            }
        }
    }
}

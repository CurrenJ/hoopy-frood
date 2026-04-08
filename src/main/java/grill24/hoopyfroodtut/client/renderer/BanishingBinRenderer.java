package grill24.hoopyfroodtut.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import grill24.hoopyfroodtut.blockentity.BanishingBinBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodTut;
import grill24.hoopyfroodtut.core.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import java.util.List;

/**
 * Renders the Banishing Bin as an armillary-sphere gyroscope.
 *
 * <p>Each of the three rings is formed by a circle of orbiting item models:
 * <ul>
 *   <li><b>Ring A</b> — equatorial, iron ingots.</li>
 *   <li><b>Ring B</b> — tilted +60° on X, blaze powder.</li>
 *   <li><b>Ring C</b> — tilted −60° on X, ender pearls.</li>
 * </ul>
 *
 * <p>Additionally, any items currently queued for banishment appear as a second layer
 * of orbiting models on their assigned ring, at a smaller orbit radius.
 *
 * <p>Animation states:
 * <ul>
 *   <li><b>Inactive:</b> rings coplanar (flat), slow spin.</li>
 *   <li><b>Active→Inactive transition:</b> rings decelerate and de-tilt over ~1.5 s.</li>
 *   <li><b>Active:</b> rings tilted to gyroscopic configuration, full spin speed.</li>
 * </ul>
 */
public class BanishingBinRenderer
        implements BlockEntityRenderer<BanishingBinBlockEntity, BanishingBinRenderState> {

    // ── Animation tuning ──────────────────────────────────────────────────────

    /** Converts game ticks to seconds for animation calculations. */
    private static final float ANIM_SPEED = 0.2f / 20.0f;

    /** Duration of the active↔inactive transition in seconds. */
    private static final float TRANSITION_DURATION = 1.5f;

    /** Base pedestal constant Y-rotation speed (~6 RPM = 0.314 rad/s). */
    private static final float BASE_SPIN_SPEED = 0.314f;

    /** Ring rotation speeds (rad/s) at full activity — prime-ish ratios keep phases aperiodic. */
    private static final float RING_A_SPEED_ACTIVE = 1.8f;
    private static final float RING_B_SPEED_ACTIVE = 2.3f;
    private static final float RING_C_SPEED_ACTIVE = 1.3f;

    /** Tilt angles (radians) for rings B and C at full activity. */
    private static final float RING_B_TILT = (float) Math.toRadians(60.0);
    private static final float RING_C_TILT = (float) Math.toRadians(-60.0);

    /** Precession speeds (rad/s) — how fast each ring's tilt axis rotates around Y. */
    private static final float RING_B_PREC_SPEED = 0.31f;
    private static final float RING_C_PREC_SPEED = 0.23f;

    /** Y center of all rings when fully inactive (sunken position). */
    private static final float RING_Y_INACTIVE = 4.0f / 16.0f;
    /** Y center of all rings when fully active. */
    private static final float RING_Y_ACTIVE = 0.5f;

    // ── Ring item configuration ───────────────────────────────────────────────

    /** Radius (in blocks) of each structural ring from block center. */
    private static final float RING_A_RADIUS = 5.5f / 16.0f;
    private static final float RING_B_RADIUS = 4.5f / 16.0f;
    private static final float RING_C_RADIUS = 3.5f / 16.0f;

    /** Scale of each structural ring item. */
    private static final float RING_ITEM_SCALE = 0.12f;

    /** How fast each ring item spins on its own axis as it orbits (multiplier on orbit angle). */
    private static final float RING_ITEM_SELF_SPIN = 2.5f;

    // ── Banished item orbit configuration ────────────────────────────────────

    /** Radius (in blocks) of queued-for-banishment items — slightly inside the ring. */
    private static final float BANISH_ORBIT_RADIUS = 2f / 16.0f;

    /** Scale of items orbiting prior to banishment. */
    private static final float BANISH_ITEM_SCALE = 0.15f;

    // ── Model key — base pedestal only ───────────────────────────────────────

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> BASE_KEY =
            new StandaloneModelKey<>(() -> HoopyFroodTut.MODID + ":banishing_bin_base");

    // ── Constructor ───────────────────────────────────────────────────────────

    public BanishingBinRenderer(BlockEntityRendererProvider.Context context) {}

    // ── BlockEntityRenderer ───────────────────────────────────────────────────

    @Override
    public BanishingBinRenderState createRenderState() {
        return new BanishingBinRenderState();
    }

    @Override
    public void extractRenderState(
            BanishingBinBlockEntity blockEntity,
            BanishingBinRenderState state,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTick, cameraPos, crumblingOverlay);

        Level level = blockEntity.getLevel();
        if (level == null) return;

        float currentTime = (level.getGameTime() + partialTick) * ANIM_SPEED;
        state.animTime = currentTime;
        state.lastReceivedTick = blockEntity.getLastReceivedTick();
        state.activationTick   = blockEntity.getActivationTick();

        // ── 1. Compute ringProgress (tilt + speed driver) ─────────────────────
        //
        // activationTick: when the bin first became active after being idle.
        //   → drives the spin-UP animation (plays once, not reset by each new item).
        // lastReceivedTick: when the most recent item arrived.
        //   → determines the activity window; spin-DOWN starts when it expires.
        long lastReceived   = blockEntity.getLastReceivedTick();
        long activationTick = blockEntity.getActivationTick();

        if (activationTick < 0) {
            // Never been activated.
            state.ringProgress = 0f;
        } else {
            boolean isActive = lastReceived >= 0
                    && (level.getGameTime() + partialTick - lastReceived)
                       <= BanishingBinBlockEntity.ACTIVITY_WINDOW;

            if (isActive) {
                // Spin-up: measured from the ONE moment the bin first became active,
                // not from the last item — so hopper feeding doesn't restart the animation.
                float timeSinceActivation = (level.getGameTime() + partialTick - activationTick) * ANIM_SPEED;
                state.ringProgress = Util.easeInOutCubic(Math.min(1f, timeSinceActivation / TRANSITION_DURATION));
            } else {
                // Spin-down: measured from when the activity window expired.
                float deactivationTime = (lastReceived + BanishingBinBlockEntity.ACTIVITY_WINDOW) * ANIM_SPEED;
                float timeSinceDeactivation = currentTime - deactivationTime;
                state.ringProgress = 1f - Util.easeInOutCubic(
                        Math.min(1f, Math.max(0f, timeSinceDeactivation / TRANSITION_DURATION)));
            }
            state.ringProgress = Math.max(0f, Math.min(1f, state.ringProgress));
        }

        // ── 2. Accumulate ring phases using delta time ─────────────────────────
        // The render state object is recreated every frame, so we persist accumulated
        // phase angles on the block entity itself (client-only fields, not saved/synced).
        if (blockEntity.clientLastAnimTime >= 0f) {
            float dt = currentTime - blockEntity.clientLastAnimTime;
            float prog = state.ringProgress;
            blockEntity.clientRingPhaseA += dt * RING_A_SPEED_ACTIVE * prog;
            blockEntity.clientRingPhaseB += dt * RING_B_SPEED_ACTIVE * prog;
            blockEntity.clientRingPhaseC += dt * RING_C_SPEED_ACTIVE * prog;
            blockEntity.clientPrecPhaseB  += dt * RING_B_PREC_SPEED  * prog;
            blockEntity.clientPrecPhaseC  += dt * RING_C_PREC_SPEED  * prog;
        }
        blockEntity.clientLastAnimTime = currentTime;
        state.ringPhaseA = blockEntity.clientRingPhaseA;
        state.ringPhaseB = blockEntity.clientRingPhaseB;
        state.ringPhaseC = blockEntity.clientRingPhaseC;
        state.precPhaseB = blockEntity.clientPrecPhaseB;
        state.precPhaseC = blockEntity.clientPrecPhaseC;

        // ── 3. Populate per-ring queued-item lists ─────────────────────────────
        state.ring0Items.clear();
        state.ring1Items.clear();
        state.ring2Items.clear();
        for (var queued : blockEntity.getQueue()) {
            switch (queued.ring()) {
                case 0 -> state.ring0Items.add(queued.stack().copy());
                case 1 -> state.ring1Items.add(queued.stack().copy());
                case 2 -> state.ring2Items.add(queued.stack().copy());
            }
        }

        // ── 4. Partition banished-type memory across the three structural rings ─
        state.memoryRing0.clear();
        state.memoryRing1.clear();
        state.memoryRing2.clear();
        var banishedList = new java.util.ArrayList<>(blockEntity.getBanishedTypes());
        int total = banishedList.size();
        int cut1 = total / 3;
        int cut2 = (total * 2) / 3;
        for (int i = 0; i < total; i++) {
            ItemStack s = banishedList.get(i).getDefaultInstance();
            if (i < cut1)       state.memoryRing0.add(s);
            else if (i < cut2)  state.memoryRing1.add(s);
            else                state.memoryRing2.add(s);
        }
    }

    @Override
    public void submit(
            BanishingBinRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector nodes,
            CameraRenderState camera) {

        var mc = Minecraft.getInstance();
        float t = state.animTime;
        float prog = state.ringProgress;

        // Accumulated angles — updated delta-per-frame in extractRenderState to prevent jumps
        float angleA = state.ringPhaseA;
        float angleB = state.ringPhaseB;
        float angleC = state.ringPhaseC;

        float tiltB = RING_B_TILT * prog;
        float tiltC = RING_C_TILT * prog;
        float precB = state.precPhaseB;
        float precC = state.precPhaseC;
        float ringCenterY = RING_Y_INACTIVE + (RING_Y_ACTIVE - RING_Y_INACTIVE) * prog;

        ItemModelResolver resolver = mc.getItemModelResolver();

        // ── Base pedestal (always rotating) ───────────────────────────────────
        BlockStateModelPart base = mc.getModelManager().getStandaloneModel(BASE_KEY);
        if (base != null) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.mulPose(Axis.YP.rotation(t * BASE_SPIN_SPEED));
            poseStack.translate(-0.5f, 0f, -0.5f);
            nodes.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                    List.of(base), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // ── Ring A — equatorial (no tilt), memory items ───────────────────────
        renderItemRing(state.memoryRing0,
                RING_A_RADIUS, RING_ITEM_SCALE,
                angleA, 0f, 0f, ringCenterY, poseStack, nodes, state, resolver);

        // ── Ring B — tilted +60°, memory items ────────────────────────────────
        renderItemRing(state.memoryRing1,
                RING_B_RADIUS, RING_ITEM_SCALE,
                angleB, tiltB, precB, ringCenterY, poseStack, nodes, state, resolver);

        // ── Ring C — tilted −60°, memory items ────────────────────────────────
        renderItemRing(state.memoryRing2,
                RING_C_RADIUS, RING_ITEM_SCALE,
                angleC, tiltC, precC, ringCenterY, poseStack, nodes, state, resolver);

        // ── Queued items orbiting prior to banishment ─────────────────────────
        renderBanishedItems(state.ring0Items, angleA, 0f,    0f,    ringCenterY, poseStack, nodes, state, resolver);
        renderBanishedItems(state.ring1Items, angleB, tiltB, precB, ringCenterY, poseStack, nodes, state, resolver);
        renderBanishedItems(state.ring2Items, angleC, tiltC, precC, ringCenterY, poseStack, nodes, state, resolver);
    }

    /**
     * Renders each item in {@code items} equally spaced in a circle (the structural ring).
     * Each item also spins on its own axis proportional to its orbit angle.
     * Does nothing if {@code items} is empty.
     */
    private static void renderItemRing(
            List<ItemStack> items,
            float radius,
            float scale,
            float orbitAngle,
            float tiltX,
            float precAngle,
            float centerY,
            PoseStack poseStack,
            SubmitNodeCollector nodes,
            BanishingBinRenderState state,
            ItemModelResolver resolver) {

        int count = items.size();
        if (count == 0) return;

        float spacing = (float) (Math.PI * 2.0 / count);
        float cosP = (float) Math.cos(precAngle);
        float sinP = (float) Math.sin(precAngle);
        float cosT = (float) Math.cos(tiltX);
        float sinT = (float) Math.sin(tiltX);

        for (int i = 0; i < count; i++) {
            float theta = orbitAngle + i * spacing;

            float localX = radius * (float) Math.cos(theta);
            float localZ = radius * (float) Math.sin(theta);

            // Precessing tilt: rotate tilt axis around Y by precAngle.
            // Step 1 — align tilt axis with X (rotate position by -precAngle around Y)
            float rotX =  localX * cosP + localZ * sinP;
            float rotZ = -localX * sinP + localZ * cosP;
            // Step 2 — apply tilt around X axis
            float tiltedZ =  rotZ * cosT;
            float worldY   = -rotZ * sinT;
            // Step 3 — rotate back around Y by +precAngle
            float worldX = rotX * cosP - tiltedZ * sinP;
            float worldZ = rotX * sinP + tiltedZ * cosP;

            poseStack.pushPose();
            poseStack.translate(0.5f + worldX, centerY + worldY, 0.5f + worldZ);
            // Self-spin around Y so items tumble as they orbit
            poseStack.mulPose(Axis.YP.rotation(theta * RING_ITEM_SELF_SPIN));
            poseStack.scale(scale, scale, scale);

            ItemStackRenderState itemState = new ItemStackRenderState();
            resolver.updateForTopItem(itemState, items.get(i), ItemDisplayContext.FIXED, null, null, 0);
            itemState.submit(poseStack, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

            poseStack.popPose();
        }
    }

    /**
     * Renders items queued for banishment equally spaced on their assigned ring,
     * orbiting at a slightly tighter radius than the structural ring items.
     */
    private static void renderBanishedItems(
            List<ItemStack> items,
            float ringPhase,
            float tiltX,
            float precAngle,
            float centerY,
            PoseStack poseStack,
            SubmitNodeCollector nodes,
            BanishingBinRenderState state,
            ItemModelResolver resolver) {

        if (items.isEmpty()) return;

        int count = items.size();
        float orbitAngle = ringPhase;
        float spacing = (float) (Math.PI * 2.0 / count);
        float cosP = (float) Math.cos(precAngle);
        float sinP = (float) Math.sin(precAngle);
        float cosT = (float) Math.cos(tiltX);
        float sinT = (float) Math.sin(tiltX);

        for (int i = 0; i < count; i++) {
            float theta = orbitAngle + i * spacing;

            float localX = BANISH_ORBIT_RADIUS * (float) Math.cos(theta);
            float localZ = BANISH_ORBIT_RADIUS * (float) Math.sin(theta);

            float rotX =  localX * cosP + localZ * sinP;
            float rotZ = -localX * sinP + localZ * cosP;
            float tiltedZ =  rotZ * cosT;
            float worldY   = -rotZ * sinT;
            float worldX = rotX * cosP - tiltedZ * sinP;
            float worldZ = rotX * sinP + tiltedZ * cosP;

            poseStack.pushPose();
            poseStack.translate(0.5f + worldX, centerY + worldY, 0.5f + worldZ);
            poseStack.mulPose(Axis.YP.rotation(theta));
            poseStack.scale(BANISH_ITEM_SCALE, BANISH_ITEM_SCALE, BANISH_ITEM_SCALE);

            ItemStackRenderState itemState = new ItemStackRenderState();
            resolver.updateForTopItem(itemState, items.get(i), ItemDisplayContext.FIXED, null, null, 0);
            itemState.submit(poseStack, nodes, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

            poseStack.popPose();
        }
    }
}

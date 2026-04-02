package grill24.hoopyfroodtut.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import grill24.hoopyfroodtut.blockentity.InfiniteImprobabilityDriveBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodTut;
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
 * Renders the Infinite Improbability Drive machine body and, when active, the
 * input item floating and spinning above the centre of the block.
 *
 * <p>The item animation consists of:
 * <ul>
 *   <li>A slow vertical bob driven by a sine wave.</li>
 *   <li>Continuous Y-axis rotation.</li>
 * </ul>
 */
public class InfiniteImprobabilityDriveRenderer
        implements BlockEntityRenderer<InfiniteImprobabilityDriveBlockEntity, InfiniteImprobabilityDriveRenderState> {

    // Speed at which animTime advances relative to game ticks.
    private static final float ANIMATION_SPEED = 0.05f;

    // Floating item constants
    /** Base Y height of the floating item above the block origin (in blocks). */
    private static final float ITEM_BASE_Y = 1f;
    /** Amplitude of the vertical bob in blocks. */
    private static final float BOB_AMPLITUDE = 0.08f;
    /** Radians per animTime unit for the Y-axis spin. */
    private static final float SPIN_SPEED = (float) (Math.PI * 2 * 0.4);
    /** Uniform scale applied to the rendered item. */
    private static final float ITEM_SCALE = 0.6f;
    private static final float STAMP_AMPLITUDE = 11.0f / 16.0f;

    // Breathing effect constants
    /** Amplitude of the breathing squish effect (ratio). */
    private static final float BREATHE_AMPLITUDE = 0.06f;
    /** Speed of the breathing effect in cycles per second. */
    private static final float BREATHE_SPEED = 0.75f;

    // Support animation constants
    /** Duration of the support animation in seconds. */
    private static final float SUPPORT_ANIMATION_DURATION = 60.0f;
    /** Y offset when inactive. */
    private static final float SUPPORT_INACTIVE_Y = -4f / 16f;
    /** Y offset when active. */
    private static final float SUPPORT_ACTIVE_Y = 0f;

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> BASE_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":infinite_improbability_drive_base");

    public static final StandaloneModelKey<BlockStateModelPart> WHEEL_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":infinite_improbability_drive_wheel");

    public static final StandaloneModelKey<BlockStateModelPart> STAMP_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":infinite_improbability_drive_stamp");

    public static final StandaloneModelKey<BlockStateModelPart> SUPPORT_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":infinite_improbability_drive_support");

    public InfiniteImprobabilityDriveRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public InfiniteImprobabilityDriveRenderState createRenderState() {
        return new InfiniteImprobabilityDriveRenderState();
    }

    @Override
    public void extractRenderState(
            InfiniteImprobabilityDriveBlockEntity blockEntity,
            InfiniteImprobabilityDriveRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        // Update held item, but preserve cached item for scale-out animation
        ItemStack currentItem = blockEntity.getHeldItem().copy();
        renderState.heldItem = currentItem;
        renderState.cachedItem = blockEntity.getCachedItem().copy();

        Level level = blockEntity.getLevel();
        if (level != null) {
            // Compute animations relative to when the state last toggled
            boolean isActive = !renderState.heldItem.isEmpty();
            float toggleTime = blockEntity.getLastToggleTime() * ANIMATION_SPEED;
            float currentGameTime = (level.getGameTime() + partialTick) * ANIMATION_SPEED;
            float elapsed = currentGameTime - toggleTime;
            float elapsedSeconds = elapsed / ANIMATION_SPEED;
            
            // Animation progress (0 to 1 during transition, then clamped)
            float t = 0f;
            if (elapsedSeconds >= 0 && elapsedSeconds < SUPPORT_ANIMATION_DURATION) {
                t = elapsedSeconds / SUPPORT_ANIMATION_DURATION;
                // Ease in out cubic
                t = t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
            } else if (elapsedSeconds >= SUPPORT_ANIMATION_DURATION) {
                t = 1f;
            }

            // Support Y offset
            float targetY = isActive ? SUPPORT_ACTIVE_Y : SUPPORT_INACTIVE_Y;
            float startY = isActive ? SUPPORT_INACTIVE_Y : SUPPORT_ACTIVE_Y;
            renderState.supportY = startY + t * (targetY - startY);

            // Wheel spin speed
            float fullSpinSpeed = SPIN_SPEED * 0.5f;
            float targetSpin = isActive ? fullSpinSpeed : 0f;
            float startSpin = isActive ? 0f : fullSpinSpeed;
            renderState.wheelSpinSpeed = startSpin + t * (targetSpin - startSpin);

            // Stamp bob amplitude
            float targetAmp = isActive ? STAMP_AMPLITUDE : 0f;
            float startAmp = isActive ? 0f : STAMP_AMPLITUDE;
            renderState.stampBobAmplitude = startAmp + t * (targetAmp - startAmp);

            // Breathing amplitude
            float targetBreatheAmp = isActive ? BREATHE_AMPLITUDE : 0f;
            float startBreatheAmp = isActive ? 0f : BREATHE_AMPLITUDE;
            renderState.breatheAmplitude = startBreatheAmp + t * (targetBreatheAmp - startBreatheAmp);

            // Item scale
            float targetItemScale = isActive ? ITEM_SCALE : 0f;
            float startItemScale = isActive ? 0f : ITEM_SCALE;
            renderState.itemScale = startItemScale + t * (targetItemScale - startItemScale);

            // Wheel scale
            float targetWheelScale = isActive ? 1f : 0f;
            float startWheelScale = isActive ? 0f : 1f;
            renderState.wheelScale = startWheelScale + t * (targetWheelScale - startWheelScale);
            
            // Compute accumulated wheel rotation, accounting for spin speed ramping
            // This integrates the spin speed over the animation duration
            if (t < 1f && elapsedSeconds >= 0) {
                // During transition: accumulate rotation from spinning at varying speeds
                float rotationThisFrame = 0f;
                
                // We need to compute the rotation integral
                // For simplicity, we approximate using the average of spin speeds over small time intervals
                // Or we can use a more accurate quadrature method
                
                // Use trapezoid rule: approximate integral by taking values at start and end
                float spinSpeedAtStart = isActive ? 0f : fullSpinSpeed;
                float spinSpeedAtEnd = isActive ? fullSpinSpeed : 0f;
                float easeAtStartOfFrame = t > 0.01f ? 
                    (t - 0.01f) < 0.5f ? 
                        4 * (t - 0.01f) * (t - 0.01f) * (t - 0.01f) : 
                        1 - (float) Math.pow(-2 * (t - 0.01f) + 2, 3) / 2
                    : 0f;
                float easeAtEndOfFrame = t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
                
                float spinAtStart = spinSpeedAtStart + easeAtStartOfFrame * (spinSpeedAtEnd - spinSpeedAtStart);
                float spinAtEnd = spinSpeedAtStart + easeAtEndOfFrame * (spinSpeedAtEnd - spinSpeedAtStart);
                
                rotationThisFrame = (spinAtStart + spinAtEnd) / 2f * elapsed;
                renderState.wheelRotationOffset = rotationThisFrame;
            } else if (t >= 1f && !isActive) {
                // After ramp-down completes, wheel is stationary
                renderState.wheelRotationOffset = renderState.wheelRotationOffset;
            } else if (t >= 1f && isActive) {
                // After ramp-up completes, continue spinning at full speed
                renderState.wheelRotationOffset += elapsed * fullSpinSpeed;
            }
            
            // Store relative animation time (resets at toggle, used for periodic motions)
            renderState.animTime = elapsed;

            // Accumulate continuous rotation time for the floating item
            renderState.itemRotationTime = currentGameTime;

            // Calculate breathing effect with out-of-phase scales
            float breathPhase = currentGameTime * BREATHE_SPEED * (float) Math.PI * 2;
            renderState.breatheScaleX = 1f + (float) Math.sin(breathPhase) * renderState.breatheAmplitude;
            renderState.breatheScaleY = 1f + (float) Math.sin(breathPhase + Math.PI * 2/3) * renderState.breatheAmplitude;
            renderState.breatheScaleZ = 1f + (float) Math.sin(breathPhase + Math.PI * 4/3) * renderState.breatheAmplitude;

            HoopyFroodTut.LOGGER.debug("Extracted render state: supportY={}, targetY={}, wheelRotationOffset={}, itemScale={}, wheelScale={}",
                    renderState.supportY, targetY, renderState.wheelRotationOffset, renderState.itemScale, renderState.wheelScale);
        }
    }

    @Override
    public void submit(
            InfiniteImprobabilityDriveRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {

        var mc = Minecraft.getInstance();
        boolean isActive = !state.heldItem.isEmpty();

        // --- Machine base ---
        BlockStateModelPart body = mc.getModelManager().getStandaloneModel(BASE_KEY);
        if (body != null) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.scale(state.breatheScaleX, state.breatheScaleY, state.breatheScaleZ);
            poseStack.translate(-0.5f, 0f, -0.5f);

            submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                    List.of(body), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // --- Floating item ---
        // Render cached item during ramp-down to show scale-out animation
        ItemStack itemToRender = isActive ? state.heldItem : state.cachedItem;
        if (!itemToRender.isEmpty() && state.itemScale > 0f) {
            float bob = (float) Math.sin(state.animTime * Math.PI * 2) * BOB_AMPLITUDE;
            float yaw  = state.itemRotationTime * SPIN_SPEED;

            poseStack.pushPose();
            poseStack.translate(0.5f, ITEM_BASE_Y + bob, 0.5f);
            poseStack.mulPose(Axis.YP.rotation(yaw));
            poseStack.scale(state.itemScale, state.itemScale, state.itemScale);

            // Allocate a fresh state per frame; reusing would corrupt submitted quads.
            ItemStackRenderState itemState = new ItemStackRenderState();
            ItemModelResolver resolver = mc.getItemModelResolver();
            resolver.updateForTopItem(itemState, itemToRender, ItemDisplayContext.FIXED, null, null, 0);
            itemState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

            poseStack.popPose();
        }

        // --- Spinning wheel ---
        BlockStateModelPart wheel = mc.getModelManager().getStandaloneModel(WHEEL_KEY);
        if (wheel != null && state.wheelScale > 0f) {
            float wheelYaw = state.wheelRotationOffset + state.animTime * state.wheelSpinSpeed;

            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.scale(state.wheelScale, state.wheelScale, state.wheelScale);
            poseStack.scale(state.breatheScaleX, state.breatheScaleY, state.breatheScaleZ);
            poseStack.mulPose(Axis.YP.rotation(wheelYaw));
            poseStack.translate(-0.5f, 0f, -0.5f);

            submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                    List.of(wheel), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // --- Stamp ---
        // Raise y up to max of 11 in of 0 with sin wave
        BlockStateModelPart stamp = mc.getModelManager().getStandaloneModel(STAMP_KEY);
        if (stamp != null) {
            float stampBob = (float) (Math.sin(state.animTime) * 0.5f + 0.5f) * state.stampBobAmplitude;
            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.scale(state.breatheScaleX, state.breatheScaleY, state.breatheScaleZ);
            poseStack.translate(-0.5f, stampBob, -0.5f);

            submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                    List.of(stamp), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }

        // --- Supports ---
        BlockStateModelPart support = mc.getModelManager().getStandaloneModel(SUPPORT_KEY);
        if (support != null) {
            poseStack.pushPose();
            poseStack.translate(0.5f, 0f, 0.5f);
            poseStack.scale(state.breatheScaleX, state.breatheScaleY, state.breatheScaleZ);
            poseStack.translate(-0.5f, state.supportY, -0.5f);

            submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                    List.of(support), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            poseStack.popPose();
        }
    }
}

package grill24.hoopyfroodtut.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import grill24.hoopyfroodtut.blockentity.BeggingItemScrabblerBlockEntity;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class BeggingItemScrabblerRenderer implements BlockEntityRenderer<BeggingItemScrabblerBlockEntity, BeggingItemScrabblerRenderState> {

    private static final float STRETCH_AMPLITUDE = 0.45f;
    private static final float ANIMATION_SPEED = 0.25f;
    private static final double PLAYER_TRACK_RADIUS = 5.0;
    /** Fraction of the yaw delta closed per render frame (frame-rate dependent, tuned for ~60fps). */
    private static final float HEAD_YAW_LERP = 0.15f;

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> BODY_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":begging_item_scrabbler_body");

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> HEAD_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":begging_item_scrabbler_head");

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> BODY_DISABLED_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":begging_item_scrabbler_body_disabled");

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> HEAD_DISABLED_KEY = new StandaloneModelKey<>(
            () -> HoopyFroodTut.MODID + ":begging_item_scrabbler_head_disabled");

    public BeggingItemScrabblerRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public BeggingItemScrabblerRenderState createRenderState() {
        return new BeggingItemScrabblerRenderState();
    }

    @Override
    public void extractRenderState(
            BeggingItemScrabblerBlockEntity blockEntity,
            BeggingItemScrabblerRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        renderState.moveDirection = blockEntity.getMoveDirection();
        renderState.hasFuel = blockEntity.getNuggets() > 0;
        renderState.hasItems = blockEntity.hasItems();
        renderState.isReturning = blockEntity.isReturning();

        var inv = blockEntity.getInventory();
        for (int i = 0; i < renderState.inventory.size(); i++) {
            renderState.inventory.set(i, i < inv.size() ? inv.get(i).copy() : ItemStack.EMPTY);
        }

        Level level = blockEntity.getLevel();
        if (level != null) {
            renderState.moveOffset = blockEntity.getForwardOffset(level.getGameTime(), partialTick);
            renderState.animTime = (level.getGameTime() + partialTick) * ANIMATION_SPEED;

            float targetYaw = computeHeadYaw(blockEntity, level, partialTick);
            // Lerp toward target along the shortest arc.
            // smoothHeadYaw lives on the block entity so it persists across frames
            // (the render state is recreated every frame by the dispatcher).
            float delta = ((targetYaw - blockEntity.smoothHeadYaw) % ((float) (2 * Math.PI)));
            if (delta > Math.PI) delta -= (float) (2 * Math.PI);
            if (delta < -Math.PI) delta += (float) (2 * Math.PI);
            blockEntity.smoothHeadYaw += delta * HEAD_YAW_LERP;
            renderState.headYaw = blockEntity.smoothHeadYaw;
        }
    }

    private static float computeHeadYaw(BeggingItemScrabblerBlockEntity blockEntity, Level level, float partialTick) {
        BlockPos pos = blockEntity.getBlockPos();
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        Vec3 lookTarget = findLookTarget(blockEntity, level, cx, cy, cz, partialTick);
        if (lookTarget == null) return 0f;

        double dx = lookTarget.x - cx;
        double dz = lookTarget.z - cz;
        return (float) Math.atan2(dx, -dz);
    }

    @Nullable
    private static Vec3 findLookTarget(BeggingItemScrabblerBlockEntity blockEntity, Level level,
                                        double cx, double cy, double cz, float partialTick) {
        // Priority 1: active item target
        UUID targetItemId = blockEntity.getTargetItemId();
        if (targetItemId != null) {
            AABB searchBox = new AABB(blockEntity.getBlockPos()).inflate(BeggingItemScrabblerBlockEntity.SEARCH_RADIUS);
            List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, searchBox,
                    e -> e.isAlive() && e.getUUID().equals(targetItemId));
            if (!items.isEmpty()) return items.get(0).position();
        }

        // Priority 2: home chest if returning
        if (blockEntity.isReturning()) {
            BlockPos homePos = blockEntity.getHomePos();
            if (homePos != null) {
                return new Vec3(homePos.getX() + 0.5, cy, homePos.getZ() + 0.5);
            }
        }

        // Priority 3: nearest player within PLAYER_TRACK_RADIUS
        List<? extends Player> players = level.players();
        Player nearest = null;
        double bestDistSq = PLAYER_TRACK_RADIUS * PLAYER_TRACK_RADIUS;
        for (Player player : players) {
            double distSq = player.distanceToSqr(cx, cy, cz);
            if (distSq < bestDistSq) {
                bestDistSq = distSq;
                nearest = player;
            }
        }
        if (nearest != null) return nearest.getEyePosition(partialTick);

        return null;
    }

    private static final float ITEM_SCALE = 0.35f;
    private static final float ITEM_Y = 0.32f;
    private static final float ITEM_ROTATION_SPEED = 0.4f;
    // Offsets for the 4 inventory slots arranged in a 2x2 grid within the hollow
    private static final float[][] ITEM_OFFSETS = {{-0.13f, -0.13f}, {0.13f, -0.13f}, {-0.13f, 0.13f}, {0.13f, 0.13f}};

    @Override
    public void submit(
            BeggingItemScrabblerRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector submitNodeCollector,
            CameraRenderState camera) {

        var mc = Minecraft.getInstance();
        var modelManager = mc.getModelManager();
        BlockStateModelPart bodyPart = modelManager.getStandaloneModel(state.hasFuel ? BODY_KEY : BODY_DISABLED_KEY);
        BlockStateModelPart headPart = modelManager.getStandaloneModel(state.hasFuel ? HEAD_KEY : HEAD_DISABLED_KEY);
        if (bodyPart == null || headPart == null) return;

        float ox = state.moveDirection.getStepX() * state.moveOffset;
        float oy = state.moveDirection.getStepY() * state.moveOffset;
        float oz = state.moveDirection.getStepZ() * state.moveOffset;

        // Apply move offset and squash-stretch once; both body and head share this pose.
        poseStack.pushPose();
        poseStack.translate(ox, oy, oz);

        if (state.hasFuel && state.moveOffset > 0f) {
            float raw = (float) Math.sin(state.animTime * ANIMATION_SPEED * 20f);
            float scaleY = Math.max(0.05f, 1f + raw * STRETCH_AMPLITUDE);
            float scaleXZ = 1f / (float) Math.sqrt(scaleY);
            poseStack.translate(0.5, 0.0, 0.5);
            poseStack.scale(scaleXZ, scaleY, scaleXZ);
            poseStack.translate(-0.5, 0.0, -0.5);
        }

        // --- Body ---
        submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                List.of(bodyPart), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        // --- Head (Y-axis swivel around block horizontal center, on top of stretch pose) ---
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        poseStack.mulPose(Axis.YP.rotation(-state.headYaw));
        poseStack.translate(-0.5, 0.0, -0.5);
        submitNodeCollector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                List.of(headPart), new int[0], state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();

        // --- Held items in the hollow space below the platform ---
        if (state.hasItems) {
            ItemModelResolver resolver = mc.getItemModelResolver();
            int slot = 0;
            for (ItemStack stack : state.inventory) {
                if (!stack.isEmpty()) {
                    float[] off = ITEM_OFFSETS[slot];
                    float yaw = state.animTime * ITEM_ROTATION_SPEED + slot * (float)(Math.PI / 2);
                    poseStack.pushPose();
                    poseStack.translate(0.5f + off[0], ITEM_Y, 0.5f + off[1]);
                    poseStack.mulPose(Axis.YP.rotation(yaw));
                    poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
                    // Fresh state per slot: the collector holds a reference to the quads list,
                    // so reusing a single instance would corrupt all previously submitted items
                    // when clear() is called for the next slot.
                    ItemStackRenderState slotState = new ItemStackRenderState();
                    resolver.updateForTopItem(slotState, stack, ItemDisplayContext.FIXED, null, null, slot);
                    slotState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
                    poseStack.popPose();
                }
                slot++;
            }
        }

        poseStack.popPose();
    }
}

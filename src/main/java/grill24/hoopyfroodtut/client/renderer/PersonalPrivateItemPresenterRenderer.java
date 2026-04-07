package grill24.hoopyfroodtut.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import grill24.hoopyfroodtut.block.PersonalPrivateItemPresenter;
import grill24.hoopyfroodtut.blockentity.PersonalPrivateItemPresenterBlockEntity;
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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.data.AtlasIds;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;

import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

public class PersonalPrivateItemPresenterRenderer
        implements BlockEntityRenderer<PersonalPrivateItemPresenterBlockEntity, PersonalPrivateItemPresenterRenderState> {

    // ── Surface constants ──────────────────────────────────────────────────

    /** Resting Y height of the fluid surface, in block-local units (0–1). */
    private static final float DEFAULT_HEIGHT = 0.75f; // 12/16

    /** Per-column oscillation amplitude at waveAmp = 1. */
    private static final float BASE_AMPLITUDE = 0.025f;

    /** Item hover Y above DEFAULT_HEIGHT. */
    private static final float ITEM_HOVER_Y = 0.18f;

    /** Item display scale. */
    private static final float ITEM_SCALE = 0.5f;

    /** ARGB grey tint used when the presenter is in the private/disabled state. */
    private static final int DISABLED_COLOR = 0xFF555555;

    // ── Standalone model key (loaded separately from the blockstate) ───────

    @SuppressWarnings("unchecked")
    public static final StandaloneModelKey<BlockStateModelPart> BASE_KEY =
            new StandaloneModelKey<>(() -> HoopyFroodTut.MODID + ":ppip_base");

    // ── Sprite cache ───────────────────────────────────────────────────────

    private static final Map<PersonalPrivateItemPresenter.SurfaceTexture, Identifier> SPRITE_IDS =
            new EnumMap<>(Map.of(
                    PersonalPrivateItemPresenter.SurfaceTexture.WATER, Identifier.parse("minecraft:block/water_still"),
                    PersonalPrivateItemPresenter.SurfaceTexture.LAVA,  Identifier.parse("minecraft:block/lava_still"),
                    PersonalPrivateItemPresenter.SurfaceTexture.SLIME, Identifier.parse("minecraft:block/slime_block"),
                    PersonalPrivateItemPresenter.SurfaceTexture.HONEY, Identifier.parse("minecraft:block/honey_block_top"),
                    PersonalPrivateItemPresenter.SurfaceTexture.MAGMA, Identifier.parse("minecraft:block/magma")
            ));

    /** Per-texture base tints multiplied with surfaceColor. White = no tint. */
    private static final Map<PersonalPrivateItemPresenter.SurfaceTexture, Integer> TEXTURE_TINTS =
            new EnumMap<>(Map.of(
                    PersonalPrivateItemPresenter.SurfaceTexture.WATER, 0xFF3DBBFF,
                    PersonalPrivateItemPresenter.SurfaceTexture.LAVA,  0xFFFFFFFF,
                    PersonalPrivateItemPresenter.SurfaceTexture.SLIME, 0xFFFFFFFF,
                    PersonalPrivateItemPresenter.SurfaceTexture.HONEY, 0xFFFFFFFF,
                    PersonalPrivateItemPresenter.SurfaceTexture.MAGMA, 0xFFFFFFFF
            ));

    /** Lazy-initialised sprites, one per texture variant. */
    private final Map<PersonalPrivateItemPresenter.SurfaceTexture, TextureAtlasSprite> spriteCache =
            new EnumMap<>(PersonalPrivateItemPresenter.SurfaceTexture.class);

    // ── Surface wave physics constants ────────────────────────────────────────

    /** Wave propagation speed c (units: surface-widths per game-time-second). */
    private static final float WAVE_SPEED              = 1.8f;
    /** Linear velocity damping coefficient γ in the wave equation a = c²∇²h − 2γv. */
    private static final float WAVE_DAMPING            = 1f;
    /** Soft restoring term −κh that prevents the DC (zero-eigenvalue) mode from accumulating
     *  indefinitely when ghost-cell Neumann boundaries are in use.  Without this, every
     *  impulse deposits a permanent height offset that never decays. */
    private static final float DC_RESTORE_K            = 30.0f;
    /** Peak depth of the Gaussian dimple the item presses into the surface. */
    private static final float ITEM_FORCE_DEPTH        = 0.6f;
    /** Gaussian σ controlling how wide the item's pressure footprint is (surface fractions). */
    private static final float ITEM_FORCE_SIGMA        = 0.13f;
    /** Stiffness of the soft constraint that maintains the item dimple shape. */
    private static final float ITEM_FORCE_SPRING       = 30.0f;
    /** Magnitude of random impulses injected at the item's location each step. */
    private static final float BROWNIAN_SURFACE_IMPULSE = 0.04f;
    /** Spring constant pulling the floating item toward the centre (XZ plane). */
    private static final float PHYS_SPRING_K           = 1.5f;
    /** Width of the wall-repulsion band near each edge (fraction of surface). */
    private static final float PHYS_WALL_MARGIN        = 0.12f;
    /** Peak repulsion force at the wall margin. */
    private static final float PHYS_WALL_STRENGTH      = 4.0f;
    /** Hard position clamp keeping the item away from the absolute edge. */
    private static final float PHYS_POSITION_LIMIT     = 0.05f;
    /** Magnitude of the random Brownian nudge applied to the item each tick. */
    private static final float PHYS_BROWNIAN_FORCE     = 1.2f;
    /** Base of the exponential velocity damping per second for item XZ motion. */
    private static final double PHYS_DAMPING_BASE      = 0.12;

    // ── In-world ItemEntity impact constants ──────────────────────────────────

    /** Block-local Y threshold for impact detection — just above the visual fluid surface. */
    private static final float IMPACT_DETECTION_THRESHOLD = DEFAULT_HEIGHT + 0.05f;
    /** Gaussian σ controlling the splash footprint radius (surface fractions). */
    private static final float IMPACT_SIGMA                = 0.12f;
    /** Scales the item entity's downward speed (blocks/tick) into a surface impulse magnitude. */
    private static final float IMPACT_VELOCITY_SCALE       = 15.0f;
    /** Minimum splash impulse so even slow-falling items create a visible ripple. */
    private static final float IMPACT_MIN_STRENGTH         = 0.6f;
    /** Minimum XZ speed (blocks/tick) for a floating item to generate sliding ripples. */
    private static final float SLIDING_MIN_SPEED           = 0.003f;
    /** Scales the item entity's horizontal speed (blocks/tick) into a sliding ripple impulse. */
    private static final float SLIDING_VELOCITY_SCALE      = 4.0f;

    // ── Surface physics state ──────────────────────────────────────────────

    /** Per-block persistent surface physics state. Keyed by BlockPos. */
    private static final Map<BlockPos, SurfacePhysicsState> SURFACE_PHYSICS_CACHE = new HashMap<>();

    /**
     * Holds the full state of the spring-mass surface lattice plus the item XZ position.
     * <p>
     * The surface is a (gridSize+1)² grid of vertices. {@code h[]} stores displacement
     * from rest (positive = up), {@code v[]} stores velocity.  Both are indexed
     * {@code col*(gridSize+1)+row} where col∈[0,gridSize], row∈[0,gridSize].
     * Boundary vertices (col=0, col=gridSize, row=0, row=gridSize) are held fixed at 0.
     */
    private static final class SurfacePhysicsState {
        float[] h;     // displacement from DEFAULT_HEIGHT (read buffer)
        float[] hNext; // write buffer — swapped with h after each step
        float[] v;     // velocity (updated in-place before h is swapped)
        int gridSize;
        // Item XZ wander (Brownian walk, separate from surface heights)
        float itemX = 0.5f, itemZ = 0.5f;
        float itemVx = 0f,  itemVz = 0f;
        float lastGameTime = -1f;
        final Random rng;
        /** Block-local Y of each nearby ItemEntity from the previous render frame, keyed by entity ID. */
        final Map<Integer, Float> lastEntityY = new HashMap<>();
        /** Splash impulses queued by in-world ItemEntity impacts; drained at the end of stepSurface. */
        final List<float[]> pendingImpacts = new ArrayList<>();

        SurfacePhysicsState(BlockPos pos) {
            this.gridSize = 0;
            this.h     = new float[0];
            this.hNext = new float[0];
            this.v     = new float[0];
            this.rng   = new Random(pos.asLong());
        }

        /** Resize arrays when gridSize changes (resets all state to zero). */
        void ensureGrid(int newGridSize) {
            if (newGridSize != gridSize) {
                gridSize = newGridSize;
                int n = (newGridSize + 1) * (newGridSize + 1);
                h     = new float[n];
                hNext = new float[n];
                v     = new float[n];
            }
        }

        int idx(int col, int row) { return col * (gridSize + 1) + row; }
    }

    private static SurfacePhysicsState getSurface(BlockPos pos) {
        return SURFACE_PHYSICS_CACHE.computeIfAbsent(pos, SurfacePhysicsState::new);
    }

    /**
     * Copies height values from {@code neighbor}'s first interior row/column into
     * {@code s}'s boundary vertices on the given side.
     *
     * <p>Call this <em>after</em> {@link #stepSurface}.  The written values persist in
     * {@code s.h} until the next step's Laplacian reads them (ghost-cell physics coupling),
     * and are also included in the height snapshot sent to the render thread (visual continuity).
     *
     * <p>Only non-corner boundary vertices (avoiding [0,0], [G,0], [0,G], [G,G]) are
     * written; corners are left at 0 so adjacent coupling in both X and Z directions
     * does not fight over the corner value.
     */
    private static void applyNeighborBoundary(SurfacePhysicsState s, SurfacePhysicsState neighbor,
                                              Direction dir, int G) {
        switch (dir) {
            case EAST ->  { // our east edge (col=G) ← neighbor's first interior col (col=1)
                for (int row = 1; row < G; row++)
                    s.h[s.idx(G, row)] = neighbor.h[neighbor.idx(1, row)];
            }
            case WEST ->  { // our west edge (col=0) ← neighbor's last interior col (col=G-1)
                for (int row = 1; row < G; row++)
                    s.h[s.idx(0, row)] = neighbor.h[neighbor.idx(G - 1, row)];
            }
            case SOUTH -> { // our south edge (row=G) ← neighbor's first interior row (row=1)
                for (int col = 1; col < G; col++)
                    s.h[s.idx(col, G)] = neighbor.h[neighbor.idx(col, 1)];
            }
            case NORTH -> { // our north edge (row=0) ← neighbor's last interior row (row=G-1)
                for (int col = 1; col < G; col++)
                    s.h[s.idx(col, 0)] = neighbor.h[neighbor.idx(col, G - 1)];
            }
            default -> {}
        }
    }

    /**
     * Drains {@code s.pendingImpacts} and applies each as a Gaussian downward velocity impulse
     * to the surface lattice.  Called at the end of {@link #stepSurface} so impacts take effect
     * on the very next integration step.
     */
    private static void applyPendingImpacts(SurfacePhysicsState s, int gridSize) {
        if (s.pendingImpacts.isEmpty()) return;
        int G = gridSize;
        float cellSize = 1.0f / G;
        float invSigma2x2 = 1f / (2f * IMPACT_SIGMA * IMPACT_SIGMA);
        for (float[] impact : s.pendingImpacts) {
            float ix = impact[0], iz = impact[1], strength = impact[2];
            for (int col = 1; col < G; col++) {
                float vx = col * cellSize;
                for (int row = 1; row < G; row++) {
                    float vz = row * cellSize;
                    float ddx = vx - ix;
                    float ddz = vz - iz;
                    s.v[s.idx(col, row)] -= strength
                            * (float) Math.exp(-(ddx * ddx + ddz * ddz) * invSigma2x2);
                }
            }
        }
        s.pendingImpacts.clear();
    }

    /**
     * Advances the surface wave simulation by one frame.
     *
     * Surface heights evolve via the damped 2-D wave equation (finite differences):
     * <pre>
     *   a[i,j] = c² · ∇²h[i,j] − 2γ · v[i,j]   +   item_force[i,j]
     *   v[i,j] += a[i,j] · dt
     *   h[i,j] += v[i,j] · dt          (semi-implicit: uses updated v)
     * </pre>
     * The item exerts a soft-spring force toward a Gaussian target displacement while
     * present.  When the item is removed the forcing vanishes and the surface rings down
     * naturally through the wave physics and damping — no artificial fade factor is needed.
     *
     * The item's XZ position is a separate Brownian walk that drives where the forcing
     * is applied.
     */
    private static void stepSurface(SurfacePhysicsState s, int gridSize,
                                     float gameTime, float waveAmp, boolean hasItem, float itemY) {
        s.ensureGrid(gridSize);
        if (s.lastGameTime < 0f) {
            s.lastGameTime = gameTime;
            return;
        }
        float dt = gameTime - s.lastGameTime;
        s.lastGameTime = gameTime;
        if (dt <= 0f || dt > 0.5f) return; // skip huge gaps (unpause, chunk load, etc.)

        // Clamp dt to the CFL stability bound.  The 2-D wave equation with symplectic Euler
        // requires c·dt/dx ≤ 1/√2, i.e. dt ≤ 1/(√2·c·G).  Values above this (low frame rate,
        // pause/resume, etc.) cause exponential blow-up; the 0.8 safety factor leaves headroom
        // for the ghost-cell boundary coupling between adjacent blocks.
        int G = gridSize;
        float maxStableDt = 0.8f / (1.4143f * WAVE_SPEED * G);
        if (dt > maxStableDt) dt = maxStableDt;
        int N = G + 1; // vertices per side
        float dx = 1.0f / G;
        // c² / dx² — coefficient of the discrete Laplacian
        float c2_dx2 = (WAVE_SPEED * WAVE_SPEED) / (dx * dx);
        float twoGamma = 2f * WAVE_DAMPING;

        // ── Step item XZ position (Brownian walk, only while item is present) ──
        if (hasItem) {
            float fx = (s.rng.nextFloat() * 2f - 1f) * PHYS_BROWNIAN_FORCE
                     + PHYS_SPRING_K * (0.5f - s.itemX);
            float fz = (s.rng.nextFloat() * 2f - 1f) * PHYS_BROWNIAN_FORCE
                     + PHYS_SPRING_K * (0.5f - s.itemZ);
            if (s.itemX < PHYS_WALL_MARGIN)
                fx += PHYS_WALL_STRENGTH * (PHYS_WALL_MARGIN - s.itemX) / PHYS_WALL_MARGIN;
            if (s.itemX > 1f - PHYS_WALL_MARGIN)
                fx -= PHYS_WALL_STRENGTH * (s.itemX - (1f - PHYS_WALL_MARGIN)) / PHYS_WALL_MARGIN;
            if (s.itemZ < PHYS_WALL_MARGIN)
                fz += PHYS_WALL_STRENGTH * (PHYS_WALL_MARGIN - s.itemZ) / PHYS_WALL_MARGIN;
            if (s.itemZ > 1f - PHYS_WALL_MARGIN)
                fz -= PHYS_WALL_STRENGTH * (s.itemZ - (1f - PHYS_WALL_MARGIN)) / PHYS_WALL_MARGIN;
            float itemDamping = (float) Math.pow(PHYS_DAMPING_BASE, dt);
            s.itemVx = (s.itemVx + fx * dt) * itemDamping;
            s.itemVz = (s.itemVz + fz * dt) * itemDamping;
            s.itemX  = Math.max(PHYS_POSITION_LIMIT, Math.min(1f - PHYS_POSITION_LIMIT, s.itemX + s.itemVx * dt));
            s.itemZ  = Math.max(PHYS_POSITION_LIMIT, Math.min(1f - PHYS_POSITION_LIMIT, s.itemZ + s.itemVz * dt));
        }

        // ── Pre-compute Gaussian item pressure target for each interior vertex ──
        // target[i] = 0 when no item; non-zero dimple when item present.
        // The soft-spring force is ITEM_FORCE_SPRING * (target - h), pulling the
        // surface toward the desired displaced shape.
        float invSigma2x2 = 1f / (2f * ITEM_FORCE_SIGMA * ITEM_FORCE_SIGMA);
        float depth = ITEM_FORCE_DEPTH * waveAmp;

        // ── Wave equation integration (interior vertices only; boundary stays at 0) ──
        //
        // IMPORTANT: all Laplacian reads come from s.h (the OLD buffer); all new heights
        // are written to s.hNext.  The buffers are swapped at the end of the step.
        // This prevents the Gauss-Seidel effect (reading partially-updated neighbours)
        // that causes energy growth and eventual divergence.
        for (int col = 1; col < G; col++) {
            float vx = col * dx;
            for (int row = 1; row < G; row++) {
                int idx = s.idx(col, row);
                float h_cur = s.h[idx];
                float v_cur = s.v[idx];

                // Discrete Laplacian (5-point stencil) — reads only from old s.h
                float lap = s.h[s.idx(col - 1, row)] + s.h[s.idx(col + 1, row)]
                          + s.h[s.idx(col, row - 1)] + s.h[s.idx(col, row + 1)]
                          - 4f * h_cur;

                float acc = c2_dx2 * lap - twoGamma * v_cur - DC_RESTORE_K * h_cur;

                // Item forcing: soft spring toward Gaussian target displacement.
                // The Gaussian uses the full 3D distance so the item exerts less
                // force on the surface when it bobs higher above it.
                if (hasItem) {
                    float vz     = row * dx;
                    float ddx    = vx - s.itemX;
                    float ddy    = itemY - DEFAULT_HEIGHT; // Y separation above the rest surface
                    float ddz    = vz - s.itemZ;
                    float target = -depth * (float) Math.exp(-(ddx * ddx + ddy * ddy + ddz * ddz) * invSigma2x2);
                    acc += ITEM_FORCE_SPRING * (target - h_cur);
                }

                // Symplectic Euler: update v with acceleration, then advance h with new v
                float v_new = v_cur + acc * dt;
                s.v[idx]    = v_new;
                s.hNext[idx] = h_cur + v_new * dt; // write to next buffer, not s.h
            }
        }

        // Swap read/write buffers — s.h now holds the freshly computed heights
        float[] tmp = s.h;
        s.h    = s.hNext;
        s.hNext = tmp;

        // ── Stochastic surface excitation at the item position ──
        // Injects random impulses into the nearest interior vertex so the
        // surface has small, organically varying ripples while the item is present.
        if (hasItem) {
            int ci = Math.min(G - 1, Math.max(1, Math.round(s.itemX * G)));
            int cj = Math.min(G - 1, Math.max(1, Math.round(s.itemZ * G)));
            s.v[s.idx(ci, cj)] += (s.rng.nextFloat() * 2f - 1f) * BROWNIAN_SURFACE_IMPULSE;
        }

        // ── Apply splash impulses from in-world ItemEntity collisions ──────────
        applyPendingImpacts(s, G);
    }

    // ── Constructor ────────────────────────────────────────────────────────

    public PersonalPrivateItemPresenterRenderer(BlockEntityRendererProvider.Context context) {}

    // ── BlockEntityRenderer ────────────────────────────────────────────────

    @Override
    public PersonalPrivateItemPresenterRenderState createRenderState() {
        return new PersonalPrivateItemPresenterRenderState();
    }

    @Override
    public void extractRenderState(
            PersonalPrivateItemPresenterBlockEntity blockEntity,
            PersonalPrivateItemPresenterRenderState renderState,
            float partialTick,
            Vec3 cameraPos,
            ModelFeatureRenderer.CrumblingOverlay crumblingOverlay) {
        BlockEntityRenderer.super.extractRenderState(
                blockEntity, renderState, partialTick, cameraPos, crumblingOverlay);

        renderState.isPrivate    = blockEntity.isPrivate();
        renderState.surfaceColor = blockEntity.getSurfaceColor();
        renderState.gridSize     = blockEntity.getGridSize();
        renderState.waveAmp      = blockEntity.getWaveAmp();
        renderState.waveSpeed    = blockEntity.getWaveSpeed();
        renderState.bobAmp       = blockEntity.getBobAmp();
        renderState.bobSpeed     = blockEntity.getBobSpeed();
        renderState.spinItem        = blockEntity.isSpinItem();
        renderState.voxelMode       = blockEntity.isVoxelMode();
        renderState.physicsEnabled  = blockEntity.isPhysicsEnabled();
        renderState.surfaceTexture = blockEntity.getBlockState()
                .getValue(PersonalPrivateItemPresenter.SURFACE_TEXTURE);

        // Copy item — hide it from the render state when private so no item-render pass fires
        renderState.storedItem = renderState.isPrivate
                ? net.minecraft.world.item.ItemStack.EMPTY
                : blockEntity.getStoredItem().copy();

        var level = blockEntity.getLevel();
        if (level != null) {
            renderState.gameTime = (level.getGameTime() + partialTick) * 0.05f;
        }

        // Advance the surface wave simulation and snapshot heights into the render state.
        // When physics is disabled the render state heights array is cleared so the render
        // methods fall back to the mathematical wave function.
        // Compute item Y (same formula as renderFloatingItem) so physics can use it
        float bobY = renderState.bobAmp * (float) Math.sin(
                renderState.gameTime * renderState.bobSpeed * (float) Math.PI * 2.0f / 2.5f);
        renderState.itemY = DEFAULT_HEIGHT + ITEM_HOVER_Y + bobY;

        if (renderState.physicsEnabled) {
            boolean hasItem = !renderState.storedItem.isEmpty();
            BlockPos pos = blockEntity.getBlockPos();
            SurfacePhysicsState surf = getSurface(pos);
            int G = renderState.gridSize;

            // ── Detect in-world ItemEntity impacts and queue splash impulses ──────
            // Items fall through the block (no collision shape for ItemEntity), so we
            // track each entity's block-local Y frame-to-frame and queue an impulse
            // when it crosses the visual fluid surface threshold.
            if (level != null) {
                AABB scanBox = new AABB(
                        pos.getX() - 0.1, pos.getY() + 0.0, pos.getZ() - 0.1,
                        pos.getX() + 1.1, pos.getY() + 2.5, pos.getZ() + 1.1);
                List<ItemEntity> nearbyItems =
                        level.getEntitiesOfClass(ItemEntity.class, scanBox);

                Set<Integer> currentIds = new HashSet<>();
                for (ItemEntity item : nearbyItems) {
                    int id = item.getId();
                    currentIds.add(id);
                    float localY = (float) (item.getY() - pos.getY());
                    Float prevY = surf.lastEntityY.get(id);
                    surf.lastEntityY.put(id, localY);

                    // Detect downward crossing of the fluid surface threshold
                    if (prevY != null && prevY > IMPACT_DETECTION_THRESHOLD
                            && localY <= IMPACT_DETECTION_THRESHOLD) {
                        float localX = Math.max(0.05f, Math.min(0.95f,
                                (float) (item.getX() - pos.getX())));
                        float localZ = Math.max(0.05f, Math.min(0.95f,
                                (float) (item.getZ() - pos.getZ())));
                        float downSpeed = (float) Math.abs(
                                Math.min(0.0, item.getDeltaMovement().y));
                        if (downSpeed < 0.01f) downSpeed = prevY - localY;
                        float strength = Math.max(IMPACT_MIN_STRENGTH,
                                downSpeed * IMPACT_VELOCITY_SCALE);
                        surf.pendingImpacts.add(new float[]{localX, localZ, strength});
                    }

                    // Continuous sliding ripples: item floating at or below the surface
                    // with horizontal velocity generates ripples each frame
                    if (localY <= IMPACT_DETECTION_THRESHOLD) {
                        Vec3 vel = item.getDeltaMovement();
                        float xzSpeed = (float) Math.sqrt(vel.x() * vel.x() + vel.z() * vel.z());
                        if (xzSpeed > SLIDING_MIN_SPEED) {
                            float localX = Math.max(0.05f, Math.min(0.95f,
                                    (float) (item.getX() - pos.getX())));
                            float localZ = Math.max(0.05f, Math.min(0.95f,
                                    (float) (item.getZ() - pos.getZ())));
                            surf.pendingImpacts.add(new float[]{localX, localZ,
                                    xzSpeed * SLIDING_VELOCITY_SCALE});
                        }
                    }
                }

                // ── Detect player movement impacts (players are larger, so stronger effect) ──
                List<LivingEntity> nearbyPlayers = level.getEntitiesOfClass(LivingEntity.class, scanBox);
                for (LivingEntity player : nearbyPlayers) {
                    int id = player.getId();
                    currentIds.add(id);
                    float localY = (float) (player.getY() - pos.getY());
                    Float prevY = surf.lastEntityY.get(id);
                    surf.lastEntityY.put(id, localY);

                    // Detect downward crossing — player feet breaking the surface
                    if (prevY != null && prevY > IMPACT_DETECTION_THRESHOLD
                            && localY <= IMPACT_DETECTION_THRESHOLD) {
                        float localX = Math.max(0.05f, Math.min(0.95f,
                                (float) (player.getX() - pos.getX())));
                        float localZ = Math.max(0.05f, Math.min(0.95f,
                                (float) (player.getZ() - pos.getZ())));
                        float downSpeed = (float) Math.abs(
                                Math.min(0.0, player.getDeltaMovement().y()));
                        if (downSpeed < 0.01f) downSpeed = prevY - localY;
                        // Players displace more fluid than items — scale strength up
                        float strength = Math.max(IMPACT_MIN_STRENGTH,
                                downSpeed * IMPACT_VELOCITY_SCALE) * 3.0f;
                        surf.pendingImpacts.add(new float[]{localX, localZ, strength});
                    }

                    // Continuous ripples while player is at or below the surface
                    if (localY <= IMPACT_DETECTION_THRESHOLD) {
                        Vec3 vel = player.getDeltaMovement();
                        float xzSpeed = (float) Math.sqrt(vel.x() * vel.x() + vel.z() * vel.z());
                        if (xzSpeed > SLIDING_MIN_SPEED) {
                            float localX = Math.max(0.05f, Math.min(0.95f,
                                    (float) (player.getX() - pos.getX())));
                            float localZ = Math.max(0.05f, Math.min(0.95f,
                                    (float) (player.getZ() - pos.getZ())));
                            surf.pendingImpacts.add(new float[]{localX, localZ,
                                    xzSpeed * SLIDING_VELOCITY_SCALE * 2.0f});
                        }
                    }
                }

                surf.lastEntityY.keySet().retainAll(currentIds);
            }

            // ── Adjacent PPIP boundary coupling ───────────────────────────────────
            // For each cardinal neighbour that is also a physics-enabled PPIP with the
            // same gridSize, we collect it here and — after our own step — copy its
            // nearest interior row/column into our boundary vertices (ghost-cell method).
            // The written values persist in s.h until the next step's Laplacian reads
            // them, allowing waves to propagate across block joins rather than reflect.
            record NeighborEntry(SurfacePhysicsState state, Direction dir) {}
            var coupledNeighbors = new ArrayList<NeighborEntry>();
            if (level != null) {
                for (Direction dir : Direction.Plane.HORIZONTAL) {
                    BlockPos nPos = pos.relative(dir);
                    if (level.getBlockEntity(nPos) instanceof PersonalPrivateItemPresenterBlockEntity nBE
                            && nBE.isPhysicsEnabled() && nBE.getGridSize() == G) {
                        SurfacePhysicsState ns = SURFACE_PHYSICS_CACHE.get(nPos);
                        if (ns != null && ns.gridSize == G)
                            coupledNeighbors.add(new NeighborEntry(ns, dir));
                    }
                }
            }

            stepSurface(surf, G, renderState.gameTime, renderState.waveAmp, hasItem, renderState.itemY);

            // Post-step: apply ghost-cell boundary data and record which sides are coupled
            // so the renderer can suppress walls/skirts on shared edges.
            renderState.hasNeighborNorth = false;
            renderState.hasNeighborSouth = false;
            renderState.hasNeighborWest  = false;
            renderState.hasNeighborEast  = false;
            for (NeighborEntry ne : coupledNeighbors) {
                applyNeighborBoundary(surf, ne.state(), ne.dir(), G);
                switch (ne.dir()) {
                    case NORTH -> renderState.hasNeighborNorth = true;
                    case SOUTH -> renderState.hasNeighborSouth = true;
                    case WEST  -> renderState.hasNeighborWest  = true;
                    case EAST  -> renderState.hasNeighborEast  = true;
                    default -> {}
                }
            }

            renderState.itemX = surf.itemX;
            renderState.itemZ = surf.itemZ;
            int n = (G + 1) * (G + 1);
            if (renderState.physicsHeights == null || renderState.physicsHeights.length != n) {
                renderState.physicsHeights = new float[n];
            }
            System.arraycopy(surf.h, 0, renderState.physicsHeights, 0, n);
        } else {
            renderState.hasNeighborNorth = false;
            renderState.hasNeighborSouth = false;
            renderState.hasNeighborWest  = false;
            renderState.hasNeighborEast  = false;
            renderState.itemX = 0.5f;
            renderState.itemZ = 0.5f;
            renderState.physicsHeights = null;
        }
    }

    @Override
    public void submit(
            PersonalPrivateItemPresenterRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            CameraRenderState camera) {

        int surfColor = state.isPrivate ? DISABLED_COLOR : multiplyColors(
                state.surfaceColor, TEXTURE_TINTS.getOrDefault(state.surfaceTexture, 0xFFFFFFFF));

        // 1. Opaque base (bottom only — walls drawn below with surface texture)
        BlockStateModelPart basePart = Minecraft.getInstance()
                .getModelManager().getStandaloneModel(BASE_KEY);
        if (basePart != null) {
            collector.submitBlockModel(poseStack, Sheets.cutoutBlockSheet(),
                    List.of(basePart), new int[0],
                    state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        }

        // 2. Outer walls using surface texture
        renderBaseWalls(state, poseStack, collector, surfColor);

        // 3. Animated fluid surface
        renderFluidSurface(state, poseStack, collector, surfColor);

        // 3. Floating item (suppressed when private)
        if (!state.storedItem.isEmpty()) {
            renderFloatingItem(state, poseStack, collector);
        }
    }

    // ── Base walls ─────────────────────────────────────────────────────────

    private void renderBaseWalls(
            PersonalPrivateItemPresenterRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int argbColor) {

        TextureAtlasSprite sprite = getSurfaceSprite(state.surfaceTexture);
        if (sprite == null) return;

        int rawA = (argbColor >> 24) & 0xFF;
        final int a = rawA == 0 ? 255 : rawA;
        final int r = (argbColor >> 16) & 0xFF;
        final int g = (argbColor >>  8) & 0xFF;
        final int b =  argbColor        & 0xFF;

        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();
        int lightCoords = state.lightCoords;

        collector.submitCustomGeometry(poseStack, Sheets.cutoutBlockSheet(),
                (pose, buffer) -> {
                    Matrix4f mat = pose.pose();

                    // North (z=0, outward -Z) — omit on shared edges
                    if (!state.hasNeighborNorth) {
                        Vector3f normN = pose.transformNormal(0, 0, -1, new Vector3f());
                        emitVertex(mat, buffer, 1f, 0f,            0f, r, g, b, a, u1, v1, lightCoords, normN);
                        emitVertex(mat, buffer, 0f, 0f,            0f, r, g, b, a, u0, v1, lightCoords, normN);
                        emitVertex(mat, buffer, 0f, DEFAULT_HEIGHT, 0f, r, g, b, a, u0, v0, lightCoords, normN);
                        emitVertex(mat, buffer, 1f, DEFAULT_HEIGHT, 0f, r, g, b, a, u1, v0, lightCoords, normN);
                    }

                    // South (z=1, outward +Z) — omit on shared edges
                    if (!state.hasNeighborSouth) {
                        Vector3f normS = pose.transformNormal(0, 0, 1, new Vector3f());
                        emitVertex(mat, buffer, 0f, 0f,            1f, r, g, b, a, u0, v1, lightCoords, normS);
                        emitVertex(mat, buffer, 1f, 0f,            1f, r, g, b, a, u1, v1, lightCoords, normS);
                        emitVertex(mat, buffer, 1f, DEFAULT_HEIGHT, 1f, r, g, b, a, u1, v0, lightCoords, normS);
                        emitVertex(mat, buffer, 0f, DEFAULT_HEIGHT, 1f, r, g, b, a, u0, v0, lightCoords, normS);
                    }

                    // West (x=0, outward -X) — omit on shared edges
                    if (!state.hasNeighborWest) {
                        Vector3f normW = pose.transformNormal(-1, 0, 0, new Vector3f());
                        emitVertex(mat, buffer, 0f, 0f,            0f, r, g, b, a, u0, v1, lightCoords, normW);
                        emitVertex(mat, buffer, 0f, 0f,            1f, r, g, b, a, u1, v1, lightCoords, normW);
                        emitVertex(mat, buffer, 0f, DEFAULT_HEIGHT, 1f, r, g, b, a, u1, v0, lightCoords, normW);
                        emitVertex(mat, buffer, 0f, DEFAULT_HEIGHT, 0f, r, g, b, a, u0, v0, lightCoords, normW);
                    }

                    // East (x=1, outward +X) — omit on shared edges
                    if (!state.hasNeighborEast) {
                        Vector3f normE = pose.transformNormal(1, 0, 0, new Vector3f());
                        emitVertex(mat, buffer, 1f, 0f,            1f, r, g, b, a, u0, v1, lightCoords, normE);
                        emitVertex(mat, buffer, 1f, 0f,            0f, r, g, b, a, u1, v1, lightCoords, normE);
                        emitVertex(mat, buffer, 1f, DEFAULT_HEIGHT, 0f, r, g, b, a, u1, v0, lightCoords, normE);
                        emitVertex(mat, buffer, 1f, DEFAULT_HEIGHT, 1f, r, g, b, a, u0, v0, lightCoords, normE);
                    }
                });
    }

    // ── Fluid surface ──────────────────────────────────────────────────────

    private void renderFluidSurface(
            PersonalPrivateItemPresenterRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int argbColor) {
        if (state.voxelMode) {
            renderFluidSurfaceVoxel(state, poseStack, collector, argbColor);
        } else {
            renderFluidSurfaceSmooth(state, poseStack, collector, argbColor);
        }
    }

    private void renderFluidSurfaceSmooth(
            PersonalPrivateItemPresenterRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int argbColor) {

        TextureAtlasSprite sprite = getSurfaceSprite(state.surfaceTexture);
        if (sprite == null) return;

        int gridSize   = state.gridSize;
        float cellSize = 1.0f / gridSize;
        float time     = state.gameTime * state.waveSpeed;

        // Extract per-channel color so values are effectively final for capture in the lambda
        int rawA = (argbColor >> 24) & 0xFF;
        final int a = rawA == 0 ? 255 : rawA; // treat fully-transparent spec as fully-opaque
        final int r = (argbColor >> 16) & 0xFF;
        final int g = (argbColor >>  8) & 0xFF;
        final int b =  argbColor        & 0xFF;

        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();

        // Pre-compute corner heights for all (gridSize+1)² vertices
        float[][] heights = new float[gridSize + 1][gridSize + 1];
        if (state.physicsHeights != null) {
            // Physics mode: heights come directly from the wave simulation snapshot.
            // Each entry is a displacement from DEFAULT_HEIGHT.
            int N = gridSize + 1;
            for (int col = 0; col <= gridSize; col++) {
                for (int row = 0; row <= gridSize; row++) {
                    heights[col][row] = DEFAULT_HEIGHT + state.physicsHeights[col * N + row];
                }
            }
        } else {
            // Non-physics mode: procedural wave function.
            float amp = BASE_AMPLITUDE * state.waveAmp;
            for (int col = 0; col <= gridSize; col++) {
                for (int row = 0; row <= gridSize; row++) {
                    float phase = (col * 0.4f) + (row * 0.4f);
                    float h = DEFAULT_HEIGHT + amp * (float) Math.sin(time * 2.0f + phase);
                    float maxDelta = amp * 3.0f;
                    heights[col][row] = Math.max(DEFAULT_HEIGHT - maxDelta, Math.min(DEFAULT_HEIGHT + maxDelta, h));
                }
            }
        }

        int lightCoords = state.lightCoords;

        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(),
                (pose, buffer) -> {
                    Matrix4f mat    = pose.pose();
                    Vector3f upNorm = pose.transformNormal(0, 1, 0, new Vector3f());

                    for (int col = 0; col < gridSize; col++) {
                        for (int row = 0; row < gridSize; row++) {
                            float x0 = col * cellSize,   x1 = x0 + cellSize;
                            float z0 = row * cellSize,   z1 = z0 + cellSize;
                            float h00 = heights[col    ][row    ];
                            float h10 = heights[col + 1][row    ];
                            float h11 = heights[col + 1][row + 1];
                            float h01 = heights[col    ][row + 1];

                            // UV interpolated across the sprite region
                            float fu0 = u0 + (u1 - u0) * (float) col / gridSize;
                            float fu1 = u0 + (u1 - u0) * (float) (col + 1) / gridSize;
                            float fv0 = v0 + (v1 - v0) * (float) row / gridSize;
                            float fv1 = v0 + (v1 - v0) * (float) (row + 1) / gridSize;

                            // Top-face quad (CCW winding when viewed from above)
                            emitVertex(mat, buffer, x0, h00, z0, r, g, b, a, fu0, fv0, lightCoords, upNorm);
                            emitVertex(mat, buffer, x0, h01, z1, r, g, b, a, fu0, fv1, lightCoords, upNorm);
                            emitVertex(mat, buffer, x1, h11, z1, r, g, b, a, fu1, fv1, lightCoords, upNorm);
                            emitVertex(mat, buffer, x1, h10, z0, r, g, b, a, fu1, fv0, lightCoords, upNorm);
                        }
                    }

                    // Skirt quads: connect deformed surface edges down to DEFAULT_HEIGHT,
                    // flush with the outer wall faces, to eliminate gaps.
                    // V is mapped proportionally: V(y) = v0 + (v1-v0)*(1-y), same pixel density as top face.
                    float vBase = v0 + (v1 - v0) * (1.0f - DEFAULT_HEIGHT);

                    // North (z=0, outward normal -Z) — omit on shared edges
                    if (!state.hasNeighborNorth) {
                        Vector3f norm = pose.transformNormal(0, 0, -1, new Vector3f());
                        for (int col = 0; col < gridSize; col++) {
                            float x0s = col * cellSize, x1s = x0s + cellSize;
                            float hL  = heights[col][0], hR = heights[col + 1][0];
                            float su0 = u0 + (u1 - u0) * (float) col / gridSize;
                            float su1 = u0 + (u1 - u0) * (float) (col + 1) / gridSize;
                            float vTopL = v0 + (v1 - v0) * (1.0f - hL);
                            float vTopR = v0 + (v1 - v0) * (1.0f - hR);
                            emitVertex(mat, buffer, x1s, DEFAULT_HEIGHT, 0f, r, g, b, a, su1, vBase,  lightCoords, norm);
                            emitVertex(mat, buffer, x0s, DEFAULT_HEIGHT, 0f, r, g, b, a, su0, vBase,  lightCoords, norm);
                            emitVertex(mat, buffer, x0s, hL,             0f, r, g, b, a, su0, vTopL,  lightCoords, norm);
                            emitVertex(mat, buffer, x1s, hR,             0f, r, g, b, a, su1, vTopR,  lightCoords, norm);
                        }
                    }
                    // South (z=1, outward normal +Z) — omit on shared edges
                    if (!state.hasNeighborSouth) {
                        Vector3f norm = pose.transformNormal(0, 0, 1, new Vector3f());
                        for (int col = 0; col < gridSize; col++) {
                            float x0s = col * cellSize, x1s = x0s + cellSize;
                            float hL  = heights[col][gridSize], hR = heights[col + 1][gridSize];
                            float su0 = u0 + (u1 - u0) * (float) col / gridSize;
                            float su1 = u0 + (u1 - u0) * (float) (col + 1) / gridSize;
                            float vTopL = v0 + (v1 - v0) * (1.0f - hL);
                            float vTopR = v0 + (v1 - v0) * (1.0f - hR);
                            emitVertex(mat, buffer, x0s, DEFAULT_HEIGHT, 1f, r, g, b, a, su0, vBase,  lightCoords, norm);
                            emitVertex(mat, buffer, x1s, DEFAULT_HEIGHT, 1f, r, g, b, a, su1, vBase,  lightCoords, norm);
                            emitVertex(mat, buffer, x1s, hR,             1f, r, g, b, a, su1, vTopR,  lightCoords, norm);
                            emitVertex(mat, buffer, x0s, hL,             1f, r, g, b, a, su0, vTopL,  lightCoords, norm);
                        }
                    }
                    // West (x=0, outward normal -X) — omit on shared edges
                    if (!state.hasNeighborWest) {
                        Vector3f norm = pose.transformNormal(-1, 0, 0, new Vector3f());
                        for (int row = 0; row < gridSize; row++) {
                            float z0s = row * cellSize, z1s = z0s + cellSize;
                            float hN  = heights[0][row], hS = heights[0][row + 1];
                            float sw0 = u0 + (u1 - u0) * (float) row / gridSize;
                            float sw1 = u0 + (u1 - u0) * (float) (row + 1) / gridSize;
                            float vTopN = v0 + (v1 - v0) * (1.0f - hN);
                            float vTopS = v0 + (v1 - v0) * (1.0f - hS);
                            emitVertex(mat, buffer, 0f, DEFAULT_HEIGHT, z1s, r, g, b, a, sw1, vBase,  lightCoords, norm);
                            emitVertex(mat, buffer, 0f, DEFAULT_HEIGHT, z0s, r, g, b, a, sw0, vBase,  lightCoords, norm);
                            emitVertex(mat, buffer, 0f, hN,             z0s, r, g, b, a, sw0, vTopN,  lightCoords, norm);
                            emitVertex(mat, buffer, 0f, hS,             z1s, r, g, b, a, sw1, vTopS,  lightCoords, norm);
                        }
                    }
                    // East (x=1, outward normal +X) — omit on shared edges
                    if (!state.hasNeighborEast) {
                        Vector3f norm = pose.transformNormal(1, 0, 0, new Vector3f());
                        for (int row = 0; row < gridSize; row++) {
                            float z0s = row * cellSize, z1s = z0s + cellSize;
                            float hN  = heights[gridSize][row], hS = heights[gridSize][row + 1];
                            float sw0 = u0 + (u1 - u0) * (float) row / gridSize;
                            float sw1 = u0 + (u1 - u0) * (float) (row + 1) / gridSize;
                            float vTopN = v0 + (v1 - v0) * (1.0f - hN);
                            float vTopS = v0 + (v1 - v0) * (1.0f - hS);
                            emitVertex(mat, buffer, 1f, DEFAULT_HEIGHT, z0s, r, g, b, a, sw0, vBase,  lightCoords, norm);
                            emitVertex(mat, buffer, 1f, DEFAULT_HEIGHT, z1s, r, g, b, a, sw1, vBase,  lightCoords, norm);
                            emitVertex(mat, buffer, 1f, hS,             z1s, r, g, b, a, sw1, vTopS,  lightCoords, norm);
                            emitVertex(mat, buffer, 1f, hN,             z0s, r, g, b, a, sw0, vTopN,  lightCoords, norm);
                        }
                    }
                });
    }

    private void renderFluidSurfaceVoxel(
            PersonalPrivateItemPresenterRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector,
            int argbColor) {

        TextureAtlasSprite sprite = getSurfaceSprite(state.surfaceTexture);
        if (sprite == null) return;

        int gridSize   = state.gridSize;
        float cellSize = 1.0f / gridSize;
        float time     = state.gameTime * state.waveSpeed;

        int rawA = (argbColor >> 24) & 0xFF;
        final int a = rawA == 0 ? 255 : rawA;
        final int r = (argbColor >> 16) & 0xFF;
        final int g = (argbColor >>  8) & 0xFF;
        final int b =  argbColor        & 0xFF;

        float u0 = sprite.getU0(), u1 = sprite.getU1();
        float v0 = sprite.getV0(), v1 = sprite.getV1();

        // Per-cell height sampled at each cell center
        float[][] cellH = new float[gridSize][gridSize];
        if (state.physicsHeights != null) {
            // Physics mode: average the four corner displacements to get cell-centre height.
            int N = gridSize + 1;
            for (int col = 0; col < gridSize; col++) {
                for (int row = 0; row < gridSize; row++) {
                    float disp = 0.25f * (state.physicsHeights[col * N + row]
                                        + state.physicsHeights[(col + 1) * N + row]
                                        + state.physicsHeights[col * N + (row + 1)]
                                        + state.physicsHeights[(col + 1) * N + (row + 1)]);
                    cellH[col][row] = DEFAULT_HEIGHT + disp;
                }
            }
        } else {
            // Non-physics mode: procedural wave function.
            float amp = BASE_AMPLITUDE * state.waveAmp;
            float maxDelta = amp * 3.0f;
            for (int col = 0; col < gridSize; col++) {
                for (int row = 0; row < gridSize; row++) {
                    float phase = (col * 0.4f) + (row * 0.4f);
                    float h = DEFAULT_HEIGHT + amp * (float) Math.sin(time * 2.0f + phase);
                    cellH[col][row] = Math.max(DEFAULT_HEIGHT - maxDelta, Math.min(DEFAULT_HEIGHT + maxDelta, h));
                }
            }
        }

        int lightCoords = state.lightCoords;

        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(),
                (pose, buffer) -> {
                    Matrix4f mat    = pose.pose();
                    Vector3f upNorm = pose.transformNormal(0, 1, 0, new Vector3f());

                    for (int col = 0; col < gridSize; col++) {
                        for (int row = 0; row < gridSize; row++) {
                            float x0 = col * cellSize, x1 = x0 + cellSize;
                            float z0 = row * cellSize, z1 = z0 + cellSize;
                            float h  = cellH[col][row];

                            float fu0 = u0 + (u1 - u0) * (float) col / gridSize;
                            float fu1 = u0 + (u1 - u0) * (float) (col + 1) / gridSize;
                            float fv0 = v0 + (v1 - v0) * (float) row / gridSize;
                            float fv1 = v0 + (v1 - v0) * (float) (row + 1) / gridSize;

                            // Flat top face
                            emitVertex(mat, buffer, x0, h, z0, r, g, b, a, fu0, fv0, lightCoords, upNorm);
                            emitVertex(mat, buffer, x0, h, z1, r, g, b, a, fu0, fv1, lightCoords, upNorm);
                            emitVertex(mat, buffer, x1, h, z1, r, g, b, a, fu1, fv1, lightCoords, upNorm);
                            emitVertex(mat, buffer, x1, h, z0, r, g, b, a, fu1, fv0, lightCoords, upNorm);

                            // North side (z=z0): expose face where this column is taller than its row-1 neighbor
                            float hN = (row > 0) ? cellH[col][row - 1] : DEFAULT_HEIGHT;
                            if (h > hN && (row > 0 || !state.hasNeighborNorth)) {
                                Vector3f norm = pose.transformNormal(0, 0, -1, new Vector3f());
                                float vBot = v0 + (v1 - v0) * (1.0f - hN);
                                float vTop = v0 + (v1 - v0) * (1.0f - h);
                                emitVertex(mat, buffer, x1, hN, z0, r, g, b, a, fu1, vBot, lightCoords, norm);
                                emitVertex(mat, buffer, x0, hN, z0, r, g, b, a, fu0, vBot, lightCoords, norm);
                                emitVertex(mat, buffer, x0, h,  z0, r, g, b, a, fu0, vTop, lightCoords, norm);
                                emitVertex(mat, buffer, x1, h,  z0, r, g, b, a, fu1, vTop, lightCoords, norm);
                            }

                            // South side (z=z1): expose face where this column is taller than its row+1 neighbor
                            float hS = (row < gridSize - 1) ? cellH[col][row + 1] : DEFAULT_HEIGHT;
                            if (h > hS && (row < gridSize - 1 || !state.hasNeighborSouth)) {
                                Vector3f norm = pose.transformNormal(0, 0, 1, new Vector3f());
                                float vBot = v0 + (v1 - v0) * (1.0f - hS);
                                float vTop = v0 + (v1 - v0) * (1.0f - h);
                                emitVertex(mat, buffer, x0, hS, z1, r, g, b, a, fu0, vBot, lightCoords, norm);
                                emitVertex(mat, buffer, x1, hS, z1, r, g, b, a, fu1, vBot, lightCoords, norm);
                                emitVertex(mat, buffer, x1, h,  z1, r, g, b, a, fu1, vTop, lightCoords, norm);
                                emitVertex(mat, buffer, x0, h,  z1, r, g, b, a, fu0, vTop, lightCoords, norm);
                            }

                            // West side (x=x0): expose face where this column is taller than its col-1 neighbor
                            float hW = (col > 0) ? cellH[col - 1][row] : DEFAULT_HEIGHT;
                            if (h > hW && (col > 0 || !state.hasNeighborWest)) {
                                Vector3f norm = pose.transformNormal(-1, 0, 0, new Vector3f());
                                float vBot = v0 + (v1 - v0) * (1.0f - hW);
                                float vTop = v0 + (v1 - v0) * (1.0f - h);
                                float sw0 = u0 + (u1 - u0) * (float) row / gridSize;
                                float sw1 = u0 + (u1 - u0) * (float) (row + 1) / gridSize;
                                emitVertex(mat, buffer, x0, hW, z0, r, g, b, a, sw0, vBot, lightCoords, norm);
                                emitVertex(mat, buffer, x0, hW, z1, r, g, b, a, sw1, vBot, lightCoords, norm);
                                emitVertex(mat, buffer, x0, h,  z1, r, g, b, a, sw1, vTop, lightCoords, norm);
                                emitVertex(mat, buffer, x0, h,  z0, r, g, b, a, sw0, vTop, lightCoords, norm);
                            }

                            // East side (x=x1): expose face where this column is taller than its col+1 neighbor
                            float hE = (col < gridSize - 1) ? cellH[col + 1][row] : DEFAULT_HEIGHT;
                            if (h > hE && (col < gridSize - 1 || !state.hasNeighborEast)) {
                                Vector3f norm = pose.transformNormal(1, 0, 0, new Vector3f());
                                float vBot = v0 + (v1 - v0) * (1.0f - hE);
                                float vTop = v0 + (v1 - v0) * (1.0f - h);
                                float sw0 = u0 + (u1 - u0) * (float) row / gridSize;
                                float sw1 = u0 + (u1 - u0) * (float) (row + 1) / gridSize;
                                emitVertex(mat, buffer, x1, hE, z1, r, g, b, a, sw1, vBot, lightCoords, norm);
                                emitVertex(mat, buffer, x1, hE, z0, r, g, b, a, sw0, vBot, lightCoords, norm);
                                emitVertex(mat, buffer, x1, h,  z0, r, g, b, a, sw0, vTop, lightCoords, norm);
                                emitVertex(mat, buffer, x1, h,  z1, r, g, b, a, sw1, vTop, lightCoords, norm);
                            }
                        }
                    }
                });
    }

    private static void emitVertex(
            Matrix4f mat, com.mojang.blaze3d.vertex.VertexConsumer buffer,
            float lx, float ly, float lz,
            int r, int g, int b, int a,
            float u, float v,
            int lightCoords,
            Vector3f normal) {
        Vector3f pos = mat.transformPosition(new Vector3f(lx, ly, lz), new Vector3f());
        buffer.addVertex(pos.x(), pos.y(), pos.z())
              .setColor(r, g, b, a)
              .setUv(u, v)
              .setOverlay(OverlayTexture.NO_OVERLAY)
              .setLight(lightCoords)
              .setNormal(normal.x(), normal.y(), normal.z());
    }

    // ── Floating item ──────────────────────────────────────────────────────

    private static void renderFloatingItem(
            PersonalPrivateItemPresenterRenderState state,
            PoseStack poseStack,
            SubmitNodeCollector collector) {

        poseStack.pushPose();
        poseStack.translate(state.itemX, state.itemY, state.itemZ);

        if (state.spinItem) {
            float spinAngle = state.gameTime * (float) (2 * Math.PI) * 0.25f; // quarter-turn per time unit
            poseStack.mulPose(Axis.YP.rotation(spinAngle));
        }

        poseStack.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);

        ItemModelResolver resolver = Minecraft.getInstance().getItemModelResolver();
        ItemStackRenderState itemRenderState = new ItemStackRenderState();
        resolver.updateForTopItem(itemRenderState, state.storedItem,
                ItemDisplayContext.FIXED, null, null, 0);
        itemRenderState.submit(poseStack, collector,
                state.lightCoords, OverlayTexture.NO_OVERLAY, 0);

        poseStack.popPose();
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Multiplies two ARGB colors channel-by-channel (each channel scaled 0–255). */
    private static int multiplyColors(int a, int b) {
        int ra = (a >> 16) & 0xFF, ga = (a >> 8) & 0xFF, ba2 = a & 0xFF, aa = (a >> 24) & 0xFF;
        int rb = (b >> 16) & 0xFF, gb = (b >> 8) & 0xFF, bb2 = b & 0xFF, ab = (b >> 24) & 0xFF;
        int r = (ra * rb) / 255, g = (ga * gb) / 255, bl = (ba2 * bb2) / 255;
        int alpha = aa == 0 ? ab : (ab == 0 ? aa : (aa * ab) / 255);
        return (alpha << 24) | (r << 16) | (g << 8) | bl;
    }

    private TextureAtlasSprite getSurfaceSprite(PersonalPrivateItemPresenter.SurfaceTexture variant) {
        return spriteCache.computeIfAbsent(variant, v -> Minecraft.getInstance()
                .getAtlasManager()
                .getAtlasOrThrow(AtlasIds.BLOCKS)
                .getSprite(SPRITE_IDS.get(v)));
    }
}

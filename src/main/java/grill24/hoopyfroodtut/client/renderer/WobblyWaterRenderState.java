package grill24.hoopyfroodtut.client.renderer;

import grill24.hoopyfroodtut.block.WobblyWater;
import grill24.hoopyfroodtut.blockentity.WobblyWaterBlockEntity;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class WobblyWaterRenderState extends BlockEntityRenderState {
    /** Whether a non-depositer player is nearby; hides item and dims surface when true. */
    public boolean isPrivate = false;
    /** Item currently on display (EMPTY when none stored or when isPrivate). */
    public ItemStack storedItem = ItemStack.EMPTY;
    /** Continuous client-side time used to drive all animations (game-time + partial tick). */
    public float gameTime = 0f;
    /** ARGB tint applied to the fluid surface. */
    public int surfaceColor = 0xFFFFFFFF;
    /** Biome-blended ARGB water color sampled at this block's position (water texture only). */
    public int biomeWaterColor = 0xFFFFFFFF;
    /** N×N column grid — higher values give a more detailed ripple. */
    public int gridSize = 8;
    /** Amplitude multiplier for the wave motion. */
    public float waveAmp = 1.0f;
    /** Speed multiplier for the wave motion. */
    public float waveSpeed = 1.0f;
    /** Vertical bob amplitude for the floating item. */
    public float bobAmp = 0.08f;
    /** Speed multiplier for the item bob cycle. */
    public float bobSpeed = 1.0f;
    /** When true, the item rotates slowly on the Y axis. */
    public boolean spinItem = false;
    /** When true, each grid cell is rendered as a flat-topped voxel column instead of a smooth mesh. */
    public boolean voxelMode = false;
    /** Which texture is used for the animated fluid surface. */
    public WobblyWater.SurfaceTexture surfaceTexture = WobblyWater.SurfaceTexture.WATER;
    /** Physics-simulated item position on the surface (block-local, 0–1). */
    public float itemX = 0.5f;
    public float itemY = 0f;
    public float itemZ = 0.5f;
    /** When true, physics drives item position and the wave pattern is suppressed. */
    public boolean physicsEnabled = false;
    /** Which cardinal sides have an adjacent coupled Wobbly Water block; suppresses walls/skirts on those sides. */
    public boolean hasNeighborNorth = false;
    public boolean hasNeighborSouth = false;
    public boolean hasNeighborWest  = false;
    public boolean hasNeighborEast  = false;
    /**
     * Snapshot of surface height displacements from the wave simulation, indexed
     * {@code col*(gridSize+1)+row}.  {@code null} when physicsEnabled is false,
     * in which case render methods use the mathematical wave function instead.
     */
    public float[] physicsHeights = null;
    /** Leaf particles drifting on the fluid surface. Non-null only when physics is active. */
    public float[] leafX        = null;
    public float[] leafZ        = null;
    public float[] leafY        = null;
    public float[] leafAngle    = null;
    public int[]   leafQuadrant = null;
    public int     leafCount    = 0;
    /** Which particle type to render on the surface. */
    public WobblyWaterBlockEntity.SurfaceParticleType surfaceParticleType
            = WobblyWaterBlockEntity.SurfaceParticleType.NONE;
    /** Biome dry-foliage ARGB color, used to tint leaf-litter particles. */
    public int dryFoliageColor = 0xFFFFFFFF;
    /** Per-particle size scale factors (0–1); actual size = sizeMin + scale * (sizeMax - sizeMin). */
    public float[] leafScale = null;
    /** Configured size range for surface particles (block units). */
    public float particleSizeMin = 0.04f;
    public float particleSizeMax = 0.08f;
}

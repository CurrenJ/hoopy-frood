package grill24.hoopyfroodtut.client.renderer;

import grill24.hoopyfroodtut.block.PersonalPrivateItemPresenter;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class PersonalPrivateItemPresenterRenderState extends BlockEntityRenderState {
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
    public PersonalPrivateItemPresenter.SurfaceTexture surfaceTexture = PersonalPrivateItemPresenter.SurfaceTexture.WATER;
    /** Physics-simulated item position on the surface (block-local, 0–1). */
    public float itemX = 0.5f;
    public float itemY = 0f;
    public float itemZ = 0.5f;
    /** When true, physics drives item position and the wave pattern is suppressed. */
    public boolean physicsEnabled = false;
    /** Which cardinal sides have an adjacent coupled PPIP; suppresses walls/skirts on those sides. */
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
}

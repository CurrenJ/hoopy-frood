package grill24.hoopyfroodtut.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

public class InfiniteImprobabilityDriveRenderState extends BlockEntityRenderState {
    /** The item currently being converted, or {@link ItemStack#EMPTY} when idle. */
    public ItemStack heldItem = ItemStack.EMPTY;
    /** Monotonically-increasing animation time used for bob and spin calculations. */
    public float animTime = 0f;
    /** Y offset for the support model part, animated when active state changes. */
    public float supportY = 0f;
    /** Current spin speed for the wheel, ramps up/down with active state. */
    public float wheelSpinSpeed = 0f;
    /** Current bob amplitude for the stamp, ramps up/down with active state. */
    public float stampBobAmplitude = 0f;
    /** Accumulated wheel rotation angle from before current transition. */
    public float wheelRotationOffset = 0f;
    /** Current scale for the floating item (0 to 1). */
    public float itemScale = 0f;
    /** Current scale for the wheel (0 to 1). */
    public float wheelScale = 0f;
    /** Cached item for rendering during scale-out phase when item is removed from inventory. */
    public ItemStack cachedItem = ItemStack.EMPTY;
    /** Continuous rotation time for the floating item (doesn't reset on state changes). */
    public float itemRotationTime = 0f;
    /** Current amplitude for the breathing effect, ramps up/down with active state. */
    public float breatheAmplitude = 0f;
    /** Breathing scale for X axis. */
    public float breatheScaleX = 1f;
    /** Breathing scale for Y axis. */
    public float breatheScaleY = 1f;
    /** Breathing scale for Z axis. */
    public float breatheScaleZ = 1f;
}

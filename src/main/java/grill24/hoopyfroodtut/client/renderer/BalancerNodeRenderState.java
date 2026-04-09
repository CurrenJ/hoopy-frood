package grill24.hoopyfroodtut.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

/**
 * Per-frame render snapshot for the Balancer Node.
 *
 * Phases are accumulated delta-per-frame on the block entity to avoid cumulative
 * game-time error (jumps on reload, pause, or long frames).
 */
public class BalancerNodeRenderState extends BlockEntityRenderState {
    /** Direction the node faces outward (protrusion direction). */
    public Direction facing = Direction.NORTH;

    /** Accumulated rotation angle for element 0 (back plate), in radians. */
    public float phase0 = 0f;
    /** Accumulated rotation angle for element 1 (middle, spins opposite), in radians. */
    public float phase1 = 0f;
    /** Accumulated rotation angle for element 2 (front tip), in radians. */
    public float phase2 = 0f;
}

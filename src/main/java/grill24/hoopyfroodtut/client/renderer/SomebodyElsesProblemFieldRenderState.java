package grill24.hoopyfroodtut.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class SomebodyElsesProblemFieldRenderState extends BlockEntityRenderState {
    /**
     * Animation progress from resting (0) to fully active (1).
     * Drives ring height offsets, floater rise, and orbit radius simultaneously.
     */
    public float ringProgress = 0f;
    /** Current orbit angle for the floaters in radians (advances continuously with game time). */
    public float orbitAngle = 0f;
}

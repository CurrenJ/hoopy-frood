package grill24.hoopyfroodtut.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;

public class DisposableCaterpillarRenderState extends BlockEntityRenderState {
    public Direction facing = Direction.NORTH;
    public boolean isActive = false;
    public float animTime = 0f;
    public float forwardOffset = 0f;
    public boolean hasSturdy = false;
}

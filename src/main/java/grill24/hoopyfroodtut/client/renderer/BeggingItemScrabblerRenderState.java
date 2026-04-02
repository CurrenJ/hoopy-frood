package grill24.hoopyfroodtut.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;

public class BeggingItemScrabblerRenderState extends BlockEntityRenderState {
    /** World-space direction of the current (or last) move. */
    public Direction moveDirection = Direction.NORTH;
    /** Eased 0→1 progress of the advance animation (0 = idle). */
    public float moveOffset = 0f;
    /** Game time for wobble animation. */
    public float animTime = 0f;
    /** Whether the scrabbler has nugget fuel. */
    public boolean hasFuel = false;
    /** Whether the scrabbler has at least one item in its inventory. */
    public boolean hasItems = false;
    /** Whether the scrabbler is currently returning to its home container. */
    public boolean isReturning = false;
    /** Y-axis rotation (radians) to track the current target; 0 = facing North. */
    public float headYaw = 0f;
    /** Snapshot of the inventory for rendering held items in the hollow space below. */
    public NonNullList<ItemStack> inventory = NonNullList.withSize(4, ItemStack.EMPTY);
}

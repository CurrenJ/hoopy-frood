package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An angled repeater that turns signal right: input comes from behind the player (when placed),
 * output exits to the player's right.
 *
 * FACING | Input from             | Output to
 * -------|------------------------|----------
 * WEST   | SOUTH (FACING.ccw)     | EAST
 * NORTH  | WEST  (FACING.ccw)     | SOUTH
 * EAST   | NORTH (FACING.ccw)     | WEST
 * SOUTH  | EAST  (FACING.ccw)     | NORTH
 */
public class RightAngledRepeater extends AngledRepeater {

    public static final MapCodec<RightAngledRepeater> CODEC = simpleCodec(RightAngledRepeater::new);

    public RightAngledRepeater(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<RightAngledRepeater> codec() {
        return CODEC;
    }

    @Override
    public Direction getInputDirection(BlockState state) {
        return state.getValue(FACING).getCounterClockWise();
    }

    @Override
    protected Direction getPlacementFacing(Direction playerDir) {
        return playerDir.getCounterClockWise();
    }
}

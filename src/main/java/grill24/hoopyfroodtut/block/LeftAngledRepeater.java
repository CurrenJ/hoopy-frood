package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

/**
 * An angled repeater that turns signal left: input comes from behind the player (when placed),
 * output exits to the player's left.
 *
 * FACING | Input from             | Output to
 * -------|------------------------|----------
 * EAST   | SOUTH (FACING.cw)      | WEST
 * SOUTH  | WEST  (FACING.cw)      | NORTH
 * WEST   | NORTH (FACING.cw)      | EAST
 * NORTH  | EAST  (FACING.cw)      | SOUTH
 */
public class LeftAngledRepeater extends AngledRepeater {

    public static final MapCodec<LeftAngledRepeater> CODEC = simpleCodec(LeftAngledRepeater::new);

    public LeftAngledRepeater(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<LeftAngledRepeater> codec() {
        return CODEC;
    }

    @Override
    public Direction getInputDirection(BlockState state) {
        return state.getValue(FACING).getClockWise();
    }

    @Override
    protected Direction getPlacementFacing(Direction playerDir) {
        return playerDir.getClockWise();
    }
}

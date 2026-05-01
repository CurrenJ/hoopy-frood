package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ScaffoldedRightAngledRepeater extends ScaffoldedAngledRepeater {

    public static final MapCodec<ScaffoldedRightAngledRepeater> CODEC = simpleCodec(ScaffoldedRightAngledRepeater::new);

    public ScaffoldedRightAngledRepeater(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ScaffoldedRightAngledRepeater> codec() {
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

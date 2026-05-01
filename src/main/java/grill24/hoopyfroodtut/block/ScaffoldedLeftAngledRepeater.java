package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;

public class ScaffoldedLeftAngledRepeater extends ScaffoldedAngledRepeater {

    public static final MapCodec<ScaffoldedLeftAngledRepeater> CODEC = simpleCodec(ScaffoldedLeftAngledRepeater::new);

    public ScaffoldedLeftAngledRepeater(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<ScaffoldedLeftAngledRepeater> codec() {
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

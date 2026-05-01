package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ScaffoldedInverter extends Inverter {

    public static final MapCodec<ScaffoldedInverter> CODEC = simpleCodec(ScaffoldedInverter::new);

    public ScaffoldedInverter(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public MapCodec<Inverter> codec() {
        return (MapCodec) CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }
}

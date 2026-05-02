package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.ReleaseLatchBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

public class ScaffoldedReleaseLatch extends ReleaseLatch {

    public static final MapCodec<ScaffoldedReleaseLatch> CODEC = simpleCodec(ScaffoldedReleaseLatch::new);

    public ScaffoldedReleaseLatch(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends ReleaseLatch> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.block();
    }

    @Override
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.block();
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReleaseLatchBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != HoopyFroodBlockEntityTypes.RELEASE_LATCH.get()) return null;
        return (BlockEntityTicker<T>) (BlockEntityTicker<ReleaseLatchBlockEntity>) ReleaseLatchBlockEntity::tick;
    }
}

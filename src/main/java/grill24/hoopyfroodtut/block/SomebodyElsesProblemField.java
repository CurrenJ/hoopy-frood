package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.SomebodyElsesProblemFieldBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The Somebody Else's Problem Field.
 * <p>
 * When placed, it creates an invisible null zone (radius {@link grill24.hoopyfroodtut.core.SepFieldManager#FIELD_RADIUS}
 * blocks) in which mob AI simply cannot function: mobs will refuse to acquire
 * targets inside the zone and pathfinding requests that would enter it are
 * silently discarded. The mechanic is the SEP joke made literal — the AI
 * treats everything in the field as not a problem.
 * <p>
 * Visually the block glows faintly to indicate an active field is present.
 * No tick logic is needed; all suppression happens via the event bus and a
 * {@link grill24.hoopyfroodtut.mixin.PathNavigationMixin}.
 */
public class SomebodyElsesProblemField extends BaseEntityBlock {

    public static final MapCodec<SomebodyElsesProblemField> CODEC =
            simpleCodec(SomebodyElsesProblemField::new);

    public SomebodyElsesProblemField(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // -------------------------------------------------------------------------
    // Block entity — no tick needed
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SomebodyElsesProblemFieldBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return null;
    }

    // -------------------------------------------------------------------------
    // Rendering — standard cube model (blockstate JSON generated via datagen)
    // -------------------------------------------------------------------------

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}

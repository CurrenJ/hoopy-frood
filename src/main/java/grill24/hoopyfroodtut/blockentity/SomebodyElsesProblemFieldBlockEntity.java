package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.SepFieldManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Block entity for the Somebody Else's Problem Field.
 * <p>
 * Its sole purpose is to register/unregister the field position with
 * {@link SepFieldManager} so the event handlers and pathfinding mixin can
 * query active fields in O(n) without scanning the world each tick.
 */
public class SomebodyElsesProblemFieldBlockEntity extends BlockEntity {

    public SomebodyElsesProblemFieldBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.SOMEBODY_ELSES_PROBLEM_FIELD.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Registration lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            SepFieldManager.register(level, worldPosition);
        }
    }

    @Override
    public void onChunkUnloaded() {
        if (level != null && !level.isClientSide()) {
            SepFieldManager.unregister(level, worldPosition);
        }
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {
            SepFieldManager.unregister(level, worldPosition);
        }
        super.setRemoved();
    }
}

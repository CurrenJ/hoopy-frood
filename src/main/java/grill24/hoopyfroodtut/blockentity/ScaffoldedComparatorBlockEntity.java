package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Block entity for ScaffoldedComparator.
 * Replicates ComparatorBlockEntity's stored output signal with its own
 * BlockEntityType so it doesn't clash with the vanilla COMPARATOR type.
 */
public class ScaffoldedComparatorBlockEntity extends BlockEntity {

    private int output = 0;

    public ScaffoldedComparatorBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.SCAFFOLDED_COMPARATOR.get(), pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("OutputSignal", this.output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.output = input.getIntOr("OutputSignal", 0);
    }

    public int getOutputSignal() {
        return this.output;
    }

    public void setOutputSignal(int value) {
        this.output = value;
    }
}

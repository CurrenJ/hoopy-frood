package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.piston.PistonHeadBlock;
import net.minecraft.world.level.block.state.properties.PistonType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Piston head that recognizes {@link SturdyPistonBaseBlock} as a valid base block.
 *
 * <p>Vanilla {@link PistonHeadBlock#isFittingBase} only checks for
 * {@code Blocks.PISTON} / {@code Blocks.STICKY_PISTON}. This override also accepts
 * our sturdy piston variants so the head doesn't self-destruct after extension.
 */
public class SturdyPistonHeadBlock extends PistonHeadBlock {
    public static final MapCodec<PistonHeadBlock> CODEC = simpleCodec(SturdyPistonHeadBlock::new);

    public SturdyPistonHeadBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<PistonHeadBlock> codec() {
        return CODEC;
    }

    @Override
    protected boolean isFittingBase(BlockState armState, BlockState potentialBase) {
        Block baseBlock = armState.getValue(TYPE) == PistonType.DEFAULT ? Blocks.PISTON : Blocks.STICKY_PISTON;
        Block sturdyVariant = armState.getValue(TYPE) == PistonType.DEFAULT
                ? HoopyFroodTutBlocks.STURDY_PISTON.get()
                : HoopyFroodTutBlocks.STICKY_STURDY_PISTON.get();
        return (potentialBase.is(baseBlock) || potentialBase.is(sturdyVariant))
                && potentialBase.getValue(PistonBaseBlock.EXTENDED)
                && potentialBase.getValue(PistonBaseBlock.FACING) == armState.getValue(FACING);
    }
}

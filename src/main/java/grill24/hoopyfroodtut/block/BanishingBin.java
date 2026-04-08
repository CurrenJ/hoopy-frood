package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.BanishingBinBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Banishing Bin — a craftable block that accepts items and permanently removes them from
 * the world by teleporting them to a seeded, extremely distant location.
 *
 * Visual design: a gyroscopic armillary-sphere of three independently rotating rings around
 * a central void, rendered entirely by BER.
 */
public class BanishingBin extends BaseEntityBlock {

    public static final MapCodec<BanishingBin> CODEC = simpleCodec(BanishingBin::new);

    public BanishingBin(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BanishingBinBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type,
                HoopyFroodBlockEntityTypes.BANISHING_BIN.get(),
                BanishingBinBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    private static final VoxelShape OUTLINE_SHAPE = Shapes.box(3.0 / 16.0, 0, 3.0 / 16.0,
            13.0 / 16.0, 1.0, 13.0 / 16.0);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(HoopyFroodItems.BANISHING_BIN_ITEM.get()));
        // Drop any queued items that haven't been banished yet
        if (params.getOptionalParameter(LootContextParams.BLOCK_ENTITY) instanceof BanishingBinBlockEntity be) {
            for (var queued : be.getQueue()) {
                drops.add(queued.stack().copy());
            }
        }
        return drops;
    }
}

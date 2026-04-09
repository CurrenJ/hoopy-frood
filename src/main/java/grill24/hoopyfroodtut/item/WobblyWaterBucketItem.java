package grill24.hoopyfroodtut.item;

import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A filled bucket containing Wobbly Water. Looks identical to the water bucket.
 * Right-clicking places the Wobbly Water block; the bucket becomes empty on use.
 */
public class WobblyWaterBucketItem extends BucketItem {

    public WobblyWaterBucketItem(Item.Properties properties) {
        // Pass Fluids.WATER so NeoForge fluid-handling hooks see this as a water bucket.
        super(Fluids.WATER, properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);

        if (hit.getType() == HitResult.Type.MISS || hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }

        BlockPos pos = hit.getBlockPos();
        Direction face = hit.getDirection();
        BlockPos placePos = pos.relative(face);

        if (!level.mayInteract(player, pos) || !player.mayUseItemAt(placePos, face, itemStack)) {
            return InteractionResult.FAIL;
        }

        BlockState target = level.getBlockState(placePos);
        if (!target.canBeReplaced()) {
            return InteractionResult.FAIL;
        }

        if (!level.isClientSide()) {
            level.setBlock(placePos, HoopyFroodTutBlocks.WOBBLY_WATER.get().defaultBlockState(), Block.UPDATE_ALL_IMMEDIATE);
            level.playSound(null, placePos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(player, GameEvent.FLUID_PLACE, placePos);
            if (player instanceof ServerPlayer sp) {
                CriteriaTriggers.PLACED_BLOCK.trigger(sp, placePos, itemStack);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        ItemStack empty = ItemUtils.createFilledResult(itemStack, player, getEmptySuccessItem(itemStack, player));
        return InteractionResult.SUCCESS.heldItemTransformedTo(empty);
    }
}

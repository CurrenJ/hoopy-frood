package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.entity.InertPrimedTnt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

public class InertTntBlock extends TntBlock {
    @SuppressWarnings("unchecked")
    private static final MapCodec<TntBlock> CODEC = (MapCodec<TntBlock>) (MapCodec<?>) simpleCodec(InertTntBlock::new);

    public InertTntBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<TntBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean onCaughtFire(BlockState state, Level world, BlockPos pos, Direction face, @Nullable LivingEntity igniter) {
        if (world instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.TNT_EXPLODES)) {
            InertPrimedTnt tnt = new InertPrimedTnt(world, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, igniter);
            world.addFreshEntity(tnt);
            world.playSound(null, tnt.getX(), tnt.getY(), tnt.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            world.gameEvent(igniter, GameEvent.PRIME_FUSE, pos);
            return true;
        }
        return false;
    }

    @Override
    public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
        if (level.getGameRules().get(GameRules.TNT_EXPLODES)) {
            InertPrimedTnt primed = new InertPrimedTnt(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, explosion.getIndirectSourceEntity());
            int fuse = primed.getFuse();
            primed.setFuse((short) (level.getRandom().nextInt(fuse / 4) + fuse / 8));
            level.addFreshEntity(primed);
        }
    }
}

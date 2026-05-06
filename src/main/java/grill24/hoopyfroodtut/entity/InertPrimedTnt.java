package grill24.hoopyfroodtut.entity;

import grill24.hoopyfroodtut.Config;
import grill24.hoopyfroodtut.core.HoopyFroodEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gamerules.GameRules;
import org.jspecify.annotations.Nullable;

public class InertPrimedTnt extends PrimedTnt {
    private static final ExplosionDamageCalculator INERT_DAMAGE_CALCULATOR = new ExplosionDamageCalculator() {
        @Override
        public boolean shouldBlockExplode(Explosion explosion, BlockGetter level, BlockPos pos, BlockState state, float power) {
            return false;
        }

        @Override
        public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
            return false;
        }

        @Override
        public float getKnockbackMultiplier(Entity entity) {
            return Config.INERT_TNT_KNOCKBACK.get() ? 1.0F : 0.0F;
        }
    };

    public InertPrimedTnt(EntityType<? extends PrimedTnt> type, Level level) {
        super(type, level);
    }

    public InertPrimedTnt(Level level, double x, double y, double z, @Nullable LivingEntity owner) {
        super(HoopyFroodEntities.INERT_PRIMED_TNT.get(), level);
        this.setPos(x, y, z);
        double rot = level.getRandom().nextDouble() * (float) (Math.PI * 2);
        this.setDeltaMovement(-Math.sin(rot) * 0.02, 0.2F, -Math.cos(rot) * 0.02);
        this.setFuse(80);
        this.xo = x;
        this.yo = y;
        this.zo = z;
        this.owner = EntityReference.of(owner);
    }

    @Override
    protected void explode() {
        if (this.level() instanceof ServerLevel level && level.getGameRules().get(GameRules.TNT_EXPLODES)) {
            this.level()
                    .explode(
                            this,
                            Explosion.getDefaultDamageSource(this.level(), this),
                            INERT_DAMAGE_CALCULATOR,
                            this.getX(),
                            this.getY(0.0625),
                            this.getZ(),
                            4.0F,
                            false,
                            Level.ExplosionInteraction.TNT
                    );
        }
    }
}

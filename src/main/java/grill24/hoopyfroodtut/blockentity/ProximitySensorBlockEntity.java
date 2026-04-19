package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.block.ProximitySensor;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ProximitySensorBlockEntity extends BlockEntity {

    public int radius = 8;
    public boolean playersOnly = true;
    public int currentSignal = 0;

    public ProximitySensorBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.PROXIMITY_SENSOR.get(), pos, state);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        radius      = input.getIntOr("radius", 8);
        playersOnly = input.getBooleanOr("players_only", true);
        currentSignal = input.getIntOr("current_signal", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("radius", radius);
        output.putBoolean("players_only", playersOnly);
        output.putInt("current_signal", currentSignal);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ProximitySensorBlockEntity be) {
        if (level.getGameTime() % 4 != 0) return;

        int newSignal = be.computeSignal(level, pos);
        if (newSignal == be.currentSignal) return;

        be.currentSignal = newSignal;
        be.setChanged();

        boolean newActive = newSignal > 0;
        if (state.getValue(ProximitySensor.ACTIVE) != newActive) {
            level.setBlock(pos, state.setValue(ProximitySensor.ACTIVE, newActive), 3);
        } else {
            // ACTIVE didn't change but signal strength did — setBlock won't fire, so
            // manually notify neighbors so redstone wire re-queries getSignal.
            level.updateNeighborsAt(pos, state.getBlock());
        }
        level.updateNeighbourForOutputSignal(pos, state.getBlock());
    }

    private int computeSignal(Level level, BlockPos pos) {
        double cx = pos.getX() + 0.5, cy = pos.getY() + 0.5, cz = pos.getZ() + 0.5;
        double nearest = Double.MAX_VALUE;

        if (playersOnly) {
            Player p = level.getNearestPlayer(cx, cy, cz, radius, false);
            if (p != null) nearest = Math.sqrt(p.distanceToSqr(cx, cy, cz));
        } else {
            AABB box = new AABB(pos).inflate(radius);
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, box,
                    e -> !e.isSpectator());
            for (LivingEntity e : entities) {
                double d = Math.sqrt(e.distanceToSqr(cx, cy, cz));
                if (d < nearest) nearest = d;
            }
        }

        if (nearest > radius) return 0;
        return Math.max(0, 15 - (int) (nearest / radius * 15));
    }
}

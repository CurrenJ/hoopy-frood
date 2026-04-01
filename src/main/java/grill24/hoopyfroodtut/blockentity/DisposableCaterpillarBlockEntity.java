package grill24.hoopyfroodtut.blockentity;

import grill24.hoopyfroodtut.block.DisposableCaterpillar;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodTutBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

/**
 * Block entity for the Disposable Caterpillar.
 * <p>
 * Stores how many "charges" (remaining moves) this caterpillar has. Each time the
 * caterpillar processes a redstone rising edge it:
 * <ol>
 *   <li>Mines the block directly ahead (in the FACING direction), dropping its items.</li>
 *   <li>If charges &gt; 1: places a new Disposable Caterpillar in that now-empty space
 *       with {@code charges - 1}.</li>
 *   <li>Destroys itself, dropping nothing.</li>
 * </ol>
 * Charges are transferred to/from the item form via the
 * {@code hoopyfroodtut:caterpillar_charges} data component.
 */
public class DisposableCaterpillarBlockEntity extends BlockEntity {

    private int charges = 1;
    private int torches = 0;
    private int blocksSinceLastTorch = 0;

    private float mineProgress = 0f;
    private float miningSpeedMultiplier = 0.1f;

    private int advanceTimestamp = -1;

    private int cooldown = 0;

    private static boolean ENABLE_CHAIN_TRIGGER = true;
    private static int COOLDOWN_AFTER = 20;
    public static int ADVANCE_FORWARD_DURATION = 20;
    private static final int TORCH_INTERVAL = 12;

    public DisposableCaterpillarBlockEntity(BlockPos pos, BlockState state) {
        super(HoopyFroodBlockEntityTypes.DISPOSABLE_CATERPILLAR.get(), pos, state);
    }

    // -------------------------------------------------------------------------
    // Data accessors and mutators
    // -------------------------------------------------------------------------

    public int getCharges() {
        return charges;
    }

    public void setCharges(int charges) {
        this.charges = Math.max(1, charges);
        setChanged();
    }

    public int getTorches() {
        return torches;
    }

    public void setTorches(int torches) {
        this.torches = Math.max(0, torches);
        setChanged();
    }

    public int getBlocksSinceLastTorch() {
        return blocksSinceLastTorch;
    }

    public void setBlocksSinceLastTorch(int blocksSinceLastTorch) {
        this.blocksSinceLastTorch = blocksSinceLastTorch;
        setChanged();
    }

    public int getCooldown() { return cooldown; }

    public void setCooldown(int cooldown) {
        this.cooldown = cooldown;
        setChanged();
    }

    public int getAdvanceTimestamp() {
        return advanceTimestamp;
    }

    public void setAdvanceTimestamp(int advanceTimestamp) {
        this.advanceTimestamp = advanceTimestamp;
        setChanged();
    }

    public void addCooldown(int amount) {
        setCooldown(this.cooldown + amount);
    }

    // -------------------------------------------------------------------------
    // Persistence
    // -------------------------------------------------------------------------

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.charges = input.getIntOr("Charges", 1);
        this.torches = input.getIntOr("Torches", 0);
        this.blocksSinceLastTorch = input.getIntOr("BlocksSinceLastTorch", 0);
        this.cooldown = input.getIntOr("Cooldown", 0);
        this.advanceTimestamp = input.getIntOr("AdvanceTimestamp", -1);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Charges", charges);
        output.putInt("Torches", torches);
        output.putInt("BlocksSinceLastTorch", blocksSinceLastTorch);
        output.putInt("Cooldown", cooldown);
        output.putInt("AdvanceTimestamp", advanceTimestamp);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = this.saveWithoutMetadata(registries);
        tag.putInt("AdvanceTimestamp", advanceTimestamp);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // The packet uses the CompoundTag returned by #getUpdateTag. An alternative overload of #create exists
        // that allows you to specify a custom update tag, including the ability to omit data the client might not need.
        return ClientboundBlockEntityDataPacket.create(this);
    }

    // Optionally: Run some custom logic when the packet is received.
    // The super/default implementation forwards to #loadWithComponents.
    @Override
    public void onDataPacket(Connection connection, ValueInput input) {
        super.onDataPacket(connection, input);
        this.advanceTimestamp = input.getIntOr("AdvanceTimestamp", -1);
    }


    // -------------------------------------------------------------------------
    // Tick — rising-edge redstone detection
    // -------------------------------------------------------------------------

    /**
     * Called every server tick. Detects the rising edge of a redstone signal (powered
     * but not yet triggered) and fires {@link #process} exactly once per pulse.
     * The TRIGGERED block-state property is used as persistent "already seen this pulse"
     * flag so behaviour is correct across server restarts.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, DisposableCaterpillarBlockEntity entity) {
        // Spawn particles at tail while mining
        if (state.getValue(DisposableCaterpillar.TRIGGERED) && entity.advanceTimestamp < 0 && level.getGameTime() % 6 == 0) {
            spawnParticle(level, pos, state);
        }

        boolean isPowered = level.hasNeighborSignal(pos);
        boolean wasTriggered = state.getValue(DisposableCaterpillar.TRIGGERED);

        if (isPowered || wasTriggered) {
            // Rising edge: mark as triggered first, then process.
            // Marking before processing prevents re-entry if a block update somehow
            // re-evaluates this tick before the self-destruct completes.

            BlockState triggeredState = state.setValue(DisposableCaterpillar.TRIGGERED, true);
            level.setBlock(pos, triggeredState, Block.UPDATE_ALL);
            entity.process((ServerLevel) level, pos, triggeredState);
        } else if (!isPowered && wasTriggered) {
            // Falling edge: reset the trigger flag so the next pulse fires again.
            level.setBlock(pos, state.setValue(DisposableCaterpillar.TRIGGERED, false), Block.UPDATE_ALL);
        }
    }

    private static void spawnParticle(Level level, BlockPos pos, BlockState state) {
        if (state.hasProperty(DisposableCaterpillar.FACING) && level instanceof ServerLevel serverLevel) {
            // Hard-coded offset relative to default model orientation (facing north with legs down) that looks approximately like the tail end of the caterpillar.
            Vec3 tailOffset = new Vec3(0, -0.3, -0.6);
            Direction facing = state.getValue(DisposableCaterpillar.FACING);
            Vec3 rotatedOffset = switch (facing) {
                case SOUTH -> tailOffset;
                case EAST -> new Vec3(tailOffset.z, tailOffset.y, tailOffset.x);
                case WEST -> new Vec3(-tailOffset.z, tailOffset.y, -tailOffset.x);
                case DOWN -> new Vec3(tailOffset.x, -tailOffset.z, -tailOffset.y);
                case UP -> new Vec3(tailOffset.x, tailOffset.z, -tailOffset.y);
                default -> new Vec3(tailOffset.x, tailOffset.y, -tailOffset.z); // NORTH
            };
            Vec3 tailPos = Vec3.atCenterOf(pos).add(rotatedOffset);
            serverLevel.sendParticles(ParticleTypes.SMOKE, tailPos.x(), tailPos.y(), tailPos.z(), 8, 0, 0.04, 0, 0.03);
        }
    }

    // -------------------------------------------------------------------------
    // Process — mine, (optionally) place successor, self-destruct
    // -------------------------------------------------------------------------

    /**
     * Performs the caterpillar's one-shot mining action:
     * <ol>
     *   <li>Looks at the block at {@code pos + FACING}.</li>
     *   <li>If that block is unbreakable (hardness &lt; 0) the caterpillar aborts and
     *       only destroys itself.</li>
     *   <li>Otherwise the target block is destroyed with normal loot drops.</li>
     *   <li>If {@code charges &gt; 1} a new Disposable Caterpillar is placed in the
     *       vacated space with {@code charges - 1}.</li>
     *   <li>This block is destroyed with no drops.</li>
     * </ol>
     */
    public void process(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(DisposableCaterpillar.FACING);
        BlockPos targetPos = pos.relative(facing);
        BlockState targetState = level.getBlockState(targetPos);

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        if (advanceTimestamp >= 0 && level.getGameTime() - advanceTimestamp < ADVANCE_FORWARD_DURATION) {
            // Still in "advance forward" phase after mining: skip processing to allow the animation to play out.
            return;
        }

        // Advance animation has finished — place successor regardless of what now occupies the target
        // (e.g. water may have refilled the position after being mined).
        if (advanceTimestamp >= 0) {
            tryPlaceSuccessor(level, pos, state, COOLDOWN_AFTER);
            return;
        }

        // Liquid blocks (water, lava) are treated as instantly passable — no mining needed.
        boolean targetIsPassable = targetState.isAir() || targetState.getBlock() instanceof LiquidBlock;
        if (targetIsPassable) {
            if (targetState.getBlock() instanceof LiquidBlock) {
                level.destroyBlock(targetPos, false); // Remove liquid without drops
            }
            setAdvanceTimestamp((int) level.getGameTime());
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL); // Notify clients to start the advance animation
            return;
        }

        // Unbreakable blocks (bedrock, end portal frame, etc.) — abort and destroy self.
        if (targetState.getDestroySpeed(level, targetPos) < 0f) {
            level.destroyBlock(pos, false);
            return;
        }

        float destroySpeed = targetState.getDestroySpeed(level, targetPos);
        if(mineProgress <= 10f && destroySpeed > 0f) {
            // Vanilla formula: progress += playerSpeed / hardness / constant
            // miningSpeedMultiplier acts as the "player speed"; divide by hardness so harder blocks take longer.
            mineProgress += (miningSpeedMultiplier / destroySpeed) * 10f;
            level.destroyBlockProgress(-1, targetPos, (int) (mineProgress));
            return;
        } else {
            // Mine the target block (drops items naturally), then begin advance animation.
            setAdvanceTimestamp((int) level.getGameTime());
            level.destroyBlock(targetPos, true);
            level.sendBlockUpdated(pos, state, state, Block.UPDATE_ALL); // Notify clients to start the advance animation
            return;
        }
    }

    private void tryPlaceSuccessor(ServerLevel level, BlockPos pos, BlockState state, int cooldown) {
        int newBlocksSinceLastTorch = blocksSinceLastTorch + 1;
        boolean shouldPlaceTorch = torches > 0 && newBlocksSinceLastTorch >= TORCH_INTERVAL;
        boolean torchPlaced = shouldPlaceTorch && tryPlaceTorch(level, pos, state);

        int successorTorches = torchPlaced ? torches - 1 : torches;
        int successorBlocksSince = torchPlaced ? 0 : newBlocksSinceLastTorch;

        if (charges <= 1) {
            // No successor: torch has already replaced the block (if placed), otherwise destroy.
            if (!torchPlaced) {
                level.destroyBlock(pos, false);
            }
        } else {
            // Place the successor at targetPos, then vacate pos.
            placeSuccessor(level, pos, state, cooldown, successorTorches, successorBlocksSince);
            if (!torchPlaced) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
    }

    /**
     * Attempts to place a standing torch at {@code pos}. Falls back to a wall torch
     * on the face the caterpillar came from. Returns true if a torch was placed.
     */

    private static Direction[] walls = new Direction[] {Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST};
    private boolean tryPlaceTorch(ServerLevel level, BlockPos pos, BlockState caterpillarState) {
        BlockState torchState = Blocks.TORCH.defaultBlockState();
        if (torchState.canSurvive(level, pos)) {
            level.setBlock(pos, torchState, Block.UPDATE_ALL);
            return true;
        }

        for (Direction dir : walls) {
            BlockState wallTorch = Blocks.WALL_TORCH.defaultBlockState()
                    .setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, dir);
            if (wallTorch.canSurvive(level, pos)) {
                level.setBlock(pos, wallTorch, Block.UPDATE_ALL);
                return true;
            }
        }

        return false;
    }

    private void placeSuccessor(ServerLevel level, BlockPos pos, BlockState state, int cooldown, int successorTorches, int successorBlocksSince) {
        Direction facing = state.getValue(DisposableCaterpillar.FACING);
        BlockPos targetPos = pos.relative(facing);

        BlockState successorState = HoopyFroodTutBlocks.DISPOSABLE_CATERPILLAR.get()
                .defaultBlockState()
                .setValue(DisposableCaterpillar.FACING, facing)
                .setValue(DisposableCaterpillar.TRIGGERED, false);
        level.setBlock(targetPos, successorState, Block.UPDATE_ALL);

        if (level.getBlockEntity(targetPos) instanceof DisposableCaterpillarBlockEntity successor) {
            successor.setCharges(charges - 1);
            successor.setTorches(successorTorches);
            successor.setBlocksSinceLastTorch(successorBlocksSince);
            successor.addCooldown(cooldown);
            if (ENABLE_CHAIN_TRIGGER) {
                BlockState triggeredState = level.getBlockState(targetPos).setValue(DisposableCaterpillar.TRIGGERED, true);
                level.setBlock(targetPos, triggeredState, Block.UPDATE_ALL);
            }
        }
    }
}

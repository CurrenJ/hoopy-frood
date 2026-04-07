package grill24.hoopyfroodtut.block;

import com.mojang.serialization.MapCodec;
import grill24.hoopyfroodtut.blockentity.PersonalPrivateItemPresenterBlockEntity;
import grill24.hoopyfroodtut.core.HoopyFroodBlockEntityTypes;
import grill24.hoopyfroodtut.core.HoopyFroodItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Personal Private Item Presenter — a single-item display block whose animated fluid
 * surface hides the stored item (and switches to a disabled visual state) whenever any
 * player who is NOT the original depositer comes within range.
 */
public class PersonalPrivateItemPresenter extends BaseEntityBlock {

    // ── Surface texture variants (cycled with the debug stick) ────────────

    public enum SurfaceTexture implements StringRepresentable {
        WATER("water"),
        LAVA("lava"),
        SLIME("slime"),
        HONEY("honey"),
        MAGMA("magma");

        private final String name;
        SurfaceTexture(String name) { this.name = name; }

        @Override
        public String getSerializedName() { return name; }
    }

    public static final EnumProperty<SurfaceTexture> SURFACE_TEXTURE =
            EnumProperty.create("surface_texture", SurfaceTexture.class);

    // ── Codec / registration ───────────────────────────────────────────────

    public static final MapCodec<PersonalPrivateItemPresenter> CODEC =
            simpleCodec(PersonalPrivateItemPresenter::new);

    public PersonalPrivateItemPresenter(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SURFACE_TEXTURE, SurfaceTexture.WATER));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SURFACE_TEXTURE);
    }

    // -------------------------------------------------------------------------
    // Player interaction
    // -------------------------------------------------------------------------

    /**
     * Right-click with an item in hand: store it (if slot empty) or swap with stored item.
     */
    @Override
    protected InteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof PersonalPrivateItemPresenterBlockEntity be) {
            ItemStack stored = be.getStoredItem();
            // Take one item from the held stack (respects creative mode)
            ItemStack toStore = stack.copyWithCount(1);
            if (!player.isCreative()) stack.shrink(1);

            if (stored.isEmpty()) {
                be.setStoredItem(toStore, player.getUUID());
            } else {
                // Swap: return stored item to player, then store the new item
                if (!player.getInventory().add(stored.copy())) {
                    player.drop(stored.copy(), false);
                }
                be.setStoredItem(toStore, player.getUUID());
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    /**
     * Right-click with empty hand: eject the stored item back into the player's inventory.
     */
    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (level.isClientSide()) return InteractionResult.SUCCESS;

        if (level.getBlockEntity(pos) instanceof PersonalPrivateItemPresenterBlockEntity be) {
            ItemStack stored = be.getStoredItem();
            if (!stored.isEmpty()) {
                if (!player.getInventory().add(stored.copy())) {
                    player.drop(stored.copy(), false);
                }
                be.setStoredItem(ItemStack.EMPTY, null);
                return InteractionResult.SUCCESS_SERVER;
            }
        }
        return InteractionResult.PASS;
    }

    // -------------------------------------------------------------------------
    // Drops
    // -------------------------------------------------------------------------

    @Override
    protected List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        BlockEntity be = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        List<ItemStack> drops = new ArrayList<>();
        drops.add(new ItemStack(HoopyFroodItems.PERSONAL_PRIVATE_ITEM_PRESENTER_ITEM.get()));
        if (be instanceof PersonalPrivateItemPresenterBlockEntity ppip) {
            ItemStack stored = ppip.getStoredItem();
            if (!stored.isEmpty()) {
                drops.add(stored.copy());
            }
        }
        return drops;
    }

    // -------------------------------------------------------------------------
    // Redstone — 0 when empty, 15 when item present
    // -------------------------------------------------------------------------

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        if (level.getBlockEntity(pos) instanceof PersonalPrivateItemPresenterBlockEntity be) {
            return be.getStoredItem().isEmpty() ? 0 : 15;
        }
        return 0;
    }

    // -------------------------------------------------------------------------
    // Block entity
    // -------------------------------------------------------------------------

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PersonalPrivateItemPresenterBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(type,
                HoopyFroodBlockEntityTypes.PERSONAL_PRIVATE_ITEM_PRESENTER.get(),
                PersonalPrivateItemPresenterBlockEntity::tick);
    }

    // -------------------------------------------------------------------------
    // Rendering hints
    // -------------------------------------------------------------------------

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    /** Outline and ray-cast shape — a flat slab up to the resting fluid surface level (12/16). */
    private static final VoxelShape OUTLINE_SHAPE = Shapes.box(0, 0, 0, 1, 12.0 / 16.0, 1);

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos,
                                  CollisionContext context) {
        return OUTLINE_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        // Items and players pass through so they interact with the fluid surface
        // rather than standing on top of the invisible block hitbox.
        if (context instanceof EntityCollisionContext ecc
                && (ecc.getEntity() instanceof ItemEntity || ecc.getEntity() instanceof LivingEntity)) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos,
                                        CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
}

package grill24.hoopyfroodtut.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Render state snapshot for the Banishing Bin.
 *
 * <p>Extracted once per frame from the block entity; the renderer reads only this object
 * so that rendering is fully decoupled from block entity mutation.
 *
 * <p>Ring phase angles ({@link #ringPhaseA}, {@link #ringPhaseB}, {@link #ringPhaseC}) are
 * accumulated incrementally — delta-per-frame — rather than recomputed from absolute game
 * time. This prevents the large jump that would occur if speed were suddenly scaled against
 * the large absolute time value.
 */
public class BanishingBinRenderState extends BlockEntityRenderState {

    /** Current game time in seconds (ticks + partial tick scaled by ANIM_SPEED). */
    public float animTime = 0f;

    /**
     * How close the bin is to its fully-active state, in [0, 1].
     * 0 = fully inactive (rings still), 1 = fully active (rings gyroscopic, full speed).
     */
    public float ringProgress = 0f;

    /** Game tick at which the most recent item was received. Drives the spin-down. */
    public long lastReceivedTick = -1L;

    /**
     * Game tick at which the bin first became active after being idle.
     * Drives the spin-up animation — not reset by subsequent items while active.
     */
    public long activationTick = -1L;

    /**
     * Accumulated rotation angles for each ring (radians).
     * Copied each frame from the block entity's persistent client-side phase fields.
     */
    public float ringPhaseA = 0f;
    public float ringPhaseB = 0f;
    public float ringPhaseC = 0f;

    /** Accumulated precession angle for ring B's tilt axis (radians). */
    public float precPhaseB = 0f;
    /** Accumulated precession angle for ring C's tilt axis (radians). */
    public float precPhaseC = 0f;

    /** Items currently orbiting on ring 0 (equatorial, iron). */
    public final List<ItemStack> ring0Items = new ArrayList<>();

    /** Items currently orbiting on ring 1 (60° tilt, blaze powder). */
    public final List<ItemStack> ring1Items = new ArrayList<>();

    /** Items currently orbiting on ring 2 (−60° tilt, ender pearls). */
    public final List<ItemStack> ring2Items = new ArrayList<>();

    /** Permanent memory items for the structural ring 0 (drawn from banishedTypes partition). */
    public final List<ItemStack> memoryRing0 = new ArrayList<>();

    /** Permanent memory items for the structural ring 1. */
    public final List<ItemStack> memoryRing1 = new ArrayList<>();

    /** Permanent memory items for the structural ring 2. */
    public final List<ItemStack> memoryRing2 = new ArrayList<>();
}

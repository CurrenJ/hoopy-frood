package grill24.hoopyfroodtut.mixin;

import grill24.hoopyfroodtut.block.WobblyWater;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppresses vanilla fluid mesh generation for Wobbly Water blocks in water texture mode.
 * The block returns a live water {@link FluidState} so entities get swimming
 * physics and biome-color lookups work, but the custom BER handles all water
 * rendering — vanilla should not also tessellate water quads into the chunk mesh.
 */
@Mixin(FluidRenderer.class)
public class FluidRendererMixin {

    @Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
    private void ww_skipWaterForWobblyWater(
            BlockAndTintGetter level,
            BlockPos pos,
            FluidRenderer.Output output,
            BlockState blockState,
            FluidState fluidState,
            CallbackInfo ci) {
        if (blockState.getBlock() instanceof WobblyWater
                && blockState.getValue(WobblyWater.SURFACE_TEXTURE)
                        == WobblyWater.SurfaceTexture.WATER) {
            ci.cancel();
        }
    }
}

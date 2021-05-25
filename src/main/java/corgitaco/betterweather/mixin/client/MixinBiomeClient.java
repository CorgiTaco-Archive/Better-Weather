package corgitaco.betterweather.mixin.client;

import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static corgitaco.betterweather.client.ColorCalculator.Type.*;
import static corgitaco.betterweather.client.ColorCalculator.getBiomeColor;

@Mixin(Biome.class)
public abstract class MixinBiomeClient {

    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void getGrassColor(double posX, double posZ, CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(getBiomeColor((Biome) (Object) this, GRASS, info.getReturnValue()));
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private void getFoliageColor(CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(getBiomeColor((Biome) (Object) this, FOLIAGE, info.getReturnValue()));
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void getSkyColor(CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(getBiomeColor((Biome) (Object) this, SKY, info.getReturnValue()));
    }

    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private void getFogColor(CallbackInfoReturnable<Integer> info) {
        info.setReturnValue(getBiomeColor((Biome) (Object) this, FOG, info.getReturnValue()));
    }
}

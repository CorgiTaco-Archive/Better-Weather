package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.season.client.BiomeColorCalculator;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

import static corgitaco.betterweather.season.client.BiomeColorCalculator.modifyBiomeColor;

@Mixin(Biome.class)
public abstract class MixinBiomeClient {

    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void modifyGrassColor(double posX, double posZ, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(modifyBiomeColor(BiomeColorCalculator.ColorType.GRASS, new Color(cir.getReturnValue()), (Biome) (Object) this).getRGB());
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private void modifyFoliageColor(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(modifyBiomeColor(BiomeColorCalculator.ColorType.FOLIAGE, new Color(cir.getReturnValue()), (Biome) (Object) this).getRGB());
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void modifySkyColor(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(modifyBiomeColor(BiomeColorCalculator.ColorType.SKY, new Color(cir.getReturnValue()), (Biome) (Object) this).getRGB());
    }

    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private void modifyFogColor(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(modifyBiomeColor(BiomeColorCalculator.ColorType.FOG, new Color(cir.getReturnValue()), (Biome) (Object) this).getRGB());
    }
}

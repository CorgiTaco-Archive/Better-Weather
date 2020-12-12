package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.BiomeColorCalculator;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(Biome.class)
public class MixinBiomes {


    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void modifyGrassColor(double posX, double posZ, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(BiomeColorCalculator.modifyBiomeColor(true, new Color(cir.getReturnValue()), BetterWeather.seasonData.getSubSeason()).getRGB());
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private void modifyFoliageColor(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(BiomeColorCalculator.modifyBiomeColor(false, new Color(cir.getReturnValue()), BetterWeather.seasonData.getSubSeason()).getRGB());
    }
}

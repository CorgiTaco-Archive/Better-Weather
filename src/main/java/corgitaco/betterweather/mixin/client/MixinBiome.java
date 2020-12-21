package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.season.BWSeasons;
import corgitaco.betterweather.season.BiomeColorCalculator;
import corgitaco.betterweather.season.Season;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(Biome.class)
public class MixinBiome {


    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void modifyGrassColor(double posX, double posZ, CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(!Season.getSeasonFromEnum(BWSeasons.SeasonVal.SUMMER).containsSubSeason(BWSeasons.cachedSubSeason) ? BiomeColorCalculator.modifyBiomeColor(true, new Color(cir.getReturnValue()), Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason)).getRGB() : cir.getReturnValue());
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private void modifyFoliageColor(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(!Season.getSeasonFromEnum(BWSeasons.SeasonVal.SUMMER).containsSubSeason(BWSeasons.cachedSubSeason) ? BiomeColorCalculator.modifyBiomeColor(false, new Color(cir.getReturnValue()), Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason)).getRGB() : cir.getReturnValue());
    }
}

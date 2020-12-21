package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.season.BWSeasons;
import corgitaco.betterweather.season.BiomeColorCalculator;
import corgitaco.betterweather.season.Season;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(Biome.class)
public abstract class MixinBiome {

    @Shadow public abstract Biome.Category getCategory();

    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void modifyGrassColor(double posX, double posZ, CallbackInfoReturnable<Integer> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE)
            cir.setReturnValue(!Season.getSeasonFromEnum(BWSeasons.SeasonVal.SUMMER).containsSubSeason(BWSeasons.cachedSubSeason) ? BiomeColorCalculator.modifyBiomeColor(BiomeColorCalculator.ColorType.GRASS, new Color(cir.getReturnValue()), Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason)).getRGB() : cir.getReturnValue());
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private void modifyFoliageColor(CallbackInfoReturnable<Integer> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE)
            cir.setReturnValue(!Season.getSeasonFromEnum(BWSeasons.SeasonVal.SUMMER).containsSubSeason(BWSeasons.cachedSubSeason) ? BiomeColorCalculator.modifyBiomeColor(BiomeColorCalculator.ColorType.FOLIAGE, new Color(cir.getReturnValue()), Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason)).getRGB() : cir.getReturnValue());
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void modifySkyColor(CallbackInfoReturnable<Integer> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE)
            cir.setReturnValue(!Season.getSeasonFromEnum(BWSeasons.SeasonVal.SUMMER).containsSubSeason(BWSeasons.cachedSubSeason) ? BiomeColorCalculator.modifyBiomeColor(BiomeColorCalculator.ColorType.SKY, new Color(cir.getReturnValue()), Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason)).getRGB() : cir.getReturnValue());
    }

    @Inject(method = "getDownfall", at = @At("RETURN"), cancellable = true)
    private void modifyDownfall(CallbackInfoReturnable<Float> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE)
            cir.setReturnValue((float) (cir.getReturnValue() + Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getHumidityModifier()));
    }

    @Inject(method = "getTemperature()F", at = @At("RETURN"), cancellable = true)
    private void modifyTemperature(CallbackInfoReturnable<Float> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE)
            cir.setReturnValue((float) (cir.getReturnValue() + Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getTempModifier()));
    }
}

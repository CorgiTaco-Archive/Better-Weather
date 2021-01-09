package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.BWSeasonSystem;
import corgitaco.betterweather.season.client.BiomeColorCalculator;
import corgitaco.betterweather.season.Season;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.*;

@Mixin(Biome.class)
public abstract class MixinBiomeClient {

    @Shadow public abstract Biome.Category getCategory();

    @Inject(method = "getGrassColor", at = @At("RETURN"), cancellable = true)
    private void modifyGrassColor(double posX, double posZ, CallbackInfoReturnable<Integer> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE) {
            if (BetterWeather.biomeRegistryEarlyAccess != null)
                cir.setReturnValue(BiomeColorCalculator.modifyBiomeColor(BiomeColorCalculator.ColorType.GRASS, new Color(cir.getReturnValue()), Season.getSubSeasonFromEnum(BWSeasonSystem.cachedSubSeason), (Biome) (Object) this).getRGB());

        }
    }

    @Inject(method = "getFoliageColor", at = @At("RETURN"), cancellable = true)
    private void modifyFoliageColor(CallbackInfoReturnable<Integer> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE) {
            if (BetterWeather.biomeRegistryEarlyAccess != null)
                cir.setReturnValue(BiomeColorCalculator.modifyBiomeColor(BiomeColorCalculator.ColorType.FOLIAGE, new Color(cir.getReturnValue()), Season.getSubSeasonFromEnum(BWSeasonSystem.cachedSubSeason), (Biome) (Object) this).getRGB());

        }
    }

    @Inject(method = "getSkyColor", at = @At("RETURN"), cancellable = true)
    private void modifySkyColor(CallbackInfoReturnable<Integer> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE) {
            if (BetterWeather.biomeRegistryEarlyAccess != null)
                cir.setReturnValue(BiomeColorCalculator.modifyBiomeColor(BiomeColorCalculator.ColorType.SKY, new Color(cir.getReturnValue()), Season.getSubSeasonFromEnum(BWSeasonSystem.cachedSubSeason), (Biome) (Object) this).getRGB());

        }
    }

    @Inject(method = "getFogColor", at = @At("RETURN"), cancellable = true)
    private void modifyFogColor(CallbackInfoReturnable<Integer> cir) {
        if (this.getCategory() != Biome.Category.NETHER || this.getCategory() != Biome.Category.THEEND || this.getCategory() != Biome.Category.NONE) {
            if (BetterWeather.biomeRegistryEarlyAccess != null)
                cir.setReturnValue(BiomeColorCalculator.modifyBiomeColor(BiomeColorCalculator.ColorType.FOG, new Color(cir.getReturnValue()), Season.getSubSeasonFromEnum(BWSeasonSystem.cachedSubSeason), (Biome) (Object) this).getRGB());
        }
    }
}

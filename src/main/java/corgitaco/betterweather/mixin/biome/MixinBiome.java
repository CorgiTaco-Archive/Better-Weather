package corgitaco.betterweather.mixin.biome;

import corgitaco.betterweather.season.BWSeasons;
import corgitaco.betterweather.season.Season;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Biome.class)
public abstract class MixinBiome {

    @Shadow
    public abstract Biome.Category getCategory();

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

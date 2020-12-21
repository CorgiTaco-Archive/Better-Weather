package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.season.BWSeasons;
import corgitaco.betterweather.season.Season;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorldInfo.class)
public class MixinServerWorldInfo {

    @Inject(method = "getRainTime", at = @At("RETURN"), cancellable = true)
    private void modifyCurrentRainTime(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue((int) (cir.getReturnValue() * (1 / Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventChanceMultiplier())));
    }

    @Inject(method = "getThunderTime", at = @At("RETURN"), cancellable = true)
    private void modifyCurrentThunderTime(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue((int) (cir.getReturnValue() * (1 / Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventChanceMultiplier())));
    }
}
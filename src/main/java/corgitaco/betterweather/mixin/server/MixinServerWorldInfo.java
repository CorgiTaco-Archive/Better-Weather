package corgitaco.betterweather.mixin.server;

import corgitaco.betterweather.access.IsWeatherForced;
import corgitaco.betterweather.season.BWSeasons;
import corgitaco.betterweather.season.Season;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorldInfo.class)
public abstract class MixinServerWorldInfo implements IsWeatherForced {

    @Shadow public abstract boolean isRaining();

    @Shadow public abstract boolean isThundering();

    private boolean weatherIsForced;

    @Inject(method = "getRainTime", at = @At("RETURN"), cancellable = true)
    private void modifyCurrentRainTime(CallbackInfoReturnable<Integer> cir) {
        if (!this.isRaining() || !weatherIsForced) //Sets the time between rain and the length of the current rain when rolled.
            cir.setReturnValue((int) (cir.getReturnValue() * (1 / Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventChanceMultiplier())));
    }

    @Inject(method = "getThunderTime", at = @At("RETURN"), cancellable = true)
    private void modifyCurrentThunderTime(CallbackInfoReturnable<Integer> cir) {
        if (!this.isThundering() || !weatherIsForced) //Sets the time between thunderstorms and the length of the current thunderstorm when rolled.
            cir.setReturnValue((int) (cir.getReturnValue() * (1 / Season.getSubSeasonFromEnum(BWSeasons.cachedSubSeason).getWeatherEventChanceMultiplier())));
    }

    @Override
    public void setWeatherForced(boolean flag) {
        weatherIsForced = flag;
    }

    @Override
    public boolean isWeatherForced() {
        return weatherIsForced;
    }
}
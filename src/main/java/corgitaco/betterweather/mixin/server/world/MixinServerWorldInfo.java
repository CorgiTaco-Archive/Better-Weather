package corgitaco.betterweather.mixin.server.world;

import corgitaco.betterweather.helpers.WeatherTime;
import net.minecraft.world.storage.ServerWorldInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerWorldInfo.class)
public abstract class MixinServerWorldInfo implements WeatherTime {

    @Shadow
    public abstract boolean isRaining();

    private double multiplier = 0.0;

    @Inject(method = {"getRainTime", "getThunderTime"}, at = @At("RETURN"), cancellable = true)
    private void adjustRainAndThunderTime(CallbackInfoReturnable<Integer> cir) {
        if (!isRaining()) {
            cir.setReturnValue((int) (cir.getReturnValue() * multiplier));
        }
    }

    @Override
    public void setWeatherTimeMultiplier(double multiplier) {
        this.multiplier = multiplier;
    }
}
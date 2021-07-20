package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.DimensionRenderInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionRenderInfo.class)
public abstract class MixinDimensionRenderInfo {
    Minecraft minecraft = Minecraft.getInstance();

    @Inject(method = "func_230492_a_", at = @At("HEAD"), cancellable = true)
    private void constantSkyColor(float skyAngle, float tickDelta, CallbackInfoReturnable<float[]> cir) {
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) minecraft.world).getWeatherEventContext();
        if (weatherEventContext != null) {
            if (!weatherEventContext.getCurrentEvent().getClientSettings().sunsetSunriseColor()) {
                cir.setReturnValue(null);
            }
        }
    }
}

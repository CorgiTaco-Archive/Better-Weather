package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionRenderInfo.class)
public abstract class MixinDimensionRenderInfo {
    Minecraft minecraft = Minecraft.getInstance();

    @Inject(at = @At("HEAD"), method = "func_230492_a_", cancellable = true)
    private void yeet(float f, float g, CallbackInfoReturnable<float[]> cir) {
        if (minecraft.world != null) {
            BetterWeather.setWeatherData(minecraft.world);
            if (BetterWeather.weatherData.isBlizzard() && minecraft.world.getWorldInfo().isRaining() && minecraft.world.getDimensionKey() == World.OVERWORLD)
                cir.setReturnValue(null);
        }
    }
}
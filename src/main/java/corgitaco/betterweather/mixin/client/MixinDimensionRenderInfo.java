package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.api.BetterWeatherWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionRenderInfo.class)
public abstract class MixinDimensionRenderInfo {
    Minecraft minecraft = Minecraft.getInstance();

    @Inject(at = @At("HEAD"), method = "func_230492_a_", cancellable = true)
    private void constantSkyColor(float float1, float float2, CallbackInfoReturnable<float[]> cir) {
        if (minecraft.world != null && ((BetterWeatherWorldData) minecraft.world).getSeasonContext() != null) {
//            if (WeatherData.currentWeatherEvent.disableSkyColor()) {
                float f1 = MathHelper.cos(float1 * ((float) Math.PI * 2F)) - 0.0F;
                if (f1 >= -0.4F && f1 <= 0.4F) {
                    float f3 = (f1 - -0.0F) / 0.4F * 0.5F + 0.5F;
                    float alpha = 1.0F - (1.0F - MathHelper.sin(f3 * (float) Math.PI)) * 0.99F;
                    alpha = alpha * alpha;
                    float[] rgba = {1.0F, 1.0F, 1.0F, alpha};
                    cir.setReturnValue(rgba);
//                }
            }
        }
    }
}
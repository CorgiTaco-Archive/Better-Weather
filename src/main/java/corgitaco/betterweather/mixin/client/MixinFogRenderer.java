package corgitaco.betterweather.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FogRenderer.class)
public abstract class MixinFogRenderer {

    @Redirect(method = "updateFogColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    private static float doNotDarkenFogWithRainStrength(ClientWorld world, float delta) {
        return ((BetterWeatherWorldData) world).getWeatherEventContext() != null ? 0.0F : world.getRainStrength(delta);
    }

    @Inject(method = "setupFog(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/FogRenderer$FogType;FZF)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void forceWeather(ActiveRenderInfo activeRenderInfoIn, FogRenderer.FogType fogTypeIn, float farPlaneDistance, boolean nearFog, float partialticks, CallbackInfo ci) {
        ClientWorld world = Minecraft.getInstance().world;
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();
        if (weatherEventContext != null) {
            WeatherEvent currentEvent = weatherEventContext.getCurrentEvent();
            float currentFogDensity = currentEvent.getClientSettings().fogDensity();
            float blendedFogDensity = currentEvent.fogDensity(world, activeRenderInfoIn.getBlockPos());

            if (currentFogDensity != -1.0F && blendedFogDensity > 0.0F) {
                RenderSystem.fogDensity(MathHelper.lerp(activeRenderInfoIn.getRenderViewEntity().world.getRainStrength(Minecraft.getInstance().getRenderPartialTicks()), 0.0F, blendedFogDensity));
                ci.cancel();
            }
        }
    }
}

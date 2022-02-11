package corgitaco.betterweather.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.util.BetterWeatherWorldData;
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

    @Redirect(method = "setupColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainLevel(F)F"))
    private static float doNotDarkenFogWithRainStrength(ClientWorld world, float delta) {
        return ((BetterWeatherWorldData) world).getWeatherContext() != null ? 0.0F : world.getRainLevel(delta);
    }

    @Inject(method = "setupFog(Lnet/minecraft/client/renderer/ActiveRenderInfo;Lnet/minecraft/client/renderer/FogRenderer$FogType;FZF)V", at = @At("HEAD"), cancellable = true, remap = false)
    private static void forceWeather(ActiveRenderInfo activeRenderInfoIn, FogRenderer.FogType fogTypeIn, float farPlaneDistance, boolean nearFog, float partialticks, CallbackInfo ci) {
        ClientWorld world = Minecraft.getInstance().level;
        WeatherContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherContext();
        if (weatherEventContext != null) {
            WeatherEvent currentEvent = weatherEventContext.getCurrentEvent();
            float currentFogDensity = currentEvent.getClientSettings().fogDensity();
            float blendedFogDensity = weatherEventContext.getCurrentEvent().getClient().fogDensity(world, activeRenderInfoIn.getBlockPosition(), currentEvent::isValidBiome);

            if (currentFogDensity != -1.0F && blendedFogDensity > 0.0F) {
                RenderSystem.fogDensity(MathHelper.lerp(activeRenderInfoIn.getEntity().level.getRainLevel(Minecraft.getInstance().getFrameTime()), 0.0F, blendedFogDensity));
                ci.cancel();
            }
        }
    }
}

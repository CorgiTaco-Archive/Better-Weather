package corgitaco.betterweather.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow @Final private Minecraft mc;

    @Shadow public int ticks;

    @Shadow private ClientWorld world;

    @Inject(at = @At("HEAD"), method = "renderRainSnow(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", cancellable = true)
    private void renderWeather(LightTexture lightmapIn, float partialTicks, double x, double y, double z, CallbackInfo ci) {
        if (WeatherData.currentWeatherEvent.renderWeather(mc, this.world, lightmapIn, ticks, partialTicks, x, y, z))
            ci.cancel();
    }

    @Inject(at = @At("HEAD"), method = "loadRenderers()V", cancellable = true)
    private void cancelGameSettingsUpdate(CallbackInfo ci) {
        if (mc.world != null) {
            //We do this to ensure that the our Weather Data is not null before using it.
            if (BetterWeather.weatherData.isBlizzard() && mc.world.isRaining()) {
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "addRainParticles(Lnet/minecraft/client/renderer/ActiveRenderInfo;)V", cancellable = true)
    private void stopRainParticles(ActiveRenderInfo activeRenderInfoIn, CallbackInfo ci) {
        if (mc.world != null) {
            if (WeatherData.currentWeatherEvent.weatherParticlesAndSound(activeRenderInfoIn, this.mc))
                ci.cancel();
        }
    }



    @Inject(at = @At("HEAD"), method = "renderSky(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", cancellable = true)
    private void changeSkyColor(MatrixStack stack, float f, CallbackInfo ci) {
        if (mc.world != null) {
            if (WeatherData.currentWeatherEvent.disableSkyColor())
                ci.cancel();
        }
    }

    @Redirect(method = "renderSky(Lcom/mojang/blaze3d/matrix/MatrixStack;F)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    public float cancelRainSunRemoval(ClientWorld clientWorld, float delta) {
        return 0.0F;
    }
}

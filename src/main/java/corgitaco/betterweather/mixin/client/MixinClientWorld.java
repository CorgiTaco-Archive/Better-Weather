package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.weatherevent.weatherevents.vanilla.Clear;
import net.minecraft.client.network.play.ClientPlayNetHandler;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DimensionType;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void setupClientGameData(ClientPlayNetHandler handler, ClientWorld.ClientWorldInfo info, RegistryKey<World> key, DimensionType dimtype, int i, Supplier<IProfiler> profiler, WorldRenderer renderer, boolean b1, long b2, CallbackInfo ci) {
        BetterWeather.setWeatherData((ClientWorld) (Object) this);
        BetterWeather.setSeasonData((ClientWorld) (Object) this);
        WeatherData.currentWeatherEvent = new Clear();
    }

    @Redirect(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    private float doNotDarkenSkyWithRainStrength(ClientWorld world, float delta) {
        return BetterWeatherUtil.isOverworld(world.getDimensionKey()) ? 0.0F : world.getRainStrength(delta);
    }

    @Redirect(method = "getCloudColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    private float doNotDarkenCloudsWithRainStrength(ClientWorld world, float delta) {
        return 0.0F;
    }

    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true)
    private void modifyCloudColor(float partialTicks, CallbackInfoReturnable<Vector3d> cir) {
        if (BetterWeatherUtil.isOverworld(((ClientWorld)(Object) this).getDimensionKey())) {
            int rgbColor = WeatherData.currentWeatherEvent.modifyCloudColor(BetterWeatherUtil.transformFloatColor(cir.getReturnValue()), ((ClientWorld) (Object) this).getRainStrength(partialTicks)).getRGB();
            float r = (float) (rgbColor >> 16 & 255) / 255.0F;
            float g = (float) (rgbColor >> 8 & 255) / 255.0F;
            float b = (float) (rgbColor & 255) / 255.0F;
            cir.setReturnValue(new Vector3d(r, g, b));
        }
    }

    @Redirect(method = "getSunBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    private float sunBrightness(ClientWorld world, float delta) {
        float rainStrength = ((ClientWorld) (Object) this).getRainStrength(delta);
        return BetterWeatherUtil.isOverworld(world.getDimensionKey()) ? rainStrength * WeatherData.currentWeatherEvent.daylightBrightness() : rainStrength;
    }
}

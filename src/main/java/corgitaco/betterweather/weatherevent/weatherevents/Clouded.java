package corgitaco.betterweather.weatherevent.weatherevents;

import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.awt.*;

public class Clouded extends WeatherEvent {
    public Clouded() {
        super(WeatherEventSystem.CLOUDED, 0.4);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {

    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc) {

    }

    @Override
    public boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        return true;
    }

    @Override
    public Color modifySkyColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return BetterWeatherUtil.blendColor(returnColor, BetterWeatherUtil.DEFAULT_RAIN_SKY, rainStrength);
    }

    @Override
    public Color modifyCloudColor(Color returnColor, float rainStrength) {
        return BetterWeatherUtil.blendColor(returnColor, BetterWeatherUtil.DEFAULT_RAIN_CLOUDS, rainStrength);
    }

    @Override
    public Color modifyFogColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return BetterWeatherUtil.blendColor(returnColor, BetterWeatherUtil.DEFAULT_RAIN_FOG, rainStrength);
    }

    @OnlyIn(Dist.CLIENT)
    public float skyOpacity() {
        return 0.85F;
    }

    @Override
    public float daylightBrightness() {
        return 2.5F;
    }
}

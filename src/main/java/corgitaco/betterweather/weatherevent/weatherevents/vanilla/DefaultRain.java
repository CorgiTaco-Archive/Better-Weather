package corgitaco.betterweather.weatherevent.weatherevents.vanilla;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.awt.*;

public class DefaultRain extends WeatherEvent {
    public DefaultRain() {
        super(new BetterWeatherID(BetterWeather.MOD_ID, "DEFAULT"), 0.5);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc) {
    }

    @Override
    public boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        return false;
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

    @Override
    public boolean weatherParticlesAndSound(ActiveRenderInfo renderInfo, Minecraft mc) {
        return false;
    }

    @Override
    public boolean fillBlocksWithWater() {
        return true;
    }

    @Override
    public boolean spawnSnowInFreezingClimates() {
        return true;
    }
}

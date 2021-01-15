package corgitaco.betterweather.weatherevent.weatherevents.vanilla;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;

public class Default extends WeatherEvent {
    public Default() {
        super(new BetterWeatherID(BetterWeather.MOD_ID, "DEFAULT"), 0.5);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime, Iterable<ChunkHolder> loadedChunks) {
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc, int postClientTicksLeft) {
    }

    @Override
    public boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        return false;
    }
}

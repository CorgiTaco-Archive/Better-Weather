package corgitaco.betterweather.weatherevent.weatherevents;

import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;

public class Clouded extends WeatherEvent {
    public Clouded() {
        super(WeatherEventSystem.CLOUDED, 0.4);
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

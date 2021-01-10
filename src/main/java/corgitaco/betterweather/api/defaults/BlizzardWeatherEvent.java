package corgitaco.betterweather.api.defaults;

import corgitaco.betterweather.api.WeatherEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;

public class BlizzardWeatherEvent extends WeatherEvent {
    public BlizzardWeatherEvent(String name, double defaultChance) {
        super(name, defaultChance);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime, Iterable<ChunkHolder> loadedChunks) {

    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc) {

    }

}

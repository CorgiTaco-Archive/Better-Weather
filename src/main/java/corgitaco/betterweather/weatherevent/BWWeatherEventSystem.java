package corgitaco.betterweather.weatherevent;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.List;

public class BWWeatherEventSystem {

    private static BetterWeather.WeatherEvent cachedEvent = BetterWeather.WeatherEvent.NONE;

    public static void updateWeatherEventPacket(List<ServerPlayerEntity> players, World world) {
        BetterWeather.setWeatherData(world);

        BetterWeather.WeatherEvent currentEvent = BetterWeather.weatherData.getEventValue();

        if (cachedEvent != currentEvent) {
            players.forEach(player -> {
                NetworkHandler.sendTo(player, new WeatherEventPacket(cachedEvent.name()));
            });
            cachedEvent = currentEvent;
        }
    }
}

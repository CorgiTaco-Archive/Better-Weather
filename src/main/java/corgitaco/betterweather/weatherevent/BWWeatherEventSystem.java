package corgitaco.betterweather.weatherevent;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;

import java.util.List;

public class BWWeatherEventSystem {

    public static void updateWeatherEventPacket(List<ServerPlayerEntity> players, World world) {
        BetterWeather.setWeatherData(world);

        if (world.getWorldInfo().getGameTime() % 25 == 0) {
            players.forEach(player ->{
                NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEvent()));
            });
        }
    }
}

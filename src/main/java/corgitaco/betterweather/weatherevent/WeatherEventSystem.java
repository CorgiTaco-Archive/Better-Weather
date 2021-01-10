package corgitaco.betterweather.weatherevent;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.access.IsWeatherForced;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.ServerWorldInfo;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class WeatherEventSystem {

    public static final String NONE = "NONE";
    public static final String BLIZZARD = "BLIZZARD";
    public static final String ACID_RAIN = "ACID_RAIN";

    public static IdentityHashMap<String, Double> WEATHER_EVENT_CONTROLLER = Util.make((new IdentityHashMap<>()), (map) -> {
        map.put(WeatherEventSystem.BLIZZARD, 0.5);
        map.put(WeatherEventSystem.ACID_RAIN, 0.25);
    });

    private static String cachedEvent = NONE;

    public static void updateWeatherEventPacket(List<ServerPlayerEntity> players, World world, boolean justJoined) {
        BetterWeather.setWeatherData(world);

        String currentEvent = BetterWeather.weatherData.getEvent();

        if (!cachedEvent.equals(currentEvent) || justJoined) {
            players.forEach(player -> {
                NetworkHandler.sendTo(player, new WeatherEventPacket(currentEvent));
            });
            cachedEvent = currentEvent;
        }
    }

    private static int tickCounter;

    public static void rollWeatherEventChance(Random random, boolean isRaining, boolean isThundering, ServerWorldInfo worldInfo, List<ServerPlayerEntity> players) {
        boolean isRainActive = isRaining || isThundering;


        if (tickCounter == 0) {
            if (isRainActive) {
                AtomicBoolean weatherEventWasSet = new AtomicBoolean(false);
                if (!BetterWeather.weatherData.isWeatherForced()) { //If weather isn't forced, roll chance
                    WeatherEventSystem.WEATHER_EVENT_CONTROLLER.forEach((event, chance) -> {
                        if (random.nextDouble() < chance) {
                            weatherEventWasSet.set(true);
                            BetterWeather.weatherData.setEvent(event);
                        }
                    });
                    if (!weatherEventWasSet.get())
                        BetterWeather.weatherData.setEvent(WeatherEventSystem.NONE);
                    players.forEach(player -> NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEvent())));
                }
                tickCounter++;
            }
        } else {
            if (!isRainActive) {
                if (tickCounter > 0) {
                    BetterWeather.weatherData.setEvent(WeatherEventSystem.NONE);
                    ((IsWeatherForced) worldInfo).setWeatherForced(false);
                    BetterWeather.weatherData.setWeatherForced(((IsWeatherForced) worldInfo).isWeatherForced());
                    players.forEach(player -> NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEvent())));
                    tickCounter = 0;
                }
            }
        }
    }
}

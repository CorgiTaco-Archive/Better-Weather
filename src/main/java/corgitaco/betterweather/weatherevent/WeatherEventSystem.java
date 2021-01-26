package corgitaco.betterweather.weatherevent;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherEntryPoint;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.datastorage.BetterWeatherEventData;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import corgitaco.betterweather.datastorage.network.packet.util.RefreshRenderersPacket;
import corgitaco.betterweather.helper.IsWeatherForced;
import corgitaco.betterweather.weatherevent.weatherevents.AcidRain;
import corgitaco.betterweather.weatherevent.weatherevents.Blizzard;
import corgitaco.betterweather.weatherevent.weatherevents.Clouded;
import corgitaco.betterweather.weatherevent.weatherevents.LightRain;
import corgitaco.betterweather.weatherevent.weatherevents.vanilla.Clear;
import corgitaco.betterweather.weatherevent.weatherevents.vanilla.DefaultRain;
import corgitaco.betterweather.weatherevent.weatherevents.vanilla.DefaultThunder;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ServerWorldInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class WeatherEventSystem {

    public static final BetterWeatherID ACID_RAIN = new BetterWeatherID(BetterWeather.MOD_ID, "ACID_RAIN");
    public static final BetterWeatherID BLIZZARD = new BetterWeatherID(BetterWeather.MOD_ID, "BLIZZARD");
    public static final BetterWeatherID CLEAR = new BetterWeatherID(BetterWeather.MOD_ID, "CLEAR");
    public static final BetterWeatherID CLOUDED = new BetterWeatherID(BetterWeather.MOD_ID, "CLOUDED");
    public static final BetterWeatherID LIGHT_RAIN = new BetterWeatherID(BetterWeather.MOD_ID, "LIGHT_RAIN");
    public static final BetterWeatherID DEFAULT = new BetterWeatherID(BetterWeather.MOD_ID, "DEFAULT");
    public static final BetterWeatherID DEFAULT_THUNDER = new BetterWeatherID(BetterWeather.MOD_ID, "DEFAULT_THUNDER");

    public static HashMap<BetterWeatherID, Double> WEATHER_EVENT_CONTROLLER = new HashMap<>();

    public static HashMap<BetterWeatherID, WeatherEvent> WEATHER_EVENTS = new HashMap<>();
    private static boolean isFadingOut = true;

    public static void fillWeatherEventsMapAndWeatherEventController() {
        for (WeatherEvent weatherEvent : BetterWeatherEntryPoint.WEATHER_EVENTS) {
            WEATHER_EVENTS.put(weatherEvent.getID(), weatherEvent);
            WEATHER_EVENT_CONTROLLER.put(weatherEvent.getID(), weatherEvent.getDefaultChance());
        }
    }

    public static void addDefaultWeatherEvents() {
        BetterWeatherEntryPoint.WEATHER_EVENTS.add(new Blizzard());
        BetterWeatherEntryPoint.WEATHER_EVENTS.add(new AcidRain());
        BetterWeatherEntryPoint.WEATHER_EVENTS.add(new DefaultRain());
        BetterWeatherEntryPoint.WEATHER_EVENTS.add(new DefaultThunder());
        BetterWeatherEntryPoint.WEATHER_EVENTS.add(new Clear());
        BetterWeatherEntryPoint.WEATHER_EVENTS.add(new Clouded());
        BetterWeatherEntryPoint.WEATHER_EVENTS.add(new LightRain());
    }

    private static BetterWeatherID cachedEvent = CLEAR;

    public static void updateWeatherEventPacket(ServerWorld world, List<ServerPlayerEntity> players, boolean justJoined) {

        BetterWeatherID currentEvent = BetterWeatherEventData.get(world).getEventID();

        if (!cachedEvent.equals(currentEvent) || justJoined) {
            players.forEach(player -> {
                NetworkHandler.sendToClient(player, new WeatherEventPacket(currentEvent.toString()));
            });
            cachedEvent = currentEvent;
        }
    }


    public static void rollWeatherEventChance(Random random, ServerWorld world, boolean isRaining, ServerWorldInfo worldInfo, List<ServerPlayerEntity> players) {
        if (world.rainingStrength == 0.0F) {
            if (isRaining) {
                if (!BetterWeatherEventData.get(world).isWeatherForced()) {
                    AtomicBoolean weatherEventWasSet = new AtomicBoolean(false);
                    //If weather isn't forced, roll chance
                    WeatherEventSystem.WEATHER_EVENT_CONTROLLER.forEach((event, chance) -> {
                        if (event != WeatherEventSystem.CLEAR) {
                            if (random.nextDouble() < chance) {
                                weatherEventWasSet.set(true);
                                BetterWeatherEventData.get(world).setEvent(event.toString());
                            }
                        }
                    });
                    if (!weatherEventWasSet.get())
                        BetterWeatherEventData.get(world).setEvent(WeatherEventSystem.DEFAULT.toString());

                    players.forEach(player -> {
                        NetworkHandler.sendToClient(player, new WeatherEventPacket(BetterWeatherEventData.get(world).getEventString()));
                        if (WeatherData.currentWeatherEvent.refreshPlayerRenderer())
                            NetworkHandler.sendToClient(player, new RefreshRenderersPacket());
                    });
                }
            }
        } else {
            if (!isRaining) {
                if (world.rainingStrength == 1.0F) {
                    isFadingOut = true;
                } else if (world.rainingStrength <= 0.011F && isFadingOut) {
                    boolean refreshRenderersPost = WeatherData.currentWeatherEvent.refreshPlayerRenderer();
                    BetterWeatherEventData.get(world).setEvent(WeatherEventSystem.CLEAR.toString());
                    ((IsWeatherForced) worldInfo).setWeatherForced(false);
                    BetterWeatherEventData.get(world).setWeatherForced(((IsWeatherForced) worldInfo).isWeatherForced());
                    players.forEach(player -> {
                        NetworkHandler.sendToClient(player, new WeatherEventPacket(BetterWeatherEventData.get(world).getEventString()));
                        if (refreshRenderersPost)
                            NetworkHandler.sendToClient(player, new RefreshRenderersPacket());
                    });
                    isFadingOut = false;
                }
            }
        }
    }
}

package corgitaco.betterweather.weatherevent;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventContext;
import corgitaco.betterweather.api.weather.WeatherEventSettings;
import corgitaco.betterweather.data.storage.WeatherEventSavedData;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;

public class BWWeatherEventContext implements WeatherEventContext {
    public static final String CONFIG_NAME = "weather-settings.toml";

    public static final Codec<BWWeatherEventContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.STRING.fieldOf("currentEvent").forGetter((weatherEventContext) -> {
            return weatherEventContext.currentEvent.getID();
        }), ResourceLocation.CODEC.fieldOf("worldID").forGetter((weatherEventContext) -> {
            return weatherEventContext.worldID;
        }), Codec.unboundedMap(Codec.STRING, WeatherEvent.CODEC).fieldOf("weather_events").forGetter((seasonContext) -> {
            return seasonContext.weatherEvents;
        })).apply(builder, (currentEvent, worldID, weatherEvents) -> new BWWeatherEventContext(currentEvent, worldID, new HashMap<>(weatherEvents)));
    });


    private final HashMap<String, WeatherEvent> weatherEvents = new HashMap<>();
    private final ResourceLocation worldID;
    private final Registry<Biome> biomeRegistry;
    private final Path weatherConfigPath;
    private final File weatherConfigFile;

    private WeatherEvent currentEvent;

    //Packet Constructor
    public BWWeatherEventContext(String name, ResourceLocation worldID, HashMap<String, WeatherEvent> weatherEvents) {
        this(name, worldID, null, weatherEvents);
    }

    //Server world constructor
    public BWWeatherEventContext(WeatherEventSavedData weatherEventSavedData, RegistryKey<World> worldID, Registry<Biome> biomeRegistry) {
        this(weatherEventSavedData.getEvent(), worldID.getLocation(), biomeRegistry, null);
    }

    public BWWeatherEventContext(String currentEvent, ResourceLocation worldID, @Nullable Registry<Biome> biomeRegistry, @Nullable HashMap<String, WeatherEvent> weatherEvents) {
        this.worldID = worldID;
        this.biomeRegistry = biomeRegistry;
        this.weatherConfigPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("weather");
        this.weatherConfigFile = this.weatherConfigPath.resolve(CONFIG_NAME).toFile();

        boolean isClient = weatherEvents != null;
        boolean isPacket = biomeRegistry == null;

        if (isClient) {
            this.weatherEvents.putAll(weatherEvents);
        }
        if (!isPacket) {
            this.handleConfig(isClient);
            this.currentEvent = this.weatherEvents.get(currentEvent);
        }
    }


    public void handleConfig(boolean isClient) {

    }

    @Override
    public boolean isLocalizedWeather() {
        return false;
    }

    @Override
    public String getCurrentWeatherEventKey() {
        return null;
    }

    @Override
    public WeatherEventSettings getCurrentWeatherEventSettings() {
        return new WeatherEventSettings() {
            @Override
            public double getTemperatureModifierAtPosition(BlockPos pos) {
                return 0;
            }

            @Override
            public double getHumidityModifierAtPosition(BlockPos pos) {
                return 0;
            }
        };
    }
}

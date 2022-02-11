package corgitaco.betterweather;

import corgitaco.betterweather.api.BetterWeatherRegistry;
import corgitaco.betterweather.common.network.NetworkHandler;
import corgitaco.betterweather.common.season.config.SeasonConfigSerializers;
import corgitaco.betterweather.common.weather.event.*;
import corgitaco.betterweather.common.weather.event.client.settings.*;
import corgitaco.betterweather.server.BetterWeatherGameRules;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

@Mod("betterweather")
public class BetterWeather {
    public static final String MOD_ID = "betterweather";
    public static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve(MOD_ID);
    public static final Logger LOGGER = LogManager.getLogger();

    public static final boolean USING_OPTIFINE = new LazyValue<>(() -> {
        try {
            return Class.forName("net.optifine.Config") != null;
        } catch (final Exception e) {
            return false;
        }
    }).get();

    public BetterWeather() {
        if (!CONFIG_PATH.toFile().exists())
            CONFIG_PATH.toFile().mkdir();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::lateSetup);
        SeasonConfigSerializers.clinit();
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(BetterWeatherGameRules::init);
        NetworkHandler.init();

        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "acid_rain"), AcidRainClientSettings.CODEC);
        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "blizzard"), BlizzardClientSettings.CODEC);
        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "cloudy"), CloudyClientSettings.CODEC);
        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "none"), NoneClientSettings.CODEC);
        Registry.register(BetterWeatherRegistry.CLIENT_WEATHER_EVENT_SETTINGS, new ResourceLocation(MOD_ID, "rain"), RainClientSettings.CODEC);

        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "acid_rain"), AcidRain.CODEC);
        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "blizzard"), Blizzard.CODEC);
        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "cloudy"), Cloudy.CODEC);
        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "none"), None.CODEC);
        Registry.register(BetterWeatherRegistry.WEATHER_EVENT, new ResourceLocation(MOD_ID, "rain"), Rain.CODEC);

        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "none"), None.DEFAULT);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "acid_rain"), AcidRain.DEFAULT);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "blizzard"), Blizzard.DEFAULT);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "cloudy"), Cloudy.DEFAULT);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "rain"), Rain.DEFAULT);

        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "acid_rain_thundering"), AcidRain.DEFAULT_THUNDERING);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "blizzard_thundering"), Blizzard.DEFAULT_THUNDERING);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "cloudy_thundering"), Cloudy.DEFAULT_THUNDERING);
        BetterWeatherRegistry.DEFAULT_EVENTS.put(new ResourceLocation(BetterWeather.MOD_ID, "thundering"), Rain.DEFAULT_THUNDERING);

    }

    private void clientSetup(FMLClientSetupEvent event) {
        if (USING_OPTIFINE) {
            LOGGER.info("Optifine is loaded.");
        }
    }

    private void lateSetup(FMLLoadCompleteEvent event) {

    }
}

package corgitaco.betterweather.config.json.overrides;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.Season;
import corgitaco.betterweather.util.storage.OverrideStorage;
import net.minecraft.world.biome.Biome;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;

public class BiomeOverrideJsonHandler {


    public static void handleOverrideJsonConfigs(Path path, IdentityHashMap<Object, OverrideStorage> objectToOverrideStorageDefault, Season.SubSeason subSeason) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BiomeToOverrideStorageJsonStorage.class, new OverrideDeserializer());
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();

        final File CONFIG_FILE = new File(String.valueOf(path));

        if (!CONFIG_FILE.exists() && !objectToOverrideStorageDefault.isEmpty()) {
            createOverridesJson(path, objectToOverrideStorageDefault);
        }

        if (CONFIG_FILE.exists()) {
            try (Reader reader = new FileReader(path.toString())) {
                BiomeToOverrideStorageJsonStorage biomeToOverrideStorageJsonStorage = gson.fromJson(reader, BiomeToOverrideStorageJsonStorage.class);
                if (biomeToOverrideStorageJsonStorage != null) {
                    subSeason.setBiomeToOverrideStorage(biomeToOverrideStorageJsonStorage.getBiomeToOverrideStorage());

                } else
                    BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-biomes.json could not be read");

            } catch (IOException e) {
                BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-biomes.json could not be read");
            }
        }
    }



    public static void createOverridesJson(Path path, IdentityHashMap<Object, OverrideStorage> objectToOverrideStorageDefault) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BiomeToOverrideStorageJsonStorage.ObjectToOverrideStorageJsonStorage.class, new OverrideDeserializer.ObjectToOverrideStorageJsonStorageSerializer());
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();


        String jsonString = gson.toJson(new BiomeToOverrideStorageJsonStorage.ObjectToOverrideStorageJsonStorage(objectToOverrideStorageDefault));

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-biomes.json could not be created");
        }
    }
}

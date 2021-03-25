package corgitaco.betterweather.config.season.overrides;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.BWSubseasonSettings;
import corgitaco.betterweather.season.storage.OverrideStorage;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;

public class BiomeOverrideJsonHandler {


    public static void handleOverrideJsonConfigs(Path path, IdentityHashMap<Object, OverrideStorage> objectToOverrideStorageDefault, BWSubseasonSettings subSeasonSettings, Registry<Biome> biomeRegistry, boolean isClient) {
        Gson gson = new GsonBuilder().registerTypeAdapter(BiomeToOverrideStorageJsonStorage.class, new OverrideDeserializer(biomeRegistry, subSeasonSettings.getBiomeToOverrideStorage(), subSeasonSettings.getCropToMultiplierStorage(), isClient)).setPrettyPrinting().disableHtmlEscaping().create();

        final File CONFIG_FILE = path.toFile();

        if (!CONFIG_FILE.exists() && !objectToOverrideStorageDefault.isEmpty()) {
            createOverridesJson(path, objectToOverrideStorageDefault);
        }

        if (CONFIG_FILE.exists()) {
            try (Reader reader = new FileReader(path.toString())) {
                BiomeToOverrideStorageJsonStorage biomeToOverrideStorageJsonStorage = gson.fromJson(reader, BiomeToOverrideStorageJsonStorage.class);
                if (biomeToOverrideStorageJsonStorage != null) {
                    subSeasonSettings.setBiomeToOverrideStorage(biomeToOverrideStorageJsonStorage.getBiomeToOverrideStorage());
                    subSeasonSettings.setCropToMultiplierStorage(biomeToOverrideStorageJsonStorage.getSeasonCropOverrides());

                } else
                    BetterWeather.LOGGER.error("\"" + path.toString() + "\" could not be read...");

            } catch (IOException e) {
                BetterWeather.LOGGER.error("\"" + path.toString() + "\" could not be read..." + e.toString());
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
            BetterWeather.LOGGER.error("\"" + path.toString() + "\" could not be created..." + e.toString());
        }
    }
}

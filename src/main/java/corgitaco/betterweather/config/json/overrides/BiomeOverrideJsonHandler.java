package corgitaco.betterweather.config.json.overrides;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.util.storage.OverrideStorage;
import net.minecraft.world.biome.Biome;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;

public class BiomeOverrideJsonHandler {


    public static void createOverridesJson(Path path) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(BiomeToOverrideStorageJsonStorage.ObjectToOverrideStorageJsonStorage.class, new OverrideDeserializer.ObjectToOverrideStorageJsonStorageSerializer());
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();

        IdentityHashMap<Object, OverrideStorage> objectToOverrideStorage = new IdentityHashMap<>();


        OverrideStorage overrideStorage = new OverrideStorage();
        OverrideStorage.OverrideClientStorage clientStorage = overrideStorage.getClientStorage().setGrassColorBlendStrength(0);
        overrideStorage.setClientStorage(clientStorage);
        objectToOverrideStorage.put(Biome.Category.SWAMP, overrideStorage);
        String jsonString = gson.toJson(new BiomeToOverrideStorageJsonStorage.ObjectToOverrideStorageJsonStorage(objectToOverrideStorage));

        try {
            Files.createDirectories(path);
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-biomes.json could not be created");
        }
    }
}

package corgitaco.betterweather.season.config.cropfavoritebiomes;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import corgitaco.betterweather.BetterWeather;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;

public class CropFavoriteBiomesConfigHandler {

    @SuppressWarnings("unchecked")
    public static IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> handle(Path path, IdentityHashMap<Block, Object2DoubleArrayMap<Object>> objectToOverrideStorageDefault, Registry<Biome> biomeRegistry) {
        Gson gson = new GsonBuilder().registerTypeAdapter(IdentityHashMap.class, new CropFavoriteBiomesDeserializer(biomeRegistry)).setPrettyPrinting().disableHtmlEscaping().create();

        final File CONFIG_FILE = path.toFile();

        if (!CONFIG_FILE.exists() && !objectToOverrideStorageDefault.isEmpty()) {
            create(path, biomeRegistry, objectToOverrideStorageDefault);
        }

        if (CONFIG_FILE.exists()) {
            try (Reader reader = new FileReader(path.toString())) {
                IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropToFavoriteBiome = gson.fromJson(reader, IdentityHashMap.class);
                if (cropToFavoriteBiome != null) {
                    return cropToFavoriteBiome;
                } else
                    BetterWeather.LOGGER.error("\"" + path.toString() + "\" could not be read...");

            } catch (IOException e) {
                BetterWeather.LOGGER.error("\"" + path.toString() + "\" could not be read..." + e.toString());
            }
        }
        return new IdentityHashMap<>();
    }

    public static void create(Path path, Registry<Biome> biomeRegistry, IdentityHashMap<Block, Object2DoubleArrayMap<Object>> objectToOverrideStorageDefault) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(IdentityHashMap.class, new CropFavoriteBiomesDeserializer(biomeRegistry));
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();


        String jsonString = gson.toJson(objectToOverrideStorageDefault);

        try {
            Files.createDirectories(path.getParent());
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            BetterWeather.LOGGER.error("\"" + path.toString() + "\" could not be created..." + e.toString());
        }
    }


}

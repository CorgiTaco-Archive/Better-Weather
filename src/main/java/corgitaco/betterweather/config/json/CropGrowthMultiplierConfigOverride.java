package corgitaco.betterweather.config.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.Season;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CropGrowthMultiplierConfigOverride {

    public static void handleCropGrowthMultiplierConfig(Path path, Season.SubSeason subSeason) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();
        final File CONFIG_FILE = new File(String.valueOf(path));

        if (!CONFIG_FILE.exists()) {
            File directory = CONFIG_FILE.getParentFile();
            if (!directory.exists())
                directory.mkdirs();
            createCropGrowthMultiplierConfigDefaults(path, subSeason);
        }
        try (Reader reader = new FileReader(path.toString())) {
            Map<String, Double> cropToMultiplierHolder = gson.fromJson(reader, Map.class);
            if (cropToMultiplierHolder != null) {
                subSeason.getCropToMultiplierMap().putAll(cropToMultiplierHolder);
            } else
                BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-rivers.json could not be read");

        } catch (IOException e) {
            BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-rivers.json could not be read");
        }
    }


    public static void createCropGrowthMultiplierConfigDefaults(Path path, Season.SubSeason subSeason) {
        Map<String, Double> sortedMap = new TreeMap<>(Comparator.comparing(String::toString));
        for (String blockID : Stream.concat(BlockTags.CROPS.getAllElements().stream(), BlockTags.BEE_GROWABLES.getAllElements().stream()).map(Registry.BLOCK::getKey).map(ResourceLocation::toString).collect(Collectors.toSet())) {

            switch (subSeason.getSubSeasonVal()) {
                case SPRING_START:
                    sortedMap.put(blockID, 2.0);
                    break;
                case SPRING_MID:
                    sortedMap.put(blockID, 2.0);
                    break;
                case SPRING_END:
                    sortedMap.put(blockID, 1.5);
                    break;
                case SUMMER_START:
                    sortedMap.put(blockID, 1.0);
                    break;
                case SUMMER_MID:
                    sortedMap.put(blockID, 1.0);
                    break;
                case SUMMER_END:
                    sortedMap.put(blockID, 0.8);
                    break;
                case AUTUMN_START:
                    sortedMap.put(blockID, 0.7);
                    break;
                case AUTUMN_MID:
                    sortedMap.put(blockID, 0.65);
                    break;
                case AUTUMN_END:
                    sortedMap.put(blockID, 0.6);
                    break;
                case WINTER_START:
                    sortedMap.put(blockID, 0.5);
                    break;
                case WINTER_MID:
                    sortedMap.put(blockID, 0.4);
                    break;
                case WINTER_END:
                    sortedMap.put(blockID, 0.4);
                    break;
            }
        }

        createCropGrowthDefaultConfig(path, sortedMap);
    }

    public static void createCropGrowthDefaultConfig(Path path, Map<String, Double> cropToMultiplierMap) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();

        String jsonString = gson.toJson(cropToMultiplierMap);

        try {
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-rivers.json could not be created");
        }
    }
}

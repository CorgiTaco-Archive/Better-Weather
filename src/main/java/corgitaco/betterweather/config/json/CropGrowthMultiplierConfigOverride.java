//package corgitaco.betterweather.config.json;
//
//import com.google.gson.Gson;
//import com.google.gson.GsonBuilder;
//import corgitaco.betterweather.BetterWeather;
//import corgitaco.betterweather.season.Season;
//
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.io.Reader;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.Comparator;
//import java.util.Map;
//import java.util.TreeMap;
//
//public class CropGrowthMultiplierConfigOverride {
//
//    public static void handleCropGrowthMultiplierConfig(Path path, Season.SubSeason subSeason) {
//        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.setPrettyPrinting();
//        gsonBuilder.disableHtmlEscaping();
//        Gson gson = gsonBuilder.create();
//        final File CONFIG_FILE = new File(String.valueOf(path));
//
//        if (!CONFIG_FILE.exists()) {
//            File directory = CONFIG_FILE.getParentFile();
//            if (!directory.exists())
//                directory.mkdirs();
//            createCropGrowthMultiplierConfigDefaults(path);
//        }
//        try (Reader reader = new FileReader(path.toString())) {
//            Map<String, Double> cropToMultiplierHolder = gson.fromJson(reader, Map.class);
//            if (cropToMultiplierHolder != null) {
//                subSeason.getCropToMultiplierMap().putAll(cropToMultiplierHolder);
//            } else
//                BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-rivers.json could not be read");
//
//        } catch (IOException e) {
//            BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-rivers.json could not be read");
//        }
//    }
//
//
//    public static void createCropGrowthMultiplierConfigDefaults(Path path) {
//        Map<String, Double> sortedMap = new TreeMap<>(Comparator.comparing(String::toString));
//        createCropGrowthDefaultConfig(path, sortedMap);
//    }
//
//    public static void createCropGrowthDefaultConfig(Path path, Map<String, Double> cropToMultiplierMap) {
//        GsonBuilder gsonBuilder = new GsonBuilder();
//        gsonBuilder.setPrettyPrinting();
//        gsonBuilder.disableHtmlEscaping();
//        Gson gson = gsonBuilder.create();
//
//        String jsonString = gson.toJson(cropToMultiplierMap);
//
//        try {
//            Files.write(path, jsonString.getBytes());
//        } catch (IOException e) {
//            BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-rivers.json could not be created");
//        }
//    }
//}

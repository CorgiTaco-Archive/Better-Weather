package corgitaco.betterweather.config.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.SeasonSystem;
import corgitaco.betterweather.season.Season;
import net.minecraft.util.Util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class SeasonConfig {

    @SuppressWarnings({"RedundantCast", "unchecked", "rawtypes"})
    public static void handleBWSeasonsConfig(Path path) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();
        final File CONFIG_FILE = new File(String.valueOf(path));

        Map<String, Season> defaultMap = Util.make((new TreeMap<>()), (map) -> {
            map.put(SeasonSystem.SeasonVal.SPRING.toString(), Season.SPRING);
            map.put(SeasonSystem.SeasonVal.SUMMER.toString(), Season.SUMMER);
            map.put(SeasonSystem.SeasonVal.AUTUMN.toString(), Season.AUTUMN);
            map.put(SeasonSystem.SeasonVal.WINTER.toString(), Season.WINTER);
        });

        if (!CONFIG_FILE.exists()) {
            createSeasonJson(path, defaultMap);
        }

        try (Reader reader = new FileReader(path.toString())) {
            Map biomeDataListHolder = gson.fromJson(reader, Map.class);
            if (biomeDataListHolder != null) {
                Map<String, Season> stringSeasonMap = new TreeMap<>();

                for (int idx = 0; idx < SeasonSystem.SeasonVal.values().length; idx++) {
                    String seasonName = SeasonSystem.SeasonVal.values()[idx].toString();
                    Season object = new Gson().fromJson(new Gson().toJson(((LinkedTreeMap<String, Object>) biomeDataListHolder.get(seasonName))), Season.class);
                    stringSeasonMap.put(seasonName, object);
                }

                for (int idx = 0; idx < SeasonSystem.SubSeasonVal.values().length; idx++) {
                    String subSeasonName = SeasonSystem.SubSeasonVal.values()[idx].toString();

                    for (int idx2 = 0; idx2 < SeasonSystem.SeasonVal.values().length; idx2++) {
                        String seasonName = SeasonSystem.SeasonVal.values()[idx2].toString();
                        if (subSeasonName.contains(seasonName)) {
                            if (subSeasonName.contains("START")) {
                                stringSeasonMap.get(seasonName).getStart().setSubSeasonVal(SeasonSystem.SubSeasonVal.valueOf(subSeasonName));
                                stringSeasonMap.get(seasonName).getStart().setParentSeason(SeasonSystem.SeasonVal.valueOf(seasonName));
                                Season.SUB_SEASON_MAP.put(subSeasonName, stringSeasonMap.get(seasonName).getStart());
                            } else if (subSeasonName.contains("MID")) {
                                stringSeasonMap.get(seasonName).getMid().setSubSeasonVal(SeasonSystem.SubSeasonVal.valueOf(subSeasonName));
                                stringSeasonMap.get(seasonName).getMid().setParentSeason(SeasonSystem.SeasonVal.valueOf(seasonName));
                                Season.SUB_SEASON_MAP.put(subSeasonName, stringSeasonMap.get(seasonName).getMid());
                            } else {
                                stringSeasonMap.get(seasonName).getEnd().setSubSeasonVal(SeasonSystem.SubSeasonVal.valueOf(subSeasonName));
                                stringSeasonMap.get(seasonName).getEnd().setParentSeason(SeasonSystem.SeasonVal.valueOf(seasonName));
                                Season.SUB_SEASON_MAP.put(subSeasonName, stringSeasonMap.get(seasonName).getEnd());
                            }
                        }
                        Season.SubSeason subSeason = Season.SUB_SEASON_MAP.get(subSeasonName);
                        subSeason.getClient().parseHexColors();
                        subSeason.processInfo();
                    }
                }
                Season.SEASON_MAP = stringSeasonMap;
            } else
                BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-seasons.json could not be read");

        } catch (IOException e) {
            BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-seasons.json could not be read");
        }
    }

    public static void createSeasonJson(Path path, Map<String, Season> seasonStringMap) {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gsonBuilder.disableHtmlEscaping();
        Gson gson = gsonBuilder.create();

        String jsonString = gson.toJson(seasonStringMap);

        try {
            Files.write(path, jsonString.getBytes());
        } catch (IOException e) {
            BetterWeather.LOGGER.error(BetterWeather.MOD_ID + "-rivers.json could not be created");
        }
    }
}

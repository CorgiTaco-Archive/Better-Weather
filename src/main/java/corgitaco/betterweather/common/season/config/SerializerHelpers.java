package corgitaco.betterweather.common.season.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.common.season.BWSeason;
import corgitaco.betterweather.common.season.BWSubseasonSettings;
import corgitaco.betterweather.common.season.config.overrides.BiomeOverrideJsonHandler;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import net.minecraft.block.Block;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

public class SerializerHelpers {
    public static final ITag.INamedTag<Block> GLOBAL_AFFECTED_CROPS = BlockTags.createOptional(new ResourceLocation(BetterWeather.MOD_ID, "global.affected_crops"));


    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(Util.make(new HashMap<>(), (map) -> {
        map.put("yearLength", "Represents this world's year length in ticks(a minecraft day is 24000 ticks). Season length is 1/4 of this value. Sub season length is 1/12(or 1/3 of season length) of this value.");
        map.put("tickSeasonTimeWhenNoPlayersOnline", "Does Season Time tick in this world when no players are online?");
        map.put("tempModifier", "Modifies this world's temperature.");
        map.put("cropGrowthChanceMultiplier", "Multiplies the growth rate of crops when ticked.");
        map.put("entityBreedingBlacklist", "Blacklist specific mobs from breeding.");

        map.put("humidityModifier", "Modifies this world's humidity.");
        map.put("weatherEventChanceMultiplier", "Multiplies the chance of a weather event occurring.");

        map.put("fogColorBlendStrength", "The strength of this world's fog color blend towards the value of \"fogTargetHexColor\".\nRange: 0 - 1.0");
        map.put("fogTargetHexColor", "Blends the world's fog color towards this value. Blend strength is determined by the value of \"fogColorBlendStrength\".");

        map.put("foliageColorBlendStrength", "The strength of this world's sky color blend towards the value of \"foliageTargetHexColor\".\nRange: 0 - 1.0");
        map.put("foliageTargetHexColor", "Blends this world's foliage color towards this value. Blend strength is determined by the value of \"foliageColorBlendStrength\".");

        map.put("grassColorBlendStrength", "The strength of this world's grass color blend towards the value of \"grassTargetHexColor\".\nRange: 0 - 1.0");
        map.put("grassTargetHexColor", "Blends this world's grass color towards this value. Blend strength is determined by the value of \"grassColorBlendStrength\".");

        map.put("skyColorBlendStrength", "The strength of this world's sky color blend towards the value of \"skyTargetHexColor\".\nRange: 0 - 1.0");
        map.put("skyTargetHexColor", "Blends this world's grass color towards this value. Blend strength is determined by the value of \"skyColorBlendStrength\".");

        map.put("weatherEventController", "Represents the chance of the listed weather event.");
    }), true);


    public static SeasonConfigHolder handleConfig(Registry<Biome> biomeRegistry, ResourceLocation worldID, File seasonConfigFile, Path seasonOverridesPath, boolean isClient, Map<Season.Key, BWSeason> seasons) {
        createConfig(seasonConfigFile);
        if (!seasonConfigFile.exists()) {
            BetterWeather.LOGGER.error("\"%s\" does not exist and therefore cannot be read, using defaults...", seasonConfigFile.toString());
            return SeasonConfigHolder.DEFAULT_CONFIG_HOLDER;
        }

        SeasonConfigHolder configHolder = read(seasonConfigFile, isClient, seasons);
        fillSubSeasonOverrideStorageAndSetCropTags(biomeRegistry, worldID, seasonOverridesPath, isClient, seasons);
        return configHolder;
    }

    private static void createConfig(File seasonConfigFile) {
        CommentedConfig readConfig = seasonConfigFile.exists() ? CommentedFileConfig.builder(seasonConfigFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
        if (readConfig instanceof CommentedFileConfig) {
            ((CommentedFileConfig) readConfig).load();
        }
        CommentedConfig encodedConfig = (CommentedConfig) SeasonConfigHolder.CODEC.encodeStart(CONFIG_OPS, SeasonConfigHolder.DEFAULT_CONFIG_HOLDER).result().get();

        try {
            Files.createDirectories(seasonConfigFile.toPath().getParent());
            new TomlWriter().write(seasonConfigFile.exists() ? TomlCommentedConfigOps.recursivelyUpdateAndSortConfig(readConfig, encodedConfig) : encodedConfig, seasonConfigFile, WritingMode.REPLACE);
        } catch (IOException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
    }

    private static SeasonConfigHolder read(File seasonConfigFile, boolean isClient, Map<Season.Key, BWSeason> seasons) {
        try (Reader reader = new FileReader(seasonConfigFile)) {
            Optional<SeasonConfigHolder> configHolder = SeasonConfigHolder.CODEC.parse(CONFIG_OPS, new TomlParser().parse(reader)).resultOrPartial(BetterWeather.LOGGER::error);
            if (configHolder.isPresent()) {
                final Map<Season.Key, BWSeason> seasonSettings = configHolder.get().getSeasonKeySeasonMap();
                if (!isClient) {
                    seasons.putAll(seasonSettings);
                } else {
                    seasons.putAll(SeasonConfigHolder.DEFAULT_CONFIG_HOLDER.getSeasonKeySeasonMap());
                }
                seasons.putAll(seasonSettings);
                for (Map.Entry<Season.Key, BWSeason> entry : seasonSettings.entrySet()) {
                    Season.Key key = entry.getKey();
                    BWSeason season = entry.getValue();
                    for (Season.Phase phase : Season.Phase.values()) {
                        seasons.get(key).getSettingsForPhase(phase).setClient(season.getSettingsForPhase(phase).getClientSettings()); //Only update client settings on the client.
                    }
                }
            }
            return configHolder.orElse(SeasonConfigHolder.DEFAULT_CONFIG_HOLDER);
        } catch (IOException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
        return SeasonConfigHolder.DEFAULT_CONFIG_HOLDER; // We should never hit this ever.
    }

    private static void fillSubSeasonOverrideStorageAndSetCropTags(Registry<Biome> biomeRegistry, ResourceLocation worldID, Path seasonOverridesPath, boolean isClient, Map<Season.Key, BWSeason> seasons) {
        for (Map.Entry<Season.Key, BWSeason> seasonKeySeasonEntry : seasons.entrySet()) {
            Season.Key seasonKey = seasonKeySeasonEntry.getKey();
            seasonKeySeasonEntry.getValue().setSeasonKey(seasonKey);
            Map<Season.Phase, BWSubseasonSettings> phaseSettings = seasonKeySeasonEntry.getValue().getPhaseSettings();
            for (Map.Entry<Season.Phase, BWSubseasonSettings> phaseSubSeasonSettingsEntry : phaseSettings.entrySet()) {
                String mapKey = seasonKey + "-" + phaseSubSeasonSettingsEntry.getKey();
                if (!isClient) {
                    String worldKey = worldID.toString().replace(":", ".");
                    ITag.INamedTag<Block> unenhancedCrops = BWSeason.UNAFFECTED_CROPS.get(mapKey);
                    phaseSubSeasonSettingsEntry.getValue().setCropTags(
                            BWSeason.AFFECTED_CROPS.get(mapKey).getValues().isEmpty() ? BWSeason.AFFECTED_CROPS.get(worldKey).getValues().isEmpty() ? GLOBAL_AFFECTED_CROPS : BWSeason.AFFECTED_CROPS.get(worldKey) : GLOBAL_AFFECTED_CROPS,
                            unenhancedCrops);
                }
                BiomeOverrideJsonHandler.handleOverrideJsonConfigs(seasonOverridesPath.resolve(seasonKeySeasonEntry.getKey().toString() + "-" + phaseSubSeasonSettingsEntry.getKey() + ".json"), seasonKeySeasonEntry.getKey() == Season.Key.WINTER ? BWSubseasonSettings.WINTER_OVERRIDE : new IdentityHashMap<>(), phaseSubSeasonSettingsEntry.getValue(), biomeRegistry, isClient);
            }
        }
    }

    public static void clinit() {
    }
}

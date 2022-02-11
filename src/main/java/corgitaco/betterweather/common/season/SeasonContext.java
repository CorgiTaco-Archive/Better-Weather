package corgitaco.betterweather.common.season;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.Climate;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.common.network.NetworkHandler;
import corgitaco.betterweather.common.network.packet.season.YearTimePacket;
import corgitaco.betterweather.common.network.packet.util.RefreshRenderersPacket;
import corgitaco.betterweather.common.savedata.SeasonSavedData;
import corgitaco.betterweather.common.season.config.SeasonConfigHolder;
import corgitaco.betterweather.common.season.config.cropfavoritebiomes.CropFavoriteBiomesConfigHandler;
import corgitaco.betterweather.common.season.config.overrides.BiomeOverrideJsonHandler;
import corgitaco.betterweather.server.BetterWeatherGameRules;
import corgitaco.betterweather.util.BetterWeatherUtil;
import corgitaco.betterweather.util.BiomeUpdate;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ServerWorldInfo;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

@SuppressWarnings("deprecation")
public class SeasonContext implements Climate {
    public static final String CONFIG_NAME = "season-settings.toml";

    public static final ITag.INamedTag<Block> GLOBAL_AFFECTED_CROPS = BlockTags.createOptional(new ResourceLocation(BetterWeather.MOD_ID, "global.affected_crops"));

    public static final Codec<SeasonContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.INT.fieldOf("yearLength").forGetter((seasonContext) -> {
            return seasonContext.yearLength;
        }), Codec.INT.fieldOf("yearTime").forGetter((seasonContext) -> {
            return seasonContext.yearTime;
        }), Codec.simpleMap(Season.Key.CODEC, BWSeason.PACKET_CODEC, IStringSerializable.keys(Season.Key.values())).fieldOf("seasons").forGetter((seasonContext) -> {
            return seasonContext.seasons;
        }), Codec.unboundedMap(ResourceLocation.CODEC, Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE)).fieldOf("cropFavoriteBiomes").forGetter((seasonContext) -> {
            Map<ResourceLocation, Map<ResourceLocation, Double>> serialized = new HashMap<>();
            for (Map.Entry<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> blockToFavoriteBiome : seasonContext.cropToFavoriteBiomes.entrySet()) {
                Map<ResourceLocation, Double> favBiomeSerialized = new HashMap<>();
                for (Object2DoubleMap.Entry<RegistryKey<Biome>> favBiomeToBonus : blockToFavoriteBiome.getValue().object2DoubleEntrySet()) {
                    favBiomeSerialized.put(favBiomeToBonus.getKey().location(), favBiomeToBonus.getDoubleValue());
                }
                serialized.put(Registry.BLOCK.getKey(blockToFavoriteBiome.getKey()), favBiomeSerialized);
            }
            return serialized;
        })).apply(builder, (yearLength, yearTime, seasonMap, cropFavoriteBiomesSerialized) -> {
            IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropFavoriteBiomes = new IdentityHashMap<>();
            for (Map.Entry<ResourceLocation, Map<ResourceLocation, Double>> serializedCropEntry : cropFavoriteBiomesSerialized.entrySet()) {
                Object2DoubleArrayMap<RegistryKey<Biome>> favBiomes = new Object2DoubleArrayMap<>();
                for (Map.Entry<ResourceLocation, Double> value : serializedCropEntry.getValue().entrySet()) {
                    favBiomes.put(RegistryKey.create(Registry.BIOME_REGISTRY, value.getKey()), value.getValue().doubleValue());
                }
                Optional<Block> optional = Registry.BLOCK.getOptional(serializedCropEntry.getKey());
                if (!optional.isPresent()) {
                    throw new IllegalArgumentException("\"" + serializedCropEntry.getKey() + "\" is not a crop in the CLIENT registry! Failing packet serialization....");
                }
                cropFavoriteBiomes.put(optional.get(), favBiomes);
            }
            return new SeasonContext(null, yearLength, yearTime, cropFavoriteBiomes, new IdentityHashMap<>(seasonMap));
        });
    });

    public static final IdentityHashMap<Block, Object2DoubleArrayMap<Object>> BLOCK_TO_FAVORITE_BIOMES_DEFAULT = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(Blocks.MELON_STEM, Util.make(new Object2DoubleArrayMap<>(), (favoriteBiomes) -> {
            favoriteBiomes.put(Biome.Category.JUNGLE, 0.4);
        }));

        map.put(Blocks.SWEET_BERRY_BUSH, Util.make(new Object2DoubleArrayMap<>(), (favoriteBiomes) -> {
            favoriteBiomes.put(Biome.Category.TAIGA, 0.4);
        }));
    });

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

    private final IdentityHashMap<Season.Key, BWSeason> seasons = new IdentityHashMap<>();
    private final IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropToFavoriteBiomes = new IdentityHashMap<>();
    private boolean tickSeasonTimeWhenNoPlayersOnline = true;

    private BWSeason currentSeason;
    private int yearLength;
    private int yearTime;

    //Packet Constructor
    public SeasonContext(int yearLength, int yearTime, IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropToFavoriteBiomes, ResourceLocation worldID, IdentityHashMap<Season.Key, BWSeason> seasons) {
        this(null, yearLength, yearTime, cropToFavoriteBiomes, seasons);
    }

    //Server world constructor
    public SeasonContext(ServerWorld world) {
        SeasonSavedData seasonSavedData = SeasonSavedData.get(world);
        this.yearLength = seasonSavedData.getYearLength();
        this.yearTime = seasonSavedData.getYearTime();
        ResourceLocation worldID = world.dimension().location();
        Path seasonsPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("seasons");
        File seasonConfigFile = seasonsPath.resolve(CONFIG_NAME).toFile();
        Path seasonOverridesPath = seasonsPath.resolve("overrides");
        this.tickSeasonTimeWhenNoPlayersOnline = this.handleConfig(world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), worldID, seasonConfigFile, seasonOverridesPath, false).isTickSeasonTimeWhenNoPlayersOnline();
        this.cropToFavoriteBiomes.putAll(CropFavoriteBiomesConfigHandler.handle(seasonsPath.resolve("crop-favorite-biomes.json"), BLOCK_TO_FAVORITE_BIOMES_DEFAULT, world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)));
        this.currentSeason = this.seasons.get(Season.getSeasonFromTime(world.getDayTime(), this.yearLength));
        this.currentSeason.setPhaseForTime(this.yearTime, this.yearLength);
    }

    //Client Constructor
    public SeasonContext(@Nullable ClientWorld world, int yearLength, int yearTime, IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropToFavoriteBiomes, @Nullable IdentityHashMap<Season.Key, BWSeason> seasons) {
        this.yearLength = yearLength;
        this.yearTime = yearTime;
        if (world != null) {
            ResourceLocation worldID = world.dimension().location();
            Path seasonsPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("seasons");
            File seasonConfigFile = seasonsPath.resolve(CONFIG_NAME).toFile();
            Path seasonOverridesPath = seasonsPath.resolve("overrides");
            this.seasons.putAll(seasons);
            this.cropToFavoriteBiomes.putAll(cropToFavoriteBiomes);
            this.tickSeasonTimeWhenNoPlayersOnline = this.handleConfig(world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), worldID, seasonConfigFile, seasonOverridesPath, true).isTickSeasonTimeWhenNoPlayersOnline();
            this.currentSeason = this.seasons.get(Season.getSeasonFromTime(this.yearTime, this.yearLength));
            this.currentSeason.setPhaseForTime(this.yearTime, this.yearLength);
        }
    }

    public void setSeason(World world, Season.Key newSeason, Season.Phase phase) {
        BWSubseasonSettings oldSettings = this.currentSeason.getCurrentSettings();
        this.yearTime = updateYearTime(world, Season.getSeasonAndPhaseStartTime(newSeason, phase, this.yearLength));
    }

    public void tick(World world) {
        this.updateYearTime(world);
    }

    private boolean seasonChange(World world, BWSubseasonSettings prevSettings) {
        return seasonChange(world, this.currentSeason.getCurrentSettings(), prevSettings);
    }

    private boolean seasonChange(World world, BWSubseasonSettings prevSettings, BWSubseasonSettings currentSettings) {
        if (prevSettings != currentSettings) {
            ((BiomeUpdate) world).updateBiomeData();
            if (!world.isClientSide) {
                updateWeatherMultiplier(world, prevSettings.getWeatherEventChanceMultiplier(), currentSettings.getWeatherEventChanceMultiplier());
                broadcast(((ServerWorld) world).players(), true);
            }
            return true;
        }
        return false;
    }

    public void updateWeatherMultiplier(World world, double prevMultiplier, double currentMultiplier) {
        if (world.getLevelData() instanceof ServerWorldInfo) {
            ServerWorldInfo worldInfo = (ServerWorldInfo) world.getLevelData();
            if (!worldInfo.isRaining()) {
                worldInfo.setRainTime(BetterWeatherUtil.transformRainOrThunderTimeToCurrentSeason(worldInfo.getRainTime(), prevMultiplier, currentMultiplier));
                worldInfo.setThunderTime(BetterWeatherUtil.transformRainOrThunderTimeToCurrentSeason(worldInfo.getThunderTime(), prevMultiplier, currentMultiplier));
            }
        }
    }

    /**
     * Called every block random tick.
     */
    public void enhanceCropRandomTick(ServerWorld world, BlockPos pos, Block block, BlockState self, CallbackInfo ci) {
        if (this.getCurrentSeason().getCurrentSettings().getEnhancedCrops().contains(block)) {
            //Collect the crop multiplier for the given subseason.
            RegistryKey<Biome> currentBiomeKey = world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).getResourceKey(world.getBiome(pos)).get();
            double cropBonus = this.cropToFavoriteBiomes.containsKey(block) ? this.cropToFavoriteBiomes.get(block).getOrDefault(currentBiomeKey, 0.0) : 0.0;

            double cropGrowthMultiplier = getCurrentSubSeasonSettings().getCropGrowthMultiplier(currentBiomeKey, block) + cropBonus;
            if (cropGrowthMultiplier == 1) {
                return;
            }
            BlockPos.Mutable mutable = new BlockPos.Mutable().set(pos.above(1));

            for (int move = 0; move <= 16; move++) {
                if (world.getBlockState(mutable.move(Direction.UP)).is(Tags.Blocks.GLASS)) {
                    cropGrowthMultiplier = cropGrowthMultiplier * 1.5;
                    break;
                }
            }

            //Pretty self explanatory, basically run a chance on whether or not the crop will tick for this tick
            if (cropGrowthMultiplier < 1) {
                if (world.getRandom().nextDouble() < cropGrowthMultiplier) {
                    block.randomTick(self, world, pos, world.getRandom());
                } else {
                    ci.cancel();
                }
            }

            //Here we gather a random number of ticks that this block will tick for this given tick.
            //We do a random.nextDouble() to determine if we get the ceil or floor value for the given crop growth multiplier.
            else if (cropGrowthMultiplier > 1) {
                int numberOfTicks = world.getRandom().nextInt((world.getRandom().nextDouble() + (cropGrowthMultiplier - 1) < cropGrowthMultiplier) ? (int) Math.ceil(cropGrowthMultiplier) : (int) cropGrowthMultiplier) + 1;
                for (int tick = 0; tick < numberOfTicks; tick++) {
                    if (tick > 0) {
                        self = world.getBlockState(pos);
                        // Only continue random tick enhancement if block hasn't changed.
                        if (block != self.getBlock()) {
                            break;
                        }
                    }

                    block.randomTick(self, world, pos, world.getRandom());
                }
            }
        }
    }

    public void broadcast(List<ServerPlayerEntity> players) {
        broadcast(players, false);
    }

    public void broadcast(List<ServerPlayerEntity> players, boolean refreshRenderers) {
        NetworkHandler.sendToAllPlayers(players, new YearTimePacket(this.getYearTime()));
        if (refreshRenderers) {
            NetworkHandler.sendToAllPlayers(players, new RefreshRenderersPacket());
        }
    }

    public int updateYearTime(World world) {
        return updateYearTime(world, Integer.MIN_VALUE);
    }

    public int updateYearTime(World world, int newYearTime) {
        if (world.getGameRules().getBoolean(BetterWeatherGameRules.DO_SEASON_CYCLE)) {
            BWSubseasonSettings preUpdateSettings = this.getCurrentSeason().getCurrentSettings();
            if (newYearTime > 0) {
                this.yearTime = newYearTime - 1;
            }
            if (!world.isClientSide) {
                if (!this.tickSeasonTimeWhenNoPlayersOnline && world.players().isEmpty()) {
                    return this.yearTime;
                }
            }

            this.currentSeason = this.seasons.get(Season.getSeasonFromTime(this.yearTime > this.yearLength ? this.yearTime = 0 : this.yearTime++, this.yearLength)).setPhaseForTime(this.yearTime, this.yearLength);

            if (world.getLevelData().getGameTime() % 50 == 0 || seasonChange(world, preUpdateSettings)) {
                save(world);
                if (!world.isClientSide) {
                    broadcast(((IServerWorld) world).getLevel().players());
                }
            }
        }
        return this.yearTime;
    }

    private void save(World world) {
        SeasonSavedData.get(world).setFromSeasonContext(this);
    }

    /**********Configs**********/

    public SeasonConfigHolder handleConfig(Registry<Biome> biomeRegistry, ResourceLocation worldID, File seasonConfigFile, Path seasonOverridesPath, boolean isClient) {
        createConfig(seasonConfigFile);
        if (!seasonConfigFile.exists()) {
            BetterWeather.LOGGER.error("\"%s\" does not exist and therefore cannot be read, using defaults...", seasonConfigFile.toString());
            return SeasonConfigHolder.DEFAULT_CONFIG_HOLDER;
        }

        SeasonConfigHolder configHolder = read(seasonConfigFile, isClient);
        fillSubSeasonOverrideStorageAndSetCropTags(biomeRegistry, worldID, seasonOverridesPath, isClient);
        return configHolder;
    }

    private void createConfig(File seasonConfigFile) {
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

    private SeasonConfigHolder read(File seasonConfigFile, boolean isClient) {
        try (Reader reader = new FileReader(seasonConfigFile)) {
            Optional<SeasonConfigHolder> configHolder = SeasonConfigHolder.CODEC.parse(CONFIG_OPS, new TomlParser().parse(reader)).resultOrPartial(BetterWeather.LOGGER::error);
            if (configHolder.isPresent()) {
                this.yearLength = configHolder.get().getSeasonCycleLength();
                final IdentityHashMap<Season.Key, BWSeason> seasonSettings = configHolder.get().getSeasonKeySeasonMap();
                if (!isClient) {
                    this.seasons.putAll(seasonSettings);
                } else {
                    this.seasons.putAll(SeasonConfigHolder.DEFAULT_CONFIG_HOLDER.getSeasonKeySeasonMap());
                }
                this.seasons.putAll(seasonSettings);
                for (Map.Entry<Season.Key, BWSeason> entry : seasonSettings.entrySet()) {
                    Season.Key key = entry.getKey();
                    BWSeason season = entry.getValue();
                    for (Season.Phase phase : Season.Phase.values()) {
                        this.seasons.get(key).getSettingsForPhase(phase).setClient(season.getSettingsForPhase(phase).getClientSettings()); //Only update client settings on the client.
                    }
                }
            }
            return configHolder.orElse(SeasonConfigHolder.DEFAULT_CONFIG_HOLDER);
        } catch (IOException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
        return SeasonConfigHolder.DEFAULT_CONFIG_HOLDER; // We should never hit this ever.
    }

    private void fillSubSeasonOverrideStorageAndSetCropTags(Registry<Biome> biomeRegistry, ResourceLocation worldID, Path seasonOverridesPath, boolean isClient) {
        for (Map.Entry<Season.Key, BWSeason> seasonKeySeasonEntry : this.seasons.entrySet()) {
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

    public BWSeason getCurrentSeason() {
        return currentSeason;
    }

    public BWSeason getSeasonForYearTime(long yearTime) {
        return this.seasons.get(Season.getSeasonFromTime(yearTime, this.yearLength));
    }

    public BWSubseasonSettings getCurrentSubSeasonSettings() {
        return this.currentSeason.getCurrentSettings();
    }

    public int getYearTime() {
        return this.yearTime;
    }

    @Nullable
    @Override
    public Season getSeason() {
        return this.currentSeason;
    }

    public IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> getCropFavoriteBiomeBonuses() {
        return this.cropToFavoriteBiomes;
    }


    @Override
    public int getYearLength() {
        return yearLength;
    }

    public IdentityHashMap<Season.Key, BWSeason> getSeasons() {
        return seasons;
    }
}

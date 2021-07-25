package corgitaco.betterweather.season;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.season.SubseasonSettings;
import corgitaco.betterweather.data.network.NetworkHandler;
import corgitaco.betterweather.data.network.packet.season.SeasonContextConstructingPacket;
import corgitaco.betterweather.data.network.packet.season.SeasonTimePacket;
import corgitaco.betterweather.data.network.packet.util.RefreshRenderersPacket;
import corgitaco.betterweather.data.storage.SeasonSavedData;
import corgitaco.betterweather.helpers.BiomeUpdate;
import corgitaco.betterweather.season.config.SeasonConfigHolder;
import corgitaco.betterweather.season.config.cropfavoritebiomes.CropFavoriteBiomesConfigHandler;
import corgitaco.betterweather.season.config.overrides.BiomeOverrideJsonHandler;
import corgitaco.betterweather.server.BetterWeatherGameRules;
import corgitaco.betterweather.util.BetterWeatherUtil;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
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
public class SeasonContext implements Season {
    public static final String CONFIG_NAME = "season-settings.toml";

    public static final ITag.INamedTag<Block> GLOBAL_AFFECTED_CROPS = BlockTags.createOptional(new ResourceLocation(BetterWeather.MOD_ID, "global.affected_crops"));

    public static final Codec<SeasonContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.INT.fieldOf("currentYearTime").forGetter((seasonContext) -> {
            return seasonContext.currentYearTime;
        }), Codec.INT.fieldOf("yearLength").forGetter((seasonContext) -> {
            return seasonContext.yearLength;
        }), ResourceLocation.CODEC.fieldOf("worldID").forGetter((seasonContext) -> {
            return seasonContext.worldID;
        }), Codec.simpleMap(Key.CODEC, BWSeason.PACKET_CODEC, IStringSerializable.createKeyable(Key.values())).fieldOf("seasons").forGetter((seasonContext) -> {
            return seasonContext.seasons;
        }), Codec.unboundedMap(ResourceLocation.CODEC, Codec.unboundedMap(ResourceLocation.CODEC, Codec.DOUBLE)).fieldOf("cropFavoriteBiomes").forGetter((seasonContext) -> {
            Map<ResourceLocation, Map<ResourceLocation, Double>> serialized = new HashMap<>();
            for (Map.Entry<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> blockToFavoriteBiome : seasonContext.cropToFavoriteBiomes.entrySet()) {
                Map<ResourceLocation, Double> favBiomeSerialized = new HashMap<>();
                for (Object2DoubleMap.Entry<RegistryKey<Biome>> favBiomeToBonus : blockToFavoriteBiome.getValue().object2DoubleEntrySet()) {
                    favBiomeSerialized.put(favBiomeToBonus.getKey().getLocation(), favBiomeToBonus.getDoubleValue());
                }
                serialized.put(Registry.BLOCK.getKey(blockToFavoriteBiome.getKey()), favBiomeSerialized);
            }
            return serialized;
        })).apply(builder, (currentYearTime, yearLength, worldID, seasonMap, cropFavoriteBiomesSerialized) -> {
            IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropFavoriteBiomes = new IdentityHashMap<>();
            for (Map.Entry<ResourceLocation, Map<ResourceLocation, Double>> serializedCropEntry : cropFavoriteBiomesSerialized.entrySet()) {
                Object2DoubleArrayMap<RegistryKey<Biome>> favBiomes = new Object2DoubleArrayMap<>();
                for (Map.Entry<ResourceLocation, Double> value : serializedCropEntry.getValue().entrySet()) {
                    favBiomes.put(RegistryKey.getOrCreateKey(Registry.BIOME_KEY, value.getKey()), value.getValue().doubleValue());
                }
                Optional<Block> optional = Registry.BLOCK.getOptional(serializedCropEntry.getKey());
                if (!optional.isPresent()) {
                    throw new IllegalArgumentException("\"" + serializedCropEntry.getKey() + "\" is not a crop in the CLIENT registry! Failing packet serialization....");
                }
                cropFavoriteBiomes.put(optional.get(), favBiomes);
            }
            return new SeasonContext(currentYearTime, yearLength, cropFavoriteBiomes, worldID, new IdentityHashMap<>(seasonMap));
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

    private final ResourceLocation worldID;
    private final Registry<Biome> biomeRegistry;
    private final File seasonConfigFile;
    private final Path seasonsPath;
    private final Path seasonOverridesPath;
    private final IdentityHashMap<Season.Key, BWSeason> seasons = new IdentityHashMap<>();
    private final IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropToFavoriteBiomes;
    private boolean tickSeasonTimeWhenNoPlayersOnline = true;

    private BWSeason currentSeason;
    private int currentYearTime;
    private int yearLength;

    //Packet Constructor
    public SeasonContext(int currentYearTime, int yearLength, IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropToFavoriteBiomes, ResourceLocation worldID, IdentityHashMap<Season.Key, BWSeason> seasons) {
        this(currentYearTime, yearLength, worldID, cropToFavoriteBiomes, null, seasons);
    }

    //Server world constructor
    public SeasonContext(SeasonSavedData seasonData, RegistryKey<World> worldID, Registry<Biome> biomeRegistry) {
        this(seasonData.getCurrentYearTime(), seasonData.getYearLength(), worldID.getLocation(), new IdentityHashMap<>(), biomeRegistry, null);
        this.cropToFavoriteBiomes.putAll(CropFavoriteBiomesConfigHandler.handle(seasonsPath.resolve("crop-favorite-biomes.json"), BLOCK_TO_FAVORITE_BIOMES_DEFAULT, biomeRegistry));
    }

    //Client Constructor
    public SeasonContext(int currentYearTime, int yearLength, ResourceLocation worldID, IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropToFavoriteBiomes, @Nullable Registry<Biome> biomeRegistry, @Nullable IdentityHashMap<Season.Key, BWSeason> seasons) {
        this.currentYearTime = currentYearTime;
        this.yearLength = yearLength;
        this.worldID = worldID;
        this.biomeRegistry = biomeRegistry;
        this.seasonsPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("seasons");
        this.seasonConfigFile = seasonsPath.resolve(CONFIG_NAME).toFile();
        this.seasonOverridesPath = seasonsPath.resolve("overrides");
        this.cropToFavoriteBiomes = cropToFavoriteBiomes;

        boolean isClient = seasons != null;
        boolean isPacket = biomeRegistry == null;

        if (isClient) {
            this.seasons.putAll(seasons);
        }
        if (!isPacket) {
            this.tickSeasonTimeWhenNoPlayersOnline = this.handleConfig(isClient).isTickSeasonTimeWhenNoPlayersOnline();
            this.currentSeason = this.seasons.get(Season.getSeasonFromTime(this.currentYearTime, this.yearLength));
            this.currentSeason.setPhaseForTime(this.currentYearTime, this.yearLength);
        }
    }

    public void setSeason(ServerWorld world, List<ServerPlayerEntity> players, Season.Key newSeason, Season.Phase phase) {
        BWSubseasonSettings prevSettings = this.getCurrentSubSeasonSettings();
        this.currentYearTime = Season.getSeasonAndPhaseStartTime(newSeason, phase, this.yearLength);
        this.currentSeason = seasons.get(newSeason);
        this.currentSeason.setPhaseForTime(currentYearTime, yearLength);
        BWSubseasonSettings currentSubSeasonSettings = this.getCurrentSubSeasonSettings();
        if (prevSettings != currentSubSeasonSettings) {
            onSeasonChange(world, prevSettings, currentSubSeasonSettings);
        }
    }

    public void tick(World world) {
        BWSeason prevSeason = this.currentSeason;
        Season.Phase prevPhase = this.currentSeason.getCurrentPhase();
        BWSubseasonSettings prevSettings = prevSeason.getSettingsForPhase(prevPhase);
        this.tickSeasonTime(world);

        boolean changedSeasonFlag = prevSeason != this.currentSeason;
        boolean changedPhaseFlag = prevPhase != this.currentSeason.getCurrentPhase();

        if (changedSeasonFlag || changedPhaseFlag) {
            onSeasonChange(world, prevSettings, this.currentSeason.getCurrentSettings());
        }
    }

    private void onSeasonChange(World world, BWSubseasonSettings prevSettings, BWSubseasonSettings currentSettings) {
        ((BiomeUpdate) world).updateBiomeData();
        if (!world.isRemote) {
            updateWeatherMultiplier(world, prevSettings.getWeatherEventChanceMultiplier(), currentSettings.getWeatherEventChanceMultiplier());
            updatePacket(((ServerWorld) world).getPlayers());
        }
    }

    public void updateWeatherMultiplier(World world, double prevMultiplier, double currentMultiplier) {
        if (world.getWorldInfo() instanceof ServerWorldInfo) {
            ServerWorldInfo worldInfo = (ServerWorldInfo) world.getWorldInfo();
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
            Block block1 = block;
            //Collect the crop multiplier for the given subseason.
            RegistryKey<Biome> currentBiomeKey = world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(world.getBiome(pos)).get();
            double cropBonus = this.cropToFavoriteBiomes.containsKey(block1) ? this.cropToFavoriteBiomes.get(block1).getOrDefault(currentBiomeKey, 0.0) : 0.0;

            double cropGrowthMultiplier = getCurrentSubSeasonSettings().getCropGrowthMultiplier(currentBiomeKey, block1) + cropBonus;
            if (cropGrowthMultiplier == 1) {
                return;
            }
            BlockPos.Mutable mutable = new BlockPos.Mutable().setPos(pos.up(1));

            for (int move = 0; move <= 16; move++) {
                if (world.getBlockState(mutable.move(Direction.UP)).isIn(Tags.Blocks.GLASS)) {
                    cropGrowthMultiplier = cropGrowthMultiplier * 1.5;
                    break;
                }
            }

            //Pretty self explanatory, basically run a chance on whether or not the crop will tick for this tick
            if (cropGrowthMultiplier < 1) {
                if (world.getRandom().nextDouble() < cropGrowthMultiplier) {
                    block1.randomTick(self, world, pos, world.getRandom());
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
                        block1 = self.getBlock();
                    }

                    block1.randomTick(self, world, pos, world.getRandom());
                }
            }
        }
    }

    public void updatePacket(List<ServerPlayerEntity> players) {
//        NetworkHandler.sendToAllPlayers(players, new SeasonContextConstructingPacket(this));
        NetworkHandler.sendToAllPlayers(players, new RefreshRenderersPacket());
    }

    private void tickSeasonTime(World world) {
        if (world instanceof ServerWorld) {
            if (!this.tickSeasonTimeWhenNoPlayersOnline && world.getPlayers().isEmpty()) {
                return;
            }
        }

        if (world.getGameRules().getBoolean(BetterWeatherGameRules.DO_SEASON_CYCLE)) {
            this.currentYearTime = currentYearTime > this.yearLength ? 0 : (currentYearTime + 1);
            this.currentSeason = this.seasons.get(Season.getSeasonFromTime(this.currentYearTime, this.yearLength)).setPhaseForTime(this.currentYearTime, this.yearLength);

            if (world.getWorldInfo().getGameTime() % 50 == 0) {
                save(world);
            }
        }
        if (world instanceof ServerWorld) {
            if (world.getWorldInfo().getGameTime() % 3 == 0) { // Update season time every 3 ticks
                NetworkHandler.sendToAllPlayers(((ServerWorld) world).getPlayers(), new SeasonTimePacket(this.currentYearTime));
            }
        }
    }

    private void save(World world) {
        SeasonSavedData.get(world).setCurrentYearTime(this.currentYearTime);
        SeasonSavedData.get(world).setYearLength(this.yearLength);
    }

    /**********Configs**********/

    public SeasonConfigHolder handleConfig(boolean isClient) {
        createConfig();
        if (!seasonConfigFile.exists()) {
            BetterWeather.LOGGER.error(seasonConfigFile.toString() + " does not exist and therefore cannot be read, using defaults...");
            return SeasonConfigHolder.DEFAULT_CONFIG_HOLDER;
        }

        SeasonConfigHolder configHolder = read(isClient);
        fillSubSeasonOverrideStorageAndSetCropTags(isClient);
        return configHolder;
    }

    private void createConfig() {
        CommentedConfig readConfig = this.seasonConfigFile.exists() ? CommentedFileConfig.builder(this.seasonConfigFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
        if (readConfig instanceof CommentedFileConfig) {
            ((CommentedFileConfig) readConfig).load();
        }
        CommentedConfig encodedConfig = (CommentedConfig) SeasonConfigHolder.CODEC.encodeStart(CONFIG_OPS, SeasonConfigHolder.DEFAULT_CONFIG_HOLDER).result().get();

        try {
            Files.createDirectories(seasonConfigFile.toPath().getParent());
            new TomlWriter().write(seasonConfigFile.exists() ? TomlCommentedConfigOps.recursivelyUpdateAndSortConfig(readConfig, encodedConfig) : encodedConfig, this.seasonConfigFile, WritingMode.REPLACE);
        } catch (IOException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
    }

    private SeasonConfigHolder read(boolean isClient) {
        try (Reader reader = new FileReader(seasonConfigFile)) {
            Optional<SeasonConfigHolder> configHolder = SeasonConfigHolder.CODEC.parse(CONFIG_OPS, new TomlParser().parse(reader)).resultOrPartial(BetterWeather.LOGGER::error);

            if (!isClient) {
                if (configHolder.isPresent()) {
                    this.seasons.putAll(configHolder.get().getSeasonKeySeasonMap());
                    this.yearLength = configHolder.get().getSeasonCycleLength();
                } else {
                    this.seasons.putAll(SeasonConfigHolder.DEFAULT_CONFIG_HOLDER.getSeasonKeySeasonMap());
                    this.yearLength = SeasonConfigHolder.DEFAULT_CONFIG_HOLDER.getSeasonCycleLength();
                }
            } else {
                if (configHolder.isPresent()) {
                    for (Map.Entry<Key, BWSeason> entry : configHolder.get().getSeasonKeySeasonMap().entrySet()) {
                        Key key = entry.getKey();
                        BWSeason season = entry.getValue();
                        for (Phase phase : Phase.values()) {
                            this.seasons.get(key).getSettingsForPhase(phase).setClient(season.getSettingsForPhase(phase).getClientSettings()); //Only update client settings on the client.
                        }
                    }
                }
            }
            return configHolder.orElse(SeasonConfigHolder.DEFAULT_CONFIG_HOLDER);
        } catch (IOException e) {
            BetterWeather.LOGGER.error(e.toString());
        }
        return SeasonConfigHolder.DEFAULT_CONFIG_HOLDER; // We should never hit this ever.
    }

    private void fillSubSeasonOverrideStorageAndSetCropTags(boolean isClient) {
        for (Map.Entry<Season.Key, BWSeason> seasonKeySeasonEntry : this.seasons.entrySet()) {
            Key seasonKey = seasonKeySeasonEntry.getKey();
            seasonKeySeasonEntry.getValue().setSeasonKey(seasonKey);
            Map<Season.Phase, BWSubseasonSettings> phaseSettings = seasonKeySeasonEntry.getValue().getPhaseSettings();
            for (Map.Entry<Season.Phase, BWSubseasonSettings> phaseSubSeasonSettingsEntry : phaseSettings.entrySet()) {
                String mapKey = seasonKey + "-" + phaseSubSeasonSettingsEntry.getKey();
                if (!isClient) {
                    String worldKey = worldID.toString().replace(":", ".");
                    ITag.INamedTag<Block> unenhancedCrops = BWSeason.UNAFFECTED_CROPS.get(mapKey);
                    phaseSubSeasonSettingsEntry.getValue().setCropTags(
                            BWSeason.AFFECTED_CROPS.get(mapKey).getAllElements().isEmpty() ? BWSeason.AFFECTED_CROPS.get(worldKey).getAllElements().isEmpty() ? GLOBAL_AFFECTED_CROPS : BWSeason.AFFECTED_CROPS.get(worldKey) : GLOBAL_AFFECTED_CROPS,
                            unenhancedCrops.getAllElements().isEmpty() ? BWSeason.UNAFFECTED_CROPS.get(worldKey).getAllElements().isEmpty() ? unenhancedCrops : unenhancedCrops : unenhancedCrops);
                }
                BiomeOverrideJsonHandler.handleOverrideJsonConfigs(this.seasonOverridesPath.resolve(seasonKeySeasonEntry.getKey().toString() + "-" + phaseSubSeasonSettingsEntry.getKey() + ".json"), seasonKeySeasonEntry.getKey() == Season.Key.WINTER ? BWSubseasonSettings.WINTER_OVERRIDE : new IdentityHashMap<>(), phaseSubSeasonSettingsEntry.getValue(), this.biomeRegistry, isClient);
            }
        }
    }

    public BWSeason getCurrentSeason() {
        return currentSeason;
    }

    public BWSubseasonSettings getCurrentSubSeasonSettings() {
        return this.currentSeason.getCurrentSettings();
    }

    @Override
    public Key getKey() {
        return this.currentSeason.getSeasonKey();
    }

    @Override
    public int getYearLength() {
        return yearLength;
    }

    @Override
    public int getCurrentYearTime() {
        return currentYearTime;
    }

    @Override
    public Phase getPhase() {
        return this.currentSeason.getCurrentPhase();
    }

    @Override
    public SubseasonSettings getSettings() {
        return this.currentSeason.getCurrentSettings();
    }

    @Override
    public IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> getCropFavoriteBiomeBonuses() {
        return this.cropToFavoriteBiomes;
    }

    public void setCurrentYearTime(int currentYearTime) {
        this.currentYearTime = currentYearTime;
    }

    public IdentityHashMap<Key, BWSeason> getSeasons() {
        return seasons;
    }
}

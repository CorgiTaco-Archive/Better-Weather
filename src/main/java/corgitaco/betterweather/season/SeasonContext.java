package corgitaco.betterweather.season;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.season.SubseasonSettings;
import corgitaco.betterweather.config.season.SeasonConfigHolder;
import corgitaco.betterweather.config.season.overrides.BiomeOverrideJsonHandler;
import corgitaco.betterweather.data.network.NetworkHandler;
import corgitaco.betterweather.data.network.packet.season.SeasonPacket;
import corgitaco.betterweather.data.network.packet.season.SeasonTimePacket;
import corgitaco.betterweather.data.network.packet.util.RefreshRenderersPacket;
import corgitaco.betterweather.data.storage.SeasonSavedData;
import corgitaco.betterweather.helpers.BiomeUpdate;
import corgitaco.betterweather.server.BetterWeatherGameRules;
import corgitaco.betterweather.util.BetterWeatherUtil;
import corgitaco.betterweather.util.TomlCommentedConfigOps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
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
import java.util.stream.Collectors;

public class SeasonContext implements Season {
    public static final String CONFIG_NAME = "season-settings.toml";

    public static final Codec<SeasonContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.INT.fieldOf("currentYearTime").forGetter((seasonContext) -> {
            return seasonContext.currentYearTime;
        }), Codec.INT.fieldOf("yearLength").forGetter((seasonContext) -> {
            return seasonContext.yearLength;
        }), ResourceLocation.CODEC.fieldOf("worldID").forGetter((seasonContext) -> {
            return seasonContext.worldID;
        }), Codec.simpleMap(Season.Key.CODEC, BWSeason.PACKET_CODEC, IStringSerializable.createKeyable(Season.Key.values())).fieldOf("seasons").forGetter((seasonContext) -> {
            return seasonContext.seasons;
        })).apply(builder, (currentYearTime, yearLength, worldID, seasonMap) -> new SeasonContext(currentYearTime, yearLength, worldID, new IdentityHashMap<>(seasonMap)));
    });

    public static final TomlCommentedConfigOps CONFIG_OPS = new TomlCommentedConfigOps(Util.make(new HashMap<>(), (map) -> {
        map.put("yearLength", "Represents this world's year length.");
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
    private final Path seasonOverridesPath;
    private final IdentityHashMap<Season.Key, BWSeason> seasons = new IdentityHashMap<>();

    private BWSeason currentSeason;
    private int currentYearTime;
    private int yearLength;

    //Packet Constructor
    public SeasonContext(int currentYearTime, int yearLength, ResourceLocation worldID, IdentityHashMap<Season.Key, BWSeason> seasons) {
        this(currentYearTime, yearLength, worldID, null, seasons);
    }

    //Server world constructor
    public SeasonContext(SeasonSavedData seasonData, RegistryKey<World> worldID, Registry<Biome> biomeRegistry) {
        this(seasonData.getCurrentYearTime(), seasonData.getYearLength(), worldID.getLocation(), biomeRegistry, null);
    }

    public SeasonContext(int currentYearTime, int yearLength, ResourceLocation worldID, @Nullable Registry<Biome> biomeRegistry, @Nullable IdentityHashMap<Season.Key, BWSeason> seasons) {
        this.currentYearTime = currentYearTime;
        this.yearLength = yearLength;
        this.worldID = worldID;
        this.biomeRegistry = biomeRegistry;
        Path seasonsFolderPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("seasons");
        this.seasonConfigFile = seasonsFolderPath.resolve(CONFIG_NAME).toFile();
        this.seasonOverridesPath = seasonsFolderPath.resolve("overrides");

        boolean isClient = seasons != null;
        boolean isPacket = biomeRegistry == null;

        if (isClient) {
            this.seasons.putAll(seasons);
        }
        if (!isPacket) {
            this.handleConfig(isClient);
            this.currentSeason = this.seasons.get(Season.getSeasonFromTime(currentYearTime, yearLength));
            this.currentSeason.setPhaseForTime(this.currentYearTime, this.yearLength);
        }
    }

    public void setSeason(List<ServerPlayerEntity> players, Season.Key newSeason, Season.Phase phase) {
        this.currentYearTime = Season.getSeasonAndPhaseStartTime(newSeason, phase, this.yearLength);
        this.currentSeason = seasons.get(newSeason);
        this.currentSeason.setPhaseForTime(currentYearTime, yearLength);
        this.updatePacket(players);
    }

    public void tick(World world) {
        BWSeason prevSeason = this.currentSeason;
        Season.Phase prevPhase = this.currentSeason.getCurrentPhase();
        BWSubseasonSettings prevSettings = prevSeason.getSettingsForPhase(prevPhase);
        this.tickSeasonTime(world);

        boolean changedSeasonFlag = prevSeason != this.currentSeason;
        boolean changedPhaseFlag = prevPhase != this.currentSeason.getCurrentPhase();

        if (changedSeasonFlag || changedPhaseFlag) {
            ((BiomeUpdate) world).updateBiomeData();
            if (!world.isRemote) {
                updateWeatherMultiplier(world, prevSettings.getWeatherEventChanceMultiplier(), this.currentSeason.getCurrentSettings().getWeatherEventChanceMultiplier());
                updatePacket(((ServerWorld) world).getPlayers());
            }
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
        if (BlockTags.CROPS.contains(block) || BlockTags.BEE_GROWABLES.contains(block) || BlockTags.SAPLINGS.contains(block)) {
            Block block1 = block;
            //Collect the crop multiplier for the given subseason.
            double cropGrowthMultiplier = getCurrentSubSeasonSettings().getCropGrowthMultiplier(world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(world.getBiome(pos)).get(), block1);
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
        NetworkHandler.sendToAllPlayers(players, new SeasonPacket(this));
        NetworkHandler.sendToAllPlayers(players, new RefreshRenderersPacket());
    }

    private void tickSeasonTime(World world) {
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

    public void handleConfig(boolean isClient) {
        createConfig();
        if (seasonConfigFile.exists()) {
            read(isClient);
        }

        fillSubSeasonOverrideStorage(isClient);
    }

    private void createConfig() {
        CommentedConfig readConfig = this.seasonConfigFile.exists() ? CommentedFileConfig.builder(this.seasonConfigFile).sync().autosave().writingMode(WritingMode.REPLACE).build() : CommentedConfig.inMemory();
        if (readConfig instanceof CommentedFileConfig) {
            ((CommentedFileConfig) readConfig).load();
        }
        CommentedConfig encodedConfig = (CommentedConfig) SeasonConfigHolder.CODEC.encodeStart(CONFIG_OPS, SeasonConfigHolder.DEFAULT_CONFIG_HOLDER).result().get();

        try {
            Files.createDirectories(seasonConfigFile.toPath().getParent());
            new TomlWriter().write(seasonConfigFile.exists() ? recursivelyUpdateAndSortConfig(readConfig, encodedConfig) : encodedConfig, this.seasonConfigFile, WritingMode.REPLACE);
        } catch (IOException e) {

        }
    }

    private CommentedConfig recursivelyUpdateAndSortConfig(CommentedConfig readConfig, CommentedConfig encodedConfig) {
        CommentedConfig newConfig = organizeConfig(readConfig);

        encodedConfig.valueMap().entrySet().stream().sorted(Comparator.comparing(Objects::toString)).forEachOrdered((entry) -> {
            Object object = entry.getValue();
            String key = entry.getKey();

            if (object instanceof CommentedConfig) {
                boolean hasConfig;

                //Requires a try catch due to Night Config allowing .contains() to throw a NPE.
                try {
                    hasConfig = !newConfig.contains(key);
                } catch (NullPointerException e) {
                    hasConfig = false;
                }

                if (!hasConfig) {
                    object = recursivelyUpdateAndSortConfig(newConfig.set(key, object), (CommentedConfig) object);
                } else {
                    object = recursivelyUpdateAndSortConfig(newConfig.get(key), (CommentedConfig) object);
                }
                newConfig.set(key, object);
            }

            newConfig.add(key, object);

            if (!newConfig.containsComment(key) || !newConfig.getComment(key).equals(encodedConfig.getComment(key))) {
                newConfig.setComment(key, encodedConfig.getComment(key));
            }
        });

        Set<String> keysToRemove = new HashSet<>();
        newConfig.valueMap().forEach((key, object) -> {
            if (!encodedConfig.contains(key)) {
                keysToRemove.add(key);
            }
        });

        keysToRemove.forEach(key -> {
            newConfig.removeComment(key);
            newConfig.remove(key);
        });
        return newConfig;
    }


    public CommentedConfig organizeConfig(CommentedConfig config) {
        CommentedConfig newConfig = CommentedConfig.of(Config.getDefaultMapCreator(false, true), TomlFormat.instance());

        List<Map.Entry<String, Object>> organizedCollection = config.valueMap().entrySet().stream().sorted(Comparator.comparing(Objects::toString)).collect(Collectors.toList());
        organizedCollection.forEach((stringObjectEntry -> {
            newConfig.set(stringObjectEntry.getKey(), stringObjectEntry.getValue());
        }));

        newConfig.commentMap().putAll(config.commentMap());
        return newConfig;
    }

    private void read(boolean isClient) {
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
        } catch (IOException e) {

        }
    }

    private void fillSubSeasonOverrideStorage(boolean isClient) {
        for (Map.Entry<Season.Key, BWSeason> seasonKeySeasonEntry : this.seasons.entrySet()) {
            Key key = seasonKeySeasonEntry.getKey();
            seasonKeySeasonEntry.getValue().setSeasonKey(key);
            Map<Season.Phase, BWSubseasonSettings> phaseSettings = seasonKeySeasonEntry.getValue().getPhaseSettings();
            for (Map.Entry<Season.Phase, BWSubseasonSettings> phaseSubSeasonSettingsEntry : phaseSettings.entrySet()) {
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

    public void setCurrentYearTime(int currentYearTime) {
        this.currentYearTime = currentYearTime;
    }

    public void setYearLength(int yearLength) {
        this.yearLength = yearLength;
    }

    public IdentityHashMap<Key, BWSeason> getSeasons() {
        return seasons;
    }
}

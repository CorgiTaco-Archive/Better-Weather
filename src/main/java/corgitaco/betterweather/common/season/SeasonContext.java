package corgitaco.betterweather.common.season;

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
import corgitaco.betterweather.server.BetterWeatherGameRules;
import corgitaco.betterweather.util.BetterWeatherUtil;
import corgitaco.betterweather.util.BiomeUpdate;
import corgitaco.betterweather.util.CodecUtil;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
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
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import static corgitaco.betterweather.common.season.config.SeasonConfigSerializers.handleConfig;

@SuppressWarnings("deprecation")
public class SeasonContext implements Climate {
    public static final String CONFIG_NAME = "season-settings.toml";

    public static final Codec<SeasonContext> PACKET_CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(Codec.INT.fieldOf("yearLength").forGetter((seasonContext) -> {
            return seasonContext.yearLength;
        }), Codec.INT.fieldOf("yearTime").forGetter((seasonContext) -> {
            return seasonContext.yearTime;
        }), Codec.unboundedMap(CodecUtil.BLOCK_CODEC, Codec.unboundedMap(CodecUtil.BIOME_CODEC, Codec.DOUBLE)).fieldOf("cropFavoriteBiomes").forGetter((seasonContext) -> {
            return seasonContext.cropToFavoriteBiomes;
        }), Codec.simpleMap(Season.Key.CODEC, BWSeason.PACKET_CODEC, IStringSerializable.keys(Season.Key.values())).fieldOf("seasons").forGetter((seasonContext) -> {
            return seasonContext.seasons;
        })).apply(builder, SeasonContext::new);
    });

    public static final IdentityHashMap<Block, Object2DoubleArrayMap<Object>> BLOCK_TO_FAVORITE_BIOMES_DEFAULT = Util.make(new IdentityHashMap<>(), (map) -> {
        map.put(Blocks.MELON_STEM, Util.make(new Object2DoubleArrayMap<>(), (favoriteBiomes) -> {
            favoriteBiomes.put(Biome.Category.JUNGLE, 0.4);
        }));

        map.put(Blocks.SWEET_BERRY_BUSH, Util.make(new Object2DoubleArrayMap<>(), (favoriteBiomes) -> {
            favoriteBiomes.put(Biome.Category.TAIGA, 0.4);
        }));
    });

    private final Map<Season.Key, BWSeason> seasons = new EnumMap<>(Season.Key.class);
    private final Map<Block, Map<RegistryKey<Biome>, Double>> cropToFavoriteBiomes = new Object2ObjectOpenHashMap<>();
    private boolean tickSeasonTimeWhenNoPlayersOnline = true;

    private BWSeason currentSeason;
    private int yearLength;
    private int yearTime;

    //Packet Constructor
    public SeasonContext(int yearLength, int yearTime, Map<Block, Map<RegistryKey<Biome>, Double>> cropToFavoriteBiomes, Map<Season.Key, BWSeason> seasons) {
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
        SeasonConfigHolder seasonConfigHolder = handleConfig(world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), worldID, seasonConfigFile, seasonOverridesPath, false, this.seasons);
        this.yearLength = seasonConfigHolder.getYearLength();
        this.tickSeasonTimeWhenNoPlayersOnline = seasonConfigHolder.isTickSeasonTimeWhenNoPlayersOnline();
        this.cropToFavoriteBiomes.putAll(CropFavoriteBiomesConfigHandler.handle(seasonsPath.resolve("crop-favorite-biomes.json"), BLOCK_TO_FAVORITE_BIOMES_DEFAULT, world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY)));
        this.currentSeason = this.seasons.get(Season.getSeasonFromTime(world.getDayTime(), this.yearLength));
        this.currentSeason.setPhaseForTime(this.yearTime, this.yearLength);
    }

    //Client Constructor
    public SeasonContext(@Nullable ClientWorld world, int yearLength, int yearTime, Map<Block, Map<RegistryKey<Biome>, Double>> cropToFavoriteBiomes, Map<Season.Key, BWSeason> seasons) {
        this.yearLength = yearLength;
        this.yearTime = yearTime;
        if (world != null) {
            ResourceLocation worldID = world.dimension().location();
            Path seasonsPath = BetterWeather.CONFIG_PATH.resolve(worldID.getNamespace()).resolve(worldID.getPath()).resolve("seasons");
            File seasonConfigFile = seasonsPath.resolve(CONFIG_NAME).toFile();
            Path seasonOverridesPath = seasonsPath.resolve("overrides");
            this.seasons.putAll(seasons);
            this.cropToFavoriteBiomes.putAll(cropToFavoriteBiomes);
            SeasonConfigHolder configHolder = handleConfig(world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY), worldID, seasonConfigFile, seasonOverridesPath, true, this.seasons);
            this.tickSeasonTimeWhenNoPlayersOnline = configHolder.isTickSeasonTimeWhenNoPlayersOnline();
            this.currentSeason = this.seasons.get(Season.getSeasonFromTime(this.yearTime, this.yearLength));
            this.currentSeason.setPhaseForTime(this.yearTime, this.yearLength);
        }
    }

    public void setSeason(World world, Season.Key newSeason, Season.Phase phase) {
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
            BWSubseasonSettings preTimeUpdateSettings = this.getCurrentSeason().getCurrentSettings();
            if (newYearTime > 0) {
                this.yearTime = newYearTime - 1;
            }
            if (!world.isClientSide) {
                if (!this.tickSeasonTimeWhenNoPlayersOnline && world.players().isEmpty()) {
                    return this.yearTime;
                }
            }

            this.currentSeason = this.seasons.get(Season.getSeasonFromTime(this.yearTime > this.yearLength ? this.yearTime = 0 : this.yearTime++, this.yearLength)).setPhaseForTime(this.yearTime, this.yearLength);

            if (world.getLevelData().getGameTime() % 50 == 0 || seasonChange(world, preTimeUpdateSettings)) {
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

    public Map<Block, Map<RegistryKey<Biome>, Double>> getCropFavoriteBiomeBonuses() {
        return this.cropToFavoriteBiomes;
    }

    @Override
    public int getYearLength() {
        return yearLength;
    }

    public Map<Season.Key, BWSeason> getSeasons() {
        return seasons;
    }
}

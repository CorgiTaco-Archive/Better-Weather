package corgitaco.betterweather.season;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.api.season.SubseasonSettings;
import corgitaco.betterweather.config.season.SeasonConfigHolder;
import corgitaco.betterweather.config.season.overrides.BiomeOverrideJsonHandler;
import corgitaco.betterweather.datastorage.SeasonSavedData;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.SeasonPacket;
import corgitaco.betterweather.datastorage.network.packet.util.RefreshRenderersPacket;
import corgitaco.betterweather.helpers.IBiomeUpdate;
import corgitaco.betterweather.server.BetterWeatherGameRules;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SeasonContext implements Season {
    public static final String CONFIG_NAME = "season-settings.json";

    private BWSeason currentSeason;
    private int currentYearTime;
    private int yearLength;
    private final Registry<Biome> biomeRegistry;

    private final File seasonConfigFile;
    private final Path seasonOverridesPath;

    private IdentityHashMap<Season.Key, BWSeason> seasons;

    public SeasonContext(SeasonSavedData seasonData, RegistryKey<World> worldKey, Registry<Biome> biomeRegistry) {
        this.currentYearTime = seasonData.getCurrentYearTime();
        this.yearLength = seasonData.getYearLength();
        this.biomeRegistry = biomeRegistry;

        ResourceLocation dimensionLocation = worldKey.getLocation();
        Path seasonsFolderPath = BetterWeather.CONFIG_PATH.resolve(dimensionLocation.getNamespace()).resolve(dimensionLocation.getPath()).resolve("seasons");
        this.seasonConfigFile = seasonsFolderPath.resolve(CONFIG_NAME).toFile();
        this.seasonOverridesPath = seasonsFolderPath.resolve("overrides");

        this.handleConfig();
        this.currentSeason = seasons.get(Season.getSeasonFromTime(currentYearTime, yearLength)).setPhaseForTime(this.currentYearTime, this.yearLength);
    }

    public void handleConfig() {
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        if (!seasonConfigFile.exists()) {
            create(gson);
        }
        if (seasonConfigFile.exists()) {
            read();
        }

        fillSubSeasonOverrideStorage();
    }

    private void fillSubSeasonOverrideStorage() {
        for (Map.Entry<Season.Key, BWSeason> seasonKeySeasonEntry : this.seasons.entrySet()) {
            IdentityHashMap<Season.Phase, BWSubseasonSettings> phaseSettings = seasonKeySeasonEntry.getValue().getPhaseSettings();
            for (Map.Entry<Season.Phase, BWSubseasonSettings> phaseSubSeasonSettingsEntry : phaseSettings.entrySet()) {
                BiomeOverrideJsonHandler.handleOverrideJsonConfigs(this.seasonOverridesPath.resolve(seasonKeySeasonEntry.getKey().toString() + "-" + phaseSubSeasonSettingsEntry.getKey() + ".json"), seasonKeySeasonEntry.getKey() == Season.Key.WINTER ? BWSubseasonSettings.WINTER_OVERRIDE : new IdentityHashMap<>(), phaseSubSeasonSettingsEntry.getValue(), this.biomeRegistry);
            }
        }
    }

    public void setSeason(List<ServerPlayerEntity> players, Season.Key newSeason, Season.Phase phase) {
        this.currentYearTime = Season.getSeasonAndPhaseStartTime(newSeason, phase, this.yearLength);
        this.currentSeason = seasons.get(newSeason);
        this.currentSeason.setPhaseForTime(currentYearTime, yearLength);
        this.updatePacket(players);
    }

    private void create(Gson gson) {
        String toJson = gson.toJson(SeasonConfigHolder.CODEC.encodeStart(JsonOps.INSTANCE, SeasonConfigHolder.DEFAULT_CONFIG_HOLDER).result().get());

        try {
            Files.createDirectories(seasonConfigFile.toPath().getParent());
            Files.write(seasonConfigFile.toPath(), toJson.getBytes());
        } catch (IOException e) {

        }
    }

    private void read() {
        try (Reader reader = new FileReader(seasonConfigFile)) {
            JsonObject jsonObject = new JsonParser().parse(reader).getAsJsonObject();
            Optional<SeasonConfigHolder> configHolder = SeasonConfigHolder.CODEC.parse(JsonOps.INSTANCE, jsonObject).resultOrPartial(BetterWeather.LOGGER::error);
            if (configHolder.isPresent()) {
                this.seasons = configHolder.get().getSeasonKeySeasonMap();
                this.yearLength = configHolder.get().getSeasonCycleLength();
            } else {
                this.seasons = SeasonConfigHolder.DEFAULT_CONFIG_HOLDER.getSeasonKeySeasonMap();
                this.yearLength = SeasonConfigHolder.DEFAULT_CONFIG_HOLDER.getSeasonCycleLength();
            }
        } catch (IOException e) {

        }
    }

    public void tick(World world) {
        BWSeason prevSeason = this.currentSeason;
        this.tickSeasonTime(world);

        Season.Phase prevPhase = this.currentSeason.getCurrentPhase();
        this.currentSeason.tick(currentYearTime, yearLength);

        if (prevSeason != this.currentSeason || prevPhase != this.currentSeason.getCurrentPhase()) {
            ((IBiomeUpdate) world).updateBiomeData(this.getCurrentSubSeasonSettings());
            if (!world.isRemote) {
                updatePacket(((ServerWorld) world).getPlayers());

            }
        }
    }

    public void tickCrops(ServerWorld world, BlockPos posIn, Block block, BlockState self, CallbackInfo ci) {
        BWSubseasonSettings subSeason = this.getCurrentSubSeasonSettings();
        if (subSeason.getBiomeToOverrideStorage().isEmpty() && subSeason.getCropToMultiplierStorage().isEmpty()) {
            if (BlockTags.CROPS.contains(block) || BlockTags.BEE_GROWABLES.contains(block) || BlockTags.SAPLINGS.contains(block)) {
                cropTicker(world, posIn, block, subSeason, self, ci);
            }
        } else {
            if (BlockTags.CROPS.contains(block) || BlockTags.BEE_GROWABLES.contains(block) || BlockTags.SAPLINGS.contains(block)) {
                cropTicker(world, posIn, block, subSeason, self, ci);
            }
        }
    }

    private static void cropTicker(ServerWorld world, BlockPos posIn, Block block, BWSubseasonSettings subSeason, BlockState self, CallbackInfo ci) {
        //Collect the crop multiplier for the given subseason.
        double cropGrowthMultiplier = subSeason.getCropGrowthMultiplier(world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(world.getBiome(posIn)).get(), block);
        if (cropGrowthMultiplier == 1)
            return;

        //Pretty self explanatory, basically run a chance on whether or not the crop will tick for this tick
        if (cropGrowthMultiplier < 1) {
            if (world.getRandom().nextDouble() < cropGrowthMultiplier) {
                block.randomTick(self, world, posIn, world.getRandom());
            } else
                ci.cancel();
        }
        //Here we gather a random number of ticks that this block will tick for this given tick.
        //We do a random.nextDouble() to determine if we get the ceil or floor value for the given crop growth multiplier.
        else if (cropGrowthMultiplier > 1) {
            int numberOfTicks = world.getRandom().nextInt((world.getRandom().nextDouble() + (cropGrowthMultiplier - 1) < cropGrowthMultiplier) ? (int) Math.ceil(cropGrowthMultiplier) : (int) cropGrowthMultiplier) + 1;
            for (int tick = 0; tick < numberOfTicks; tick++) {
                if (tick > 0) {
                    self = world.getBlockState(posIn);
                    block = self.getBlock();
                }

                block.randomTick(self, world, posIn, world.getRandom());
            }
        }
    }

    public void updatePacket(List<ServerPlayerEntity> players) {
        for (ServerPlayerEntity player : players) {
            NetworkHandler.sendToClient(player, new SeasonPacket(this.currentYearTime, this.yearLength));
            NetworkHandler.sendToClient(player, new RefreshRenderersPacket());
        }
    }

    private void tickSeasonTime(World world) {
        if (world.getGameRules().getBoolean(BetterWeatherGameRules.DO_SEASON_CYCLE)) {
            this.currentYearTime = currentYearTime > this.yearLength ? 0 : (currentYearTime + 1);
            this.currentSeason = this.seasons.get(Season.getSeasonFromTime(this.currentYearTime, this.yearLength)).setPhaseForTime(this.currentYearTime, this.yearLength);

            if (world.getWorldInfo().getGameTime() % 50 == 0) {
                save(world);
            }
        }
    }

    private void save(World world) {
        SeasonSavedData.get(world).setCurrentYearTime(this.currentYearTime);
        SeasonSavedData.get(world).setYearLength(this.yearLength);
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

    @OnlyIn(Dist.CLIENT)
    public void setCurrentYearTime(int currentYearTime) {
        this.currentYearTime = currentYearTime;
    }

    @OnlyIn(Dist.CLIENT)
    public void setYearLength(int yearLength) {
        this.yearLength = yearLength;
    }
}

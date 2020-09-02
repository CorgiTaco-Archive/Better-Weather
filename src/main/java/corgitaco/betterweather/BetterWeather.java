package corgitaco.betterweather;

import com.google.common.collect.Lists;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.datastorage.BetterWeatherData;
import corgitaco.betterweather.server.BetterWeatherCommand;
import corgitaco.betterweather.weatherevents.AcidRain;
import corgitaco.betterweather.weatherevents.Blizzard;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BetterWeather implements ModInitializer {
    public static Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "betterweather";

    static boolean damageAnimals = false;
    static boolean damageMonsters = false;
    static boolean damagePlayer = false;

    public static boolean destroyGrass = false;
    public static boolean destroyLeaves = false;
    public static boolean destroyCrops = false;
    public static boolean destroyPlants = false;

    public static List<Block> blocksToNotDestroyList = new ArrayList<>();

    public void configReader() {
//        GlobalEntityTypeAttributes.put(BWEntityRegistry.TORNADO, TornadoEntity.setCustomAttributes().create());
        String entityTypes = BetterWeatherConfig.entityTypesToDamage;
        String removeSpaces = entityTypes.trim().toLowerCase().replace(" ", "");
        String[] entityList = removeSpaces.split(",");

        for (String s : entityList) {
            if (s.equalsIgnoreCase("animal") && !damageAnimals)
                damageAnimals = true;
            if (s.equalsIgnoreCase("monster") && !damageMonsters)
                damageMonsters = true;
            if (s.equalsIgnoreCase("player") && !damagePlayer)
                damagePlayer = true;
        }

        String allowedBlockTypesToDestroy = BetterWeatherConfig.allowedBlocksToDestroy;
        String removeBlockTypeSpaces = allowedBlockTypesToDestroy.trim().toLowerCase().replace(" ", "");
        String[] blockTypeToDestroyList = removeBlockTypeSpaces.split(",");

        for (String s : blockTypeToDestroyList) {
            if (s.equalsIgnoreCase("grass") && !destroyGrass)
                destroyGrass = true;
            if (s.equalsIgnoreCase("leaves") && !destroyLeaves)
                destroyLeaves = true;
            if (s.equalsIgnoreCase("crops") && !destroyCrops)
                destroyCrops = true;
            if (s.equalsIgnoreCase("plants") && !destroyCrops)
                destroyPlants = true;
        }

        String blocksToNotDestroy = BetterWeatherConfig.blocksToNotDestroy;
        String removeBlocksToNotDestroySpaces = blocksToNotDestroy.trim().toLowerCase().replace(" ", "");
        String[] blocksToNotDestroyList = removeBlocksToNotDestroySpaces.split(",");
        for (String s : blocksToNotDestroyList) {
            net.minecraft.world.level.block.Block block = Registry.BLOCK.get(new ResourceLocation(s));
            if (block != null)
                BetterWeather.blocksToNotDestroyList.add(block);
            else
                LOGGER.error("A block registry name you added to the \"BlocksToNotDestroy\" list was incorrect, you put: " + s + "\n Please fix it or this block will be destroyed.");
        }
    }

//    public void clientSetup(FMLClientSetupEvent event) {
////        RenderingRegistry.registerEntityRenderingHandler(BWEntityRegistry.TORNADO, TornadoRenderer::new);
//
//    }

    public static int dataCache = 0;

    @Override
    public void onInitialize() {
        configReader();
        ServerTickEvents.END_WORLD_TICK.register(event -> BetterWeatherEvents.worldTick(event.getLevel()));
        BetterWeatherEvents.commandRegisterEvent();
    }

    public static class BetterWeatherEvents {
        public static BetterWeatherData weatherData = null;

        public static void worldTick(Level world) {
            setWeatherData(world);
            ServerLevel serverWorld = (ServerLevel) world;
            int tickSpeed = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            long worldTime = world.getLevelData().getGameTime();

            //Rolls a random chance for acid rain once every 5000 ticks and will not run when raining to avoid disco colored rain.
            if (worldTime == 100 || worldTime % 24000 == 0 && !world.getLevelData().isRaining()) {
                Random random = world.random;
                weatherData.setAcidRain(random.nextFloat() < BetterWeatherConfig.acidRainChance);
                weatherData.setBlizzard(false);
            }
            if (worldTime == 100 || worldTime % 24000 == 0 && !world.getLevelData().isRaining()) {
                Random random = world.random;
                weatherData.setBlizzard(random.nextFloat() + 0.05 < BetterWeatherConfig.blizzardChance);
                weatherData.setAcidRain(false);
            }

            if (world.getLevelData().isRaining()) {
                if (dataCache == 0)
                    dataCache++;
            } else {
                if (dataCache != 0) {
                    if (weatherData.isBlizzard())
                        weatherData.setBlizzard(false);
                    if (weatherData.isAcidRain())
                        weatherData.setAcidRain(false);
                }
            }


            List<ChunkHolder> list = Lists.newArrayList((serverWorld.getChunkSource()).chunkMap.getChunks());
            list.forEach(chunkHolder -> {
                Optional<LevelChunk> optional = chunkHolder.getTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
                //Gets chunks to tick
                if (optional.isPresent()) {
                    Optional<LevelChunk> optional1 = chunkHolder.getEntityTickingChunkFuture().getNow(ChunkHolder.UNLOADED_LEVEL_CHUNK).left();
                    if (optional1.isPresent()) {
                        LevelChunk chunk = optional1.get();
                        Blizzard.blizzardEvent(chunk, serverWorld, tickSpeed, worldTime);
                        if (BetterWeatherConfig.decaySnowAndIce)
                            Blizzard.doesIceAndSnowDecay(chunk, serverWorld, worldTime);
                        AcidRain.acidRainEvent(chunk, serverWorld, tickSpeed, worldTime);
                    }
                }
            });
        }

//    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
//        setWeatherData(event.player.world);
//    }

        public static void entityTickEvent(net.minecraft.world.entity.Entity entity) {
            if (entity.level != null)
                setWeatherData(entity.level);


            if (damageMonsters) {
                if (entity.getType().getCategory() == MobCategory.MONSTER) {
                    Level world = entity.level;
                    BlockPos entityPos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());

                    if (world.canSeeSky(entityPos) && BetterWeatherEvents.weatherData.isAcidRain() && world.getLevelData().isRaining() && world.getGameTime() % BetterWeatherConfig.hurtEntityTickSpeed == 0) {
                        entity.hurt(DamageSource.GENERIC, 0.5F);
                    }
                }
            }

            if (damageAnimals) {
                if (entity.getType().getCategory() == MobCategory.CREATURE || entity.getType().getCategory() == MobCategory.AMBIENT) {
                    Level world = entity.level;
                    BlockPos entityPos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());

                    if (world.canSeeSky(entityPos) && BetterWeatherEvents.weatherData.isAcidRain() && world.getLevelData().isRaining() && world.getGameTime() % BetterWeatherConfig.hurtEntityTickSpeed == 0) {
                        entity.hurt(DamageSource.GENERIC, (float) BetterWeatherConfig.hurtEntityDamage);
                    }
                }
            }

            if (damagePlayer) {
                if (entity instanceof ServerPlayer) {
                    Level world = entity.level;
                    BlockPos entityPos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());

                    if (world.canSeeSky(entityPos) && BetterWeatherEvents.weatherData.isAcidRain() && world.getLevelData().isRaining() && world.getGameTime() % BetterWeatherConfig.hurtEntityTickSpeed == 0) {
                        entity.hurt(DamageSource.GENERIC, 0.5F);
                    }
                }
            }
            Blizzard.blizzardEntityHandler(entity);
        }

        public static void commandRegisterEvent() {
            CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
                BetterWeatherCommand.register(dispatcher);
            });
            BetterWeather.LOGGER.info("BW: \"Server Starting\" Event Complete!");
        }

        public static void setWeatherData(LevelAccessor world) {
            if (BetterWeatherEvents.weatherData == null)
                BetterWeatherEvents.weatherData = BetterWeatherData.get(world);
        }
    }


    public enum WeatherType {
        BLIZZARD,
        HAIL,
        HEATWAVE,
        WINDSTORM,
        SANDSTORM,
        ACIDRAIN
    }
}

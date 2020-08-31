package corgitaco.betterweather;

import com.google.common.collect.Lists;
import com.sun.org.apache.xpath.internal.operations.Mod;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.config.BetterWeatherConfigClient;
import corgitaco.betterweather.datastorage.BetterWeatherData;
import corgitaco.betterweather.server.BetterWeatherCommand;
import corgitaco.betterweather.weatherevents.AcidRain;
import corgitaco.betterweather.weatherevents.Blizzard;
import jdk.nashorn.internal.ir.Block;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.LevelChunk;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.swing.text.html.parser.Entity;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class BetterWeather implements ModInitializer {
    public static Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "betterweather";

    public BetterWeather() {
    }

    static boolean damageAnimals = false;
    static boolean damageMonsters = false;
    static boolean damagePlayer = false;

    public static boolean destroyGrass = false;
    public static boolean destroyLeaves = false;
    public static boolean destroyCrops = false;
    public static boolean destroyPlants = false;

    public static List<Block> blocksToNotDestroyList = new ArrayList<>();

//    public void commonSetup(FMLCommonSetupEvent event) {
////        GlobalEntityTypeAttributes.put(BWEntityRegistry.TORNADO, TornadoEntity.setCustomAttributes().create());
//
//        BetterWeatherConfig.loadConfig(BetterWeatherConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.resolve(MOD_ID + "-common.toml"));
//        String entityTypes = BetterWeatherConfig.entityTypesToDamage;
//        String removeSpaces = entityTypes.trim().toLowerCase().replace(" ", "");
//        String[] entityList = removeSpaces.split(",");
//
//        for (String s : entityList) {
//            if (s.equalsIgnoreCase("animal") && !damageAnimals)
//                damageAnimals = true;
//            if (s.equalsIgnoreCase("monster") && !damageMonsters)
//                damageMonsters = true;
//            if (s.equalsIgnoreCase("player") && !damagePlayer)
//                damagePlayer = true;
//        }
//
//        String allowedBlockTypesToDestroy = BetterWeatherConfig.allowedBlocksToDestroy;
//        String removeBlockTypeSpaces = allowedBlockTypesToDestroy.trim().toLowerCase().replace(" ", "");
//        String[] blockTypeToDestroyList = removeBlockTypeSpaces.split(",");
//
//        for (String s : blockTypeToDestroyList) {
//            if (s.equalsIgnoreCase("grass") && !destroyGrass)
//                destroyGrass = true;
//            if (s.equalsIgnoreCase("leaves") && !destroyLeaves)
//                destroyLeaves = true;
//            if (s.equalsIgnoreCase("crops") && !destroyCrops)
//                destroyCrops = true;
//            if (s.equalsIgnoreCase("plants") && !destroyCrops)
//                destroyPlants = true;
//        }
//        ForgeRegistry<Block> blockRegistry = ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS);
//
//        String blocksToNotDestroy = BetterWeatherConfig.blocksToNotDestroy;
//        String removeBlocksToNotDestroySpaces = blocksToNotDestroy.trim().toLowerCase().replace(" ", "");
//        String[] blocksToNotDestroyList = removeBlocksToNotDestroySpaces.split(",");
//        for (String s : blocksToNotDestroyList) {
//            Block block = blockRegistry.getValue(new ResourceLocation(s));
//            if (block != null)
//                BetterWeather.blocksToNotDestroyList.add(block);
//            else
//                LOGGER.error("A block registry name you added to the \"BlocksToNotDestroy\" list was incorrect, you put: " + s + "\n Please fix it or this block will be destroyed.");
//        }
//    }
//
//    public void clientSetup(FMLClientSetupEvent event) {
////        RenderingRegistry.registerEntityRenderingHandler(BWEntityRegistry.TORNADO, TornadoRenderer::new);
//
//    }

    public static int dataCache = 0;

    @Override
    public void onInitialize() {
        ServerEntityEvents.ENTITY_LOAD.register(event -> playerTickEvent(event));

        ServerTickEvents.END_WORLD_TICK.register(event -> BetterWeatherEvents.worldTick(event.getLevel()));

    }

    public static class BetterWeatherEvents {
        public static BetterWeatherData weatherData = null;

        public static void worldTick(Level world) {
            setWeatherData(world);
            ServerLevel serverWorld = (ServerLevel) world;
            int tickSpeed = world.getGameRules().getInt(GameRules.RULE_RANDOMTICKING);
            long worldTime = world.getLevelData().getGameTime();

            //Rolls a random chance for acid rain once every 5000 ticks and will not run when raining to avoid disco colored rain.
            if (worldTime == 100 || worldTime % 5000 == 0 && !world.getLevelData().isRaining()) {
                Random random = world.random;
                weatherData.setAcidRain(random.nextFloat() < BetterWeatherConfig.acidRainChance);
                weatherData.setBlizzard(false);
            }
            if (worldTime == 100 || worldTime % 5000 == 0 && !world.getLevelData().isRaining()) {
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
    }

    public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
        setWeatherData(event.player.world);
    }

    public static void entityTickEvent(net.minecraft.world.entity.Entity entity) {
        if (damageMonsters) {
            if (entity.(true) == EntityClassification.MONSTER) {
                Level world = entity.level;
                BlockPos entityPos = new BlockPos(entity.getX(), entity.getY(), entity.getZ());

                if (world.canSeeSky(entityPos) && BetterWeatherEvents.weatherData.isAcidRain() && world.getLevelData().isRaining() && world.getGameTime() % BetterWeatherConfig.hurtEntityTickSpeed == 0) {
                    entity.attackEntityFrom(DamageSource.GENERIC, 0.5F);
                }
            }
        }

        if (damageAnimals) {
            if (event.getEntity().getClassification(true) == EntityClassification.CREATURE || event.getEntity().getClassification(true) == EntityClassification.AMBIENT) {
                Entity entity = event.getEntity();
                Level world = entity.world;
                BlockPos entityPos = new BlockPos(entity.getPositionVec());

                if (world.canSeeSky(entityPos) && BetterWeatherEvents.weatherData.isAcidRain() && world.getLevelData().isRaining() && world.getGameTime() % BetterWeatherConfig.hurtEntityTickSpeed == 0) {
                    entity.attackEntityFrom(DamageSource.GENERIC, BetterWeatherConfig.hurtEntityDamage.floatValue());
                }
            }
        }

        if (damagePlayer) {
            if (event.getEntity() instanceof PlayerEntity) {
                Entity entity = event.getEntity();
                Level world = entity.world;
                BlockPos entityPos = new BlockPos(entity.getPositionVec());

                if (world.canSeeSky(entityPos) && weatherData.isAcidRain() && world.getLevelData().isRaining() && world.getGameTime() % BetterWeatherConfig.hurtEntityTickSpeed == 0) {
                    entity.attackEntityFrom(DamageSource.GENERIC, 0.5F);
                }
            }
        }
        Blizzard.blizzardEntityHandler(event.getEntity());
    }

    public static final ResourceLocation RAIN_TEXTURE = new ResourceLocation("textures/environment/rain.png");
    public static final ResourceLocation ACID_RAIN_TEXTURE = new ResourceLocation(MOD_ID, "textures/environment/acid_rain.png");

    static int idx = 0;

    @SubscribeEvent
    public static void clientTickEvent(TickEvent.ClientTickEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.phase == TickEvent.Phase.START) {
            if (minecraft.world != null) {
                setWeatherData(minecraft.world);
                if (minecraft.world.getLevelData().isRaining() && weatherData.isAcidRain()) {

                    if (!BetterWeatherConfigClient.removeSmokeParticles)
                        AcidRain.addAcidRainParticles(minecraft.gameRenderer.getActiveRenderInfo(), minecraft, minecraft.worldRenderer);

                    if (WorldRenderer.RAIN_TEXTURES != ACID_RAIN_TEXTURE && weatherData.isAcidRain())
                        WorldRenderer.RAIN_TEXTURES = ACID_RAIN_TEXTURE;
                    else if (WorldRenderer.RAIN_TEXTURES != RAIN_TEXTURE && !weatherData.isAcidRain())
                        WorldRenderer.RAIN_TEXTURES = RAIN_TEXTURE;
                }

                if (minecraft.world.getLevelData().isRaining() && weatherData.isBlizzard()) {
                    minecraft.worldRenderer.renderDistanceLevelChunks = BetterWeatherConfigClient.forcedRenderDistanceDuringBlizzards;
                    idx = 0;
                }
                if (minecraft.worldRenderer.renderDistanceLevelChunks != minecraft.gameSettings.renderDistanceLevelChunks && !weatherData.isBlizzard() && idx == 0) {
                    minecraft.worldRenderer.renderDistanceLevelChunks = minecraft.gameSettings.renderDistanceLevelChunks;
                    idx++;
                }
                Blizzard.blizzardSoundHandler(minecraft, minecraft.gameRenderer.getActiveRenderInfo());
            }
        }
    }

    @SubscribeEvent
    public static void commandRegisterEvent(FMLServerStartingEvent event) {
        BetterWeather.LOGGER.debug("BW: \"Server Starting\" Event Starting...");
        BetterWeatherCommand.register(event.getServer().getCommandManager().getDispatcher());
        BetterWeather.LOGGER.info("BW: \"Server Starting\" Event Complete!");
    }

    public static void setWeatherData(LevelAccessor world) {
        if (weatherData == null)
            weatherData = BetterWeatherData.get(world);
    }
}


@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public static class BetterWeatherClient {

    static int idx2 = 0;

    @SubscribeEvent
    public static void renderFogEvent(EntityViewRenderEvent.FogDensity event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (BetterWeatherConfigClient.blizzardFog) {
            if (minecraft.world != null && minecraft.player != null) {
                BlockPos playerPos = new BlockPos(minecraft.player.getPositionVec());
                if (BetterWeatherEvents.weatherData.isBlizzard() && minecraft.world.getLevelData().isRaining() && Blizzard.doBlizzardsAffectDeserts(minecraft.world.getBiome(playerPos))) {
                    event.setDensity(0.1F);
                    event.setCanceled(true);
                    if (idx2 != 0)
                        idx2 = 0;
                } else {
                    if (idx2 == 0) {
                        event.setCanceled(false);
                        idx2++;
                    }
                }
            }
        }
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

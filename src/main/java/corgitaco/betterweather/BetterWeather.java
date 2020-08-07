package corgitaco.betterweather;

import com.google.common.collect.Lists;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.datastorage.BetterWeatherData;
import corgitaco.betterweather.weatherevents.AcidRain;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

@Mod("betterweather")
public class BetterWeather {
    public static Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "betterweather";

    public BetterWeather() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        BetterWeatherConfig.loadConfig(BetterWeatherConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-common.toml"));
    }

    static boolean damageAnimals = false;
    static boolean damageMonsters = false;
    static boolean damagePlayer = false;

    public static boolean destroyGrass = false;
    public static boolean destroyLeaves = false;
    public static boolean destroyCrops = false;
    public static boolean destroyPlants = false;

    public static List<Block> blocksToNotDestroyList = new ArrayList<>();

    public void commonSetup(FMLCommonSetupEvent event) {
        BetterWeatherConfig.loadConfig(BetterWeatherConfig.COMMON_CONFIG, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "-common.toml"));
        String entityTypes = BetterWeatherConfig.entityTypesToDamage.get();
        String removeSpaces = entityTypes.trim().toLowerCase().replace(" ", "");
        List<String> entityList = Arrays.asList(removeSpaces.split(","));

        for (String s : entityList) {
            if (s.equalsIgnoreCase("animal") && !damageAnimals)
                damageAnimals = true;
            if (s.equalsIgnoreCase("monster") && !damageMonsters)
                damageMonsters = true;
            if (s.equalsIgnoreCase("player") && !damagePlayer)
                damagePlayer = true;
        }

        String allowedBlockTypesToDestroy = BetterWeatherConfig.allowedBlocksToDestroy.get();
        String removeBlockTypeSpaces = allowedBlockTypesToDestroy.trim().toLowerCase().replace(" ", "");
        List<String> blockTypeToDestroyList = Arrays.asList(removeBlockTypeSpaces.split(","));

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
        ForgeRegistry<Block> blockRegistry = ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS);

        String blocksToNotDestroy = BetterWeatherConfig.blocksToNotDestroy.get();
        String removeBlocksToNotDestroySpaces = blocksToNotDestroy.trim().toLowerCase().replace(" ", "");
        List<String> blocksToNotDestroyList = Arrays.asList(removeBlocksToNotDestroySpaces.split(","));
        for (String s : blocksToNotDestroyList) {
            Block block = blockRegistry.getValue(new ResourceLocation(s));
            if (block != null)
                BetterWeather.blocksToNotDestroyList.add(block);
            else
                LOGGER.error("A block registry name you added to the \"BlocksToNotDestroy\" list was incorrect, you put: " + s + "\n Please fix it or this block will be destroyed.");
        }
    }

    public void clientSetup(FMLClientSetupEvent event) {
        Minecraft minecraft = event.getMinecraftSupplier().get();
        GameRenderer gameRenderer = minecraft.gameRenderer;
    }

    @Mod.EventBusSubscriber(modid = BetterWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class BetterWeatherEvents {
        public static BetterWeatherData weatherData = null;

        @SubscribeEvent
        public static void worldTick(TickEvent.WorldTickEvent event) {
            setWeatherData(event.world);
            if (event.side.isServer() && event.phase == TickEvent.Phase.END) {
                ServerWorld serverWorld = (ServerWorld) event.world;
                World world = event.world;
                int tickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
                long worldTime = world.getWorldInfo().getGameTime();

                //Rolls a random chance for acid rain once every 2500 ticks and will not run when raining to avoid disco colored rain.
                if (worldTime == 100 || worldTime % 5000 == 0 && !event.world.getWorldInfo().isRaining()) {
                    Random random = world.rand;
                    weatherData.setAcidRain(random.nextFloat() < BetterWeatherConfig.acidRainChance.get());
                }

                List<ChunkHolder> list = Lists.newArrayList((serverWorld.getChunkProvider()).chunkManager.getLoadedChunksIterable());
                list.forEach(chunkHolder -> {
                    Optional<Chunk> optional = chunkHolder.getTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
                    //Gets chunks to tick
                    if (optional.isPresent()) {
                        Optional<Chunk> optional1 = chunkHolder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
                        if (optional1.isPresent()) {
                            Chunk chunk = optional1.get();
//                            SandStorm.sandStormEvent(chunk, serverWorld, tickSpeed);
//                            HailStorm.hailStormEvent(chunk, serverWorld, tickSpeed);
//                            Blizzard.blizzardEvent(chunk, serverWorld, tickSpeed);
                            AcidRain.acidRainEvent(chunk, serverWorld, tickSpeed, worldTime);
                        }
                    }
                });
            }
        }

        @SubscribeEvent
        public static void renderTickEvent(TickEvent.RenderTickEvent event) {

        }

        @SubscribeEvent
        public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
            setWeatherData(event.player.world);
//            if (weatherData.isAcidRain())
//                event.player.sendStatusMessage(new StringTextComponent("reeeeeeeee"), true);

        }

        @SubscribeEvent
        public static void entityTickEvent(LivingEvent.LivingUpdateEvent event) {
            if (damageMonsters) {
                if (event.getEntity().getClassification(true) == EntityClassification.MONSTER) {
                    Entity entity = event.getEntity();
                    World world = entity.world;
                    BlockPos entityPos = new BlockPos(entity.getPositionVec());

                    if (world.canSeeSky(entityPos) && weatherData.isAcidRain() && world.getWorldInfo().isRaining() && world.getGameTime() % BetterWeatherConfig.hurtEntityTickSpeed.get() == 0) {
                        entity.attackEntityFrom(DamageSource.GENERIC, 0.5F);
                    }
                }
            }

            if (damageAnimals) {
                if (event.getEntity().getClassification(true) == EntityClassification.CREATURE || event.getEntity().getClassification(true) == EntityClassification.AMBIENT) {
                    Entity entity = event.getEntity();
                    World world = entity.world;
                    BlockPos entityPos = new BlockPos(entity.getPositionVec());

                    if (world.canSeeSky(entityPos) && weatherData.isAcidRain() && world.getWorldInfo().isRaining() && world.getGameTime() % BetterWeatherConfig.hurtEntityTickSpeed.get() == 0) {
                        entity.attackEntityFrom(DamageSource.GENERIC, BetterWeatherConfig.hurtEntityDamage.get().floatValue());
                    }
                }
            }

            if (damagePlayer) {
                if (event.getEntity() instanceof PlayerEntity) {
                    Entity entity = event.getEntity();
                    World world = entity.world;
                    BlockPos entityPos = new BlockPos(entity.getPositionVec());

                    if (world.canSeeSky(entityPos) && weatherData.isAcidRain() && world.getWorldInfo().isRaining() && world.getGameTime() % BetterWeatherConfig.hurtEntityTickSpeed.get() == 0) {
                        entity.attackEntityFrom(DamageSource.GENERIC, 0.5F);
                    }
                }
            }
        }

        public static final ResourceLocation RAIN_TEXTURE = new ResourceLocation("textures/environment/rain.png");
        public static final ResourceLocation ACID_RAIN_TEXTURE = new ResourceLocation(MOD_ID,"textures/environment/acid_rain.png");

        @SubscribeEvent
        public static void clientTickEvent(TickEvent.ClientTickEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (minecraft.world != null) {
                setWeatherData(minecraft.world);
                if (minecraft.world.getWorldInfo().isRaining() && weatherData.isAcidRain()) {

                    if (!BetterWeatherConfig.removeSmokeParticles.get())
                        AcidRain.addAcidRainParticles(minecraft.gameRenderer.getActiveRenderInfo(), minecraft, minecraft.worldRenderer);

                    if (WorldRenderer.RAIN_TEXTURES != ACID_RAIN_TEXTURE && weatherData.isAcidRain())
                        WorldRenderer.RAIN_TEXTURES = ACID_RAIN_TEXTURE;
                    else if (WorldRenderer.RAIN_TEXTURES != RAIN_TEXTURE && !weatherData.isAcidRain())
                        WorldRenderer.RAIN_TEXTURES = RAIN_TEXTURE;
                }
            }
        }

        public static void setWeatherData(IWorld world) {
            if (weatherData == null)
                weatherData = BetterWeatherData.get(world);
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

package corgitaco.betterweather;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.LiteralCommandNode;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.config.BetterWeatherConfigClient;
import corgitaco.betterweather.config.json.SeasonConfig;
import corgitaco.betterweather.datastorage.BetterWeatherData;
import corgitaco.betterweather.datastorage.BetterWeatherSeasonData;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.season.BWSeasonSystem;
import corgitaco.betterweather.server.SetSeasonCommand;
import corgitaco.betterweather.server.SetWeatherCommand;
import corgitaco.betterweather.weatherevent.BWWeatherEventSystem;
import corgitaco.betterweather.weatherevent.weatherevents.AcidRain;
import corgitaco.betterweather.weatherevent.weatherevents.Blizzard;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Mod("betterweather")
public class BetterWeather {
    public static Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "betterweather";
    public static int SEASON_LENGTH = 180000;
    public static final int SEASON_CYCLE_LENGTH = SEASON_LENGTH * 4;

    public static final Path CONFIG_PATH = new File(String.valueOf(FMLPaths.CONFIGDIR.get().resolve(MOD_ID))).toPath();


    public BetterWeather() {
        File dir = new File(CONFIG_PATH.toString());
        if (!dir.exists())
            dir.mkdir();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        BetterWeatherConfig.loadConfig(BetterWeatherConfig.COMMON_CONFIG, CONFIG_PATH.resolve(MOD_ID + "-common.toml"));
        BetterWeatherConfigClient.loadConfig(BetterWeatherConfigClient.COMMON_CONFIG, CONFIG_PATH.resolve(MOD_ID + "-client.toml"));
    }

    public static BetterWeatherSeasonData seasonData = null;
    public static BetterWeatherData weatherData = null;


    public void commonSetup(FMLCommonSetupEvent event) {
//        GlobalEntityTypeAttributes.put(BWEntityRegistry.TORNADO, TornadoEntity.setCustomAttributes().create());
        BetterWeatherConfig.handleCommonConfig();
        NetworkHandler.init();
        SeasonConfig.handleBWSeasonsConfig(CONFIG_PATH.resolve(MOD_ID + "-seasons.json"));
    }


    public void clientSetup(FMLClientSetupEvent event) {
//        RenderingRegistry.registerEntityRenderingHandler(BWEntityRegistry.TORNADO, TornadoRenderer::new);

    }


    public enum WeatherEvent {
        NONE,
        ACID_RAIN,
        BLIZZARD,
//        HAIL,
//        HEATWAVE,
//        WINDSTORM,
//        SANDSTORM,
    }


    @Mod.EventBusSubscriber(modid = BetterWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class BetterWeatherEvents {
        @SubscribeEvent
        public static void worldTick(TickEvent.WorldTickEvent event) {
            setWeatherData(event.world);
            setSeasonData(event.world);

            if (event.phase == TickEvent.Phase.END) {
                if (event.side.isServer()) {
                    ServerWorld serverWorld = (ServerWorld) event.world;
                    World world = event.world;
                    int tickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
                    long worldTime = world.getWorldInfo().getGameTime();

                    BWSeasonSystem.seasonTime();

                    serverWorld.getPlayers().forEach(player -> {
                        BWSeasonSystem.updateSeasonPacket(player, world);
                        BWWeatherEventSystem.updateWeatherEventPacket(player, world);
                    });

                    List<ChunkHolder> list = Lists.newArrayList((serverWorld.getChunkProvider()).chunkManager.getLoadedChunksIterable());
                    modifyLiveWorld(serverWorld, tickSpeed, worldTime, list);
                }
            }
        }

        private static void modifyLiveWorld(ServerWorld serverWorld, int tickSpeed, long worldTime, List<ChunkHolder> list) {
            list.forEach(chunkHolder -> {
                Optional<Chunk> optional = chunkHolder.getTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
                //Gets chunks to tick
                if (optional.isPresent()) {
                    Optional<Chunk> optional1 = chunkHolder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
                    if (optional1.isPresent()) {
                        Chunk chunk = optional1.get();
                        Blizzard.addSnowAndIce(chunk, serverWorld, tickSpeed, worldTime);
                        if (BetterWeatherConfig.decaySnowAndIce.get())
                            Blizzard.doesIceAndSnowDecay(chunk, serverWorld, worldTime);
                        AcidRain.acidRainEvent(chunk, serverWorld, tickSpeed, worldTime);


                    }
                }
            });
        }

        @SubscribeEvent
        public static void renderTickEvent(TickEvent.RenderTickEvent event) {

        }

        @SubscribeEvent
        public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
            setWeatherData(event.player.world);
        }

        @SubscribeEvent
        public static void entityTickEvent(LivingEvent.LivingUpdateEvent event) {
            AcidRain.entityHandler(event.getEntity());
            Blizzard.blizzardEntityHandler(event.getEntity());
        }


        @SubscribeEvent
        public static void clientTickEvent(TickEvent.ClientTickEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (event.phase == TickEvent.Phase.START) {
                if (minecraft.world != null && minecraft.player != null) {
                    if (minecraft.world.getWorldInfo().getGameTime() % 10 == 0) {
                        BWSeasonSystem.clientSeason();

                    }

                    AcidRain.handleRainTexture(minecraft);

                    Blizzard.handleBlizzardRenderDistance(minecraft);
                    Blizzard.blizzardSoundHandler(minecraft, minecraft.gameRenderer.getActiveRenderInfo());
                }
            }
        }

        @SubscribeEvent
        public static void commandRegisterEvent(FMLServerStartingEvent event) {
            BetterWeather.LOGGER.debug("BW: \"Server Starting\" Event Starting...");
            register(event.getServer().getCommandManager().getDispatcher());
            BetterWeather.LOGGER.info("BW: \"Server Starting\" Event Complete!");
        }


        public static void register(CommandDispatcher<CommandSource> dispatcher) {
            LOGGER.debug("Registering Better Weather commands...");
            LiteralCommandNode<CommandSource> source = dispatcher.register(
                    Commands.literal(MOD_ID).requires(commandSource -> commandSource.hasPermissionLevel(3))
                            .then(SetSeasonCommand.register(dispatcher))
                            .then(SetWeatherCommand.register(dispatcher))

            );
            dispatcher.register(Commands.literal(MOD_ID).redirect(source));
            LOGGER.debug("Registered Better Weather Commands!");
        }
    }

    public static void setSeasonData(IWorld world) {
        if (seasonData == null)
            seasonData = BetterWeatherSeasonData.get(world);
    }

    public static void setWeatherData(IWorld world) {
        if (weatherData == null)
            weatherData = BetterWeatherData.get(world);
    }


    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class BetterWeatherClient {

        @SubscribeEvent
        public static void renderFogEvent(EntityViewRenderEvent.FogDensity event) {
            Minecraft minecraft = Minecraft.getInstance();
            Blizzard.handleFog(event, minecraft);
        }

        @SubscribeEvent
        public static void renderGameOverlayEventText(RenderGameOverlayEvent.Text event) {
            if (Minecraft.getInstance().gameSettings.showDebugInfo) {
                event.getLeft().add("Season: " + WordUtils.capitalize(BWSeasonSystem.cachedSeason.toString().toLowerCase()) + " | " + WordUtils.capitalize(BWSeasonSystem.cachedSubSeason.toString().replace("_", " ").replace(BWSeasonSystem.cachedSeason.toString(), "").toLowerCase()));
            }
        }
    }
}

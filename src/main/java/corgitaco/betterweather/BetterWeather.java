package corgitaco.betterweather;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.compat.OptifineCompat;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.config.BetterWeatherConfigClient;
import corgitaco.betterweather.config.json.SeasonConfig;
import corgitaco.betterweather.config.json.overrides.BiomeOverrideJsonHandler;
import corgitaco.betterweather.datastorage.BetterWeatherEventData;
import corgitaco.betterweather.datastorage.BetterWeatherGeneralData;
import corgitaco.betterweather.datastorage.BetterWeatherSeasonData;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.GeneralPacket;
import corgitaco.betterweather.season.Season;
import corgitaco.betterweather.season.SeasonSystem;
import corgitaco.betterweather.server.ConfigReloadCommand;
import corgitaco.betterweather.server.SetSeasonCommand;
import corgitaco.betterweather.server.SetWeatherCommand;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;

@Mod("betterweather")
public class BetterWeather {
    public static Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "betterweather";
    public static int SEASON_LENGTH = 240000;
    public static int SEASON_CYCLE_LENGTH = SEASON_LENGTH * 4;
    public static boolean useSeasons = true;
    public static boolean usingOptifine = true;

    public static final Path CONFIG_PATH = new File(String.valueOf(FMLPaths.CONFIGDIR.get().resolve(MOD_ID))).toPath();

    public static Registry<Biome> biomeRegistryEarlyAccess;

    public BetterWeather() {
        File dir = new File(CONFIG_PATH.toString());
        if (!dir.exists())
            dir.mkdir();

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::lateSetup);
        BetterWeatherConfig.loadConfig(CONFIG_PATH.resolve(MOD_ID + "-common.toml"));
        BetterWeatherConfigClient.loadConfig(CONFIG_PATH.resolve(MOD_ID + "-client.toml"));
    }

    public static BetterWeatherSeasonData seasonData = null;
    public static BetterWeatherEventData weatherData = null;
    public static BetterWeatherGeneralData generalData = null;


    public void commonSetup(FMLCommonSetupEvent event) {
//        GlobalEntityTypeAttributes.put(BWEntityRegistry.TORNADO, TornadoEntity.setCustomAttributes().create());
        BetterWeatherConfig.handleCommonConfig();
        WeatherEventSystem.addDefaultWeatherEvents();
        NetworkHandler.init();
    }

    public void lateSetup(FMLLoadCompleteEvent event) {
        WeatherEventSystem.fillWeatherEventsMapAndWeatherEventController();
    }


    public void clientSetup(FMLClientSetupEvent event) {
//        usingOptifine = OptifineCompat.IS_OPTIFINE_PRESENT.getValue();
//        RenderingRegistry.registerEntityRenderingHandler(BWEntityRegistry.TORNADO, TornadoRenderer::new);
    }

    public static void loadClientConfigs() {
        BetterWeatherConfigClient.loadConfig(CONFIG_PATH.resolve(MOD_ID + "-client.toml"));
        loadSeasonConfigs();
    }

    public static void loadSeasonConfigs() {
        if (useSeasons) {
            SeasonConfig.handleBWSeasonsConfig(BetterWeather.CONFIG_PATH.resolve(BetterWeather.MOD_ID + "-seasons.json"));
            Season.SUB_SEASON_MAP.forEach((subSeasonName, subSeason) -> {
                Path overrideFilePath = CONFIG_PATH.resolve("overrides").resolve(subSeasonName + "-override.json");
                if (subSeason.getParentSeason() == SeasonData.SeasonVal.WINTER)
                    BiomeOverrideJsonHandler.handleOverrideJsonConfigs(overrideFilePath, Season.SubSeason.WINTER_OVERRIDE, subSeason);
                else
                    BiomeOverrideJsonHandler.handleOverrideJsonConfigs(overrideFilePath, new IdentityHashMap<>(), subSeason);
            });
        }
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
                    if (serverWorld.getDimensionKey() == World.OVERWORLD) {
                        World world = event.world;
                        int tickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
                        long worldTime = world.getWorldInfo().getGameTime();
                        if (useSeasons) {
                            SeasonSystem.updateSeasonTime();
                            SeasonSystem.updateSeasonPacket(serverWorld.getPlayers(), world, false);
                        }

                        WeatherData.currentWeatherEvent.worldTick(serverWorld, tickSpeed, worldTime);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void renderTickEvent(TickEvent.RenderTickEvent event) {

        }

        @SubscribeEvent
        public static void worldLoadEvent(WorldEvent.Load event) {
        }

        @SubscribeEvent
        public static void playerTickEvent(TickEvent.PlayerTickEvent event) {
            setWeatherData(event.player.world);
        }

        @SubscribeEvent
        public static void entityTickEvent(LivingEvent.LivingUpdateEvent event) {
            WeatherData.currentWeatherEvent.livingEntityUpdate(event.getEntity());
        }

        @SubscribeEvent
        public static void onPlayerJoined(PlayerEvent.PlayerLoggedInEvent event) {
            updateGeneralDataPacket(Collections.singletonList((ServerPlayerEntity) event.getPlayer()), event.getPlayer().world);
            if (useSeasons)
                SeasonSystem.updateSeasonPacket(Collections.singletonList((ServerPlayerEntity) event.getPlayer()), event.getPlayer().world, true);
            WeatherEventSystem.updateWeatherEventPacketOnPlayerJoin(Collections.singletonList((ServerPlayerEntity) event.getPlayer()), event.getPlayer().world);
        }

        public static void updateGeneralDataPacket(List<ServerPlayerEntity> players, World world) {
            setGeneralData(world);

            players.forEach(player -> {
                NetworkHandler.sendTo(player, new GeneralPacket(useSeasons));
            });
        }

        @SubscribeEvent
        public static void clientTickEvent(TickEvent.ClientTickEvent event) {
            Minecraft minecraft = Minecraft.getInstance();
            if (event.phase == TickEvent.Phase.START) {
                if (minecraft.world != null && minecraft.player != null) {
                    if (minecraft.world.getDimensionKey() == World.OVERWORLD) {
                        int tickSpeed = minecraft.world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);
                        if (useSeasons) {
                            if (minecraft.world.getWorldInfo().getGameTime() % 10 == 0) {
                                SeasonSystem.clientSeason();
                            }
                        }
                        WeatherData.currentWeatherEvent.clientTick(minecraft.world, tickSpeed, minecraft.world.getWorldInfo().getGameTime(), minecraft);
                    }
                }
            }
        }

        @SubscribeEvent
        public static void commandRegisterEvent(FMLServerStartingEvent event) {
            BetterWeather.LOGGER.debug("BW: \"Server Starting\" Event Starting...");
            register(event.getServer().getCommandManager().getDispatcher());
            BetterWeather.LOGGER.info("BW: \"Server Starting\" Event Complete!");
        }
    }


    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        LOGGER.debug("Registering Better Weather commands...");
        LiteralArgumentBuilder<CommandSource> requires = Commands.literal(MOD_ID).requires(commandSource -> commandSource.hasPermissionLevel(3));
        if (useSeasons)
            requires.then(SetSeasonCommand.register(dispatcher));

        LiteralCommandNode<CommandSource> source = dispatcher.register(requires.then(SetWeatherCommand.register(dispatcher)).then(ConfigReloadCommand.register(dispatcher)));



        dispatcher.register(Commands.literal(MOD_ID).redirect(source));
        LOGGER.debug("Registered Better Weather Commands!");
    }

    public static void setSeasonData(IWorld world) {
        if (useSeasons) {
            if (seasonData == null)
                seasonData = BetterWeatherSeasonData.get(world);
        }
    }

    public static void setWeatherData(IWorld world) {
        if (weatherData == null)
            weatherData = BetterWeatherEventData.get(world);
    }

    public static void setGeneralData(IWorld world) {
        if (generalData == null)
            generalData = BetterWeatherGeneralData.get(world);
    }


    @Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class BetterWeatherClient {

        @SubscribeEvent
        public static void renderFogEvent(EntityViewRenderEvent.FogDensity event) {
            Minecraft minecraft = Minecraft.getInstance();
            WeatherData.currentWeatherEvent.handleFogDensity(event, minecraft);
        }

        @SubscribeEvent
        public static void renderGameOverlayEventText(RenderGameOverlayEvent.Text event) {
            if (useSeasons) {
                if (Minecraft.getInstance().gameSettings.showDebugInfo) {
                    event.getLeft().add("Season: " + WordUtils.capitalize(SeasonData.currentSeason.toString().toLowerCase()) + " | " + WordUtils.capitalize(SeasonData.currentSubSeason.toString().replace("_", "").replace(SeasonData.currentSeason.toString(), "").toLowerCase()));
                }
            }
        }

        @SubscribeEvent
        public static void loggedInEvent(ClientPlayerNetworkEvent.LoggedInEvent event) {
            loadClientConfigs();
        }
    }
}

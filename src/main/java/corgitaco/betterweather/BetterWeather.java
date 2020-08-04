package corgitaco.betterweather;

import com.google.common.collect.Lists;
import corgitaco.betterweather.weather.Blizzard;
import corgitaco.betterweather.weather.HailStorm;
import corgitaco.betterweather.weather.HeatWave;
import corgitaco.betterweather.weather.SandStorm;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

@Mod("betterweather")
public class BetterWeather {
    public static Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "betterweather";

    public BetterWeather() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public void commonSetup(FMLCommonSetupEvent event) {

    }

    public void clientSetup(FMLClientSetupEvent event) {

    }

    @Mod.EventBusSubscriber(modid = BetterWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class BetterWeatherEvents {

        @SubscribeEvent
        public static void worldTick(TickEvent.WorldTickEvent event) {
            ServerWorld serverWorld = (ServerWorld) event.world;
            World world = event.world;
            int tickSpeed = world.getGameRules().getInt(GameRules.RANDOM_TICK_SPEED);

            List<ChunkHolder> list = Lists.newArrayList((serverWorld.getChunkProvider()).chunkManager.getLoadedChunksIterable());
            list.forEach(chunkHolder -> {
                Optional<Chunk> optional = chunkHolder.getTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
                //Gets chunks to tick
                if (optional.isPresent()) {
                    Optional<Chunk> optional1 = chunkHolder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
                    if (optional1.isPresent()) {
                        Chunk chunk = optional1.get();
                        SandStorm.sandStormEvent(chunk, serverWorld, tickSpeed);
                        HailStorm.hailStormEvent(chunk, serverWorld, tickSpeed);
                        Blizzard.blizzardEvent(chunk, serverWorld, tickSpeed);
                        HeatWave.heatWaveEvent(chunk, serverWorld, tickSpeed);
                    }
                }
            });
        }

        @SubscribeEvent
        public static void renderTickEvent(TickEvent.RenderTickEvent event) {
        }

        @SubscribeEvent
        public static void playerTickEvent(TickEvent.PlayerTickEvent event) {

        }
    }

    public enum WeatherType {
        BLIZZARD,
        HAIL,
        HEATWAVE,
        WINDSTORM,
        SANDSTORM
    }
}

package corgitaco.betterweather.weatherevent.weatherevents;

import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;

import java.util.Optional;

import static corgitaco.betterweather.weatherevent.weatherevents.Blizzard.doBlizzardsAffectDeserts;

public class Clear extends WeatherEvent {
    public Clear() {
        super(WeatherEventSystem.CLEAR, 0);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime, Iterable<ChunkHolder> loadedChunks) {
        decayIceAndSnowFaster(world, worldTime, loadedChunks);
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc) {
        if (WorldRenderer.RAIN_TEXTURES != AcidRain.RAIN_TEXTURE)
            WorldRenderer.RAIN_TEXTURES = AcidRain.RAIN_TEXTURE;
    }

    @Override
    public boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        return false;
    }

    private static void decayIceAndSnowFaster(ServerWorld serverWorld, long worldTime, Iterable<ChunkHolder> list) {
        if (SeasonData.SeasonVal.WINTER != SeasonData.currentSeason) {
            list.forEach(chunkHolder -> {
                Optional<Chunk> optional = chunkHolder.getTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
                //Gets chunks to tick
                if (optional.isPresent()) {
                    Optional<Chunk> optional1 = chunkHolder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
                    if (optional1.isPresent()) {
                        Chunk chunk = optional1.get();
                        doesIceAndSnowDecay(chunk, serverWorld, worldTime);
                    }
                }
            });
        }
    }

    private static void doesIceAndSnowDecay(Chunk chunk, World world, long worldTime) {
        ChunkPos chunkpos = chunk.getPos();
        int chunkXStart = chunkpos.getXStart();
        int chunkZStart = chunkpos.getZStart();
        IProfiler iprofiler = world.getProfiler();
        iprofiler.startSection("iceandsnowdecay");
        BlockPos blockpos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        Biome biome = world.getBiome(blockpos);
        Block blockDown = world.getBlockState(blockpos.down()).getBlock();
        Block block = world.getBlockState(blockpos).getBlock();

        if (world.isAreaLoaded(blockpos, 1)) {
            if (biome.getTemperature(blockpos) >= BetterWeatherConfig.snowDecayTemperatureThreshold.get()) {
                if (!world.getWorldInfo().isRaining() && worldTime % BetterWeatherConfig.tickSnowAndIceDecaySpeed.get() == 0 && biome.getCategory() != Biome.Category.NETHER && biome.getCategory() != Biome.Category.THEEND && biome.getCategory() != Biome.Category.NONE && doBlizzardsAffectDeserts(biome)) {
                    if (blockDown == Blocks.SNOW)
                        world.setBlockState(blockpos.down(), Blocks.AIR.getDefaultState());
                    if (block == Blocks.SNOW)
                        world.setBlockState(blockpos, Blocks.AIR.getDefaultState());
                    if (blockDown == Blocks.ICE)
                        world.setBlockState(blockpos.down(), Blocks.WATER.getDefaultState());

                }
            }
        }
        iprofiler.endSection();
    }
}

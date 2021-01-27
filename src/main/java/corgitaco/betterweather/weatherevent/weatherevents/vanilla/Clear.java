package corgitaco.betterweather.weatherevent.weatherevents.vanilla;

import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import corgitaco.betterweather.weatherevent.weatherevents.AcidRain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.profiler.IProfiler;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import static corgitaco.betterweather.weatherevent.weatherevents.Blizzard.doBlizzardsAffectDeserts;

public class Clear extends WeatherEvent {
    public Clear() {
        super(WeatherEventSystem.CLEAR, 0);
    }

    private static void doesIceAndSnowDecayFaster(Chunk chunk, World world, long worldTime) {
        ChunkPos chunkpos = chunk.getPos();
        int chunkXStart = chunkpos.getXStart();
        int chunkZStart = chunkpos.getZStart();
        IProfiler iprofiler = world.getProfiler();
        iprofiler.startSection("iceandsnowdecay");
        BlockPos blockpos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        Biome biome = world.getBiome(blockpos);
        Block blockDown = world.getBlockState(blockpos.down()).getBlock();
        BlockState blockState = world.getBlockState(blockpos);

        if (world.isAreaLoaded(blockpos, 1)) {
            if (biome.getTemperature(blockpos) >= BetterWeatherConfig.snowDecayTemperatureThreshold.get()) {
                if (worldTime % BetterWeatherConfig.tickSnowAndIceDecaySpeed.get() == 0 && biome.getCategory() != Biome.Category.NETHER && biome.getCategory() != Biome.Category.THEEND && biome.getCategory() != Biome.Category.NONE && doBlizzardsAffectDeserts(biome)) {
                    if (blockState.getBlock() == Blocks.SNOW && blockState.hasProperty(BlockStateProperties.LAYERS_1_8)) {
                        int snowLayerHeight = blockState.get(BlockStateProperties.LAYERS_1_8);

                        if (snowLayerHeight > 1) {
                            world.setBlockState(blockpos, blockState.with(BlockStateProperties.LAYERS_1_8, snowLayerHeight - 1));
                            return;
                        }

                        if (snowLayerHeight == 1)
                            world.setBlockState(blockpos, Blocks.AIR.getDefaultState());
                    }

                    if (blockDown == Blocks.ICE)
                        world.setBlockState(blockpos.down(), Blocks.WATER.getDefaultState());

                }
            }
        }
        iprofiler.endSection();
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {
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

    @Override
    public void tickLiveChunks(Chunk chunk, ServerWorld world) {
        if (SeasonData.currentSeason != SeasonData.SeasonVal.WINTER)
            doesIceAndSnowDecayFaster(chunk, world, world.getWorldInfo().getGameTime());
    }
}

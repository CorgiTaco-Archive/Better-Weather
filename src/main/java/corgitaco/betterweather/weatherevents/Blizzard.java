package corgitaco.betterweather.weatherevents;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.SoundRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.profiler.IProfiler;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;

public class Blizzard {

    public static void blizzardEvent(Chunk chunk, World world, int tickSpeed, long worldTime) {
        ChunkPos chunkpos = chunk.getPos();
        int chunkXStart = chunkpos.getXStart();
        int chunkZStart = chunkpos.getZStart();
        IProfiler iprofiler = world.getProfiler();
        iprofiler.startSection("blizzard");
        BlockPos blockpos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        if (world.isAreaLoaded(blockpos, 1)) {
            if (BetterWeather.BetterWeatherEvents.weatherData.isBlizzard() && world.getWorldInfo().isRaining() && worldTime % 5 == 0) {
                if (world.getBlockState(blockpos.down()).getBlock() == Blocks.WATER || world.getBlockState(blockpos.down()).getFluidState().getLevel() == 8) {
                    world.setBlockState(blockpos.down(), Blocks.ICE.getDefaultState());
                }
                if (world.getBlockState(blockpos.down()).getMaterial() != Material.WATER && world.getBlockState(blockpos.down()).getMaterial() != Material.LAVA && world.getBlockState(blockpos.down()).getMaterial() != Material.ICE) {
                    if (world.getBlockState(blockpos).getBlock() != Blocks.SNOW)
                        world.setBlockState(blockpos, Blocks.SNOW.getDefaultState());

                    Block block = world.getBlockState(blockpos).getBlock();

                    if (block == Blocks.SNOW && world.getBlockState(blockpos).get(BlockStateProperties.LAYERS_1_8) == 1 && world.rand.nextInt(5) == 2)
                        world.setBlockState(blockpos, block.getDefaultState().with(BlockStateProperties.LAYERS_1_8, 2));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).get(BlockStateProperties.LAYERS_1_8) == 2 && world.rand.nextInt(5) == 3)
                        world.setBlockState(blockpos, block.getDefaultState().with(BlockStateProperties.LAYERS_1_8, 3));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).get(BlockStateProperties.LAYERS_1_8) == 3 && world.rand.nextInt(5) == 0)
                        world.setBlockState(blockpos, block.getDefaultState().with(BlockStateProperties.LAYERS_1_8, 4));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).get(BlockStateProperties.LAYERS_1_8) == 4 && world.rand.nextInt(5) == 4)
                        world.setBlockState(blockpos, block.getDefaultState().with(BlockStateProperties.LAYERS_1_8, 5));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).get(BlockStateProperties.LAYERS_1_8) == 5 && world.rand.nextInt(5) == 0)
                        world.setBlockState(blockpos, block.getDefaultState().with(BlockStateProperties.LAYERS_1_8, 6));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).get(BlockStateProperties.LAYERS_1_8) == 6 && world.rand.nextInt(5) == 1)
                        world.setBlockState(blockpos, block.getDefaultState().with(BlockStateProperties.LAYERS_1_8, 7));
                    else if (block == Blocks.SNOW && world.getBlockState(blockpos).get(BlockStateProperties.LAYERS_1_8) == 7 && world.rand.nextInt(5) == 0)
                        world.setBlockState(blockpos, Blocks.SNOW_BLOCK.getDefaultState());
                }
            }
        }
        iprofiler.endSection();
    }

    static int cycleBlizzardSounds = 0;

    public static void playWeatherSounds(Minecraft mc, ActiveRenderInfo activeRenderInfo) {
        float volume = 1.0F;
        float pitch = 1.0F;

        if (mc.world != null && mc.world.getWorldInfo().isRaining() && BetterWeather.BetterWeatherEvents.weatherData.isBlizzard()) {
            BlockPos pos = new BlockPos(activeRenderInfo.getProjectedView());
            if (mc.world.getWorldInfo().getGameTime() % 75 == 0 || cycleBlizzardSounds == 0)
                mc.world.playSound(pos, SoundRegistry.BLIZZARD, SoundCategory.WEATHER, volume, pitch, false);
            if (cycleBlizzardSounds == 0)
                cycleBlizzardSounds++;
        }
        if (mc.world != null && !mc.world.getWorldInfo().isRaining() && !BetterWeather.BetterWeatherEvents.weatherData.isBlizzard()) {
            if (cycleBlizzardSounds != 0) {
                cycleBlizzardSounds = 0;
            }
        }
    }
}

package corgitaco.betterweather.weather;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

public class AcidRain {
    public static void acidRainEvent(Chunk chunk, ServerWorld world, int gameRuleTickSpeed, long worldTime) {
        ChunkPos chunkpos = chunk.getPos();
        boolean flag = world.isRaining();
        int chunkXStart = chunkpos.getXStart();
        int chunkZStart = chunkpos.getZStart();

        IProfiler iprofiler = world.getProfiler();
        iprofiler.startSection("acidrain");
        BlockPos blockpos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        if (BetterWeather.isAcidRain && world.isRaining() && worldTime % 25 == 0) {
            if (world.getBlockState(blockpos.down()).getBlock() == Blocks.GRASS_BLOCK)
                world.setBlockState(blockpos.down(), Blocks.COARSE_DIRT.getDefaultState());
            if (world.getBlockState(blockpos).getMaterial() == Material.PLANTS || world.getBlockState(blockpos).getMaterial() == Material.TALL_PLANTS)
                world.setBlockState(blockpos, Blocks.AIR.getDefaultState());
            if (world.getBlockState(blockpos.down()).getBlock().isIn(BlockTags.LEAVES))
                world.setBlockState(blockpos.down(), Blocks.AIR.getDefaultState());
        }
        iprofiler.endSection();
    }
}

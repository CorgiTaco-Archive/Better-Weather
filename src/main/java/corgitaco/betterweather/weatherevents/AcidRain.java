package corgitaco.betterweather.weatherevents;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ParticleStatus;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Random;

public class AcidRain {
    static Block block = Registry.BLOCK.get(new ResourceLocation(BetterWeather.BW_CONFIG.acid_rain.world.blockToChangeFromGrass));

    public static void acidRainEvent(LevelChunk chunk, ServerLevel world, int gameRuleTickSpeed, long worldTime) {
        ChunkPos chunkpos = chunk.getPos();
        int chunkXStart = chunkpos.getMinBlockX();
        int chunkZStart = chunkpos.getMinBlockZ();
        ProfilerFiller iprofiler = world.getProfiler();
        iprofiler.incrementCounter("acidrain");
        BlockPos blockpos = world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        if (Blizzard.isAreaLoaded(blockpos, 1, world)) {
            if (BetterWeather.BetterWeatherEvents.weatherData.isAcidRain() && world.getLevelData().isRaining() && worldTime % BetterWeather.BW_CONFIG.acid_rain.world.blockTickDestroySpeed == 0 && BetterWeather.BW_CONFIG.acid_rain.world.destroyBlocks && world.getBiome(blockpos).getPrecipitation() == Biome.Precipitation.RAIN) {
                if (BetterWeather.destroyGrass) {
                    if (block == null) {
                        BetterWeather.LOGGER.error("The block replacing grass, registry location was incorrect. You put: " + BetterWeather.BW_CONFIG.acid_rain.world.blockToChangeFromGrass + "\n Reverting to dirt!");
                        block = Blocks.DIRT;
                    }
                    if (world.getBlockState(blockpos.below()).getBlock() == Blocks.GRASS_BLOCK)
                        world.setBlockAndUpdate(blockpos.below(), block.defaultBlockState());
                }
                if (BetterWeather.destroyPlants) {
                    if (world.getBlockState(blockpos).getMaterial() == Material.PLANT || world.getBlockState(blockpos).getMaterial() == Material.REPLACEABLE_PLANT && !BetterWeather.blocksToNotDestroyList.contains(world.getBlockState(blockpos).getBlock()))
                        world.setBlockAndUpdate(blockpos, Blocks.AIR.defaultBlockState());
                }
                if (BetterWeather.destroyLeaves) {
                    if (world.getBlockState(blockpos.below()).getBlock().is(BlockTags.LEAVES) && !BetterWeather.blocksToNotDestroyList.contains(world.getBlockState(blockpos.below()).getBlock()))
                        world.setBlockAndUpdate(blockpos.below(), Blocks.AIR.defaultBlockState());
                }
                if (BetterWeather.destroyCrops) {
                    if (world.getBlockState(blockpos).getBlock().is(BlockTags.CROPS) && !BetterWeather.blocksToNotDestroyList.contains(world.getBlockState(blockpos).getBlock()))
                        world.setBlockAndUpdate(blockpos, Blocks.AIR.defaultBlockState());
                }
            }
        }
        iprofiler.pop();
    }

    public static void addAcidRainParticles(Camera activeRenderInfoIn, Minecraft minecraft, LevelRenderer worldRenderer) {
        float f = minecraft.level.getRainLevel(1.0F) / (Minecraft.useFancyGraphics() ? 1.0F : 2.0F);
        if (!(f <= 0.0F)) {
            Random random = new Random(worldRenderer.ticks * 312987231L);
            LevelReader iworldreader = minecraft.level;
            BlockPos blockpos = new BlockPos(activeRenderInfoIn.getPosition());
            BlockPos blockpos1 = null;
            int i = (int) (100.0F * f * f) / (minecraft.options.particles == ParticleStatus.DECREASED ? 2 : 1);

            for (int j = 0; j < i; ++j) {
                int k = random.nextInt(21) - 10;
                int l = random.nextInt(21) - 10;
                BlockPos blockpos2 = iworldreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos.offset(k, 0, l)).below();
                Biome biome = iworldreader.getBiome(blockpos2);
                if (blockpos2.getY() > 0 && blockpos2.getY() <= blockpos.getY() + 10 && blockpos2.getY() >= blockpos.getY() - 10 && biome.getPrecipitation() == Biome.Precipitation.RAIN && biome.getTemperature(blockpos2) >= 0.15F) {
                    blockpos1 = blockpos2;
                    if (minecraft.options.particles == ParticleStatus.MINIMAL) {
                        break;
                    }

                    double d0 = random.nextDouble();
                    double d1 = random.nextDouble();
                    BlockState blockstate = iworldreader.getBlockState(blockpos2);
                    FluidState fluidstate = iworldreader.getFluidState(blockpos2);
                    VoxelShape voxelshape = blockstate.getCollisionShape(iworldreader, blockpos2);
                    double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
                    double d3 = fluidstate.getHeight(iworldreader, blockpos2);
                    double d4 = Math.max(d2, d3);
                    ParticleOptions iparticledata = ParticleTypes.SMOKE;
                    minecraft.level.addParticle(iparticledata, (double) blockpos2.getX() + d0, (double) blockpos2.getY() + d4, (double) blockpos2.getZ() + d1, 0.0D, 0.0D, 0.0D);
                }
            }

            if (blockpos1 != null && random.nextInt(3) < worldRenderer.rainSoundTime++) {
                worldRenderer.rainSoundTime = 0;
                if (blockpos1.getY() > blockpos.getY() + 1 && iworldreader.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, blockpos).getY() > Mth.floor((float) blockpos.getY())) {
                    minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundSource.WEATHER, 0.1F, 0.5F, false);
                } else {
                    minecraft.level.playLocalSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundSource.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
    }
}

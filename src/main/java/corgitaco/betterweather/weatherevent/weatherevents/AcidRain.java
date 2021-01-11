package corgitaco.betterweather.weatherevent.weatherevents;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.config.BetterWeatherConfigClient;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

import java.util.Optional;
import java.util.Random;

import static corgitaco.betterweather.BetterWeather.weatherData;
import static corgitaco.betterweather.config.BetterWeatherConfig.*;

public class AcidRain extends WeatherEvent {
    public static final ForgeRegistry<Block> blockRegistry = ((ForgeRegistry<Block>) ForgeRegistries.BLOCKS);
    static Block block = blockRegistry.getRaw(new ResourceLocation(blockToChangeFromGrass.get()));
    public static final ResourceLocation RAIN_TEXTURE = new ResourceLocation("textures/environment/rain.png");
    public static final ResourceLocation ACID_RAIN_TEXTURE = new ResourceLocation(BetterWeather.MOD_ID, "textures/environment/acid_rain.png");



    public AcidRain() {
        super(new BetterWeatherID(BetterWeather.MOD_ID, "ACID_RAIN"), 0.25);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime, Iterable<ChunkHolder> loadedChunks) {
        loadedChunks.forEach(chunkHolder -> {
            Optional<Chunk> optional = chunkHolder.getTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
            //Gets chunks to tick
            if (optional.isPresent()) {
                Optional<Chunk> optional1 = chunkHolder.getEntityTickingFuture().getNow(ChunkHolder.UNLOADED_CHUNK).left();
                if (optional1.isPresent()) {
                    Chunk chunk = optional1.get();
                    acidRainEvent(chunk, world, worldTime);
                }
            }
        });
    }

    @Override
    public void livingEntityUpdate(Entity entity) {
        if (damageMonsters) {
            if (entity.getClassification(true) == EntityClassification.MONSTER) {
                World world = entity.world;
                BlockPos entityPos = new BlockPos(entity.getPositionVec());

                if (world.canSeeSky(entityPos) && weatherData.isAcidRain() && world.getWorldInfo().isRaining() && world.getGameTime() % hurtEntityTickSpeed.get() == 0) {
                    entity.attackEntityFrom(DamageSource.GENERIC, 0.5F);
                }
            }
        }

        if (damageAnimals) {
            if (entity.getClassification(true) == EntityClassification.CREATURE || entity.getClassification(true) == EntityClassification.AMBIENT) {
                World world = entity.world;
                BlockPos entityPos = new BlockPos(entity.getPositionVec());

                if (world.canSeeSky(entityPos) && weatherData.isAcidRain() && world.getWorldInfo().isRaining() && world.getGameTime() % hurtEntityTickSpeed.get() == 0) {
                    entity.attackEntityFrom(DamageSource.GENERIC, hurtEntityDamage.get().floatValue());
                }
            }
        }

        if (damagePlayer) {
            if (entity instanceof PlayerEntity) {
                World world = entity.world;
                BlockPos entityPos = new BlockPos(entity.getPositionVec());

                if (world.canSeeSky(entityPos) && weatherData.isAcidRain() && world.getWorldInfo().isRaining() && world.getGameTime() % hurtEntityTickSpeed.get() == 0) {
                    entity.attackEntityFrom(DamageSource.GENERIC, 0.5F);
                }
            }
        }
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc, int postClientTicksLeft) {
        if (mc.world != null) {
            if (mc.world.getWorldInfo().isRaining() && weatherData.isAcidRain()) {
                if (!BetterWeatherConfigClient.removeSmokeParticles.get())
                    addAcidRainParticles(mc.gameRenderer.getActiveRenderInfo(), mc, mc.worldRenderer);

                if (WorldRenderer.RAIN_TEXTURES != ACID_RAIN_TEXTURE)
                    WorldRenderer.RAIN_TEXTURES = ACID_RAIN_TEXTURE;
            } else if (WorldRenderer.RAIN_TEXTURES != RAIN_TEXTURE)
                WorldRenderer.RAIN_TEXTURES = RAIN_TEXTURE;
        }
    }

    @Override
    public boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        return false;
    }


    public static void acidRainEvent(Chunk chunk, ServerWorld world, long worldTime) {
        ChunkPos chunkpos = chunk.getPos();
        int chunkXStart = chunkpos.getXStart();
        int chunkZStart = chunkpos.getZStart();
        IProfiler iprofiler = world.getProfiler();
        iprofiler.startSection("acidrain");
        BlockPos blockpos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        if (world.isAreaLoaded(blockpos, 1)) {
            if (BetterWeather.weatherData.isAcidRain() && world.getWorldInfo().isRaining() && worldTime % tickBlockDestroySpeed.get() == 0 && destroyBlocks.get() && world.getBiome(blockpos).getPrecipitation() == Biome.RainType.RAIN) {
                if (destroyGrass) {
                    if (block == null) {
                        BetterWeather.LOGGER.error("The block replacing grass, registry location was incorrect. You put: " + blockToChangeFromGrass.get() + "\n Reverting to dirt!");
                        block = Blocks.DIRT;
                    }
                    if (world.getBlockState(blockpos.down()).getBlock() == Blocks.GRASS_BLOCK)
                        world.setBlockState(blockpos.down(), block.getDefaultState());
                }
                if (destroyPlants) {
                    if (world.getBlockState(blockpos).getMaterial() == Material.PLANTS || world.getBlockState(blockpos).getMaterial() == Material.TALL_PLANTS && !blocksToNotDestroyList.contains(world.getBlockState(blockpos).getBlock()))
                        world.setBlockState(blockpos, Blocks.AIR.getDefaultState());
                }
                if (destroyLeaves) {
                    if (world.getBlockState(blockpos.down()).getBlock().isIn(BlockTags.LEAVES) && !blocksToNotDestroyList.contains(world.getBlockState(blockpos.down()).getBlock()))
                        world.setBlockState(blockpos.down(), Blocks.AIR.getDefaultState());
                }
                if (destroyCrops) {
                    if (world.getBlockState(blockpos).getBlock().isIn(BlockTags.CROPS) && !blocksToNotDestroyList.contains(world.getBlockState(blockpos).getBlock()))
                        world.setBlockState(blockpos, Blocks.AIR.getDefaultState());
                }
            }
        }
        iprofiler.endSection();
    }

    public static void addAcidRainParticles(ActiveRenderInfo activeRenderInfoIn, Minecraft mc, WorldRenderer worldRenderer) {
        float f = mc.world.getRainStrength(1.0F) / (Minecraft.isFancyGraphicsEnabled() ? 1.0F : 2.0F);
        if (!(f <= 0.0F)) {
            Random random = new Random(worldRenderer.ticks * 312987231L);
            IWorldReader iworldreader = mc.world;
            BlockPos blockpos = new BlockPos(activeRenderInfoIn.getProjectedView());
            BlockPos blockpos1 = null;
            int i = (int) (100.0F * f * f) / (mc.gameSettings.particles == ParticleStatus.DECREASED ? 2 : 1);

            for (int j = 0; j < i; ++j) {
                int k = random.nextInt(21) - 10;
                int l = random.nextInt(21) - 10;
                BlockPos blockpos2 = iworldreader.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos.add(k, 0, l)).down();
                Biome biome = iworldreader.getBiome(blockpos2);
                if (blockpos2.getY() > 0 && blockpos2.getY() <= blockpos.getY() + 10 && blockpos2.getY() >= blockpos.getY() - 10 && biome.getPrecipitation() == Biome.RainType.RAIN && biome.getTemperature(blockpos2) >= 0.15F) {
                    blockpos1 = blockpos2;
                    if (mc.gameSettings.particles == ParticleStatus.MINIMAL) {
                        break;
                    }

                    double d0 = random.nextDouble();
                    double d1 = random.nextDouble();
                    BlockState blockstate = iworldreader.getBlockState(blockpos2);
                    FluidState fluidstate = iworldreader.getFluidState(blockpos2);
                    VoxelShape voxelshape = blockstate.getCollisionShape(iworldreader, blockpos2);
                    double d2 = voxelshape.max(Direction.Axis.Y, d0, d1);
                    double d3 = (double) fluidstate.getActualHeight(iworldreader, blockpos2);
                    double d4 = Math.max(d2, d3);
                    IParticleData iparticledata = ParticleTypes.SMOKE;
                    mc.world.addParticle(iparticledata, (double) blockpos2.getX() + d0, (double) blockpos2.getY() + d4, (double) blockpos2.getZ() + d1, 0.0D, 0.0D, 0.0D);
                }
            }

            if (blockpos1 != null && random.nextInt(3) < worldRenderer.rainSoundTime++) {
                worldRenderer.rainSoundTime = 0;
                if (blockpos1.getY() > blockpos.getY() + 1 && iworldreader.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos).getY() > MathHelper.floor((float) blockpos.getY())) {
                    mc.world.playSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1F, 0.5F, false);
                } else {
                    mc.world.playSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
    }
}

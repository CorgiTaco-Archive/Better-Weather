package corgitaco.betterweather.weatherevents;

import corgitaco.betterweather.SoundRegistry;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.config.BetterWeatherConfigClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SimpleSound;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.profiler.IProfiler;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import static corgitaco.betterweather.BetterWeather.weatherData;

public class Blizzard {

    public static void addSnowAndIce(Chunk chunk, World world, int tickSpeed, long worldTime) {
        ChunkPos chunkpos = chunk.getPos();
        int chunkXStart = chunkpos.getXStart();
        int chunkZStart = chunkpos.getZStart();
        IProfiler iprofiler = world.getProfiler();
        iprofiler.startSection("blizzard");
        BlockPos blockpos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        Biome biome = world.getBiome(blockpos);
        if (world.isAreaLoaded(blockpos, 1)) {
            if (weatherData.isBlizzard() && world.getWorldInfo().isRaining() && worldTime % BetterWeatherConfig.tickSnowAndIcePlaceSpeed.get() == 0 && biome.getCategory() != Biome.Category.NETHER && biome.getCategory() != Biome.Category.THEEND && biome.getCategory() != Biome.Category.NONE && doBlizzardsAffectDeserts(biome) && BetterWeatherConfig.spawnSnowAndIce.get()) {
                if (world.getBlockState(blockpos.down()).getBlock() == Blocks.WATER && world.getBlockState(blockpos.down()).getFluidState().getLevel() == 8) {
                    world.setBlockState(blockpos.down(), Blocks.ICE.getDefaultState());
                }
                if (world.getBlockState(blockpos.down()).getMaterial() != Material.WATER && world.getBlockState(blockpos.down()).getMaterial() != Material.LAVA && world.getBlockState(blockpos.down()).getMaterial() != Material.ICE && world.getBlockState(blockpos.down()).getMaterial() != Material.CACTUS && doBlizzardsDestroyPlants(world.getBlockState(blockpos).getMaterial())) {
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

    public static void doesIceAndSnowDecay(Chunk chunk, World world, long worldTime) {
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


    static int cycleBlizzardSounds = 0;

    @OnlyIn(Dist.CLIENT)
    public static void blizzardSoundHandler(Minecraft mc, ActiveRenderInfo activeRenderInfo) {
        double volume = BetterWeatherConfigClient.blizzardVolume.get();
        double pitch = BetterWeatherConfigClient.blizzardPitch.get();
        BlockPos pos = new BlockPos(activeRenderInfo.getProjectedView());
        if (mc.world != null) {
            if (weatherData.isBlizzard() && mc.world.isRaining()) {
                mc.getSoundHandler().sndManager.setVolume(SoundCategory.WEATHER, 0.1F);
            }
//            else
//                mc.getSoundHandler().sndManager.setVolume(SoundCategory.WEATHER, mc.gameSettings.getSoundLevel(SoundCategory.WEATHER));
        }


        BlizzardLoopSoundTrack soundTrack = BetterWeatherConfigClient.blizzardLoopEnumValue.get();

        SimpleSound simplesound = new SimpleSound(soundTrack.getSoundEvent(), SoundCategory.WEATHER, (float) volume, (float) pitch, pos.getX(), pos.getY(), pos.getZ());
        if (mc.world != null && mc.world.getWorldInfo().isRaining() && weatherData.isBlizzard() && doBlizzardsAffectDeserts(mc.world.getBiome(pos))) {
            if (cycleBlizzardSounds == 0 || mc.world.getWorldInfo().getGameTime() % soundTrack.getReplayRate() == 0) {
                mc.getSoundHandler().play(simplesound);
                cycleBlizzardSounds++;
            }
        }
        if (mc.world != null) {
            if (!weatherData.isBlizzard() || !doBlizzardsAffectDeserts(mc.world.getBiome(pos))) {
                mc.getSoundHandler().stop(simplesound.getSoundLocation(), SoundCategory.WEATHER);
                if (cycleBlizzardSounds != 0)
                    cycleBlizzardSounds = 0;
            }
        }
    }

    public enum BlizzardLoopSoundTrack {
        LOOP1(SoundRegistry.BLIZZARD_LOOP1, 2400),
        LOOP2(SoundRegistry.BLIZZARD_LOOP2, 2400),
        LOOP3(SoundRegistry.BLIZZARD_LOOP3, 2400),
        LOOP4(SoundRegistry.BLIZZARD_LOOP4, 2400),
        LOOP5(SoundRegistry.BLIZZARD_LOOP5, 2400),
        LOOP6(SoundRegistry.BLIZZARD_LOOP6, 2400),
        LOOP7(SoundRegistry.BLIZZARD_LOOP7, 1200);

        private final SoundEvent soundEvent;
        private final int replayRate;
        BlizzardLoopSoundTrack(SoundEvent soundEvent, int tickReplayRate) {
            this.soundEvent = soundEvent;
            this.replayRate = tickReplayRate;
        }

        public SoundEvent getSoundEvent() {
            return this.soundEvent;
        }

        public int getReplayRate() {
           return this.replayRate;
        }

        public SoundEvent[] soundRegistries = {
                SoundRegistry.BLIZZARD_LOOP1,
                SoundRegistry.BLIZZARD_LOOP2,
                SoundRegistry.BLIZZARD_LOOP3,
                SoundRegistry.BLIZZARD_LOOP4,
                SoundRegistry.BLIZZARD_LOOP5,
                SoundRegistry.BLIZZARD_LOOP6,
                SoundRegistry.BLIZZARD_LOOP7
        };
    }

    public static void blizzardEntityHandler(Entity entity) {
        if (entity instanceof LivingEntity) {
            if (entity.world.getWorldInfo().isRaining() && weatherData.isBlizzard() && BetterWeatherConfig.doBlizzardsSlowPlayers.get())
                ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 5, BetterWeatherConfig.blizzardSlownessAmplifier.get(), true, false));
        }
    }


    public static boolean doBlizzardsAffectDeserts(Biome biome) {
        if (!BetterWeatherConfig.doBlizzardsOccurInDeserts.get())
            return biome.getCategory() != Biome.Category.DESERT;
        else
            return true;
    }

    public static boolean doBlizzardsDestroyPlants(Material material) {
        if (!BetterWeatherConfig.doBlizzardsDestroyPlants.get())
            return material != Material.PLANTS && material != Material.TALL_PLANTS && material != Material.OCEAN_PLANT;
        else
            return true;
    }

    static int idx = 0;

    @OnlyIn(Dist.CLIENT)
    public static void handleBlizzardRenderDistance(Minecraft minecraft) {
        if (minecraft.world != null) {
            if (minecraft.world.getWorldInfo().isRaining() && weatherData.isBlizzard()) {
                minecraft.worldRenderer.renderDistanceChunks = BetterWeatherConfigClient.forcedRenderDistanceDuringBlizzards.get();
                idx = 0;
            }
            if (minecraft.worldRenderer.renderDistanceChunks != minecraft.gameSettings.renderDistanceChunks && !weatherData.isBlizzard() && idx == 0) {
                minecraft.worldRenderer.renderDistanceChunks = minecraft.gameSettings.renderDistanceChunks;
                idx++;
            }
        }
    }

    static int idx2 = 0;

    @OnlyIn(Dist.CLIENT)
    public static void handleFog(EntityViewRenderEvent.FogDensity event, Minecraft minecraft) {
        if (BetterWeatherConfigClient.blizzardFog.get()) {
            if (minecraft.world != null && minecraft.player != null) {
                BlockPos playerPos = new BlockPos(minecraft.player.getPositionVec());
                if (weatherData.isBlizzard() && minecraft.world.getWorldInfo().isRaining() && Blizzard.doBlizzardsAffectDeserts(minecraft.world.getBiome(playerPos))) {
                    event.setDensity(0.1F);
                    event.setCanceled(true);
                    if (idx2 != 0)
                        idx2 = 0;
                } else {
                    if (idx2 == 0) {
                        event.setCanceled(false);
                        idx2++;
                    }
                }
            }
        }
    }
}

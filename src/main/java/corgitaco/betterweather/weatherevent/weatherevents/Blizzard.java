package corgitaco.betterweather.weatherevent.weatherevents;

import com.mojang.blaze3d.systems.RenderSystem;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.SoundRegistry;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.audio.MovingWeatherSoundHandler;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.config.BetterWeatherConfigClient;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.profiler.IProfiler;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityViewRenderEvent;

import java.util.Optional;
import java.util.Random;

public class Blizzard extends WeatherEvent {

    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];

    public Blizzard() {
        super(new BetterWeatherID(BetterWeather.MOD_ID, "BLIZZARD"), 0.3);
        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = (float) (j - 16);
                float f1 = (float) (i - 16);
                float f2 = MathHelper.sqrt(f * f + f1 * f1);
                this.rainSizeX[i << 5 | j] = -f1 / f2;
                this.rainSizeZ[i << 5 | j] = f / f2;
            }
        }
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
                    Blizzard.addSnowAndIce(chunk, world, worldTime);
                }
            }
        });
    }

    public static MovingWeatherSoundHandler BLIZZARD_SOUND = new MovingWeatherSoundHandler(SoundRegistry.BLIZZARD_LOOP1, BetterWeatherConfigClient.blizzardLoopEnumValue.get().getReplayRate(), SoundCategory.WEATHER, Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getBlockPos(), BetterWeatherConfigClient.blizzardVolume.get().floatValue(), BetterWeatherConfigClient.blizzardPitch.get().floatValue());
    static int idx2 = 0;

    public static void addSnowAndIce(Chunk chunk, World world, long worldTime) {
        BetterWeather.setWeatherData(world);
        ChunkPos chunkpos = chunk.getPos();
        int chunkXStart = chunkpos.getXStart();
        int chunkZStart = chunkpos.getZStart();
        IProfiler iprofiler = world.getProfiler();
        iprofiler.startSection("blizzard");
        BlockPos blockpos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, world.getBlockRandomPos(chunkXStart, 0, chunkZStart, 15));
        Biome biome = world.getBiome(blockpos);
        if (world.isAreaLoaded(blockpos, 1)) {
            if (world.getWorldInfo().isRaining() && worldTime % BetterWeatherConfig.tickSnowAndIcePlaceSpeed.get() == 0 && biome.getCategory() != Biome.Category.NETHER && biome.getCategory() != Biome.Category.THEEND && biome.getCategory() != Biome.Category.NONE && doBlizzardsAffectDeserts(biome) && BetterWeatherConfig.spawnSnowAndIce.get()) {
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

    @OnlyIn(Dist.CLIENT)
    public static void handleBlizzardRenderDistance(Minecraft minecraft) {
        float partialTicks = minecraft.isGamePaused() ? minecraft.renderPartialTicksPaused : minecraft.timer.renderPartialTicks;
        float rainStrength = minecraft.world.getRainStrength(partialTicks);
    }

    static int cycleBlizzardSounds = 0;

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc) {
        SoundHandler soundHandler = mc.getSoundHandler();
        if (!soundHandler.isPlaying(BLIZZARD_SOUND)) {
            MovingWeatherSoundHandler blizzardSound = new MovingWeatherSoundHandler(BetterWeatherConfigClient.blizzardLoopEnumValue.get().getSoundEvent(), BetterWeatherConfigClient.blizzardLoopEnumValue.get().getReplayRate(), SoundCategory.WEATHER, Minecraft.getInstance().gameRenderer.getActiveRenderInfo().getBlockPos(), BetterWeatherConfigClient.blizzardVolume.get().floatValue(), BetterWeatherConfigClient.blizzardPitch.get().floatValue());
            soundHandler.play(blizzardSound);
            BLIZZARD_SOUND = blizzardSound;
        }

        Blizzard.handleBlizzardRenderDistance(mc);
    }

    @Override
    public boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        float rainStrength = world.getRainStrength(partialTicks);
        lightTexture.enableLightmap();
        int floorX = MathHelper.floor(x);
        int floorY = MathHelper.floor(y);
        int floorZ = MathHelper.floor(z);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        RenderSystem.enableAlphaTest();
        RenderSystem.disableCull();
        RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.enableDepthTest();
        int graphicsQuality = 5;
        if (Minecraft.isFancyGraphicsEnabled()) {
            graphicsQuality = 10;
        }

        RenderSystem.depthMask(Minecraft.isFabulousGraphicsEnabled());
        int i1 = -1;
        float ticksAndPartialTicks = (float) ticks + partialTicks;
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        BlockPos.Mutable blockPos = new BlockPos.Mutable();

        for (int graphicQualityZ = floorZ - graphicsQuality; graphicQualityZ <= floorZ + graphicsQuality; ++graphicQualityZ) {
            for (int graphicQualityX = floorX - graphicsQuality; graphicQualityX <= floorX + graphicsQuality; ++graphicQualityX) {
                int rainSizeIdx = (graphicQualityZ - floorZ + 16) * 32 + graphicQualityX - floorX + 16;
                //These 2 doubles control the size of rain particles.
                double rainSizeX = (double) this.rainSizeX[rainSizeIdx] * 0.5D;
                double rainSizeZ = (double) this.rainSizeZ[rainSizeIdx] * 0.5D;
                blockPos.setPos(graphicQualityX, 0, graphicQualityZ);
                Biome biome = world.getBiome(blockPos);
                int topPosY = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, blockPos.getX(), blockPos.getY());
                int floorYMinusGraphicsQuality = floorY - graphicsQuality;
                int floorYPlusGraphicsQuality = floorY + graphicsQuality;
                if (floorYMinusGraphicsQuality < topPosY) {
                    floorYMinusGraphicsQuality = topPosY;
                }

                if (floorYPlusGraphicsQuality < topPosY) {
                    floorYPlusGraphicsQuality = topPosY;
                }

                int posY2 = topPosY;
                if (topPosY < floorY) {
                    posY2 = floorY;
                }

                if (floorYMinusGraphicsQuality != floorYPlusGraphicsQuality) {
                    Random random = new Random(graphicQualityX * graphicQualityX * 3121 + graphicQualityX * 45238971 ^ graphicQualityZ * graphicQualityZ * 418711 + graphicQualityZ * 13761);
                    blockPos.setPos(graphicQualityX, floorYMinusGraphicsQuality, graphicQualityZ);

                    //This is rain rendering.
                    if (i1 != 1) {
                        if (i1 >= 0) {
                            tessellator.draw();
                        }

                        i1 = 1;
                        ResourceLocation THICC_SNOW = new ResourceLocation(BetterWeather.MOD_ID, "textures/environment/thick_snow.png");

                        mc.getTextureManager().bindTexture(WorldRenderer.SNOW_TEXTURES);
                        bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                    }

                    float f7 = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.03D);
                    float fallSpeed = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.03D);
                    double d3 = (double) ((float) graphicQualityX + 0.5F) - x;
                    double d5 = (double) ((float) graphicQualityZ + 0.5F) - z;
                    float f9 = MathHelper.sqrt(d3 * d3 + d5 * d5) / (float) graphicsQuality;
                    float ticksAndPartialTicks0 = ((1.0F - f9 * f9) * 0.3F + 0.5F) * rainStrength;
                    blockPos.setPos(graphicQualityX, posY2, graphicQualityZ);
                    int k3 = WorldRenderer.getCombinedLight(world, blockPos);
                    int l3 = k3 >> 16 & '\uffff';
                    int i4 = (k3 & '\uffff') * 3;
                    int j4 = (l3 * 3 + 240) / 4;
                    int k4 = (i4 * 3 + 240) / 4;
                    if (Blizzard.doBlizzardsAffectDeserts(biome)) {
                        bufferbuilder.pos((double) graphicQualityX - x - rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYPlusGraphicsQuality - y, (double) graphicQualityZ - z - rainSizeZ + 0.5D + random.nextGaussian()).tex(0.0F + f7, (float) floorYMinusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).lightmap(k4, j4).endVertex();
                        bufferbuilder.pos((double) graphicQualityX - x + rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYPlusGraphicsQuality - y, (double) graphicQualityZ - z + rainSizeZ + 0.5D + random.nextGaussian()).tex(1.0F + f7, (float) floorYMinusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).lightmap(k4, j4).endVertex();
                        bufferbuilder.pos((double) graphicQualityX - x + rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYMinusGraphicsQuality - y, (double) graphicQualityZ - z + rainSizeZ + 0.5D + random.nextGaussian()).tex(1.0F + f7, (float) floorYPlusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).lightmap(k4, j4).endVertex();
                        bufferbuilder.pos((double) graphicQualityX - x - rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYMinusGraphicsQuality - y, (double) graphicQualityZ - z - rainSizeZ + 0.5D + random.nextGaussian()).tex(0.0F + f7, (float) floorYPlusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).lightmap(k4, j4).endVertex();
                    }
                }
            }
        }

        if (i1 >= 0) {
            tessellator.draw();
        }

        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.defaultAlphaFunc();
        RenderSystem.disableAlphaTest();
        lightTexture.disableLightmap();

        return true;
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

    @Override
    public void handleFogDensity(EntityViewRenderEvent.FogDensity event, Minecraft mc) {
        if (BetterWeatherConfigClient.blizzardFog.get()) {
            if (mc.world != null && mc.player != null) {
                BlockPos playerPos = new BlockPos(mc.player.getPositionVec());
                if (Blizzard.doBlizzardsAffectDeserts(mc.world.getBiome(playerPos))) {
                    float partialTicks = mc.isGamePaused() ? mc.renderPartialTicksPaused : mc.timer.renderPartialTicks;
                    float fade = mc.world.getRainStrength(partialTicks);

//                    event.setDensity(fade * 0.1F);
//                    event.setCanceled(true);
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

    @Override
    public void livingEntityUpdate(Entity entity) {
        if (entity instanceof LivingEntity) {
            if (BetterWeatherConfig.doBlizzardsSlowPlayers.get())
                ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.SLOWNESS, 5, BetterWeatherConfig.blizzardSlownessAmplifier.get(), true, false));
        }
    }

    @Override
    public boolean disableSkyColor() {
        return true;
    }

    @Override
    public float modifyTemperature(float biomeTemp, float modifiedBiomeTemp, double seasonModifier) {
        return Math.max(-0.5F, modifiedBiomeTemp - 0.5F);
    }

    @Override
    public int forcedRenderDistance() {
        return 3;
    }
}

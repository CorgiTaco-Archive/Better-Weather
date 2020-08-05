package corgitaco.betterweather.weather;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import java.util.Random;

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

    public static void addAcidRainParticles(ActiveRenderInfo activeRenderInfoIn, Minecraft mc, WorldRenderer worldRenderer) {
        float f = mc.world.getRainStrength(1.0F) / (Minecraft.isFancyGraphicsEnabled() ? 1.0F : 2.0F);
        if (!(f <= 0.0F)) {
            Random random = new Random(worldRenderer.ticks * 312987231L);
            IWorldReader iworldreader = mc.world;
            BlockPos blockpos = new BlockPos(activeRenderInfoIn.getProjectedView());
            BlockPos blockpos1 = null;
            int i = (int)(100.0F * f * f) / (mc.gameSettings.particles == ParticleStatus.DECREASED ? 2 : 1);

            for(int j = 0; j < i; ++j) {
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
                    double d3 = (double)fluidstate.getActualHeight(iworldreader, blockpos2);
                    double d4 = Math.max(d2, d3);
                    IParticleData iparticledata = !fluidstate.isTagged(FluidTags.LAVA) && !blockstate.isIn(Blocks.MAGMA_BLOCK) && !CampfireBlock.func_226915_i_(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
                    mc.world.addParticle(iparticledata, (double)blockpos2.getX() + d0, (double)blockpos2.getY() + d4, (double)blockpos2.getZ() + d1, 0.0D, 0.0D, 0.0D);
                }
            }

            if (blockpos1 != null && random.nextInt(3) < worldRenderer.rainSoundTime++) {
                worldRenderer.rainSoundTime = 0;
                if (blockpos1.getY() > blockpos.getY() + 1 && iworldreader.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos).getY() > MathHelper.floor((float)blockpos.getY())) {
                    mc.world.playSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1F, 0.5F, false);
                } else {
                    mc.world.playSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
    }

//    private void renderRainSnow(LightTexture lightmapIn, Minecraft mc, WorldRenderer worldRenderer, float partialTicks, double xIn, double yIn, double zIn) {
//        float f = mc.world.getRainStrength(partialTicks);
//        if (!(f <= 0.0F)) {
//            lightmapIn.enableLightmap();
//            World world = mc.world;
//            int i = MathHelper.floor(xIn);
//            int j = MathHelper.floor(yIn);
//            int k = MathHelper.floor(zIn);
//            Tessellator tessellator = Tessellator.getInstance();
//            BufferBuilder bufferbuilder = tessellator.getBuffer();
//            RenderSystem.enableAlphaTest();
//            RenderSystem.disableCull();
//            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
//            RenderSystem.enableBlend();
//            RenderSystem.defaultBlendFunc();
//            RenderSystem.defaultAlphaFunc();
//            RenderSystem.enableDepthTest();
//            int l = 5;
//            if (Minecraft.isFancyGraphicsEnabled()) {
//                l = 10;
//            }
//
//            RenderSystem.depthMask(Minecraft.func_238218_y_());
//            int i1 = -1;
//            float f1 = mc.worldRenderer.ticks + partialTicks;
//            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();
//
//            for(int j1 = k - l; j1 <= k + l; ++j1) {
//                for(int k1 = i - l; k1 <= i + l; ++k1) {
//                    int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
//                    double d0 = (double)worldRenderer.rainSizeX[l1] * 0.5D;
//                    double d1 = (double)worldRenderer.rainSizeZ[l1] * 0.5D;
//                    blockpos$mutable.setPos(k1, 0, j1);
//                    Biome biome = world.getBiome(blockpos$mutable);
//                    if (biome.getPrecipitation() != Biome.RainType.NONE) {
//                        int i2 = world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos$mutable).getY();
//                        int j2 = j - l;
//                        int k2 = j + l;
//                        if (j2 < i2) {
//                            j2 = i2;
//                        }
//
//                        if (k2 < i2) {
//                            k2 = i2;
//                        }
//
//                        int l2 = i2;
//                        if (i2 < j) {
//                            l2 = j;
//                        }
//
//                        if (j2 != k2) {
//                            Random random = new Random((long)(k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761));
//                            blockpos$mutable.setPos(k1, j2, j1);
//                            float f2 = biome.getTemperature(blockpos$mutable);
//                            if (f2 >= 0.15F) {
//                                if (i1 != 0) {
//                                    if (i1 >= 0) {
//                                        tessellator.draw();
//                                    }
//
//                                    i1 = 0;
//                                    mc.getTextureManager().bindTexture(RAIN_TEXTURES);
//                                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
//                                }
//
//                                int i3 = this.ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
//                                float f3 = -((float)i3 + partialTicks) / 32.0F * (3.0F + random.nextFloat());
//                                double d2 = (double)((float)k1 + 0.5F) - xIn;
//                                double d4 = (double)((float)j1 + 0.5F) - zIn;
//                                float f4 = MathHelper.sqrt(d2 * d2 + d4 * d4) / (float)l;
//                                float f5 = ((1.0F - f4 * f4) * 0.5F + 0.5F) * f;
//                                blockpos$mutable.setPos(k1, l2, j1);
//                                int j3 = WorldRenderer.getCombinedLight(world, blockpos$mutable);
//                                bufferbuilder.pos((double)k1 - xIn - d0 + 0.5D, (double)k2 - yIn, (double)j1 - zIn - d1 + 0.5D).tex(0.0F, (float)j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
//                                bufferbuilder.pos((double)k1 - xIn + d0 + 0.5D, (double)k2 - yIn, (double)j1 - zIn + d1 + 0.5D).tex(1.0F, (float)j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
//                                bufferbuilder.pos((double)k1 - xIn + d0 + 0.5D, (double)j2 - yIn, (double)j1 - zIn + d1 + 0.5D).tex(1.0F, (float)k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
//                                bufferbuilder.pos((double)k1 - xIn - d0 + 0.5D, (double)j2 - yIn, (double)j1 - zIn - d1 + 0.5D).tex(0.0F, (float)k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
//                            } else {
//                                if (i1 != 1) {
//                                    if (i1 >= 0) {
//                                        tessellator.draw();
//                                    }
//
//                                    i1 = 1;
//                                    mc.getTextureManager().bindTexture(SNOW_TEXTURES);
//                                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
//                                }
//
//                                float f6 = -((float)(worldRenderer.ticks & 511) + partialTicks) / 512.0F;
//                                float f7 = (float)(random.nextDouble() + (double)f1 * 0.01D * (double)((float)random.nextGaussian()));
//                                float f8 = (float)(random.nextDouble() + (double)(f1 * (float)random.nextGaussian()) * 0.001D);
//                                double d3 = (double)((float)k1 + 0.5F) - xIn;
//                                double d5 = (double)((float)j1 + 0.5F) - zIn;
//                                float f9 = MathHelper.sqrt(d3 * d3 + d5 * d5) / (float)l;
//                                float f10 = ((1.0F - f9 * f9) * 0.3F + 0.5F) * f;
//                                blockpos$mutable.setPos(k1, l2, j1);
//                                int k3 = WorldRenderer.getCombinedLight(world, blockpos$mutable);
//                                int l3 = k3 >> 16 & '\uffff';
//                                int i4 = (k3 & '\uffff') * 3;
//                                int j4 = (l3 * 3 + 240) / 4;
//                                int k4 = (i4 * 3 + 240) / 4;
//                                bufferbuilder.pos((double)k1 - xIn - d0 + 0.5D, (double)k2 - yIn, (double)j1 - zIn - d1 + 0.5D).tex(0.0F + f7, (float)j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).lightmap(k4, j4).endVertex();
//                                bufferbuilder.pos((double)k1 - xIn + d0 + 0.5D, (double)k2 - yIn, (double)j1 - zIn + d1 + 0.5D).tex(1.0F + f7, (float)j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).lightmap(k4, j4).endVertex();
//                                bufferbuilder.pos((double)k1 - xIn + d0 + 0.5D, (double)j2 - yIn, (double)j1 - zIn + d1 + 0.5D).tex(1.0F + f7, (float)k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).lightmap(k4, j4).endVertex();
//                                bufferbuilder.pos((double)k1 - xIn - d0 + 0.5D, (double)j2 - yIn, (double)j1 - zIn - d1 + 0.5D).tex(0.0F + f7, (float)k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).lightmap(k4, j4).endVertex();
//                            }
//                        }
//                    }
//                }
//            }
//
//            if (i1 >= 0) {
//                tessellator.draw();
//            }
//
//            RenderSystem.enableCull();
//            RenderSystem.disableBlend();
//            RenderSystem.defaultAlphaFunc();
//            RenderSystem.disableAlphaTest();
//            lightmapIn.disableLightmap();
//        }
//    }
}

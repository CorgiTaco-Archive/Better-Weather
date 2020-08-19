package corgitaco.betterweather.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.weatherevents.Blizzard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WorldRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow public static ResourceLocation SNOW_TEXTURES;

    @Shadow public static ResourceLocation RAIN_TEXTURES;

    @Shadow @Final private Minecraft mc;

    @Shadow public int ticks;

    @Shadow @Final private float[] rainSizeX;

    @Shadow @Final private float[] rainSizeZ;

    @Inject(at = @At("HEAD"), method = "renderRainSnow(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", cancellable = true)
    private void renderBlizzardSnow(LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn, CallbackInfo ci) {
        if (BetterWeather.BetterWeatherEvents.weatherData.isBlizzard()) {
            ci.cancel();
            float rainStrength = this.mc.world.getRainStrength(partialTicks);
            if (!(rainStrength <= 0.0F)) {
                lightmapIn.enableLightmap();
                World world = this.mc.world;
                int floorX = MathHelper.floor(xIn);
                int floorY = MathHelper.floor(yIn);
                int floorZ = MathHelper.floor(zIn);
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

                RenderSystem.depthMask(Minecraft.func_238218_y_());
                int i1 = -1;
                float ticksAndPartialTicks = (float) this.ticks + partialTicks;
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
                            int topPosY = world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockPos).getY();
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

                                    this.mc.getTextureManager().bindTexture(WorldRenderer.SNOW_TEXTURES);
                                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                float f7 = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.03D);
                                float fallSpeed = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.03D);
                                double d3 = (double) ((float) graphicQualityX + 0.5F) - xIn;
                                double d5 = (double) ((float) graphicQualityZ + 0.5F) - zIn;
                                float f9 = MathHelper.sqrt(d3 * d3 + d5 * d5) / (float) graphicsQuality;
                                float ticksAndPartialTicks0 = ((1.0F - f9 * f9) * 0.3F + 0.5F) * rainStrength;
                                blockPos.setPos(graphicQualityX, posY2, graphicQualityZ);
                                int k3 = WorldRenderer.getCombinedLight(world, blockPos);
                                int l3 = k3 >> 16 & '\uffff';
                                int i4 = (k3 & '\uffff') * 3;
                                int j4 = (l3 * 3 + 240) / 4;
                                int k4 = (i4 * 3 + 240) / 4;
                                if (Blizzard.doBlizzardsAffectDeserts(biome)) {
                                    bufferbuilder.pos((double) graphicQualityX - xIn - rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYPlusGraphicsQuality - yIn, (double) graphicQualityZ - zIn - rainSizeZ + 0.5D + random.nextGaussian()).tex(0.0F + f7, (float) floorYMinusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).lightmap(k4, j4).endVertex();
                                    bufferbuilder.pos((double) graphicQualityX - xIn + rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYPlusGraphicsQuality - yIn, (double) graphicQualityZ - zIn + rainSizeZ + 0.5D + random.nextGaussian()).tex(1.0F + f7, (float) floorYMinusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).lightmap(k4, j4).endVertex();
                                    bufferbuilder.pos((double) graphicQualityX - xIn + rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYMinusGraphicsQuality - yIn, (double) graphicQualityZ - zIn + rainSizeZ + 0.5D + random.nextGaussian()).tex(1.0F + f7, (float) floorYPlusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).lightmap(k4, j4).endVertex();
                                    bufferbuilder.pos((double) graphicQualityX - xIn - rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYMinusGraphicsQuality - yIn, (double) graphicQualityZ - zIn - rainSizeZ + 0.5D + random.nextGaussian()).tex(0.0F + f7, (float) floorYPlusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).lightmap(k4, j4).endVertex();
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
                    lightmapIn.disableLightmap();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "loadRenderers()V", cancellable = true)
    private void cancelGameSettingsUpdate(CallbackInfo ci) {
        if (mc.world != null) {
            if (BetterWeather.BetterWeatherEvents.weatherData.isBlizzard() && mc.world.isRaining()) {
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "addRainParticles(Lnet/minecraft/client/renderer/ActiveRenderInfo;)V", cancellable = true)
    private void stopRainParticles(ActiveRenderInfo activeRenderInfoIn, CallbackInfo ci) {
        if (mc.world != null) {
            if (BetterWeather.BetterWeatherEvents.weatherData.isBlizzard()) {
                ci.cancel();
            }
        }
    }
}

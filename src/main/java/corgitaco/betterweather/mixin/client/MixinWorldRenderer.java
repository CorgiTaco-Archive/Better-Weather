package corgitaco.betterweather.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.weatherevents.Blizzard;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(LevelRenderer.class)
public abstract class MixinWorldRenderer {

    @Shadow
    public int ticks;

    @Shadow
    @Final
    private float[] rainSizeX;

    @Shadow
    @Final
    private float[] rainSizeZ;


    @Shadow
    @Final
    private Minecraft minecraft;

    @Shadow
    @Final
    private static ResourceLocation SNOW_LOCATION;

    @Inject(at = @At("HEAD"), method = "renderSnowAndRain(Lnet/minecraft/client/renderer/LightTexture;FDDD)V", cancellable = true)
    private void renderBlizzardSnow(LightTexture lightmapIn, float partialTicks, double xIn, double yIn, double zIn, CallbackInfo ci) {
        if (BetterWeather.BetterWeatherEvents.weatherData.isBlizzard()) {
            ci.cancel();
            float rainStrength = this.minecraft.level.getRainLevel(partialTicks);
            if (!(rainStrength <= 0.0F)) {
                lightmapIn.turnOnLightLayer();
                Level world = this.minecraft.level;
                int floorX = Mth.floor(xIn);
                int floorY = Mth.floor(yIn);
                int floorZ = Mth.floor(zIn);
                Tesselator tessellator = Tesselator.getInstance();
                BufferBuilder bufferbuilder = tessellator.getBuilder();
                RenderSystem.enableAlphaTest();
                RenderSystem.disableCull();
                RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.defaultAlphaFunc();
                RenderSystem.enableDepthTest();
                int graphicsQuality = 5;
                if (Minecraft.useFancyGraphics()) {
                    graphicsQuality = 10;
                }

                RenderSystem.depthMask(Minecraft.useShaderTransparency());
                int i1 = -1;
                float ticksAndPartialTicks = (float) this.ticks + partialTicks;
                RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos();

                for (int graphicQualityZ = floorZ - graphicsQuality; graphicQualityZ <= floorZ + graphicsQuality; ++graphicQualityZ) {
                    for (int graphicQualityX = floorX - graphicsQuality; graphicQualityX <= floorX + graphicsQuality; ++graphicQualityX) {
                        int rainSizeIdx = (graphicQualityZ - floorZ + 16) * 32 + graphicQualityX - floorX + 16;
                        //These 2 doubles control the size of rain particles.
                        double rainSizeX = (double) this.rainSizeX[rainSizeIdx] * 0.5D;
                        double rainSizeZ = (double) this.rainSizeZ[rainSizeIdx] * 0.5D;
                        blockPos.set(graphicQualityX, 0, graphicQualityZ);
                        Biome biome = world.getBiome(blockPos);
                        int topPosY = BetterWeatherUtil.removeLeavesFromHeightMap(world, blockPos);
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
                            blockPos.set(graphicQualityX, floorYMinusGraphicsQuality, graphicQualityZ);

                            //This is rain rendering.
                            if (i1 != 1) {
                                if (i1 >= 0) {
                                    tessellator.end();
                                }

                                i1 = 1;
                                ResourceLocation THICC_SNOW = new ResourceLocation(BetterWeather.MOD_ID, "textures/environment/thick_snow.png");

                                this.minecraft.getTextureManager().bind(SNOW_LOCATION);
                                bufferbuilder.begin(7, DefaultVertexFormat.PARTICLE);
                            }

                            float f7 = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.03D);
                            float fallSpeed = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.03D);
                            double d3 = (double) ((float) graphicQualityX + 0.5F) - xIn;
                            double d5 = (double) ((float) graphicQualityZ + 0.5F) - zIn;
                            float f9 = Mth.sqrt(d3 * d3 + d5 * d5) / (float) graphicsQuality;
                            float ticksAndPartialTicks0 = ((1.0F - f9 * f9) * 0.3F + 0.5F) * rainStrength;
                            blockPos.set(graphicQualityX, posY2, graphicQualityZ);
                            int k3 = LevelRenderer.getLightColor(world, blockPos);
                            int l3 = k3 >> 16 & '\uffff';
                            int i4 = (k3 & '\uffff') * 3;
                            int j4 = (l3 * 3 + 240) / 4;
                            int k4 = (i4 * 3 + 240) / 4;
                            if (Blizzard.doBlizzardsAffectDeserts(biome)) {
                                bufferbuilder.vertex((double) graphicQualityX - xIn - rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYPlusGraphicsQuality - yIn, (double) graphicQualityZ - zIn - rainSizeZ + 0.5D + random.nextGaussian()).uv(0.0F + f7, (float) floorYMinusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).uv2(k4, j4).endVertex();
                                bufferbuilder.vertex((double) graphicQualityX - xIn + rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYPlusGraphicsQuality - yIn, (double) graphicQualityZ - zIn + rainSizeZ + 0.5D + random.nextGaussian()).uv(1.0F + f7, (float) floorYMinusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).uv2(k4, j4).endVertex();
                                bufferbuilder.vertex((double) graphicQualityX - xIn + rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYMinusGraphicsQuality - yIn, (double) graphicQualityZ - zIn + rainSizeZ + 0.5D + random.nextGaussian()).uv(1.0F + f7, (float) floorYPlusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).uv2(k4, j4).endVertex();
                                bufferbuilder.vertex((double) graphicQualityX - xIn - rainSizeX + 0.5D + random.nextGaussian() * 2, (double) floorYMinusGraphicsQuality - yIn, (double) graphicQualityZ - zIn - rainSizeZ + 0.5D + random.nextGaussian()).uv(0.0F + f7, (float) floorYPlusGraphicsQuality * 0.25F - Math.abs(fallSpeed)).color(1.0F, 1.0F, 1.0F, ticksAndPartialTicks0).uv2(k4, j4).endVertex();
                            }
                        }
                    }
                }

                if (i1 >= 0) {
                    tessellator.end();
                }

                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                RenderSystem.defaultAlphaFunc();
                RenderSystem.disableAlphaTest();
                lightmapIn.turnOffLightLayer();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "allChanged()V", cancellable = true)
    private void cancelGameSettingsUpdate(CallbackInfo ci) {
        if (minecraft.level != null) {
            //We do this to insure that the our Weather Data is not null before using it.
            BetterWeather.BetterWeatherEvents.setWeatherData(minecraft.level);
            if (BetterWeather.BetterWeatherEvents.weatherData.isBlizzard() && minecraft.level.isRaining()) {
                ci.cancel();
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "tickRain(Lnet/minecraft/client/Camera;)V", cancellable = true)
    private void stopRainParticles(Camera camera, CallbackInfo ci) {
        if (minecraft.level != null) {
            if (BetterWeather.BetterWeatherEvents.weatherData.isBlizzard()) {
                ci.cancel();
            }
        }
    }
}

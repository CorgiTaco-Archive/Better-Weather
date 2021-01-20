package corgitaco.betterweather.weatherevent.weatherevents;

import com.mojang.blaze3d.systems.RenderSystem;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Random;

import static net.minecraft.client.renderer.WorldRenderer.*;

public class LightRain extends WeatherEvent {

    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];

    public LightRain() {
        super(WeatherEventSystem.LIGHT_RAIN, 0.5);
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
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime) {
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc) {
    }

    @Override
    public boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {

        assert mc.world != null;
        float f = (mc.world.getRainStrength(partialTicks) * 0.3F);
        if (!(f <= 0.0F)) {
            int i = MathHelper.floor(x);
            int j = MathHelper.floor(y);
            int k = MathHelper.floor(z);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder bufferbuilder = tessellator.getBuffer();
            RenderSystem.enableAlphaTest();
            RenderSystem.disableCull();
            RenderSystem.normal3f(0.0F, 1.0F, 0.0F);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.defaultAlphaFunc();
            RenderSystem.enableDepthTest();
            int l = 5;
            if (Minecraft.isFancyGraphicsEnabled()) {
                l = 10;
            }

            RenderSystem.depthMask(Minecraft.isFabulousGraphicsEnabled());
            int i1 = -1;
            float ticksAndPartialTicks = (float) ticks + partialTicks;
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            for(int j1 = k - l; j1 <= k + l; ++j1) {
                for (int k1 = i - l; k1 <= i + l; ++k1) {
                    int l1 = (j1 - k + 16) * 32 + k1 - i + 16;
                    double d0 = (double) this.rainSizeX[l1] * 0.3D;
                    double d1 = (double) this.rainSizeZ[l1] * 0.3D;
                    blockpos$mutable.setPos(k1, 0, j1);
                    Biome biome = world.getBiome(blockpos$mutable);
                    if (biome.getPrecipitation() != Biome.RainType.NONE) {
                        int i2 = world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos$mutable).getY();
                        int j2 = j - l;
                        int k2 = j + l;
                        if (j2 < i2) {
                            j2 = i2;
                        }

                        if (k2 < i2) {
                            k2 = i2;
                        }

                        int l2 = i2;
                        if (i2 < j) {
                            l2 = j;
                        }

                        if (j2 != k2) {
                            Random random = new Random((long) (k1 * k1 * 3121 + k1 * 45238971 ^ j1 * j1 * 418711 + j1 * 13761));
                            blockpos$mutable.setPos(k1, j2, j1);
                            float f2 = biome.getTemperature(blockpos$mutable);
                            if (f2 >= 0.15F) {
                                if (i1 != 0) {
                                    if (i1 >= 0) {
                                        tessellator.draw();
                                    }

                                    i1 = 0;
                                    mc.getTextureManager().bindTexture(RAIN_TEXTURES);
                                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                int i3 = ticks + k1 * k1 * 3121 + k1 * 45238971 + j1 * j1 * 418711 + j1 * 13761 & 31;
                                float fallSpeed = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.01D);
                                float f3 = -((float) i3 + partialTicks) / 32.0F * (3.0F + random.nextFloat());
                                double d2 = (double) ((float) k1 + 0.5F) - x;
                                double d4 = (double) ((float) j1 + 0.5F) - z;
                                float f4 = MathHelper.sqrt(d2 * d2 + d4 * d4) / (float) l;
                                float f5 = ((1.0F - f4 * f4) * 0.5F + 0.5F) * f;
                                blockpos$mutable.setPos(k1, l2, j1);
                                int j3 = getCombinedLight(world, blockpos$mutable);
                                if(DoesItLightRainInDeserts(biome)) {
                                    bufferbuilder.pos((double) k1 - x - d0 + 0.5D, (double) k2 - y, (double) j1 - z - d1 + 0.5D).tex(0.0F + fallSpeed, (float) j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                                    bufferbuilder.pos((double) k1 - x + d0 + 0.5D, (double) k2 - y, (double) j1 - z + d1 + 0.5D).tex(1.0F + fallSpeed, (float) j2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                                    bufferbuilder.pos((double) k1 - x + d0 + 0.5D, (double) j2 - y, (double) j1 - z + d1 + 0.5D).tex(1.0F + fallSpeed, (float) k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                                    bufferbuilder.pos((double) k1 - x - d0 + 0.5D, (double) j2 - y, (double) j1 - z - d1 + 0.5D).tex(0.0F + fallSpeed, (float) k2 * 0.25F + f3).color(1.0F, 1.0F, 1.0F, f5).lightmap(j3).endVertex();
                                }
                            } else {
                                if (i1 != 1) {
                                    if (i1 >= 0) {
                                        tessellator.draw();
                                    }

                                    i1 = 1;
                                    mc.getTextureManager().bindTexture(SNOW_TEXTURES);
                                    bufferbuilder.begin(7, DefaultVertexFormats.PARTICLE_POSITION_TEX_COLOR_LMAP);
                                }

                                float f6 = -((float) (ticks & 511) + partialTicks) / 512.0F;
                                float fallSpeed2 = (float) (random.nextDouble() + (double) ticksAndPartialTicks * 0.01D * (double) ((float) random.nextGaussian()));
                                float f8 = (float) (random.nextDouble() + (double) (ticksAndPartialTicks * (float) random.nextGaussian()) * 0.001D);
                                double d3 = (double) ((float) k1 + 0.5F) - x;
                                double d5 = (double) ((float) j1 + 0.5F) - z;
                                float f9 = MathHelper.sqrt(d3 * d3 + d5 * d5) / (float) l;
                                float f10 = ((1.0F - f9 * f9) * 0.3F + 0.5F) * f;
                                blockpos$mutable.setPos(k1, l2, j1);
                                int k3 = getCombinedLight(world, blockpos$mutable);
                                int l3 = k3 >> 16 & '\uffff';
                                int i4 = (k3 & '\uffff') * 3;
                                int j4 = (l3 * 3 + 240) / 4;
                                int k4 = (i4 * 3 + 240) / 4;
                                if (DoesItLightRainInDeserts(biome)) {
                                    bufferbuilder.pos((double) k1 - x - d0 + 0.5D, (double) k2 - y, (double) j1 - z - d1 + 0.5D).tex(0.0F + fallSpeed2, (float) j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).lightmap(k4, j4).endVertex();
                                    bufferbuilder.pos((double) k1 - x + d0 + 0.5D, (double) k2 - y, (double) j1 - z + d1 + 0.5D).tex(1.0F + fallSpeed2, (float) j2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).lightmap(k4, j4).endVertex();
                                    bufferbuilder.pos((double) k1 - x + d0 + 0.5D, (double) j2 - y, (double) j1 - z + d1 + 0.5D).tex(1.0F + fallSpeed2, (float) k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).lightmap(k4, j4).endVertex();
                                    bufferbuilder.pos((double) k1 - x - d0 + 0.5D, (double) j2 - y, (double) j1 - z - d1 + 0.5D).tex(0.0F + fallSpeed2, (float) k2 * 0.25F + f6 + f8).color(1.0F, 1.0F, 1.0F, f10).lightmap(k4, j4).endVertex();
                                }
                            }
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
        }

        return true;
    }

    public static boolean DoesItLightRainInDeserts(Biome biome) {
        if (!BetterWeatherConfig.doesLightRainOccurInDeserts.get())
            return biome.getCategory() != Biome.Category.DESERT;
        else
            return true;
    }

    @Override
    public Color modifySkyColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return BetterWeatherUtil.blendColor(returnColor, BetterWeatherUtil.DEFAULT_RAIN_SKY, (rainStrength * 0.7));
    }

    @Override
    public Color modifyCloudColor(Color returnColor, float rainStrength) {
        return BetterWeatherUtil.blendColor(returnColor, BetterWeatherUtil.DEFAULT_RAIN_CLOUDS, (rainStrength * 0.7));
    }

    @Override
    public Color modifyFogColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return BetterWeatherUtil.blendColor(returnColor, BetterWeatherUtil.DEFAULT_RAIN_FOG, (rainStrength * 0.7));
    }

    @Override
    public boolean weatherParticlesAndSound(ActiveRenderInfo renderInfo, Minecraft mc) {
        return false;
    }

    @Override
    public boolean fillBlocksWithWater() {
        return true;
    }

    @Override
    public boolean drippingLeaves() {
        return true;
    }

    @Override
    public float dayLightDarkness() {
        return 2.0F;
    }
}

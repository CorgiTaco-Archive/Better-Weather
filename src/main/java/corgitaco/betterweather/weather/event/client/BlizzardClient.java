package corgitaco.betterweather.weather.event.client;

import com.mojang.blaze3d.systems.RenderSystem;
import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.weather.WeatherEventAudio;
import corgitaco.betterweather.graphics.Graphics;
import corgitaco.betterweather.graphics.opengl.ShaderProgram;
import corgitaco.betterweather.graphics.opengl.ShaderProgramBuilder;
import corgitaco.betterweather.weather.event.client.settings.BlizzardClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class BlizzardClient extends WeatherEventClient<BlizzardClientSettings> implements WeatherEventAudio {
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];
    private final ResourceLocation textureLocation;
    private final SoundEvent audio;
    private final float audioVolume;
    private final float audioPitch;

    private final Consumer<ShaderProgramBuilder> builder = builder -> {
        // TODO: read file paths from within assets
    };

    public BlizzardClient(BlizzardClientSettings clientSettings) {
        super(clientSettings);
        this.textureLocation = clientSettings.textureLocation;
        this.audio = clientSettings.getSound();
        this.audioVolume = clientSettings.getVolume();
        this.audioPitch = clientSettings.getPitch();

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
    public boolean renderWeatherShaders() {
        ShaderProgram program = buildOrGetProgram(builder);

        program.bind();

        program.unbind();

        return true;
    }

    @Override
    public boolean renderWeatherLegacy(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y,  double z, Predicate<Biome> biomePredicate) {
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
                int topPosY = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, blockPos.getX(), blockPos.getZ());
                int floorYMinusGraphicsQuality = floorY - graphicsQuality;
                int floorYPlusGraphicsQuality = floorY + graphicsQuality;
                if (floorYMinusGraphicsQuality < topPosY) {
                    floorYMinusGraphicsQuality = topPosY;
                }

                if (floorYPlusGraphicsQuality < topPosY) {
                    floorYPlusGraphicsQuality = topPosY;
                }

                int posY2 = Math.max(topPosY, floorY);

                if (floorYMinusGraphicsQuality != floorYPlusGraphicsQuality) {
                    Random random = new Random(graphicQualityX * graphicQualityX * 3121 + graphicQualityX * 45238971 ^ graphicQualityZ * graphicQualityZ * 418711 + graphicQualityZ * 13761);
                    blockPos.setPos(graphicQualityX, floorYMinusGraphicsQuality, graphicQualityZ);

                    //This is rain rendering.
                    if (i1 != 1) {
                        if (i1 >= 0) {
                            tessellator.draw();
                        }

                        i1 = 1;
                        mc.getTextureManager().bindTexture(this.textureLocation);
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
                    if (biomePredicate.test(biome)) {
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

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc, Predicate<Biome> biomePredicate) {
    }

    @Override
    public float getVolume() {
        return this.audioVolume;
    }

    @Override
    public float getPitch() {
        return this.audioPitch;
    }

    @Override
    public SoundEvent getSound() {
        return this.audio;
    }
}

package corgitaco.betterweather.weather.event.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.api.client.ColorSettings;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;

public class AcidRainClientSettings extends RainClientSettings {

    public static final Codec<AcidRainClientSettings> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(ColorSettings.CODEC.fieldOf("colorSettings").forGetter(rainClientSettings -> {
            return rainClientSettings.getColorSettings();
        }), Codec.FLOAT.fieldOf("skyOpacity").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.skyOpacity();
        }), Codec.FLOAT.fieldOf("fogDensity").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.fogDensity();
        }), Codec.BOOL.fieldOf("sunsetSunriseColor").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.sunsetSunriseColor();
        }), ResourceLocation.CODEC.fieldOf("rainTexture").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.rainTexture;
        }), ResourceLocation.CODEC.fieldOf("snowTexture").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.snowTexture;
        }), Codec.BOOL.fieldOf("snowTexture").forGetter(blizzardClientSettings -> {
            return blizzardClientSettings.addSmokeParticles;
        })).apply(builder, AcidRainClientSettings::new);
    });

    private final boolean addSmokeParticles;

    public AcidRainClientSettings(ColorSettings colorSettings, float skyOpacity, float fogDensity, boolean sunsetSunriseColor, ResourceLocation rainTexture, ResourceLocation snowTexture, boolean addSmokeParticles) {
        super(colorSettings, skyOpacity, fogDensity, sunsetSunriseColor, rainTexture, snowTexture);
        this.addSmokeParticles = addSmokeParticles;
    }

    @Override
    protected void addParticlesToWorld(Minecraft mc, BlockPos motionBlockingHeightMinus1, double randDouble, double randDouble2, BlockState blockstate, FluidState fluidstate, double particleMaxAddedY) {
        if (addSmokeParticles) {
            mc.world.addParticle(ParticleTypes.SMOKE, (double) motionBlockingHeightMinus1.getX() + randDouble, (double) motionBlockingHeightMinus1.getY() + particleMaxAddedY, (double) motionBlockingHeightMinus1.getZ() + randDouble2, 0.0D, 0.0D, 0.0D);
        }
        super.addParticlesToWorld(mc, motionBlockingHeightMinus1, randDouble, randDouble2, blockstate, fluidstate, particleMaxAddedY);
    }

    @Override
    public Codec<? extends WeatherEventClientSettings> codec() {
        return CODEC;
    }
}

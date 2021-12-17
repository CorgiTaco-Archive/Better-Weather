package corgitaco.betterweather.common.weather.event.client;

import corgitaco.betterweather.common.weather.event.client.settings.AcidRainClientSettings;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;

public class AcidRainClient extends RainClient {

    private final boolean addSmokeParticles;

    public AcidRainClient(AcidRainClientSettings clientSettings) {
        super(clientSettings);
        this.addSmokeParticles = clientSettings.addSmokeParticles;
    }

    @Override
    protected void addParticlesToWorld(Minecraft mc, BlockPos motionBlockingHeightMinus1, double randDouble, double randDouble2, BlockState blockstate, FluidState fluidstate, double particleMaxAddedY) {
        if (addSmokeParticles) {
            mc.level.addParticle(ParticleTypes.SMOKE, (double) motionBlockingHeightMinus1.getX() + randDouble, (double) motionBlockingHeightMinus1.getY() + particleMaxAddedY, (double) motionBlockingHeightMinus1.getZ() + randDouble2, 0.0D, 0.0D, 0.0D);
        }
        super.addParticlesToWorld(mc, motionBlockingHeightMinus1, randDouble, randDouble2, blockstate, fluidstate, particleMaxAddedY);
    }
}

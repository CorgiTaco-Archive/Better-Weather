package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.access.WeatherViewFrustum;
import net.minecraft.client.renderer.ViewFrustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ViewFrustum.class)
public abstract class MixinViewFrustum implements WeatherViewFrustum {

    @Shadow
    protected abstract void setCountChunksXYZ(int renderDistanceChunks);

    @Shadow
    public abstract void updateChunkPositions(double viewEntityX, double viewEntityZ);

    @Override
    public void forceRenderDistance(int renderDistance, double x, double y, double z) {
        this.setCountChunksXYZ(renderDistance);
        this.updateChunkPositions(x, z);
    }
}

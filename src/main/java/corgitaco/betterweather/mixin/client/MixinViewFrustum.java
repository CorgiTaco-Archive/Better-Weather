package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.helper.WeatherViewFrustum;
import net.minecraft.client.renderer.ViewFrustum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ViewFrustum.class)
public abstract class MixinViewFrustum implements WeatherViewFrustum {

    @Shadow
    protected abstract void setCountChunksXYZ(int renderDistanceChunks);

    @Shadow
    public abstract void updateChunkPositions(double viewEntityX, double viewEntityZ);

    /**
     * @param renderDistance Forced Render distance specified by the current weather event.
     *
     * Called every Weather Event Changing Packet, and at the tail of loadRenderers.
     *
     * Forces the current render distance down for the current weather event.
     */
    @Override
    public void forceRenderDistance(int renderDistance, double x, double y, double z) {
        if (!BetterWeather.usingOptifine) {
            this.setCountChunksXYZ(renderDistance);
            this.updateChunkPositions(x, z);
        }
    }
}

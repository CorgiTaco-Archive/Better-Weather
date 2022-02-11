package corgitaco.betterweather.mixin.chunk;

import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import corgitaco.betterweather.util.DirtyTickTracker;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    @Inject(method = "broadcastChanges", at = @At("HEAD"))
    private void runChunkUpdates(Chunk chunk, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) chunk.getLevel();

        WeatherContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherContext();

        if (weatherEventContext != null) {
            if (!((DirtyTickTracker) chunk).isTickDirty()) {
                weatherEventContext.getCurrentEvent().doChunkLoad(chunk, world);
                ((DirtyTickTracker) chunk).setTickDirty();
            }
        }
    }
}
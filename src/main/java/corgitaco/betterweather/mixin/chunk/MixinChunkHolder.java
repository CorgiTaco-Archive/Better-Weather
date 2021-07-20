package corgitaco.betterweather.mixin.chunk;

import corgitaco.betterweather.chunk.TickHelper;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {

    @Inject(method = "sendChanges", at = @At("HEAD"))
    private void runChunkUpdates(Chunk chunk, CallbackInfo ci) {
        ServerWorld world = (ServerWorld) chunk.getWorld();

        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();

        if (weatherEventContext != null) {
            if (!((TickHelper) chunk).isTickDirty()) {
                weatherEventContext.getCurrentEvent().doChunkLoad(chunk, world);
                ((TickHelper) chunk).setTickDirty();
            }
        }
    }
}
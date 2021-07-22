package corgitaco.betterweather.mixin;

import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkRenderDispatcher.class)
public interface ChunkRenderDispatcherAccess {

    @Accessor
    RegionRenderCacheBuilder getFixedBuilder();
}

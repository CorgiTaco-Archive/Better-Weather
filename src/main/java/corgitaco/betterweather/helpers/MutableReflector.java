package corgitaco.betterweather.helpers;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

public interface MutableReflector {

    void mutate(ChunkRenderDispatcher.CompiledChunk newChunk);
}

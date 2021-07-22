package corgitaco.betterweather.helpers;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;

public interface RebuildTaskMaker {


    ChunkRenderDispatcher.ChunkRender.RebuildTask createRebuildTask(ChunkRenderDispatcher.ChunkRender render);
}

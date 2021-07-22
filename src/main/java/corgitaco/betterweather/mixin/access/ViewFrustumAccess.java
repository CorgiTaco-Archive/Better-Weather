package corgitaco.betterweather.mixin.access;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ViewFrustum.class)
public interface ViewFrustumAccess {

    @Accessor
    ChunkRenderDispatcher.ChunkRender[] getRenderChunks();

    @Accessor
    void setRenderChunks(ChunkRenderDispatcher.ChunkRender[] renderChunks);

    @Invoker
    void invokeCreateRenderChunks(ChunkRenderDispatcher renderChunkFactory);
}

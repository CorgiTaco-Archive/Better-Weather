package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.helpers.RebuildTaskMaker;
import corgitaco.betterweather.mixin.access.ChunkRenderAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkRenderDispatcher.ChunkRender.class)
public class MixinChunkRenderDispatcher implements RebuildTaskMaker {

    @Override
    public ChunkRenderDispatcher.ChunkRender.RebuildTask createRebuildTask(ChunkRenderDispatcher.ChunkRender newRenderChunk) {
        BlockPos blockPos = newRenderChunk.getPosition().toImmutable();
        ChunkRenderCache chunkrendercache = newRenderChunk.createRegionRenderCache(Minecraft.getInstance().world, blockPos.add(-1, -1, -1), blockPos.add(16, 16, 16), 1);

        return newRenderChunk.new RebuildTask(new ChunkPos(newRenderChunk.getPosition()), ((ChunkRenderAccess) newRenderChunk).invokeGetDistanceSq(), chunkrendercache);
    }

    @SuppressWarnings("unchecked")
    private static <I, O> O unsafeCast(I obj) {
        return (O) obj;
    }
}

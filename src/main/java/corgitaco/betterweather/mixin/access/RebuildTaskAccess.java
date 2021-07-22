package corgitaco.betterweather.mixin.access;

import net.minecraft.client.renderer.RegionRenderCacheBuilder;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Set;

@Mixin(ChunkRenderDispatcher.ChunkRender.RebuildTask.class)
public interface RebuildTaskAccess {


    @Invoker
    Set<TileEntity> invokeCompile(float xIn, float yIn, float zIn, ChunkRenderDispatcher.CompiledChunk compiledChunkIn, RegionRenderCacheBuilder builderIn);
}

package corgitaco.betterweather.mixin.access;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.SetVisibility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Set;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public interface CompiledChunkAccess {

    @Accessor
    Set<RenderType> getLayersUsed();

    @Accessor
    Set<RenderType> getLayersStarted();

    @Accessor
    SetVisibility getSetVisibility();

    @Accessor
    BufferBuilder.State getState();

}

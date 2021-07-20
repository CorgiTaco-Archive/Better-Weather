package corgitaco.betterweather.mixin.access;

import net.minecraft.client.renderer.ViewFrustum;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WorldRenderer.class)
public interface WorldRendererAccess {

    @Accessor
    ViewFrustum getViewFrustum();

    @Accessor
    ChunkRenderDispatcher getRenderDispatcher();
}

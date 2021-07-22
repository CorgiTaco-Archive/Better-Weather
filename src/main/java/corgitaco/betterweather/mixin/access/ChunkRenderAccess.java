package corgitaco.betterweather.mixin.access;

import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkRenderDispatcher.ChunkRender.class)
public interface ChunkRenderAccess {

    @Invoker
    double invokeGetDistanceSq();
}

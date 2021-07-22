package corgitaco.betterweather.mixin.client;


import corgitaco.betterweather.helpers.MutableReflector;
import corgitaco.betterweather.mixin.access.CompiledChunkAccess;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.client.renderer.chunk.SetVisibility;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

@Mixin(ChunkRenderDispatcher.CompiledChunk.class)
public class MixinCompiledChunk implements MutableReflector {

    @Shadow
    private boolean empty;

    @Mutable
    @Shadow
    @Final
    private List<TileEntity> tileEntities;

    @Mutable
    @Shadow
    @Final
    private Set<RenderType> layersStarted;

    @Mutable
    @Shadow
    @Final
    private Set<RenderType> layersUsed;

    @Shadow private SetVisibility setVisibility;

    @Shadow @Nullable private BufferBuilder.State state;

    public void mutate(ChunkRenderDispatcher.CompiledChunk newValue) {
        this.empty = newValue.isEmpty();
        this.tileEntities = newValue.getTileEntities();
        this.layersStarted = ((CompiledChunkAccess) newValue).getLayersStarted();
        this.layersUsed = ((CompiledChunkAccess) newValue).getLayersUsed();
        this.setVisibility = ((CompiledChunkAccess) newValue).getSetVisibility();
        this.state = ((CompiledChunkAccess) newValue).getState();
//        for (Field declaredField : this.getClass().getFields()) {
//            try {
//                Field compiledChunk = this.getClass().getField(declaredField.getName());
//                compiledChunk.setAccessible(true);
//                Field newField = newValue.getClass().getField(compiledChunk.getName());
//                newField.setAccessible(true);
//                compiledChunk.set(this, newField.get(newValue));
//            } catch (NoSuchFieldException | IllegalAccessException e) {
//                e.printStackTrace();
//            }
//        }
    }
}

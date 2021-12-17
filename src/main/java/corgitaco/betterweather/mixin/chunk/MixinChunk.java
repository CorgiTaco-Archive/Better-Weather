package corgitaco.betterweather.mixin.chunk;

import corgitaco.betterweather.util.DirtyTickTracker;
import net.minecraft.world.chunk.Chunk;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Chunk.class)
public abstract class MixinChunk implements DirtyTickTracker {

    private boolean isTickDirty;
    
    @Override
    public boolean isTickDirty() {
        return isTickDirty;
    }

    @Override
    public void setTickDirty() {
        isTickDirty = true;
    }
}

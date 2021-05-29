package corgitaco.betterweather.mixin.access;

import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ChunkManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ChunkManager.class)
public interface ChunkManagerAccess {

    @Accessor
    void setGenerator(ChunkGenerator chunkGenerator);
}

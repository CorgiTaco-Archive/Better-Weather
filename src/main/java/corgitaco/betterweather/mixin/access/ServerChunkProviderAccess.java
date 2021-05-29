package corgitaco.betterweather.mixin.access;

import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ServerChunkProvider.class)
public interface ServerChunkProviderAccess {

    @Accessor
    void setGenerator(ChunkGenerator chunkGenerator);
}

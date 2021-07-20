package corgitaco.betterweather.mixin.access;

import net.minecraft.client.multiplayer.ClientChunkProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ClientChunkProvider.class)
public interface ClientChunkSourceAccess {

    @Accessor
    ClientChunkProvider.ChunkArray getArray();

}

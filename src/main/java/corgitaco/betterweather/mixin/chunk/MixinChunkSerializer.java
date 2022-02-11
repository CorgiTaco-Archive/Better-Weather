package corgitaco.betterweather.mixin.chunk;

import corgitaco.betterweather.common.savedata.BetterWeatherChunkData;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.village.PointOfInterestManager;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkPrimerWrapper;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.storage.ChunkSerializer;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkSerializer.class)
public class MixinChunkSerializer {

    @Inject(method = "read", at = @At("RETURN"))
    private static void readBetterWeatherChunkData(ServerWorld chunksection, TemplateManager compoundnbt1, PointOfInterestManager k, ChunkPos j, CompoundNBT nbt, CallbackInfoReturnable<ChunkPrimer> cir) {
        if (cir.getReturnValue() instanceof ChunkPrimerWrapper) {
            ChunkPrimerWrapper returnValue = (ChunkPrimerWrapper) cir.getReturnValue();
            CompoundNBT betterWeatherChunkData = nbt.getCompound("betterWeatherChunkData");
            Chunk wrapped = returnValue.getWrapped();
            ((BetterWeatherChunkData.Access) wrapped).set(new BetterWeatherChunkData(betterWeatherChunkData.getLong("lastLoadTime")));
        }
    }


    @Inject(method = "write", at = @At("RETURN"))
    private static void writeBetterWeatherChunkData(ServerWorld world, IChunk chunk, CallbackInfoReturnable<CompoundNBT> cir) {
        if (chunk instanceof Chunk) {
            CompoundNBT returnValue = cir.getReturnValue();
            returnValue.put("betterWeatherChunkData", ((BetterWeatherChunkData.Access) chunk).get().save());
        }
    }
}

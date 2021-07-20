package corgitaco.betterweather.mixin.client.chunk;

import corgitaco.betterweather.chunk.BlockColors;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LeavesBlock;
import net.minecraft.client.multiplayer.ClientChunkProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientChunkProvider.class)
public class MixinClientChunkCache {


    @Inject(method = "loadChunk", at = @At("RETURN"))
    private void fillList(int chunkX, int chunkZ, BiomeContainer biomeContainerIn, PacketBuffer packetIn, CompoundNBT nbtTagIn, int sizeIn, boolean fullChunk, CallbackInfoReturnable<Chunk> cir) {
        Chunk chunk = cir.getReturnValue();
        if (chunk == null) {
            return;
        }

        for (ChunkSection section : chunk.getSections()) {
            if (!ChunkSection.isEmpty(section)) {
                for (int x = 0; x < 16; x++) {
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            BlockState blockState = section.getBlockState(x, y, z);

                            if (blockState.getBlock() instanceof LeavesBlock || blockState.getBlock() == Blocks.GRASS_BLOCK) {
                                ((BlockColors) chunk).getColoredBlocks().put(new BlockPos(SectionPos.toWorld(chunkX) + x, section.getYLocation() + y, SectionPos.toWorld(chunkZ) + z), blockState);
                            }
                        }
                    }
                }
            }
        }

    }
}

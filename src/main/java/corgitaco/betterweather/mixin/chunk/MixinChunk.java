package corgitaco.betterweather.mixin.chunk;

import corgitaco.betterweather.common.savedata.BetterWeatherChunkData;
import corgitaco.betterweather.util.DirtyTickTracker;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mixin(Chunk.class)
public abstract class MixinChunk implements DirtyTickTracker, BetterWeatherChunkData.Access {

    private boolean isTickDirty;
    @Nullable
    private BetterWeatherChunkData betterWeatherChunkData = null;


    @Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/util/math/ChunkPos;Lnet/minecraft/world/biome/BiomeContainer;Lnet/minecraft/util/palette/UpgradeData;Lnet/minecraft/world/ITickList;Lnet/minecraft/world/ITickList;J[Lnet/minecraft/world/chunk/ChunkSection;Ljava/util/function/Consumer;)V", at = @At("RETURN"))
    private void attachChunkData(World world, ChunkPos p_i225781_2_, BiomeContainer p_i225781_3_, UpgradeData p_i225781_4_, ITickList<Block> p_i225781_5_, ITickList<Fluid> p_i225781_6_, long p_i225781_7_, @Nullable ChunkSection[] p_i225781_9_, @Nullable Consumer<Chunk> p_i225781_10_, CallbackInfo ci) {
        if (!world.isClientSide) {
            betterWeatherChunkData = new BetterWeatherChunkData(world.getGameTime());
        }
    }

    @Override
    public boolean isTickDirty() {
        return isTickDirty;
    }

    @Override
    public void setTickDirty() {
        isTickDirty = true;
    }

    @Override
    public BetterWeatherChunkData get() {
        return this.betterWeatherChunkData;
    }

    @Override
    public BetterWeatherChunkData set(BetterWeatherChunkData betterWeatherChunkData) {
        this.betterWeatherChunkData = betterWeatherChunkData;
        return this.betterWeatherChunkData;
    }
}

package corgitaco.betterweather.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.BlockModelRenderer;
import net.minecraft.client.renderer.chunk.ChunkRenderCache;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockModelRenderer.class)
public class MixinBlockModelRenderer {

    @Inject(method = "renderQuadSmooth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/color/BlockColors;getColor(Lnet/minecraft/block/BlockState;Lnet/minecraft/world/IBlockDisplayReader;Lnet/minecraft/util/math/BlockPos;I)I"), cancellable = true)
    private void skipIfHasTintIndex(IBlockDisplayReader blockAccessIn, BlockState stateIn, BlockPos posIn, IVertexBuilder buffer, MatrixStack.Entry matrixEntry, BakedQuad quadIn, float colorMul0, float colorMul1, float colorMul2, float colorMul3, int brightness0, int brightness1, int brightness2, int brightness3, int combinedOverlayIn, CallbackInfo ci) {
//        if (blockAccessIn instanceof ChunkRenderCache) {
//            ci.cancel();
//        }
    }
}

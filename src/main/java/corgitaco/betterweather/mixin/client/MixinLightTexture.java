package corgitaco.betterweather.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;


@Mixin(LightTexture.class)
public abstract class MixinLightTexture {


    @Shadow
    @Final
    private DynamicTexture dynamicTexture;

    @Shadow
    @Final
    private Minecraft client;
    @Shadow
    @Final
    private GameRenderer entityRenderer;
    @Shadow
    private float torchFlicker;
    @Shadow
    private boolean needsUpdate;
    @Shadow
    @Final
    private NativeImage nativeImage;

    @Shadow
    protected abstract float invGamma(float valueIn);

    @Shadow
    protected abstract float getLightBrightness(World worldIn, int lightLevelIn);

//    @Inject(method = "updateLightmap", at = @At("HEAD"), cancellable = true)
//    private void doOurLightMap(float partialTicks, CallbackInfo ci) {
//        ci.cancel();
//        if (this.needsUpdate) {
//            needsUpdate = BetterWeatherUtil.updateLightmap(partialTicks, torchFlicker, client, entityRenderer, nativeImage, dynamicTexture);
//        }
//    }
}

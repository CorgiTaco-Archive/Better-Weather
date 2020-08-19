package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.block.Blocks;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundEngine;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SoundEngine.class)
public class MixinSoundEngine {

    @Final
    @Shadow
    private GameSettings options;

    @Inject(at = @At("HEAD"), method = "getVolume(Lnet/minecraft/util/SoundCategory;)F", cancellable = true)
    private void weatherVolumeController(SoundCategory category, CallbackInfoReturnable<Float> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.world != null) {
            if (category == SoundCategory.WEATHER && BetterWeather.BetterWeatherEvents.weatherData.isBlizzard() && minecraft.world.getWorldInfo().isRaining()) {
                BlockPos pos = minecraft.gameRenderer.getActiveRenderInfo().getBlockPos();
                int motionBlockingY = minecraft.world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ());

                float finalVolume;
                float playerHeightToMotionBlockingHeightDifference = (motionBlockingY - pos.getY()) * 0.02F;
                float heightMapCalculatedVolume = options.getSoundLevel(SoundCategory.WEATHER) - (playerHeightToMotionBlockingHeightDifference + 0.5F);
                //Implement a protection to prevent the sound from stopping when it reaches volume 0.0F.
                if (pos.getY() < motionBlockingY) {
                    if (heightMapCalculatedVolume >= 0.05)
                        finalVolume = heightMapCalculatedVolume;
                    else
                        finalVolume = 0.04F;

                    //Check if the player is underwater then chop the noise volume in half(essentially muffling it)
                    if (minecraft.world.getBlockState(pos).getBlock() == Blocks.WATER && minecraft.world.getBlockState(pos).getFluidState().getLevel() >= 6)
                        finalVolume = finalVolume / 2;

                    cir.setReturnValue(finalVolume);
                }
            }
        }
    }
}


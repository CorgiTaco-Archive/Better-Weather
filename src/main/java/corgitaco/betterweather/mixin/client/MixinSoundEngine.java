package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;
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
    private Options options;

    @Inject(at = @At("HEAD"), method = "getVolume", cancellable = true)
    private void weatherVolumeController(SoundSource category, CallbackInfoReturnable<Float> cir) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level != null) {
            if (category == SoundSource.WEATHER && BetterWeather.BetterWeatherEvents.weatherData.isBlizzard() && minecraft.level.getLevelData().isRaining()) {
                BlockPos pos = minecraft.gameRenderer.getMainCamera().getBlockPosition();
                int motionBlockingY = BetterWeatherUtil.removeLeavesFromHeightMap(minecraft.level, pos);

                float finalVolume;
                float playerHeightToMotionBlockingHeightDifference = (motionBlockingY - pos.getY()) * 0.02F;
                float heightMapCalculatedVolume = options.getSoundSourceVolume(SoundSource.WEATHER) - (playerHeightToMotionBlockingHeightDifference + 0.5F);
                //Implement a protection to prevent the sound from stopping when it reaches volume 0.0F.
                if (pos.getY() < motionBlockingY) {
                    if (heightMapCalculatedVolume >= 0.05)
                        finalVolume = heightMapCalculatedVolume;
                    else
                        finalVolume = 0.04F;

                    //Check if the player is underwater then chop the noise volume in half(essentially muffling it)
                    if (minecraft.level.getBlockState(pos).getBlock() == Blocks.WATER && minecraft.level.getBlockState(pos).getFluidState().getAmount() >= 6)
                        finalVolume = finalVolume / 2;

                    cir.setReturnValue(finalVolume);
                }
            }
        }
    }
}


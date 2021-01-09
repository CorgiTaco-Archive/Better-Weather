package corgitaco.betterweather.mixin;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.Season;
import net.minecraft.client.KeyboardListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.InputMappings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardListener.class)
public abstract class MixinKeyboardListener {

    @Shadow
    protected abstract void printDebugMessage(String message, Object... args);

    private static int idx = 0;


    @Inject(method = "onKeyEvent", at = @At("RETURN"))
    private void addConfigReloadKeybind(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (InputMappings.isKeyDown(minecraft.getMainWindow().getHandle(), 293) && key == 82) {
            if (idx == 0) {
                BetterWeather.loadCommonConfigs();
                minecraft.worldRenderer.loadRenderers();
                Season.SubSeason.SeasonClient.stopSpamIDXFoliage = 0;
                Season.SubSeason.SeasonClient.stopSpamIDXGrass = 0;
                Season.SubSeason.SeasonClient.stopSpamIDXSky = 0;
                Season.SubSeason.SeasonClient.stopSpamIDXFog = 0;
                this.printDebugMessage("bw.debug.reloadconfig.message");
                idx = 1;
            }
        }
        else
            idx = 0;
    }
}

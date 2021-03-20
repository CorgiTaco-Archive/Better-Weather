package corgitaco.betterweather.mixin.client;

import net.minecraft.client.KeyboardListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardListener.class)
public abstract class MixinKeyboardListener {


    @Inject(method = "onKeyEvent", at = @At("RETURN"))
    private void addConfigReloadKeybind(long windowPointer, int key, int scanCode, int action, int modifiers, CallbackInfo ci) {
//        BetterWeatherClientUtil.configReloadKeybind(key);
    }
}

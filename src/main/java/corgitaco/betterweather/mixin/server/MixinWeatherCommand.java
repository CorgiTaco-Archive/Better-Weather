package corgitaco.betterweather.mixin.server;

import net.minecraft.command.CommandSource;
import net.minecraft.command.impl.WeatherCommand;
import net.minecraft.util.text.TranslationTextComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeatherCommand.class)
public abstract class MixinWeatherCommand {

    @Inject(method = "setRain", at = @At("HEAD"), cancellable = true)
    private static void cancelRain(CommandSource source, int time, CallbackInfoReturnable<Integer> cir) {
        source.sendFeedback(new TranslationTextComponent("commands.bw.vanillaweather.fail"), true);
        cir.setReturnValue(0);
    }

    @Inject(method = "setClear", at = @At("HEAD"), cancellable = true)
    private static void cancelClear(CommandSource source, int time, CallbackInfoReturnable<Integer> cir) {
        source.sendFeedback(new TranslationTextComponent("commands.bw.vanillaweather.fail"), true);
        cir.setReturnValue(0);
    }

    @Inject(method = "setThunder", at = @At("HEAD"), cancellable = true)
    private static void cancelThunder(CommandSource source, int time, CallbackInfoReturnable<Integer> cir) {
        source.sendFeedback(new TranslationTextComponent("commands.bw.vanillaweather.fail"), true);
        cir.setReturnValue(0);
    }
}

package corgitaco.betterweather.mixin.biome;

import corgitaco.betterweather.helpers.IBiomeModifier;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;


@Mixin(Biome.class)
public class MixinBiome implements IBiomeModifier {

    @Shadow
    @Final
    private Biome.Climate climate;
    private Supplier<Float> tempModifier = () -> 0.0F;
    private Supplier<Float> humidityModifier = () -> 0.0F;

    @Inject(method = "getDownfall", at = @At("RETURN"), cancellable = true)
    private void modifyDownfall(CallbackInfoReturnable<Float> cir) {
        if (humidityModifier == null) {
            String s = "";
        }

        cir.setReturnValue(this.climate.downfall + humidityModifier.get());
    }

    @Inject(method = "getTemperature()F", at = @At("RETURN"), cancellable = true)
    private void modifyTemperature(CallbackInfoReturnable<Float> cir) {
        if (tempModifier == null) {
            String s = "";
        }

        cir.setReturnValue(this.climate.temperature + tempModifier.get());
    }

    @Override
    public void setTempModifier(float newValue) {
        this.tempModifier = () -> newValue;
    }

    @Override
    public void setHumidityModifier(float newValue) {
        this.humidityModifier = () -> newValue;
    }
}

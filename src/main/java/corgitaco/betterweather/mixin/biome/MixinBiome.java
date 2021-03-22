package corgitaco.betterweather.mixin.biome;

import corgitaco.betterweather.api.BiomeClimate;
import corgitaco.betterweather.helpers.BiomeModifier;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;


@Mixin(Biome.class)
public abstract class MixinBiome implements BiomeModifier, BiomeClimate {

    @Shadow
    @Final
    private Biome.Climate climate;
    private Supplier<Float> tempModifier = () -> 0.0F;
    private Supplier<Float> humidityModifier = () -> 0.0F;

    @Inject(method = "getDownfall", at = @At("RETURN"), cancellable = true)
    private void modifyDownfall(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(this.climate.downfall + humidityModifier.get());
    }

    @Inject(method = "getTemperature()F", at = @At("RETURN"), cancellable = true)
    private void modifyTemperature(CallbackInfoReturnable<Float> cir) {
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

    @Override
    public double getTemperatureModifier() {
        return tempModifier.get();
    }

    @Override
    public double getHumidityModifier() {
        return humidityModifier.get();
    }
}

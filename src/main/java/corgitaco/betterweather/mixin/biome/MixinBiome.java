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


@Mixin(Biome.class)
public abstract class MixinBiome implements BiomeModifier, BiomeClimate {

    @Shadow
    @Final
    private Biome.Climate climate;

    @Inject(method = "getDownfall", at = @At("RETURN"), cancellable = true)
    private void modifyDownfall(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(this.climate.downfall + (float) ((BiomeClimate) climate).getHumidityModifier());
    }

    @Inject(method = "getTemperature()F", at = @At("RETURN"), cancellable = true)
    private void modifyTemperature(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(this.climate.temperature + (float) ((BiomeClimate) climate).getTemperatureModifier());
    }

    @Override
    public double getTemperatureModifier() {
        return ((BiomeClimate) climate).getTemperatureModifier();
    }

    @Override
    public double getHumidityModifier() {
        return ((BiomeClimate) climate).getHumidityModifier();
    }

    @Override
    public void setTempModifier(float tempModifier) {
        ((BiomeModifier) this.climate).setTempModifier(tempModifier);
    }

    @Override
    public void setHumidityModifier(float humidityModifier) {
        ((BiomeModifier) this.climate).setHumidityModifier(humidityModifier);
    }
}

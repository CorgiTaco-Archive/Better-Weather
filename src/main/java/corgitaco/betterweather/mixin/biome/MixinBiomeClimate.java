package corgitaco.betterweather.mixin.biome;

import corgitaco.betterweather.api.BiomeClimate;
import corgitaco.betterweather.helpers.BiomeModifier;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(Biome.Climate.class)
public class MixinBiomeClimate implements BiomeModifier, BiomeClimate {

    private Supplier<Float> tempModifier = () -> 0.0F;
    private Supplier<Float> humidityModifier = () -> 0.0F;

    @Override
    public double getTemperatureModifier() {
        return tempModifier.get();
    }

    @Override
    public double getHumidityModifier() {
        return humidityModifier.get();
    }

    @Override
    public void setTempModifier(float tempModifier) {
        this.tempModifier = () -> tempModifier;
    }

    @Override
    public void setHumidityModifier(float humidityModifier) {
        this.humidityModifier = () -> humidityModifier;
    }
}

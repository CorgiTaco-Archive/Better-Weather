package corgitaco.betterweather.mixin.biome;

import corgitaco.betterweather.api.BiomeClimate;
import corgitaco.betterweather.util.BiomeModifier;
import net.minecraft.util.math.BlockPos;
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
    private Biome.Climate climateSettings;

    @Inject(method = "getDownfall", at = @At("RETURN"), cancellable = true)
    private void modifyDownfall(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(this.climateSettings.downfall + (float) ((BiomeClimate) climateSettings).getHumidityModifier());
    }

    @Inject(method = "getTemperature", at = @At("RETURN"), cancellable = true)
    private void modifyTemperature(CallbackInfoReturnable<Float> cir) {
        cir.setReturnValue(this.climateSettings.temperature + (float) ((BiomeClimate) climateSettings).getTemperatureModifier());
    }

    @Override
    public double getTemperatureModifier() {
        return ((BiomeClimate) climateSettings).getTemperatureModifier();
    }

    @Override
    public double getSeasonTemperatureModifier() {
        return ((BiomeClimate) climateSettings).getSeasonTemperatureModifier();
    }

    @Override
    public double getWeatherTemperatureModifier(BlockPos pos) {
        return ((BiomeClimate) climateSettings).getWeatherTemperatureModifier(pos);
    }

    @Override
    public double getHumidityModifier() {
        return ((BiomeClimate) climateSettings).getHumidityModifier();
    }

    @Override
    public double getSeasonHumidityModifier() {
        return ((BiomeClimate) climateSettings).getSeasonHumidityModifier();
    }

    @Override
    public double getWeatherHumidityModifier(BlockPos pos) {
        return ((BiomeClimate) climateSettings).getWeatherHumidityModifier(pos);
    }

    @Override
    public void setSeasonTempModifier(float tempModifier) {
        ((BiomeModifier) this.climateSettings).setSeasonTempModifier(tempModifier);
    }

    @Override
    public void setSeasonHumidityModifier(float humidityModifier) {
        ((BiomeModifier) this.climateSettings).setSeasonHumidityModifier(humidityModifier);
    }

    @Override
    public void setWeatherTempModifier(float tempModifier) {
        ((BiomeModifier) this.climateSettings).setWeatherTempModifier(tempModifier);
    }

    @Override
    public void setWeatherHumidityModifier(float humidityModifier) {
        ((BiomeModifier) this.climateSettings).setWeatherHumidityModifier(humidityModifier);
    }
}

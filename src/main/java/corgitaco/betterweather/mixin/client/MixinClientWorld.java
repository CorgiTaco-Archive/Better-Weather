package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.api.Climate;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.common.season.SeasonContext;
import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import corgitaco.betterweather.util.BiomeModifier;
import corgitaco.betterweather.util.BiomeUpdate;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements BetterWeatherWorldData, Climate, BiomeUpdate {

    @Shadow
    public abstract DynamicRegistries registryAccess();

    @Nullable
    SeasonContext seasonContext;

    @Nullable
    private WeatherContext weatherContext;

    @Nullable
    @Override
    public SeasonContext getSeasonContext() {
        return this.seasonContext;
    }

    @Nullable
    @Override
    public SeasonContext setSeasonContext(SeasonContext seasonContext) {
        this.seasonContext = seasonContext;
        return this.seasonContext;
    }

    @Nullable
    @Override
    public WeatherContext getWeatherEventContext() {
        return this.weatherContext;
    }

    @Nullable
    @Override
    public WeatherContext setWeatherEventContext(WeatherContext weatherEventContext) {
        this.weatherContext = weatherEventContext;
        return this.weatherContext;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(BooleanSupplier hasTicksLeft, CallbackInfo ci) {
        if (this.seasonContext != null) {
            this.seasonContext.tick((ClientWorld) (Object) this);
        }
        if (this.weatherContext != null) {
            this.weatherContext.tick((ClientWorld) (Object) this);
        }
    }

    @Override
    public Season getSeason() {
        return this.seasonContext.getSeason();
    }

    @Override
    public void updateBiomeData() {
        for (Map.Entry<RegistryKey<Biome>, Biome> entry : this.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY).entrySet()) {
            Biome biome = entry.getValue();
            RegistryKey<Biome> biomeKey = entry.getKey();
            float seasonHumidityModifier = seasonContext == null ? 0.0F : (float) this.seasonContext.getCurrentSubSeasonSettings().getHumidityModifier(biomeKey);
            float seasonTemperatureModifier = seasonContext == null ? 0.0F : (float) this.seasonContext.getCurrentSubSeasonSettings().getTemperatureModifier(biomeKey);
            float weatherHumidityModifier = weatherContext == null ? 0.0F : (float) this.weatherContext.getCurrentEvent().getHumidityModifierAtPosition(null);
            float weatherTemperatureModifier = weatherContext == null ? 0.0F : (float) this.weatherContext.getCurrentEvent().getTemperatureModifierAtPosition(null);

            ((BiomeModifier) (Object) biome).setSeasonTempModifier(seasonTemperatureModifier);
            ((BiomeModifier) (Object) biome).setSeasonHumidityModifier(seasonHumidityModifier);
            ((BiomeModifier) (Object) biome).setWeatherTempModifier(weatherTemperatureModifier);
            ((BiomeModifier) (Object) biome).setWeatherHumidityModifier(weatherHumidityModifier);
        }
    }


    @Redirect(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainLevel(F)F"))
    private float doNotDarkenSkyWithRainStrength(ClientWorld world, float delta) {
        return this.weatherContext != null ? 0.0F : world.getRainLevel(delta);
    }

    @Redirect(method = "getCloudColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainLevel(F)F"))
    private float doNotDarkenCloudsWithRainStrength(ClientWorld world, float delta) {
        return this.weatherContext != null ? 0.0F : world.getRainLevel(delta);
    }

    @Redirect(method = "getSkyDarken", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainLevel(F)F"))
    private float sunBrightness(ClientWorld world, float delta) {
        float rainStrength = ((ClientWorld) (Object) this).getRainLevel(delta);
        WeatherContext weatherContext = this.weatherContext;
        return weatherContext != null ? rainStrength * weatherContext.getCurrentEvent().getClientSettings().dayLightDarkness() : rainStrength;
    }
}

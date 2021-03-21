package corgitaco.betterweather.mixin.client;

import corgitaco.betterweather.api.Climate;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.helpers.IBiomeModifier;
import corgitaco.betterweather.helpers.IBiomeUpdate;
import corgitaco.betterweather.season.SeasonContext;
import corgitaco.betterweather.season.SubSeasonSettings;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.function.BooleanSupplier;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements BetterWeatherWorldData, IBiomeUpdate, Climate {

    @Shadow
    public abstract DynamicRegistries func_241828_r();

    @Nullable
    SeasonContext seasonContext;

    @Redirect(method = "getSkyColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    private float doNotDarkenSkyWithRainStrength(ClientWorld world, float delta) {
        return 0.0F;
    }

    @Redirect(method = "getCloudColor", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
    private float doNotDarkenCloudsWithRainStrength(ClientWorld world, float delta) {
        return 0.0F;
    }

    @Inject(method = "getCloudColor", at = @At("RETURN"), cancellable = true)
    private void modifyCloudColor(float partialTicks, CallbackInfoReturnable<Vector3d> cir) {
//        int rgbColor = WeatherData.currentWeatherEvent.modifyCloudColor(BetterWeatherUtil.transformFloatColor(cir.getReturnValue()), ((ClientWorld) (Object) this).getRainStrength(partialTicks)).getRGB();
//        float r = (float) (rgbColor >> 16 & 255) / 255.0F;
//        float g = (float) (rgbColor >> 8 & 255) / 255.0F;
//        float b = (float) (rgbColor & 255) / 255.0F;
//        cir.setReturnValue(new Vector3d(r, g, b));
    }

//    @Redirect(method = "getSunBrightness", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/world/ClientWorld;getRainStrength(F)F"))
//    private float sunBrightness(ClientWorld world, float delta) {
//        float rainStrength = ((ClientWorld) (Object) this).getRainStrength(delta);
//        return rainStrength * WeatherData.currentWeatherEvent.dayLightDarkness();
//    }

    @Nullable
    @Override
    public SeasonContext getSeasonContext() {
        return this.seasonContext;
    }

    @Override
    public void setSeasonContext(SeasonContext seasonContext) {
        this.seasonContext = seasonContext;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(BooleanSupplier hasTicksLeft, CallbackInfo ci) {
        long gameTime = ((ClientWorld) (Object) this).getWorldInfo().getGameTime();
        if (gameTime % 10 == 0) {
            if (this.seasonContext != null) {
                this.seasonContext.tick((ClientWorld) (Object) this);
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void updateBiomeData(SubSeasonSettings subSeasonSettings) {
        for (Map.Entry<RegistryKey<Biome>, Biome> entry : this.func_241828_r().getRegistry(Registry.BIOME_KEY).getEntries()) {
            Biome biome = entry.getValue();
            RegistryKey<Biome> biomeKey = entry.getKey();
            ((IBiomeModifier) (Object) biome).setHumidityModifier((float) subSeasonSettings.getHumidityModifier(biomeKey));
            ((IBiomeModifier) (Object) biome).setTempModifier((float) subSeasonSettings.getTemperatureModifier(biomeKey));
        }
    }

    @Override
    public Season getSeason() {
        return this.seasonContext;
    }
}

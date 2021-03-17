package corgitaco.betterweather.mixin.biome;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import corgitaco.betterweather.helpers.IBiomeModifier;
import net.minecraft.world.biome.*;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Features;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@Mixin(Biome.class)
public class MixinBiome implements IBiomeModifier {

    @Shadow @Final private Biome.Climate climate;
    private Supplier<Float> tempModifier = null;
    private Supplier<Float> humidityModifier = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void injectFreezeTopLayer(Biome.Climate climate, Biome.Category category, float depth, float scale, BiomeAmbience effects, BiomeGenerationSettings biomeGenerationSettings, MobSpawnInfo mobSpawnInfo, CallbackInfo ci) {
        addFeatureToBiome((Biome) (Object)this, GenerationStage.Decoration.TOP_LAYER_MODIFICATION, Features.FREEZE_TOP_LAYER);
    }

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

    //Use these to add our features to vanilla's biomes.
    private static void addFeatureToBiome(Biome biome, GenerationStage.Decoration feature, ConfiguredFeature<?, ?> configuredFeature) {
        ConvertImmutableFeatures(biome);
        List<List<Supplier<ConfiguredFeature<?, ?>>>> biomeFeatures = biome.getGenerationSettings().features;
        while (biomeFeatures.size() <= feature.ordinal()) {
            biomeFeatures.add(Lists.newArrayList());
        }
        biomeFeatures.get(feature.ordinal()).add(() -> configuredFeature);
    }

    private static void ConvertImmutableFeatures(Biome biome) {
        if (biome.getGenerationSettings().features instanceof ImmutableList) {
            biome.getGenerationSettings().features = biome.getGenerationSettings().features.stream().map(Lists::newArrayList).collect(Collectors.toList());
        }
    }
}

package corgitaco.betterweather.season.client;

import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.SeasonContext;
import corgitaco.betterweather.season.SubSeasonSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class BiomeColorCalculator {

    public static Color modifyBiomeColor(ColorType colorType, Color originalColorValue, Biome biome) {
        Color fallbackColor = originalColorValue;  //modifyColorForWeatherEvent(colorType, originalColorValue, originalColorValue, null);
        Minecraft mc = Minecraft.getInstance();

        ClientWorld world = mc.world;

        if (world == null)
            return fallbackColor;

        SeasonContext seasonContext = ((BetterWeatherWorldData) mc.world).getSeasonContext();
        if (seasonContext == null) {
            return fallbackColor;
        }

        SubSeasonSettings subSeasonSettings = seasonContext.getCurrentSubSeasonSettings();

        int red = originalColorValue.getRed();
        int green = originalColorValue.getGreen();
        int blue = originalColorValue.getBlue();

        Color target;
        double blendStrength;
        DynamicRegistries dynamicRegistries = world.func_241828_r();
        Optional<RegistryKey<Biome>> optionalKey = dynamicRegistries.getRegistry(Registry.BIOME_KEY).getOptionalKey(biome);

        if (!optionalKey.isPresent()) {
            return fallbackColor;
        }

        RegistryKey<Biome> biomeKey = optionalKey.get();

        switch (colorType) {
            case GRASS:
                int targetGrassColor = subSeasonSettings.getTargetGrassColor(biomeKey);

                if (targetGrassColor == Integer.MAX_VALUE)
                    return fallbackColor;

                target = new Color(targetGrassColor);
                blendStrength = subSeasonSettings.getGrassColorBlendStrength(biomeKey);
                break;
            case FOLIAGE:
                int targetFoliageColor = subSeasonSettings.getTargetFoliageColor(biomeKey);

                if (targetFoliageColor == Integer.MAX_VALUE)
                    return fallbackColor;

                target = new Color(targetFoliageColor);
                blendStrength = subSeasonSettings.getFoliageColorBlendStrength(biomeKey);
                break;
            case FOG:
                int targetFogColor = subSeasonSettings.getTargetFogColor(biomeKey);

                if (targetFogColor == Integer.MAX_VALUE)
                    return fallbackColor;

                target = new Color(targetFogColor);
                blendStrength = subSeasonSettings.getFogColorBlendStrength(biomeKey);
                break;
            default:
                int targetSkyColor = subSeasonSettings.getTargetSkyColor(biomeKey);

                if (targetSkyColor == Integer.MAX_VALUE)
                    return fallbackColor;

                target = new Color(targetSkyColor);
                blendStrength = subSeasonSettings.getSkyColorBlendStrength(biomeKey);
                break;
        }

        red = modifiedColorValue(red, target.getRed(), blendStrength);
        green = modifiedColorValue(green, target.getGreen(), blendStrength);
        blue = modifiedColorValue(blue, target.getBlue(), blendStrength);

        int clampedRed = MathHelper.clamp(red, 0, 255);
        int clampedGreen = MathHelper.clamp(green, 0, 255);
        int clampedBlue = MathHelper.clamp(blue, 0, 255);

        Color modifiedColor = new Color(clampedRed, clampedGreen, clampedBlue);
        return modifiedColor;  // modifyColorForWeatherEvent(colorType, originalColorValue, modifiedColor, target);
    }


//    public static Color modifyColorForWeatherEvent(ColorType type, Color originalBiomeColor, Color returnColor, @Nullable Color seasonTargetColor) {
//        Minecraft instance = Minecraft.getInstance();
//        float partialTicks = instance.isGamePaused() ? instance.renderPartialTicksPaused : instance.timer.renderPartialTicks;
//
//        switch (type) {
//            case GRASS:
//                return WeatherData.currentWeatherEvent.modifyGrassColor(originalBiomeColor, returnColor, seasonTargetColor);
//            case FOLIAGE:
//                return WeatherData.currentWeatherEvent.modifyFoliageColor(originalBiomeColor, returnColor, seasonTargetColor);
//            case SKY:
//                return WeatherData.currentWeatherEvent.modifySkyColor(originalBiomeColor, returnColor, seasonTargetColor, instance.world.getRainStrength(partialTicks));
//            case FOG:
//                return WeatherData.currentWeatherEvent.modifyFogColor(originalBiomeColor, returnColor, seasonTargetColor, instance.world.getRainStrength(partialTicks));
//        }
//        return originalBiomeColor;
//    }

    private static int modifiedColorValue(int original, int target, double blendStrength) {
        return (int) MathHelper.lerp(blendStrength, original, target);
    }

    public enum ColorType {
        FOLIAGE,
        GRASS,
        SKY,
        FOG
    }
}

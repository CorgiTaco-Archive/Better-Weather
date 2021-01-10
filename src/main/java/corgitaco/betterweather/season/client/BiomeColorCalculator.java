package corgitaco.betterweather.season.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.Season;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class BiomeColorCalculator {

    public static Color modifyBiomeColor(ColorType colorType, Color originalColorValue, Season.SubSeason subSeason, Biome biome) {
        if (!BetterWeather.useSeasons)
            return originalColorValue;

        int red = originalColorValue.getRed();
        int green = originalColorValue.getGreen();
        int blue = originalColorValue.getBlue();

        Color target;
        double blendStrength;
        DynamicRegistries dynamicRegistries = Minecraft.getInstance().world.func_241828_r();
        ResourceLocation biomeKey = dynamicRegistries.getRegistry(Registry.BIOME_KEY).getKey(biome);
            if (biomeKey == null)
                return originalColorValue;

        switch (colorType) {
            case GRASS:
                int targetGrassColor = subSeason.getTargetGrassColor(biomeKey, false);

                if (targetGrassColor == Integer.MAX_VALUE)
                    return originalColorValue;

                target = new Color(targetGrassColor);
                blendStrength = subSeason.getGrassColorBlendStrength(biomeKey, false);
                break;
            case FOLIAGE:
                int targetFoliageColor = subSeason.getTargetFoliageColor(biomeKey, false);

                if (targetFoliageColor == Integer.MAX_VALUE)
                    return originalColorValue;

                target = new Color(targetFoliageColor);
                blendStrength = subSeason.getFoliageColorBlendStrength(biomeKey, false);
                break;
            case FOG:
                int targetFogColor = subSeason.getTargetFogColor(biomeKey, false);

                if (targetFogColor == Integer.MAX_VALUE)
                    return originalColorValue;

                target = new Color(targetFogColor);
                blendStrength = subSeason.getFogColorBlendStrength(biomeKey, false);
                break;
            default:
                int targetSkyColor = subSeason.getTargetSkyColor(biomeKey, false);

                if (targetSkyColor == Integer.MAX_VALUE)
                    return originalColorValue;

                target = new Color(targetSkyColor);
                blendStrength = subSeason.getSkyColorBlendStrength(biomeKey, false);
                break;
        }


        red = modifiedColorValue(red, target.getRed(), blendStrength);

        green = modifiedColorValue(green, target.getGreen(), blendStrength);

        blue = modifiedColorValue(blue, target.getBlue(), blendStrength);

        int clampedRed = MathHelper.clamp(red, 0, 255);
        int clampedGreen = MathHelper.clamp(green, 0, 255);
        int clampedBlue = MathHelper.clamp(blue, 0, 255);

        return new Color(clampedRed, clampedGreen, clampedBlue);
    }

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

package corgitaco.betterweather.season.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.season.Season;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class BiomeColorCalculator {

    public static Color modifyBiomeColor(ColorType colorType, Color originalColorValue, Season.SubSeason subSeason, Biome biome) {
        int red = originalColorValue.getRed();
        int green = originalColorValue.getGreen();
        int blue = originalColorValue.getBlue();

        Color target;
        double blendStrength;
        switch (colorType) {
            case GRASS:
                int targetGrassColor = subSeason.getTargetFoliageColor(BetterWeather.biomeRegistryEarlyAccess.getKey(biome), false);

                if (targetGrassColor == -1)
                    return originalColorValue;

                target = new Color(targetGrassColor);
                blendStrength = subSeason.getClient().getGrassColorBlendStrength();
                break;
            case FOLIAGE:
                int targetFoliageColor = subSeason.getClient().getTargetFoliageColor();

                if (targetFoliageColor == -1)
                    return originalColorValue;

                target = new Color(targetFoliageColor);
                blendStrength = subSeason.getClient().getFoliageColorBlendStrength();
                break;
            case FOG:
                int targetFogColor = subSeason.getClient().getTargetFogColor();

                if (targetFogColor == -1)
                    return originalColorValue;

                target = new Color(targetFogColor);
                blendStrength = subSeason.getClient().getFogColorBlendStrength();
                break;
            default:
                int targetSkyColor = subSeason.getClient().getTargetSkyColor();

                if (targetSkyColor == -1)
                    return originalColorValue;

                target = new Color(targetSkyColor);
                blendStrength = subSeason.getClient().getSkyColorBlendStrength();
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

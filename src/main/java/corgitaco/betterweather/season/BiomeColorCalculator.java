package corgitaco.betterweather.season;

import corgitaco.betterweather.datastorage.BetterWeatherSeasonData;
import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class BiomeColorCalculator {

    public static Color modifyBiomeColor(boolean isGrass, Color originalColorValue, BetterWeatherSeasonData.SubSeason subSeason) {

        int red = originalColorValue.getRed();
        int green = originalColorValue.getGreen();
        int blue = originalColorValue.getBlue();

        Color target;

        if (isGrass)
            target = subSeason.getGrassTarget();
        else
            target = subSeason.getFoliageTarget();

        red = modifiedColorValue(red, target.getRed());

        green = modifiedColorValue(green, target.getGreen());

        blue = modifiedColorValue(blue, target.getBlue());

        int clampedRed = MathHelper.clamp(red, 0, 255);
        int clampedGreen = MathHelper.clamp(green, 0, 255);
        int clampedBlue = MathHelper.clamp(blue, 0, 255);

        return new Color(clampedRed, clampedGreen, clampedBlue);
    }

    private static int modifiedColorValue(int original, int target) {
        double colorModifier = 0.5;
        int value = original;

        if (value < target) {
            int difference = Math.abs(target - value);
            value = (int) (value + (difference * colorModifier));

        } else if (value > target) {
            int difference = Math.abs(value - target);
            value = (int) (value - (difference * colorModifier));
        }
        return value;
    }
}

package corgitaco.betterweather.season;

import net.minecraft.util.math.MathHelper;

import java.awt.*;

public class BiomeColorCalculator {

    public static Color modifyBiomeColor(boolean isGrass, Color originalColorValue, Season.SubSeason subSeason) {
        int red = originalColorValue.getRed();
        int green = originalColorValue.getGreen();
        int blue = originalColorValue.getBlue();

        Color target;

        if (isGrass)
            target = new Color(subSeason.getTargetGrassColor());
        else
            target = new Color(subSeason.getFoliageTarget());

        red = modifiedColorValue(red, target.getRed(), subSeason.getSeasonBlendStrength());

        green = modifiedColorValue(green, target.getGreen(), subSeason.getSeasonBlendStrength());

        blue = modifiedColorValue(blue, target.getBlue(), subSeason.getSeasonBlendStrength());

        int clampedRed = MathHelper.clamp(red, 0, 255);
        int clampedGreen = MathHelper.clamp(green, 0, 255);
        int clampedBlue = MathHelper.clamp(blue, 0, 255);

        return new Color(clampedRed, clampedGreen, clampedBlue);
    }

    private static int modifiedColorValue(int original, int target, double blendStrength) {
        return (int) MathHelper.lerp(blendStrength, original , target);
    }
}

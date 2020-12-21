package corgitaco.betterweather.season;

import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class BiomeColorCalculator {

    public static Color modifyBiomeColor(boolean isGrass, Color originalColorValue, Season.SubSeason subSeason) {
        int red = originalColorValue.getRed();
        int green = originalColorValue.getGreen();
        int blue = originalColorValue.getBlue();

        Color target;

        if (isGrass)
            target = new Color(subSeason.getClient().getTargetGrassColor());
        else
            target = new Color(subSeason.getClient().getTargetFoliageColor());

        red = modifiedColorValue(red, target.getRed(), subSeason.getClient().getSeasonFoliageColorBlendStrength());

        green = modifiedColorValue(green, target.getGreen(), subSeason.getClient().getSeasonFoliageColorBlendStrength());

        blue = modifiedColorValue(blue, target.getBlue(), subSeason.getClient().getSeasonFoliageColorBlendStrength());

        int clampedRed = MathHelper.clamp(red, 0, 255);
        int clampedGreen = MathHelper.clamp(green, 0, 255);
        int clampedBlue = MathHelper.clamp(blue, 0, 255);

        return new Color(clampedRed, clampedGreen, clampedBlue);
    }

    private static int modifiedColorValue(int original, int target, double blendStrength) {
        return (int) MathHelper.lerp(blendStrength, original , target);
    }
}

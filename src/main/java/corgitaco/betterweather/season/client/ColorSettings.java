package corgitaco.betterweather.season.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import static corgitaco.betterweather.util.client.ColorUtil.tryParseColor;

public class ColorSettings {

    public static final Codec<ColorSettings> CODEC = RecordCodecBuilder.create(seasonClientSettingsInstance -> {
        return seasonClientSettingsInstance.group(Codec.STRING.optionalFieldOf("foliageTargetHexColor", "").forGetter((colorSettings) -> {
            return colorSettings.targetFoliageHexColor == Integer.MAX_VALUE ? "" : Integer.toHexString(colorSettings.targetFoliageHexColor);
        }), Codec.DOUBLE.fieldOf("foliageColorBlendStrength").orElse(0.0).forGetter((colorSettings) -> {
            return colorSettings.foliageColorBlendStrength;
        }), Codec.STRING.fieldOf("grassTargetHexColor").orElse("").forGetter((colorSettings) -> {
            return colorSettings.targetGrassHexColor == Integer.MAX_VALUE ? "" : Integer.toHexString(colorSettings.targetGrassHexColor);
        }), Codec.DOUBLE.fieldOf("grassColorBlendStrength").orElse(0.0).forGetter((colorSettings) -> {
            return colorSettings.foliageColorBlendStrength;
        }), Codec.STRING.fieldOf("skyTargetHexColor").orElse("").forGetter((colorSettings) -> {
            return colorSettings.targetSkyHexColor == Integer.MAX_VALUE ? "" : Integer.toHexString(colorSettings.targetSkyHexColor);
        }), Codec.DOUBLE.fieldOf("skyColorBlendStrength").orElse(0.0).forGetter((colorSettings) -> {
            return colorSettings.skyColorBlendStrength;
        }), Codec.STRING.fieldOf("fogTargetHexColor").orElse("").forGetter((colorSettings) -> {
            return colorSettings.targetFogHexColor == Integer.MAX_VALUE ? "" : Integer.toHexString(colorSettings.targetFogHexColor);
        }), Codec.DOUBLE.fieldOf("fogColorBlendStrength").orElse(0.0).forGetter((colorSettings) -> {
            return colorSettings.fogColorBlendStrength;
        })).apply(seasonClientSettingsInstance, ColorSettings::new);
    });

    private final int targetFoliageHexColor;
    private final double foliageColorBlendStrength;
    private final int targetGrassHexColor;
    private final double grassColorBlendStrength;
    private final int targetSkyHexColor;
    private final double skyColorBlendStrength;
    private final int targetFogHexColor;
    private final double fogColorBlendStrength;

    public ColorSettings() {
        this("", 0, "", 0);
    }

    public ColorSettings(String targetFoliageHexColor, double foliageColorBlendStrength, String targetGrassColor, double grassColorBlendStrength) {
        this(targetFoliageHexColor, foliageColorBlendStrength, targetGrassColor, grassColorBlendStrength, targetGrassColor, 0, targetGrassColor, 0);
    }

    public ColorSettings(String targetFoliageHexColor, double foliageColorBlendStrength, String targetGrassColor, double grassColorBlendStrength, String targetSkyHexColor, double skyColorBlendStrength, String targetFogHexColor, double fogColorBlendStrength) {
        this(tryParseColor(targetFoliageHexColor), foliageColorBlendStrength, tryParseColor(targetGrassColor), grassColorBlendStrength, tryParseColor(targetSkyHexColor), skyColorBlendStrength, tryParseColor(targetFogHexColor), fogColorBlendStrength);
    }

    public ColorSettings(int targetFoliageHexColor, double foliageColorBlendStrength, int targetGrassColor, double grassColorBlendStrength) {
        this(targetFoliageHexColor, foliageColorBlendStrength, targetGrassColor, grassColorBlendStrength, targetFoliageHexColor, 0, targetFoliageHexColor, 0);
    }

    public ColorSettings(int targetFoliageHexColor, double foliageColorBlendStrength, int targetGrassColor, double grassColorBlendStrength, int targetSkyHexColor, double skyColorBlendStrength, int targetFogHexColor, double fogColorBlendStrength) {
        this.targetFoliageHexColor = targetFoliageHexColor;
        this.foliageColorBlendStrength = foliageColorBlendStrength;
        this.targetGrassHexColor = targetGrassColor;
        this.grassColorBlendStrength = grassColorBlendStrength;
        this.targetSkyHexColor = targetSkyHexColor;
        this.targetFogHexColor = targetFogHexColor;
        this.fogColorBlendStrength = fogColorBlendStrength;
        this.skyColorBlendStrength = skyColorBlendStrength;
    }

    public int getTargetFoliageHexColor() {
        return targetFoliageHexColor;
    }

    public double getFoliageColorBlendStrength() {
        return foliageColorBlendStrength;
    }

    public int getTargetGrassHexColor() {
        return targetGrassHexColor;
    }

    public double getGrassColorBlendStrength() {
        return grassColorBlendStrength;
    }

    public int getTargetSkyHexColor() {
        return targetSkyHexColor;
    }

    public double getSkyColorBlendStrength() {
        return skyColorBlendStrength;
    }

    public int getTargetFogHexColor() {
        return targetFogHexColor;
    }

    public double getFogColorBlendStrength() {
        return fogColorBlendStrength;
    }
}
package corgitaco.betterweather.api.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.Map;

import static corgitaco.betterweather.util.client.ColorUtil.tryParseColor;

public class ColorSettings {

    public static final Codec<ColorSettings> CODEC = RecordCodecBuilder.create(seasonClientSettingsInstance -> {
        return seasonClientSettingsInstance.group(Codec.STRING.fieldOf("foliageTargetHexColor").forGetter((colorSettings) -> {
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
        }), Codec.STRING.fieldOf("cloudTargetHexColor").orElse("").forGetter((colorSettings) -> {
            return colorSettings.targetFogHexColor == Integer.MAX_VALUE ? "" : Integer.toHexString(colorSettings.targetFogHexColor);
        }), Codec.DOUBLE.fieldOf("cloudColorBlendStrength").orElse(0.0).forGetter((colorSettings) -> {
            return colorSettings.fogColorBlendStrength;
        })).apply(seasonClientSettingsInstance, ColorSettings::new);
    });

    public static Map<String, String> VALUE_COMMENTS = Util.make(new HashMap<>(), (map) -> {
        map.put("fogColorBlendStrength", "The strength of this world's fog color blend towards the value of \"fogTargetHexColor\".\nRange: 0 - 1.0");
        map.put("fogTargetHexColor", "Blends the world's fog color towards this value. Blend strength is determined by the value of \"fogColorBlendStrength\".");

        map.put("foliageColorBlendStrength", "The strength of this world's sky color blend towards the value of \"foliageTargetHexColor\".\nRange: 0 - 1.0");
        map.put("foliageTargetHexColor", "Blends this world's foliage color towards this value. Blend strength is determined by the value of \"foliageColorBlendStrength\".");

        map.put("grassColorBlendStrength", "The strength of this world's grass color blend towards the value of \"grassTargetHexColor\".\nRange: 0 - 1.0");
        map.put("grassTargetHexColor", "Blends this world's grass color towards this value. Blend strength is determined by the value of \"grassColorBlendStrength\".");

        map.put("skyColorBlendStrength", "The strength of this world's sky color blend towards the value of \"skyTargetHexColor\".\nRange: 0 - 1.0");
        map.put("skyTargetHexColor", "Blends this world's grass color towards this value. Blend strength is determined by the value of \"skyColorBlendStrength\".");

        map.put("cloudColorBlendStrength", "The strength of this world's cloud color blend towards the value of \"cloudTargetHexColor\".\nRange: 0 - 1.0");
        map.put("cloudTargetHexColor", "Blends this world's grass color towards this value. Blend strength is determined by the value of \"cloudColorBlendStrength\".");
    });


    private final int targetFoliageHexColor;
    private final double foliageColorBlendStrength;
    private final int targetGrassHexColor;
    private final double grassColorBlendStrength;
    private final int targetSkyHexColor;
    private final double skyColorBlendStrength;
    private final int targetCloudHexColor;
    private final double cloudColorBlendStrength;
    private final int targetFogHexColor;
    private final double fogColorBlendStrength;

    public ColorSettings() {
        this("", 0, "", 0);
    }

    public ColorSettings(String targetFoliageHexColor, double foliageColorBlendStrength, String targetGrassColor, double grassColorBlendStrength) {
        this(targetFoliageHexColor, foliageColorBlendStrength, targetGrassColor, grassColorBlendStrength, targetGrassColor, 0, targetGrassColor, 0, targetGrassColor, 0);
    }

    public ColorSettings(String targetFoliageHexColor, double foliageColorBlendStrength, String targetGrassColor, double grassColorBlendStrength, String targetSkyHexColor, double skyColorBlendStrength, String targetFogHexColor, double fogColorBlendStrength, String targetCloudHexColor, double cloudColorBlendStrength) {
        this(tryParseColor(targetFoliageHexColor), foliageColorBlendStrength, tryParseColor(targetGrassColor), grassColorBlendStrength, tryParseColor(targetSkyHexColor), skyColorBlendStrength, tryParseColor(targetFogHexColor), fogColorBlendStrength, tryParseColor(targetCloudHexColor), skyColorBlendStrength);
    }

    public ColorSettings(int targetFoliageHexColor, double foliageColorBlendStrength, int targetGrassColor, double grassColorBlendStrength) {
        this(targetFoliageHexColor, foliageColorBlendStrength, targetGrassColor, grassColorBlendStrength, targetFoliageHexColor, 0, targetFoliageHexColor, 0, targetFoliageHexColor, 0);
    }

    public ColorSettings(int targetFoliageHexColor, double foliageColorBlendStrength, int targetGrassColor, double grassColorBlendStrength, int targetSkyHexColor, double skyColorBlendStrength, int targetFogHexColor, double fogColorBlendStrength, int targetCloudHexColor, double cloudColorBlendStrength) {
        this.targetFoliageHexColor = targetFoliageHexColor;
        this.foliageColorBlendStrength = foliageColorBlendStrength;
        this.targetGrassHexColor = targetGrassColor;
        this.grassColorBlendStrength = grassColorBlendStrength;
        this.targetSkyHexColor = targetSkyHexColor;
        this.targetFogHexColor = targetFogHexColor;
        this.fogColorBlendStrength = fogColorBlendStrength;
        this.skyColorBlendStrength = skyColorBlendStrength;
        this.targetCloudHexColor = targetCloudHexColor;
        this.cloudColorBlendStrength = cloudColorBlendStrength;
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

    public int getTargetCloudHexColor() {
        return targetCloudHexColor;
    }

    public double getCloudColorBlendStrength() {
        return cloudColorBlendStrength;
    }
}
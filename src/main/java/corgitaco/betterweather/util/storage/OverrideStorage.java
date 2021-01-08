package corgitaco.betterweather.util.storage;

import net.minecraft.block.Block;

import java.util.IdentityHashMap;

public class OverrideStorage {
    private double tempModifier = Double.MAX_VALUE;
    private double humidityModifier = Double.MAX_VALUE;
    private IdentityHashMap<Block, Double> blockToCropGrowthMultiplierMap = new IdentityHashMap<>();
    private double fallBack = Double.MAX_VALUE;
    private OverrideClientStorage clientStorage = new OverrideClientStorage();

    public OverrideStorage() {}

    public IdentityHashMap<Block, Double> getBlockToCropGrowthMultiplierMap() {
        if (blockToCropGrowthMultiplierMap == null)
            blockToCropGrowthMultiplierMap = new IdentityHashMap<>();
        return blockToCropGrowthMultiplierMap;
    }

    public double getFallBack() {
        return fallBack;
    }

    public double getTempModifier() {
        return tempModifier;
    }

    public double getHumidityModifier() {
        return humidityModifier;
    }

    public OverrideClientStorage getClientStorage() {
        return clientStorage;
    }

    public OverrideStorage setTempModifier(double tempModifier) {
        this.tempModifier = tempModifier;
        return this;
    }

    public OverrideStorage setHumidityModifier(double humidityModifier) {
        this.humidityModifier = humidityModifier;
        return this;
    }

    public OverrideStorage setBlockToCropGrowthMultiplierMap(IdentityHashMap<Block, Double> blockToCropGrowthMultiplierMap) {
        this.blockToCropGrowthMultiplierMap = blockToCropGrowthMultiplierMap;
        return this;
    }

    public OverrideStorage setFallBack(double fallBack) {
        this.fallBack = fallBack;
        return this;
    }

    public OverrideStorage setClientStorage(OverrideClientStorage clientStorage) {
        this.clientStorage = clientStorage;
        return this;
    }

    public static class OverrideClientStorage {
        private String targetFoliageHexColor = "";
        private double foliageColorBlendStrength = Double.MAX_VALUE;
        private String targetGrassHexColor = "";
        private double grassColorBlendStrength = Double.MAX_VALUE;
        private String targetSkyHexColor = "";
        private double skyColorBlendStrength = Double.MAX_VALUE;
        private String targetFogHexColor = "";
        private double fogColorBlendStrength = Double.MAX_VALUE;

        public OverrideClientStorage() {}

        public String getTargetFoliageHexColor() {
            return targetFoliageHexColor;
        }

        public double getFoliageColorBlendStrength() {
            return foliageColorBlendStrength;
        }

        public String getTargetGrassHexColor() {
            return targetGrassHexColor;
        }

        public double getGrassColorBlendStrength() {
            return grassColorBlendStrength;
        }

        public String getTargetSkyHexColor() {
            return targetSkyHexColor;
        }

        public double getSkyColorBlendStrength() {
            return skyColorBlendStrength;
        }

        public String getTargetFogHexColor() {
            return targetFogHexColor;
        }

        public double getFogColorBlendStrength() {
            return fogColorBlendStrength;
        }

        public OverrideClientStorage setTargetFoliageHexColor(String targetFoliageHexColor) {
            this.targetFoliageHexColor = targetFoliageHexColor;
            return this;
        }

        public OverrideClientStorage setFoliageColorBlendStrength(double foliageColorBlendStrength) {
            this.foliageColorBlendStrength = foliageColorBlendStrength;
            return this;
        }

        public OverrideClientStorage setTargetGrassHexColor(String targetGrassHexColor) {
            this.targetGrassHexColor = targetGrassHexColor;
            return this;
        }

        public OverrideClientStorage setGrassColorBlendStrength(double grassColorBlendStrength) {
            this.grassColorBlendStrength = grassColorBlendStrength;
            return this;
        }

        public OverrideClientStorage setTargetSkyHexColor(String targetSkyHexColor) {
            this.targetSkyHexColor = targetSkyHexColor;
            return this;
        }

        public OverrideClientStorage setSkyColorBlendStrength(double skyColorBlendStrength) {
            this.skyColorBlendStrength = skyColorBlendStrength;
            return this;
        }

        public OverrideClientStorage setTargetFogHexColor(String targetFogHexColor) {
            this.targetFogHexColor = targetFogHexColor;
            return this;
        }

        public OverrideClientStorage setFogColorBlendStrength(double fogColorBlendStrength) {
            this.fogColorBlendStrength = fogColorBlendStrength;
            return this;
        }
    }
}

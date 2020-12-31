package corgitaco.betterweather.season.seasonoverrides;


import net.minecraft.block.Block;

import java.util.IdentityHashMap;

public class SeasonOverrideOption {
    private final double defaultVal;
    private final IdentityHashMap<Block/*Crop*/, Double/*Multiplier*/> crops;


    public SeasonOverrideOption(double defaultVal, IdentityHashMap<Block, Double> crops) {
        this.defaultVal = defaultVal;
        this.crops = crops;
    }


    public double getDefaultVal() {
        return defaultVal;
    }

    public IdentityHashMap<Block, Double> getCrops() {
        return crops;
    }
}

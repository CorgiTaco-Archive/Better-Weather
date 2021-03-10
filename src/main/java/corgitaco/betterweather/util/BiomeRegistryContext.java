package corgitaco.betterweather.util;

import corgitaco.betterweather.mixin.access.BiomeAccess;
import net.minecraft.world.biome.Biome;

public class BiomeRegistryContext {

    private final Biome newBiome;

    public BiomeRegistryContext(Biome biome) {
        this.newBiome = new Biome(((BiomeAccess)(Object) biome).getClimate(), biome.getCategory(), biome.getDepth(), biome.getScale(), biome.getAmbience(), biome.getGenerationSettings(), biome.getMobSpawnInfo());
    }

    public Biome getNewBiome() {
        return newBiome;
    }
}

package corgitaco.betterweather.common.season.config.overrides;

import corgitaco.betterweather.common.season.storage.OverrideStorage;
import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.IdentityHashMap;

public class BiomeToOverrideStorageJsonStorage {
    private final IdentityHashMap<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage;
    private final IdentityHashMap<Block, Double> seasonCropOverrides;

    public BiomeToOverrideStorageJsonStorage(IdentityHashMap<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage, IdentityHashMap<Block, Double> seasonCropOverrides) {
        this.biomeToOverrideStorage = biomeToOverrideStorage;
        this.seasonCropOverrides = seasonCropOverrides;
    }

    public IdentityHashMap<RegistryKey<Biome>, OverrideStorage> getBiomeToOverrideStorage() {
        return biomeToOverrideStorage;
    }

    public IdentityHashMap<Block, Double> getSeasonCropOverrides() {
        return seasonCropOverrides;
    }

    public static class ObjectToOverrideStorageJsonStorage {
        private final IdentityHashMap<Object, OverrideStorage> objectToOverrideStorage;

        public ObjectToOverrideStorageJsonStorage(IdentityHashMap<Object, OverrideStorage> objectToOverrideStorage) {
            this.objectToOverrideStorage = objectToOverrideStorage;
        }

        public IdentityHashMap<Object, OverrideStorage> getObjectToOverrideStorage() {
            return objectToOverrideStorage;
        }
    }
}

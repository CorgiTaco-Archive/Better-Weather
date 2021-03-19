package corgitaco.betterweather.config.season.overrides;

import corgitaco.betterweather.util.storage.OverrideStorage;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import java.util.IdentityHashMap;

public class BiomeToOverrideStorageJsonStorage {
    private final IdentityHashMap<ResourceLocation, OverrideStorage> biomeToOverrideStorage;
    private final IdentityHashMap<Block, Double> seasonCropOverrides;

    public BiomeToOverrideStorageJsonStorage(IdentityHashMap<ResourceLocation, OverrideStorage> biomeToOverrideStorage, IdentityHashMap<Block, Double> seasonCropOverrides) {
        this.biomeToOverrideStorage = biomeToOverrideStorage;
        this.seasonCropOverrides = seasonCropOverrides;
    }

    public IdentityHashMap<ResourceLocation, OverrideStorage> getBiomeToOverrideStorage() {
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

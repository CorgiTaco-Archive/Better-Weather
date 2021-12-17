package corgitaco.betterweather.common.season.config.overrides;

import corgitaco.betterweather.common.season.storage.OverrideStorage;
import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.IdentityHashMap;

public class BiomeToOverrideStorageJsonStorage {
    private final IdentityHashMap<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage;
    private final Object2DoubleOpenHashMap<Block> seasonCropOverrides;

    public BiomeToOverrideStorageJsonStorage(IdentityHashMap<RegistryKey<Biome>, OverrideStorage> biomeToOverrideStorage, Object2DoubleOpenHashMap<Block> seasonCropOverrides) {
        this.biomeToOverrideStorage = biomeToOverrideStorage;
        this.seasonCropOverrides = seasonCropOverrides;
    }

    public IdentityHashMap<RegistryKey<Biome>, OverrideStorage> getBiomeToOverrideStorage() {
        return biomeToOverrideStorage;
    }

    public Object2DoubleOpenHashMap<Block> getSeasonCropOverrides() {
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

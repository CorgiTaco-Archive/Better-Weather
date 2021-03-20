package corgitaco.betterweather.util;

import com.mojang.serialization.Lifecycle;
import corgitaco.betterweather.mixin.access.BiomeAccess;
import corgitaco.betterweather.mixin.access.RegistryAccess;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.*;


/**
 * Used to allow either server or world specific biome objects to function as keys to return the same common biome for the given world since each world can contain a unique biome registry.
 */
public class CommonKeyMutableRegistry extends SimpleRegistry<Biome> {


    private final IdentityHashMap<Biome, Biome> serverBiomeToWorldBiome = new IdentityHashMap<>();

    public CommonKeyMutableRegistry(MutableRegistry<Biome> serverMutableRegistry) {
        super(serverMutableRegistry.getRegistryKey(), serverMutableRegistry.getLifecycle());
        fillWorldRegistry(serverMutableRegistry);
    }


    @SuppressWarnings({"unchecked", "ConstantConditions"})
    private void fillWorldRegistry(MutableRegistry<Biome> serverRegistry) {
        for (Map.Entry<RegistryKey<Biome>, Biome> entry : serverRegistry.getEntries()) {
            Biome biome = entry.getValue();
            Biome worldBiome = this.register(serverRegistry.getId(biome), entry.getKey(), new Biome(((BiomeAccess) (Object) biome).getClimate(), biome.getCategory(), biome.getDepth(), biome.getScale(), biome.getAmbience(), biome.getGenerationSettings(), biome.getMobSpawnInfo()), ((RegistryAccess<Biome>) serverRegistry).invokeGetLifecycleByRegistry(biome));
            @Nullable ResourceLocation registryName = biome.getRegistryName();
            worldBiome.setRegistryName(registryName);
            this.serverBiomeToWorldBiome.put(biome, worldBiome);
        }
    }

    @Nullable
    @Override
    public ResourceLocation getKey(Biome value) {
        if (this.serverBiomeToWorldBiome.containsKey(value)) {
            return super.getKey(this.serverBiomeToWorldBiome.get(value));
        }

        return super.getKey(value);
    }

    @Override
    public Optional<RegistryKey<Biome>> getOptionalKey(Biome value) {
        if (this.serverBiomeToWorldBiome.containsKey(value)) {
            return super.getOptionalKey(this.serverBiomeToWorldBiome.get(value));
        }

        return super.getOptionalKey(value);
    }

    @Override
    public int getId(@Nullable Biome value) {
        if (this.serverBiomeToWorldBiome.containsKey(value)) {
            return super.getId(this.serverBiomeToWorldBiome.get(value));
        }
        return super.getId(value);
    }

    @Nullable
    @Override
    public Biome getByValue(int value) {
        return super.getByValue(value);
    }

    @Nullable
    @Override
    public Biome getValueForKey(@Nullable RegistryKey<Biome> key) {
        return super.getValueForKey(key);
    }

    @Nullable
    @Override
    public Biome getOrDefault(@Nullable ResourceLocation name) {
        return super.getOrDefault(name);
    }

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override
    public Lifecycle getLifecycleByRegistry(Biome value) {
        if (this.serverBiomeToWorldBiome.containsKey(value)) {
            return super.getLifecycleByRegistry(this.serverBiomeToWorldBiome.get(value));
        }
        return super.getLifecycleByRegistry(value);
    }

    @Override
    public Lifecycle getLifecycle() {
        return super.getLifecycle();
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return super.keySet();
    }

    @Override
    public Set<Map.Entry<RegistryKey<Biome>, Biome>> getEntries() {
        return super.getEntries();
    }

    @Override
    public boolean containsKey(ResourceLocation name) {
        return super.containsKey(name);
    }

    @Override
    public Iterator<Biome> iterator() {
        return super.iterator();
    }
}

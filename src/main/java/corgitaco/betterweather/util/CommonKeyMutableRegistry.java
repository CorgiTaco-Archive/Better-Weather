package corgitaco.betterweather.util;

import com.mojang.serialization.Lifecycle;
import corgitaco.betterweather.mixin.access.RegistryAccess;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.*;


/**
 * Used to allow either server or world specific biome objects to function to return the same common object for the given world
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
            Biome serverBiome = entry.getValue();
            Biome worldBiome = new BiomeRegistryContext(this.register(serverRegistry.getId(serverBiome), entry.getKey(), serverBiome, ((RegistryAccess<Biome>) serverRegistry).invokeGetLifecycleByRegistry(serverBiome))).getNewBiome();
            @Nullable ResourceLocation registryName = serverBiome.getRegistryName();
            worldBiome.setRegistryName(registryName);
            this.serverBiomeToWorldBiome.put(serverBiome, worldBiome);
        }
        String s = "";
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
    public Lifecycle getLifecycleByRegistry(Biome object) {
        if (this.serverBiomeToWorldBiome.containsKey(object)) {
            return super.getLifecycleByRegistry(this.serverBiomeToWorldBiome.get(object));
        }
        return super.getLifecycleByRegistry(object);
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

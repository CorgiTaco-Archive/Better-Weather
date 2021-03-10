package corgitaco.betterweather.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import corgitaco.betterweather.mixin.access.RegistryAccess;
import corgitaco.betterweather.mixin.access.SimpleRegistryAccess;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.*;
import net.minecraft.world.biome.Biome;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;


/**
 * Used to allow either server or world specific biome objects to function to return the same common object for the given world
 */
public class CommonKeyMutableRegistry extends SimpleRegistry<Biome> {

    private final MutableRegistry<Biome> serverMutableRegistry;


    private final IdentityHashMap<Biome, Biome> serverBiomeToWorldBiome = new IdentityHashMap<>();

    public CommonKeyMutableRegistry(MutableRegistry<Biome> serverMutableRegistry) {
        super(serverMutableRegistry.getRegistryKey(), serverMutableRegistry.getLifecycle());
        this.serverMutableRegistry = serverMutableRegistry;

        fillWorldRegistry(serverMutableRegistry);
    }


    @SuppressWarnings("unchecked")
    private void fillWorldRegistry(MutableRegistry<Biome> serverRegistry) {
        for (Map.Entry<RegistryKey<Biome>, Biome> entry : serverRegistry.getEntries()) {
            Biome serverBiome = entry.getValue();
            Function<Biome, DataResult<JsonElement>> biomeCodec = JsonOps.INSTANCE.withEncoder(Biome.CODEC);

            Optional<JsonElement> optional = (biomeCodec.apply(serverBiome).result());

            Biome worldBiome = new BiomeRegistryContext(this.register(serverRegistry.getId(serverBiome), entry.getKey(), serverBiome, ((RegistryAccess<Biome>) serverRegistry).invokeGetLifecycleByRegistry(serverBiome))).getNewBiome();

            this.serverBiomeToWorldBiome.put(serverBiome, worldBiome);
        }
    }

    @Nullable
    @Override
    public ResourceLocation getKey(Biome value) {
        return serverMutableRegistry.getKey(value);
    }

    @Override
    public Optional<RegistryKey<Biome>> getOptionalKey(Biome value) {
        return serverMutableRegistry.getOptionalKey(value);
    }

    @Override
    public int getId(@Nullable Biome value) {
        return serverMutableRegistry.getId(value);
    }

    @Nullable
    @Override
    public Biome getByValue(int value) {
        return serverMutableRegistry.getByValue(value);
    }

    @Nullable
    @Override
    public Biome getValueForKey(@Nullable RegistryKey<Biome> key) {
        return serverMutableRegistry.getValueForKey(key);
    }

    @Nullable
    @Override
    public Biome getOrDefault(@Nullable ResourceLocation name) {
        return serverMutableRegistry.getOrDefault(name);
    }

    @SuppressWarnings({"unchecked", "NullableProblems"})
    @Override
    public Lifecycle getLifecycleByRegistry(Biome object) {
        return ((RegistryAccess<Biome>) serverMutableRegistry).invokeGetLifecycleByRegistry(object);
    }

    @Override
    public Lifecycle getLifecycle() {
        return serverMutableRegistry.getLifecycle();
    }

    @Override
    public Set<ResourceLocation> keySet() {
        return serverMutableRegistry.keySet();
    }

    @Override
    public Set<Map.Entry<RegistryKey<Biome>, Biome>> getEntries() {
        return serverMutableRegistry.getEntries();
    }

    @Override
    public boolean containsKey(ResourceLocation name) {
        return serverMutableRegistry.containsKey(name);
    }

    @Override
    public Iterator<Biome> iterator() {
        return serverMutableRegistry.iterator();
    }
}

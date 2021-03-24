package corgitaco.betterweather.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Lifecycle;
import com.mojang.serialization.codecs.UnboundedMapCodec;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.DimensionType;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Used to redirect calls/obtainers of the biome registry to a per world biome registry instead.
 */
public class WorldDynamicRegistry extends DynamicRegistries.Impl {

    private final Impl serverRegistry;

    private final CommonKeyMutableRegistry commonKeyMutableRegistry;

    public WorldDynamicRegistry(Impl serverRegistry) {
        this.serverRegistry = serverRegistry;
        this.commonKeyMutableRegistry = new CommonKeyMutableRegistry(serverRegistry.getRegistry(Registry.BIOME_KEY));
        this.keyToSimpleRegistryMap = fill();
    }

    public Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> fill() {
        return DynamicRegistries.registryCodecMap.keySet().stream().collect(Collectors.toMap(Function.identity(), this::createStableRegistry));
    }

    private <E> SimpleRegistry<?> createStableRegistry(RegistryKey<? extends Registry<?>> key) {
        if (key.getLocation().toString().equals("minecraft:worldgen/biome"))
            return this.commonKeyMutableRegistry;
        else
            return new SimpleRegistry(key, Lifecycle.stable());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<MutableRegistry<E>> func_230521_a_(RegistryKey<? extends Registry<E>> key) {
        if (key.getLocation().toString().equals("minecraft:worldgen/biome")) {
            return Optional.of((MutableRegistry<E>) commonKeyMutableRegistry);
        }

        return serverRegistry.func_230521_a_(key);
    }

    @Override
    public <E> MutableRegistry<E> getRegistry(RegistryKey<? extends Registry<E>> key) {
        if (key.getLocation().toString().equals("minecraft:worldgen/biome")) {
            return this.func_230521_a_(key).orElseThrow(() -> {
                return new IllegalStateException("Missing registry: " + key);
            });
        }

        return serverRegistry.getRegistry(key);
    }

    @Override
    public Registry<DimensionType> func_230520_a_() {
        return serverRegistry.func_230520_a_();
    }
}

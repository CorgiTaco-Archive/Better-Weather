package corgitaco.betterweather.util;

import com.mojang.serialization.Lifecycle;
import corgitaco.betterweather.mixin.access.DynamicRegistriesAccess;
import corgitaco.betterweather.mixin.access.DynamicRegistriesImplAccess;
import net.minecraft.util.RegistryKey;
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
        this.commonKeyMutableRegistry = new CommonKeyMutableRegistry(serverRegistry.registryOrThrow(Registry.BIOME_REGISTRY));
        ((DynamicRegistriesImplAccess) this).setRegistries(fill());
    }

    public Map<? extends RegistryKey<? extends Registry<?>>, ? extends SimpleRegistry<?>> fill() {
        return DynamicRegistriesAccess.getREGISTRIES().keySet().stream().collect(Collectors.toMap(Function.identity(), this::createStableRegistry));
    }

    private <E> SimpleRegistry<?> createStableRegistry(RegistryKey<? extends Registry<?>> key) {
        if (key.location().toString().equals("minecraft:worldgen/biome"))
            return this.commonKeyMutableRegistry;
        else
            return new SimpleRegistry(key, Lifecycle.stable());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> Optional<MutableRegistry<E>> registry(RegistryKey<? extends Registry<E>> key) {
        if (key.location().toString().equals("minecraft:worldgen/biome")) {
            return Optional.of((MutableRegistry<E>) commonKeyMutableRegistry);
        }

        return serverRegistry.registry(key);
    }

    @Override
    public <E> MutableRegistry<E> registryOrThrow(RegistryKey<? extends Registry<E>> key) {
        if (key.location().toString().equals("minecraft:worldgen/biome")) {
            return this.registry(key).orElseThrow(() -> {
                return new IllegalStateException("Missing registry: " + key);
            });
        }

        return serverRegistry.registryOrThrow(key);
    }

    @Override
    public Registry<DimensionType> dimensionTypes() {
        return serverRegistry.dimensionTypes();
    }
}

package corgitaco.betterweather.util;

import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.DimensionType;

import java.util.Optional;


/**
 * Used to allow either server or world specific biome objects to function as key to return the same common biome object
 */
public class WorldDynamicRegistry extends DynamicRegistries.Impl {

    private final Impl serverRegistry;

    private final CommonKeyMutableRegistry commonKeyMutableRegistry;

    public WorldDynamicRegistry(DynamicRegistries.Impl serverRegistry) {
        this.serverRegistry = serverRegistry;
        this.commonKeyMutableRegistry = new CommonKeyMutableRegistry(serverRegistry.getRegistry(Registry.BIOME_KEY));
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

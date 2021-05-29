package corgitaco.betterweather.util;

import com.mojang.serialization.Lifecycle;
import corgitaco.betterweather.mixin.access.BiomeAccess;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

/**
 * Used to allow either server or world specific biome objects to function as keys to return the same common biome for the given world since each world can contain a unique biome registry.
 */
public class CommonKeyMutableRegistry extends SimpleRegistry<Biome> {
    private final Map<Biome, Biome> storage = new IdentityHashMap<>();

    // Registry is the Server registry.
    public CommonKeyMutableRegistry(MutableRegistry<Biome> registry) {
        super(registry.getRegistryKey(), registry.getLifecycle());

        registry.getEntries().forEach(entry -> {
            Biome biome1 = entry.getValue();
            Biome biome2 = register(
                    registry.getId(biome1),
                    entry.getKey(),
                    shallow(biome1),
                    ((SimpleRegistry<Biome>) registry).getLifecycleByRegistry(biome1)
            );

            ResourceLocation name = requireNonNull(biome1.getRegistryName(), "Invalid Biome registry name.");

            storage.put(biome1, biome2.setRegistryName(name));
        });
    }

    // Creates a shallow copy of a biome.
    private static Biome shallow(Biome biome) {
        @SuppressWarnings("ConstantConditions") // Mixins are used.
        Biome.Climate climate = ((BiomeAccess) (Object) biome).getClimate();

        return BiomeAccess.create(
                climate,
                biome.getCategory(),
                biome.getDepth(),
                biome.getScale(),
                biome.getAmbience(),
                biome.getGenerationSettings(),
                biome.getMobSpawnInfo()
        );
    }

    // Overrides.
    @Override
    public ResourceLocation getKey(@NotNull Biome biome) {
        return super.getKey(get(biome));
    }

    @Override
    public @NotNull Optional<RegistryKey<Biome>> getOptionalKey(@NotNull Biome biome) {
        return super.getOptionalKey(get(biome));
    }

    @Override
    public int getId(@Nullable Biome biome) {
        return super.getId(get(biome));
    }

    @Override
    public @NotNull Lifecycle getLifecycleByRegistry(@NotNull Biome biome) {
        return super.getLifecycleByRegistry(get(biome));
    }

    // Get the mapped biome, otherwise the biome itself.
    @Contract("null -> param1")
    private Biome get(Biome biome) {
        return storage.getOrDefault(biome, biome);
    }
}

package corgitaco.betterweather.util;

import com.mojang.serialization.Lifecycle;
import corgitaco.betterweather.mixin.access.BiomeAccess;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.MutableRegistry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeAmbience;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.MobSpawnInfo;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.IdentityHashMap;
import java.util.Optional;

import static java.util.Objects.requireNonNull;


/**
 * Used to allow either server or world specific biome objects to function as keys to return the same common biome for the given world since each world can contain a unique biome registry.
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class CommonKeyMutableRegistry extends SimpleRegistry<Biome> {
    private final IdentityHashMap<Biome, Biome> server2World = new IdentityHashMap<>();

    public CommonKeyMutableRegistry(MutableRegistry<Biome> serverMutableRegistry) {
        super(serverMutableRegistry.getRegistryKey(), serverMutableRegistry.getLifecycle());
        fillWorldRegistry(serverMutableRegistry);
    }

    private void fillWorldRegistry(MutableRegistry<Biome> registry) {
        // since there are many many keys and values we need to use a stream, but not a parallel, because the order is needed.
        registry.getEntries().forEach(entry -> {
            Biome server = entry.getValue();
            Biome world = register(registry.getId(server), entry.getKey(), copy(server), ((SimpleRegistry<Biome>) registry).getLifecycleByRegistry(server)); // warn: lifecycle will always be null

            // server biome registry will never be null. if it somehow manages to be null, error.
            ResourceLocation location = requireNonNull(server.getRegistryName());
            world.setRegistryName(location);

            server2World.put(server, world);
        });
    }

    // create a shallow copy of the server biome. ignore the class cast exception, as mixins are used
    private static Biome copy(Biome biome) {
        @SuppressWarnings("ConstantConditions") Biome.Climate climate = ((BiomeAccess) (Object) biome).getClimate();
        Biome.Category category = biome.getCategory();
        float depth = biome.getDepth();
        float scale = biome.getScale();
        BiomeAmbience ambiance = biome.getAmbience();
        BiomeGenerationSettings settings = biome.getGenerationSettings();
        MobSpawnInfo info = biome.getMobSpawnInfo();

        return new Biome(climate, category, depth, scale, ambiance, settings, info);
    }

    // methods below are self explanatory. get the world biome from the server biome if available, otherwise just the server biome.

    @Override
    public ResourceLocation getKey(Biome biome) {
        return super.getKey(server2World.getOrDefault(biome, biome));
    }

    @Override
    public Optional<RegistryKey<Biome>> getOptionalKey(Biome biome) {
        return super.getOptionalKey(server2World.getOrDefault(biome, biome));
    }

    @Override
    public int getId(@Nullable Biome biome) {
        return super.getId(server2World.getOrDefault(biome, biome));
    }

    @Override
    public Lifecycle getLifecycleByRegistry(Biome biome) {
        return super.getLifecycleByRegistry(server2World.getOrDefault(biome, biome));
    }
}

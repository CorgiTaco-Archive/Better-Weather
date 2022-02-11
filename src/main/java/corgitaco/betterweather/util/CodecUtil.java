package corgitaco.betterweather.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

@SuppressWarnings("deprecation")
public class CodecUtil {
    public static final Codec<Block> BLOCK_CODEC = createLoggedExceptionRegistryCodec(Registry.BLOCK);

    public static final Codec<RegistryKey<Biome>> BIOME_CODEC = ResourceLocation.CODEC.comapFlatMap(resourceLocation -> DataResult.success(RegistryKey.create(Registry.BIOME_REGISTRY, resourceLocation)), RegistryKey::location);

    public static <T> Codec<T> createLoggedExceptionRegistryCodec(Registry<T> registry) {
        return ResourceLocation.CODEC.comapFlatMap(location -> {
            final T result = registry.get(location);

            if (result == null) {
                StringBuilder registryElements = new StringBuilder();
                for (int i = 0; i < registry.entrySet().size(); i++) {
                    final T object = registry.byId(i);
                    registryElements.append(i).append(". \"").append(registry.getKey(object).toString()).append("\"\n");
                }

                throw new IllegalArgumentException(String.format("\"%s\" is not a valid id in registry: %s.\n Current Registry Values:\n%s", location.toString(), registry.toString(), registryElements.toString()));
            }
            return DataResult.success(result);
        }, registry::getKey);
    }
}

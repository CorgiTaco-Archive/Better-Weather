package corgitaco.betterweather.common.season.config.cropfavoritebiomes;

import com.google.gson.*;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.common.season.config.overrides.OverrideDeserializer;
import it.unimi.dsi.fastutil.objects.Object2DoubleArrayMap;
import it.unimi.dsi.fastutil.objects.Object2DoubleMap;
import net.minecraft.block.Block;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
public class CropFavoriteBiomesDeserializer implements JsonSerializer<IdentityHashMap<Block, Object2DoubleArrayMap<Object>>>, JsonDeserializer<IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>>> {

    private final Registry<Biome> biomeRegistry;
    private final Map<Biome.Category, List<Biome>> categoryBiomes;
    private final Map<BiomeDictionary.Type, List<Biome>> biomeDictionaryBiomes;

    public CropFavoriteBiomesDeserializer(Registry<Biome> biomeRegistry) {
        this.biomeRegistry = biomeRegistry;
        this.categoryBiomes = biomeRegistry.entrySet().stream().map(Map.Entry::getValue).collect(Collectors.groupingBy(Biome::getBiomeCategory));
        this.biomeDictionaryBiomes = biomeRegistry.entrySet().stream().flatMap(biomeKey -> BiomeDictionary.getTypes(biomeKey.getKey()).stream().map(biomeDictionaryType -> new AbstractMap.SimpleEntry<>(biomeDictionaryType, biomeKey.getValue()))).collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));
    }

    @Override
    public IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Set<Map.Entry<String, JsonElement>> blockFavBiomesEntries = json.getAsJsonObject().entrySet();
        StringBuilder errors = new StringBuilder();
        IdentityHashMap<Block, Object2DoubleArrayMap<RegistryKey<Biome>>> cropToFavoriteBiome = new IdentityHashMap<>();

        for (Map.Entry<String, JsonElement> entry : blockFavBiomesEntries) {
            String key = entry.getKey();
            ResourceLocation resourceLocation = ResourceLocation.tryParse(key);
            Optional<Block> blockOptional = Registry.BLOCK.getOptional(resourceLocation);
            if (!blockOptional.isPresent()) {
                BetterWeather.LOGGER.error("\"" + key + "\" is not a valid block resource location, skipping...");
                continue;
            }

            Block block = blockOptional.get();

            Set<Map.Entry<String, JsonElement>> favBiomeEntries = entry.getValue().getAsJsonObject().entrySet();

            for (Map.Entry<String, JsonElement> favBiomeEntry : favBiomeEntries) {
                String favBiomeEntryKey = favBiomeEntry.getKey();
                Object type = OverrideDeserializer.extractKey(errors, favBiomeEntryKey, biomeRegistry);
                if(type == null) {
                    continue;
                }

                double asDouble = favBiomeEntry.getValue().getAsDouble();
                if (asDouble <= 0.0) {
                    BetterWeather.LOGGER.warn("Bonus must be above 0.0. You put \"" + asDouble + "\" for biome entry \"" + key + "\" at crop \"" + key + "\".");
                    continue;
                }

                if (type instanceof Biome) {
                    cropToFavoriteBiome.computeIfAbsent(block, (block1 -> new Object2DoubleArrayMap<>())).put(this.biomeRegistry.getResourceKey((Biome) type).orElse(null), asDouble);
                } else if (type instanceof Biome.Category) {
                    for (Biome biome : this.categoryBiomes.get((Biome.Category) type)) {
                        cropToFavoriteBiome.computeIfAbsent(block, (block1 -> new Object2DoubleArrayMap<>())).put(this.biomeRegistry.getResourceKey(biome).orElse(null), asDouble);
                    }
                } else if (type instanceof BiomeDictionary.Type) {
                    for (Biome biome : this.biomeDictionaryBiomes.getOrDefault((BiomeDictionary.Type) type, new ArrayList<>())) {
                        cropToFavoriteBiome.computeIfAbsent(block, (block1 -> new Object2DoubleArrayMap<>())).put(this.biomeRegistry.getResourceKey(biome).orElse(null), asDouble);
                    }
                }
            }
        }

        if (!errors.toString().isEmpty()) {
            BetterWeather.LOGGER.error(errors);
        }

        return cropToFavoriteBiome;
    }

    @Override
    public JsonElement serialize(IdentityHashMap<Block, Object2DoubleArrayMap<Object>> src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject result = new JsonObject();
        for (Map.Entry<Block, Object2DoubleArrayMap<Object>> blockObject2DoubleArrayMapEntry : src.entrySet()) {
            Block key = blockObject2DoubleArrayMapEntry.getKey();
            Object2DoubleArrayMap<Object> favoriteBiomes = blockObject2DoubleArrayMapEntry.getValue();

            JsonObject favBiomes = new JsonObject();

            for (Object2DoubleMap.Entry<Object> objectEntry : favoriteBiomes.object2DoubleEntrySet()) {
                Object object = objectEntry.getKey();
                double value = objectEntry.getDoubleValue();
                if (object instanceof Biome.Category) {
                    Biome.Category category = (Biome.Category) object;
                    favBiomes.addProperty("category/" + category.toString(), value);
                } else if (object instanceof BiomeDictionary.Type) {
                    BiomeDictionary.Type type = (BiomeDictionary.Type) object;
                    favBiomes.addProperty("forge/" + type.toString(), value);
                } else if (object instanceof ResourceLocation) {
                    ResourceLocation location = (ResourceLocation) object;
                    favBiomes.addProperty("biome/" + location.toString(), value);
                } else {
                    throw new IllegalArgumentException("Could not serialize object of class type: " + object.getClass().getName());
                }
            }

            result.add(Registry.BLOCK.getKey(key).toString(), favBiomes);
        }
        return result;
    }
}

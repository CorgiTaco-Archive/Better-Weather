package corgitaco.betterweather.season.seasonoverrides;

import com.google.gson.*;
import corgitaco.betterweather.season.BWSeasonSystem;
import corgitaco.betterweather.season.Season;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class SeasonOverrideOptionSerializer implements JsonSerializer<SeasonOverrides>, JsonDeserializer<SeasonOverrides> {
    @Override
    public SeasonOverrides deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        IdentityHashMap<Object, SeasonOverrideEntry> overrideEntryMap = new IdentityHashMap<>();
        StringBuilder errorBuilder = new StringBuilder();

        int idx = 0;
        Set<Map.Entry<String, JsonElement>> entrySet = object.entrySet();

        for (Map.Entry<String, JsonElement> entry : entrySet) {
            String key = entry.getKey();
            Object value = extractKeys(errorBuilder, key);

            if (value == null)
                continue;

            SeasonOverrideEntry seasonOverrideEntry = createSeasonOverrideEntry(entry.getValue(), value, key, idx, errorBuilder);
            overrideEntryMap.put(value, seasonOverrideEntry);
            idx++;
        }

        if (!errorBuilder.toString().isEmpty())
            throw new IllegalArgumentException("The following errors were found in \"overrides.json\": " + errorBuilder);

        return new SeasonOverrides(overrideEntryMap);
    }


    public static SeasonOverrides preprocessOverrides(SeasonOverrides overrides, Registry<Biome> biomeRegistry) {
        IdentityHashMap<Object, SeasonOverrideEntry> oldMap = overrides.getOverrides();
        IdentityHashMap<Object, SeasonOverrideEntry> newMap = new IdentityHashMap<>();
        Map<Biome.Category, List<Biome>> categoryListMap = biomeRegistry.getEntries().stream().map(Map.Entry::getValue).collect(Collectors.groupingBy(Biome::getCategory));
        Map<BiomeDictionary.Type, List<Biome>> biomeDictionaryMap = biomeRegistry.getEntries().stream()
                .flatMap(e -> BiomeDictionary.getTypes(e.getKey()).stream().map(x -> new AbstractMap.SimpleEntry<>(x, e.getValue())))
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.mapping(Map.Entry::getValue, Collectors.toList())));

        for (Object object : oldMap.keySet()) {
            SeasonOverrideEntry seasonOverrideEntry = preprocessOverrideEntry(oldMap.get(object), categoryListMap, biomeDictionaryMap);
            if (object instanceof Biome.Category) {
                for (Biome biome : categoryListMap.get(object)) {
                    newMap.put(biome, seasonOverrideEntry);
                }
            }
            else if (object instanceof BiomeDictionary.Type) {
                for (Biome biome : biomeDictionaryMap.get(object))
                    newMap.put(biome, seasonOverrideEntry);
            }
            else if (object instanceof BWSeasonSystem.SeasonVal) {
                Season.getSeasonFromEnum((BWSeasonSystem.SeasonVal) object).getSubSeasons().stream().map(Season.SubSeason::getSubSeasonVal).forEach(subSeasonVal -> newMap.put(subSeasonVal, seasonOverrideEntry));
            }
            else {
                newMap.put(object, seasonOverrideEntry);
            }
        }
        return new SeasonOverrides(newMap);
    }

    private static SeasonOverrideEntry preprocessOverrideEntry(SeasonOverrideEntry seasonOverrideEntry, Map<Biome.Category, List<Biome>> categoryListMap, Map<BiomeDictionary.Type, List<Biome>> biomeDictionaryMap) {
        IdentityHashMap<Object, SeasonOverrideOption> oldMap = seasonOverrideEntry.getSubOverrides();
        IdentityHashMap<Object, SeasonOverrideOption> newMap = new IdentityHashMap<>();

        for (Object object : oldMap.keySet()) {
            SeasonOverrideOption seasonOverrideOption = oldMap.get(object);
            if (object instanceof Biome.Category) {
                for (Biome biome : categoryListMap.get(object)) {
                    newMap.put(biome, seasonOverrideOption);
                }
            }
            else if (object instanceof BiomeDictionary.Type) {
                for (Biome biome : biomeDictionaryMap.get(object))
                    newMap.put(biome, seasonOverrideOption);
            }
            else if (object instanceof BWSeasonSystem.SeasonVal) {
                Season.getSeasonFromEnum((BWSeasonSystem.SeasonVal) object).getSubSeasons().stream().map(Season.SubSeason::getSubSeasonVal).forEach(subSeasonVal -> newMap.put(subSeasonVal, seasonOverrideOption));
            }
            else {
                newMap.put(object, seasonOverrideOption);
            }
        }
        return new SeasonOverrideEntry(seasonOverrideEntry.getPriority(), seasonOverrideEntry.getMain(), newMap);
    }


    private SeasonOverrideEntry createSeasonOverrideEntry(JsonElement element, Object outerEntryKey, String outerKeyString, int priority, StringBuilder errorBuilder) {
        JsonObject object = element.getAsJsonObject();
        SeasonOverrideOption seasonOverrideOption = seasonOverrideOption(element, errorBuilder);

        IdentityHashMap<Object, SeasonOverrideOption> subOverrides = new IdentityHashMap<>();

        for (Map.Entry<String, JsonElement> objects : object.entrySet()) {
            String key = objects.getKey();
            if (key.equals("default") || key.equals("crops"))
                continue;

            Object keyObject = extractKeys(errorBuilder, key);

            if ((outerEntryKey instanceof Biome || outerEntryKey instanceof Biome.Category || outerEntryKey instanceof BiomeDictionary.Type) && (keyObject instanceof Biome || keyObject instanceof Biome.Category || keyObject instanceof BiomeDictionary.Type)) {
                errorBuilder.append("Attempting to add override for biome ").append(key).append(" while overriding biome ").append(outerKeyString).append(".\n");
                continue;
            }

            if ((outerEntryKey instanceof BWSeasonSystem.SubSeasonVal || outerEntryKey instanceof BWSeasonSystem.SeasonVal) && (keyObject instanceof BWSeasonSystem.SeasonVal || keyObject instanceof BWSeasonSystem.SubSeasonVal)) {
                errorBuilder.append("Attempting to add override for season ").append(key).append(" while overriding season ").append(outerKeyString).append(".\n");
                continue;
            }

            SeasonOverrideOption seasonOverrideOption1 = seasonOverrideOption(objects.getValue(), errorBuilder);
            subOverrides.put(keyObject, seasonOverrideOption1);

        }
        return new SeasonOverrideEntry(priority, seasonOverrideOption, subOverrides);
    }


    private SeasonOverrideOption seasonOverrideOption(JsonElement element, StringBuilder errorBuilder) {
        JsonObject object = element.getAsJsonObject();
        double defaultVal = object.get("default").getAsDouble();

        IdentityHashMap<Block, Double> cropToMultiplier = new IdentityHashMap<>();
        Set<Map.Entry<String, JsonElement>> entrySet = object.get("crops").getAsJsonObject().entrySet();

        for (Map.Entry<String, JsonElement> cropEntry : entrySet) {
            Block block = Registry.BLOCK.getOptional(new ResourceLocation(cropEntry.getKey())).orElse(null);
            if (block != null) {
                cropToMultiplier.put(block, cropEntry.getValue().getAsDouble());
            }
            else {
                errorBuilder.append(cropEntry.getKey()).append(" is not a block in the registry!\n");
            }
        }
        return new SeasonOverrideOption(defaultVal, cropToMultiplier);
    }

    private Object extractKeys(StringBuilder errorBuilder, String key) {
        Object value;
        if (key.startsWith("category/")) {
            try {
                value = Biome.Category.valueOf(key.substring("category/".length()));
            } catch (IllegalArgumentException e) {
                errorBuilder.append(key.substring("category/".length())).append(" is not a Biome Category Value!\n");
                return null;
            }
        }
        else if (key.startsWith("forge/")) {
            value = BiomeDictionary.Type.getType(key.substring("forge/".length()));
        }
        else if (key.startsWith("biome/")) {
            value = ServerLifecycleHooks.getCurrentServer().func_244267_aX().func_230521_a_(Registry.BIOME_KEY).get().getOptional(new ResourceLocation(key.substring("biome/".length()))).orElse(null);
            if (value == null) {
                errorBuilder.append(key.substring("biome/".length())).append(" is not a biome in this world!\n");
                return null;
            }
        }
        else if (key.startsWith("season/")) {
            try {
                value = BWSeasonSystem.SeasonVal.valueOf(key.substring("season/".length()));
            } catch (IllegalArgumentException e) {
                errorBuilder.append(key.substring("season/".length())).append(" is not a Season!\n");
                return null;
            }
        }
        else if (key.startsWith("subseason/")) {
            try {
                value = BWSeasonSystem.SubSeasonVal.valueOf(key.substring("subseason/".length()));
            } catch (IllegalArgumentException e) {
                errorBuilder.append(key.substring("subseason/".length())).append(" is not a Subseason!\n");
                return null;
            }
        }
        else {
            errorBuilder.append(key).append(" is not a Biome/Category/Forge/SubSeason/Season identifier\n");
            return null;
        }
        return value;
    }

    @Override
    public JsonElement serialize(SeasonOverrides src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }

}

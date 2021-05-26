package corgitaco.betterweather.util.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.BWSubseasonSettings;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

import static net.minecraft.util.registry.Registry.BIOME_KEY;

public class ColorUtil {
    private static final int MASK = 0xFF; // bit mask

    public static final int DEFAULT_RAIN_SKY = pack(103, 114, 136);
    public static final int DEFAULT_RAIN_FOG = pack(89, 100, 142);
    public static final int DEFAULT_RAIN_CLOUDS = pack(158, 158, 158);

    public static final int DEFAULT_THUNDER_SKY = pack(42, 45, 51);
    public static final int DEFAULT_THUNDER_FOG = pack(85, 95, 135);
    public static final int DEFAULT_THUNDER_CLOUDS = pack(37, 37, 37);


    public static int getBiomeColor(Biome biome, Type type, int previous) {
        Minecraft minecraft = Minecraft.getInstance();

        ClientWorld world = minecraft.world;
        if (world == null) {
            return previous;
        }

        SeasonContext context = ((BetterWeatherWorldData) world).getSeasonContext();
        if (context == null) {
            return previous;
        }

        Optional<RegistryKey<Biome>> optionalKey = world.func_241828_r().getRegistry(BIOME_KEY).getOptionalKey(biome);
        if (!optionalKey.isPresent()) {
            return previous;
        }

        RegistryKey<Biome> key = optionalKey.get();
        BWSubseasonSettings settings = context.getCurrentSubSeasonSettings();

        int target;
        double blendStrength;
        switch (type) {
            case GRASS:
                int grassTarget = settings.getTargetGrassColor(key);

                if (grassTarget == Integer.MAX_VALUE) {
                    return previous;
                }

                target = grassTarget;
                blendStrength = settings.getGrassColorBlendStrength(key);
                break;
            case FOLIAGE:
                int foliageTarget = settings.getTargetFoliageColor(key);

                if (foliageTarget == Integer.MAX_VALUE) {
                    return previous;
                }

                target = foliageTarget;
                blendStrength = settings.getFoliageColorBlendStrength(key);
                break;
            case FOG:
                int fogTarget = settings.getTargetFogColor(key);

                if (fogTarget == Integer.MAX_VALUE) {
                    return previous;
                }

                target = fogTarget;
                blendStrength = settings.getFogColorBlendStrength(key);
                break;
            default:
                int skyTarget = settings.getTargetSkyColor(key);

                if (skyTarget == Integer.MAX_VALUE) {
                    return previous;
                }

                target = skyTarget;
                blendStrength = settings.getSkyColorBlendStrength(key);
                break;
        }

        return blend(previous, target, blendStrength);
    }

    public static int blend(int start, int end, double blend) {
        int[] us = unpack(start); // unpacked start
        int[] ue = unpack(end); // unpacked end

        return mix(us, ue, (float) blend);
    }

    public static int mix(int[] start, int[] end, float blend) {
        float alpha = lerp(start[0], end[0], blend);
        float red = lerp(start[1], end[1], blend);
        float green = lerp(start[2], end[2], blend);
        float blue = lerp(start[3], end[3], blend);

        long mask = 0xFFL;

        return (int) ((((int) alpha & mask) << 24) | (((int) red & mask) << 16) | (((int) green & mask) << 8) | ((int) blue & mask));
    }

    private static float lerp(int start, int end, float blend) {
        return start + ((end - start) * blend);
    }

    // packs rgb channels into a decimal
    public static int pack(int a, int r, int g, int b) {
        return (a & MASK) << 24 | (r & MASK) << 16 | (g & MASK) << 8 | b & MASK;
    }

    public static int pack(int r, int g, int b) {
        return pack(255, r, g, b);
    }

    // unpacks rgb channels from a decimal
    public static int[] unpack(int decimal) {
        return new int[]{
                (decimal >> 24) & MASK,
                (decimal >> 16) & MASK,
                (decimal >> 8) & MASK,
                decimal & MASK
        };
    }

    public static int tryParseColor(String input) {
        int result = Integer.MAX_VALUE;

        if (input.isEmpty()) {
            return result;
        }

        try {
            result = (int) Long.parseLong(input.replace("#", "").replace("0x", ""), 16);
        } catch (NumberFormatException e) {
            BetterWeather.LOGGER.error("Not a valid hex string: " + input + ". Doing nothing with this value...");
        }
        return result;
    }

    public enum Type {
        GRASS,
        FOLIAGE,
        FOG,
        SKY
    }
}

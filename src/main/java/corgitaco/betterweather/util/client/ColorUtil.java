package corgitaco.betterweather.util.client;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.BWSubseasonSettings;
import corgitaco.betterweather.season.SeasonContext;
import corgitaco.betterweather.season.client.ColorSettings;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

import static net.minecraft.util.registry.Registry.BIOME_KEY;

/*
Notes:

- Biome colors don't use alpha, so really no need to blend the alpha.
- The American English spelling of "colour" is normalised so much, on the internet in general.
 */
@SuppressWarnings("unused") // Default constants are not being used.
public final class ColorUtil {
    private static final long BIT_MASK = 0xFF; // 255

    public static final int DEFAULT_RAIN_SKY = pack(103, 114, 136);
    public static final int DEFAULT_RAIN_FOG = pack(89, 100, 142);
    public static final int DEFAULT_RAIN_CLOUDS = pack(158, 158, 158);

    public static final int DEFAULT_THUNDER_SKY = pack(42, 45, 51);
    public static final int DEFAULT_THUNDER_FOG = pack(85, 95, 135);
    public static final int DEFAULT_THUNDER_CLOUDS = pack(37, 37, 37);

    // Preferable for utility classes to not be instantiated.
    private ColorUtil() {
    }

    public static int getBiomeColor(Biome biome, Type type, int previous) {
        Minecraft minecraft = Minecraft.getInstance();

        ClientWorld world = minecraft.world;
        if (world == null) {
            return previous;
        }

        SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();

        if (seasonContext == null && weatherEventContext == null) {
            return previous;
        }

        Optional<RegistryKey<Biome>> optionalKey = world.func_241828_r().getRegistry(BIOME_KEY).getOptionalKey(biome);
        if (!optionalKey.isPresent()) {
            return previous;
        }


        RegistryKey<Biome> key = optionalKey.get();

        int seasonTarget = Integer.MAX_VALUE;
        double seasonBlend = -1;

        if (seasonContext != null) {
            BWSubseasonSettings settings = seasonContext.getCurrentSubSeasonSettings();

            switch (type) {
                case GRASS:
                    seasonTarget = clamp(settings.getTargetGrassColor(key), previous);
                    seasonBlend = settings.getGrassColorBlendStrength(key);
                    break;
                case FOLIAGE:
                    seasonTarget = clamp(settings.getTargetFoliageColor(key), previous);
                    seasonBlend = settings.getFoliageColorBlendStrength(key);
                    break;
                case FOG:
                    seasonTarget = clamp(settings.getTargetFogColor(key), previous);
                    seasonBlend = settings.getFogColorBlendStrength(key);
                    break;
                default:
                    seasonTarget = clamp(settings.getTargetSkyColor(key), previous);
                    seasonBlend = settings.getSkyColorBlendStrength(key);
                    break;
            }
        }

        int weatherTarget = Integer.MAX_VALUE;
        double weatherBlend = -1;

        if (weatherEventContext != null) {
            ColorSettings colorSettings = weatherEventContext.getCurrentEvent().getClientSettings().getColorSettings();

            switch (type) {
                case GRASS:
                    if (!weatherEventContext.isRefreshRenderers()) {
                        break;
                    }
                    weatherTarget = clamp(colorSettings.getTargetFogHexColor(), previous);
                    weatherBlend = colorSettings.getGrassColorBlendStrength();
                    break;
                case FOLIAGE:
                    if (!weatherEventContext.isRefreshRenderers()) {
                        break;
                    }
                    weatherTarget = clamp(colorSettings.getTargetFoliageHexColor(), previous);
                    weatherBlend = colorSettings.getFoliageColorBlendStrength();
                    break;
                case FOG:
                    weatherTarget = clamp(colorSettings.getTargetFogHexColor(), previous);
                    weatherBlend = colorSettings.getFogColorBlendStrength();
                    break;
                default:
                    weatherTarget = clamp(colorSettings.getTargetSkyHexColor(), previous);
                    weatherBlend = colorSettings.getSkyColorBlendStrength();
                    break;
            }
        }

        int seasonMix = mix(unpack(previous), unpack(seasonTarget), seasonBlend);
        int weatherMix = weatherEventContext != null ? mix(unpack(seasonContext != null ? seasonMix : previous), unpack(weatherTarget), weatherBlend) : Integer.MAX_VALUE;
        return weatherMix == Integer.MAX_VALUE ? seasonMix : weatherMix;
    }

    private static int clamp(int target, int fallback) {
        return target == Integer.MAX_VALUE ? fallback : target;
    }

    // Mix two colour arrays together. Similar to additive color mixing.
    public static int mix(int[] start, int end[], double blend) {
        return pack(
                lerp(start[0], end[0], blend), // Alpha.
                lerp(start[1], end[1], blend), // Red.
                lerp(start[2], end[2], blend), // Green.
                lerp(start[3], end[3], blend)  // Blue.
        );
    }

    // Interpolate between color channels.
    private static int lerp(int start, int end, double blend) {
        return (int) (start + ((end - start) * blend));
    }

    // Packs ARGB into a decimal.
    public static int pack(int a, int r, int g, int b) {
        return (int) (
                ((a & BIT_MASK) << 24) |
                        ((r & BIT_MASK) << 16) |
                        ((g & BIT_MASK)) << 8 |
                        (b & BIT_MASK)
        );
    }

    // Packs ARGB decimal, with an alpha of 255.
    public static int pack(int r, int g, int b) {
        return pack(255, r, g, b);
    }

    // Unpacks ARGB channels from a decimal.
    public static int[] unpack(int decimal) {
        return new int[]{
                (int) ((decimal >> 24) & BIT_MASK), // Alpha.
                (int) ((decimal >> 16) & BIT_MASK), // Red.
                (int) ((decimal >> 8) & BIT_MASK),  // Green.
                (int) (decimal & BIT_MASK)          // Blue.
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
            BetterWeather.LOGGER.warn("\"{}\" is not a valid hex. Defaulted to Vanilla.", input);
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

package corgitaco.betterweather.client;

import com.google.common.util.concurrent.AtomicDouble;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.BWSubseasonSettings;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.concurrent.atomic.AtomicInteger;

import static net.minecraft.util.math.MathHelper.lerp;
import static net.minecraft.util.registry.Registry.BIOME_KEY;
import static net.minecraftforge.api.distmarker.Dist.CLIENT;

@OnlyIn(CLIENT)
public class ColorCalculator {
    private static final int MASK = 0xFF; // bit mask

    private ColorCalculator() {
    }

    public static int getBiomeColor(Biome biome, Type type, int previous) {
        Minecraft minecraft = Minecraft.getInstance();

        ClientWorld world = minecraft.world;
        if (world == null) return previous;

        SeasonContext context = ((BetterWeatherWorldData) world).getSeasonContext();
        if (context == null) return previous;

        AtomicInteger target = new AtomicInteger(previous);
        AtomicDouble blend = new AtomicDouble(0.0D);

        DynamicRegistries registries = world.func_241828_r();
        registries.getRegistry(BIOME_KEY).getOptionalKey(biome).ifPresent(key -> {
            BWSubseasonSettings settings = context.getCurrentSubSeasonSettings();

            switch (type) {
                case GRASS:
                    target.set(settings.getTargetGrassColor(key));
                    blend.set(settings.getGrassColorBlendStrength(key));
                    break;
                case FOLIAGE:
                    target.set(settings.getTargetFoliageColor(key));
                    blend.set(settings.getFoliageColorBlendStrength(key));
                    break;
                case FOG:
                    target.set(settings.getTargetFogColor(key));
                    blend.set(settings.getFogColorBlendStrength(key));
                    break;
                default:
                    target.set(settings.getTargetSkyColor(key));
                    blend.set(settings.getSkyColorBlendStrength(key));
                    break;
            }
        });

        return blend(previous, target.get(), blend.get());
    }

    public static int blend(int start, int end, double blend) {
        int[] us = unpack(start); // unpacked start
        int[] ue = unpack(end); // unpacked end

        return pack((int) lerp(us[0], ue[0], blend),
                    (int) lerp(us[1], ue[1], blend),
                    (int) lerp(us[2], ue[2], blend));
    }

    // packs rgb channels into a decimal
    public static int pack(int r, int g, int b) {
        return (r & MASK) << 16 | (g & MASK) << 8 | b & MASK;
    }

    // unpacks rgb channels from a decimal
    public static int[] unpack(int decimal) {
        return new int[] {(decimal >> 16) & MASK, (decimal >> 8) & MASK, decimal & MASK};
    }

    public enum Type {
        GRASS,
        FOLIAGE,
        FOG,
        SKY
    }
}

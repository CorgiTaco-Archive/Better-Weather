package corgitaco.betterweather.util;

/**
 * May only be called from the Server and is only castable to or extenders of {@link net.minecraft.world.server.ServerWorld}.
 */
public interface BiomeUpdate {
    void updateBiomeData();
}

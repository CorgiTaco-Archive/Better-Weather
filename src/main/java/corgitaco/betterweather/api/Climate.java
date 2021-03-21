package corgitaco.betterweather.api;

import corgitaco.betterweather.api.season.Season;
import net.minecraft.world.World;

/**
 * A class used to acquire a world's Climate info.
 * Safely castable to {@link net.minecraft.client.world.ClientWorld} or {@link net.minecraft.world.server.ServerWorld}.
 */
public interface Climate {
    Season getSeason();

    static Season getSeason(World world) {
        return ((Climate) world).getSeason();
    }

    static Climate getClimate(World world) {
        return ((Climate) world);
    }
}

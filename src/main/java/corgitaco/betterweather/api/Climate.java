package corgitaco.betterweather.api;

import corgitaco.betterweather.api.season.Season;
import net.minecraft.world.World;

import javax.annotation.Nullable;

/**
 * A class used to acquire a world's Climate info.
 * Safely castable to or extenders of: {@link net.minecraft.client.world.ClientWorld} or {@link net.minecraft.world.server.ServerWorld}.
 * <p></p>
 * Basically the functioning "entry point" for Better Weather's Data.
 */
public interface Climate {
    /**
     * @return If null, seasons are not enabled for this world.
     */
    @Nullable
    Season getSeason();

    @Nullable
    static Season getSeason(World world) {
        return ((Climate)(Object) world).getSeason();
    }

    static Climate getClimate(World world) {
        return ((Climate)(Object) world);
    }
}

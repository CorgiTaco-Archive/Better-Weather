package corgitaco.betterweather.mixin;

import corgitaco.betterweather.BetterWeather;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.connect.IMixinConnector;

public class MixinConnector implements IMixinConnector {

    @Override
    public void connect() {
        BetterWeather.LOGGER.debug("Better Weather: Connecting Mixin...");
        Mixins.addConfiguration("betterweather.mixins.json");
        BetterWeather.LOGGER.info("Better Weather: Mixin Connected!");
    }
}
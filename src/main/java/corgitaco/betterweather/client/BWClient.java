package corgitaco.betterweather.client;

import corgitaco.betterweather.BetterWeather;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class BWClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(BetterWeather.BetterWeatherEvents::clientTickEvent);
    }
}
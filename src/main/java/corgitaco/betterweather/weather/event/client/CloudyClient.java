package corgitaco.betterweather.weather.event.client;

import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.api.client.graphics.Graphics;
import corgitaco.betterweather.weather.event.client.settings.CloudyClientSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.biome.Biome;

import java.util.function.Predicate;

public class CloudyClient extends WeatherEventClient<CloudyClientSettings> {

    public CloudyClient(CloudyClientSettings clientSettings) {
        super(clientSettings);
    }

    @Override
    public boolean renderWeatherShaders(Graphics graphics, ClientWorld world, double x, double y, double z) {
        return false;
    }

    @Override
    public boolean renderWeatherLegacy(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z, Predicate<Biome> biomePredicate) {
        return true;
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc, Predicate<Biome> biomePredicate) {

    }
}

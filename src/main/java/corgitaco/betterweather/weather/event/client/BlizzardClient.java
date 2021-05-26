package corgitaco.betterweather.weather.event.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import corgitaco.betterweather.api.weather.WeatherEventClient;
import corgitaco.betterweather.season.client.ColorSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;

public class BlizzardClient extends WeatherEventClient {
    public static final Codec<BlizzardClient> CODEC = RecordCodecBuilder.create((builder) -> {
        return builder.group(ColorSettings.CODEC.fieldOf("color_settings").forGetter(blizzardClient -> {
            return blizzardClient.getColorSettings();
        })).apply(builder, BlizzardClient::new);
    });


    public BlizzardClient(ColorSettings colorSettings) {
        super(colorSettings);
    }

    @Override
    public boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        return false;
    }

    @Override
    public Codec<? extends WeatherEventClient> codec() {
        return CODEC;
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc) {

    }
}

package corgitaco.betterweather.block.weathervane;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.util.ResourceLocation;

import java.util.Objects;

public class WeatherVaneType {
    // TODO: 1.17
    public static final WeatherVaneType COPPER = new WeatherVaneType(new ResourceLocation(BetterWeather.MOD_ID, "entity/weathervane/copper_weather_vane"));
    public static final WeatherVaneType IRON = new WeatherVaneType(new ResourceLocation(BetterWeather.MOD_ID, "entity/weathervane/iron_weather_vane"));

    private final ResourceLocation textureLocation;

    public WeatherVaneType(ResourceLocation textureLocation) {
        this.textureLocation = new ResourceLocation(textureLocation.getNamespace(), textureLocation.getPath() + ".png");
    }

    public ResourceLocation getTextureLocation() {
        return textureLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WeatherVaneType that = (WeatherVaneType) o;
        return Objects.equals(textureLocation, that.textureLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(textureLocation);
    }
}
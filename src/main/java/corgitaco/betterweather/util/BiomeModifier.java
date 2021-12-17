package corgitaco.betterweather.util;

public interface BiomeModifier {

    void setSeasonTempModifier(float tempModifier);

    void setSeasonHumidityModifier(float humidityModifier);

    void setWeatherTempModifier(float tempModifier);

    void setWeatherHumidityModifier(float humidityModifier);
}

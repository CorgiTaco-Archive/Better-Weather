package corgitaco.betterweather.api.weather;

public interface WeatherEventContext {

    boolean isLocalizedWeather();

    String getCurrentWeatherEventKey();

    WeatherEventSettings getCurrentWeatherEventSettings();
}

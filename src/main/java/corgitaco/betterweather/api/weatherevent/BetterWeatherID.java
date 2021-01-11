package corgitaco.betterweather.api.weatherevent;

import java.util.Objects;

public class BetterWeatherID {

    private final String weatherID;

    public BetterWeatherID(String weatherID) {
        this.weatherID = weatherID;
    }

    public BetterWeatherID(String modID, String key) {
        this.weatherID = modID.toLowerCase() + "-" + key.toUpperCase();
    }

    @Override
    public String toString() {
        return weatherID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BetterWeatherID that = (BetterWeatherID) o;
        return weatherID.equals(that.weatherID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(weatherID);
    }
}

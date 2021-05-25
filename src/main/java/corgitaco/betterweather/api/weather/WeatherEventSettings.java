package corgitaco.betterweather.api.weather;

import net.minecraft.util.math.BlockPos;

public interface WeatherEventSettings {

    double getTemperatureModifierAtPosition(BlockPos pos);

    double getHumidityModifierAtPosition(BlockPos pos);
}

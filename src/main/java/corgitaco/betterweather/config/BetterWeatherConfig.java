package corgitaco.betterweather.config;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry;

@Config(name = "betterweather-common")
public class BetterWeatherConfig implements ConfigData {


    @ConfigEntry.Category("acid_rain")
    @ConfigEntry.Gui.TransitiveObject
    public AcidRainConfig acid_rain = new AcidRainConfig();


    @ConfigEntry.Category("blizzard")
    @ConfigEntry.Gui.TransitiveObject
    public BlizzardConfig blizzard = new BlizzardConfig();
}

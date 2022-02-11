package corgitaco.betterweather.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.common.savedata.WeatherEventSavedData;
import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.common.weather.WeatherEventInstance;
import corgitaco.betterweather.common.weather.WeatherForecast;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class WeatherForecastCommand {
    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("weatherForecast").executes(cs -> setWeatherEvent(cs.getSource())).then(Commands.literal("recompute").executes(cs -> recompute(cs.getSource())));
    }

    public static int recompute(CommandSource source) {
        ServerWorld world = source.getLevel();

        WeatherContext weatherContext = ((BetterWeatherWorldData) world).getWeatherContext();

        if (weatherContext == null) {
            source.sendFailure(new TranslationTextComponent("betterweather.commands.disabled"));
            return 0;
        }
        WeatherForecast weatherForecast = weatherContext.getWeatherForecast();
        weatherForecast.getForecast().clear();
        weatherForecast.setLastCheckedGameTime(Long.MIN_VALUE);
        weatherContext.computeWeatherForecast(world, weatherForecast, world.getGameTime());
        WeatherEventSavedData.get(world).setForecast(weatherContext.getWeatherForecast());
        source.sendSuccess(new TranslationTextComponent("betterweather.weatherforecast.recompute"), true);
        return 1;
    }


    public static int setWeatherEvent(CommandSource source) {
        ServerWorld world = source.getLevel();
        WeatherContext weatherContext = ((BetterWeatherWorldData) world).getWeatherContext();

        if (weatherContext == null) {
            source.sendFailure(new TranslationTextComponent("betterweather.commands.disabled"));
            return 0;
        }

        long dayLength = weatherContext.getWeatherTimeSettings().getDayLength();
        long dayTime = world.getDayTime();

        TranslationTextComponent textComponent = null;

        WeatherForecast weatherForecast = weatherContext.getWeatherForecast();

        for (int i = Math.min(100, weatherForecast.getForecast().size() - 1); i > 0; i--) {
            WeatherEventInstance weatherEventInstance = weatherForecast.getForecast().get(i);
            WeatherEvent event = weatherEventInstance.getEvent(weatherContext.getWeatherEvents());
            String name = event.getKey();

            if (textComponent == null) {
                textComponent = new TranslationTextComponent(name);
            } else {
                textComponent.append(", ").append(new TranslationTextComponent(name));
            }
            textComponent.append(new TranslationTextComponent("betterweather.weatherforecast.days_left", weatherEventInstance.getTimeUntil(dayTime, dayLength) / dayLength));
        }

        if (textComponent != null) {
            source.sendSuccess(new TranslationTextComponent("betterweather.weatherforecast.header", textComponent.append(".")), true);
        } else {
            source.sendSuccess(new TranslationTextComponent("betterweather.weatherforecast.empty", textComponent).withStyle(TextFormatting.YELLOW), true);
        }

        return 1;
    }
}
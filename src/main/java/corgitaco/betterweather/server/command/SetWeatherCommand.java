package corgitaco.betterweather.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.common.savedata.WeatherEventSavedData;
import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.common.weather.WeatherEventInstance;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.Arrays;
import java.util.List;

public class SetWeatherCommand {

    public static final String WEATHER_NOT_ENABLED = "null";

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("setWeatherEvent").then(
                Commands.argument("weatherEvent", StringArgumentType.string())
                        .suggests((ctx, sb) -> {
                            WeatherContext weatherEventContext = ((BetterWeatherWorldData) ctx.getSource().getLevel()).getWeatherContext();
                            return ISuggestionProvider.suggest(weatherEventContext != null ? weatherEventContext.getWeatherEvents().keySet().stream() : Arrays.stream(new String[]{WEATHER_NOT_ENABLED}), sb);
                        }).executes(cs -> setLunarEvent(cs.getSource(), cs.getArgument("weatherEvent", String.class)))
        );
    }

    public static int setLunarEvent(CommandSource source, String weatherEventKey) {
        ServerWorld world = source.getLevel();
        WeatherContext weatherContext = ((BetterWeatherWorldData) world).getWeatherContext();

        if (weatherEventKey.equals(WEATHER_NOT_ENABLED) || weatherContext == null) {
            source.sendFailure(new TranslationTextComponent("betterweather.commands.disabled"));
            return 0;
        }

        long dayLength = weatherContext.getWeatherTimeSettings().getDayLength();
        long currentDay = (world.getDayTime() / dayLength);

        if (weatherContext.getWeatherEvents().containsKey(weatherEventKey)) {
            WeatherEventInstance commandInstance = new WeatherEventInstance(weatherEventKey, currentDay);
            List<WeatherEventInstance> forecast = weatherContext.getWeatherForecast().getForecast();
            if (!forecast.isEmpty()) {
                WeatherEventInstance weatherEventInstance = forecast.get(0);
                if (weatherEventInstance.active(currentDay)) {
                    forecast.remove(0);
                    weatherContext.getWeatherForecast().getPastEvents().add(weatherEventInstance);
                }
            }
            forecast.add(0, commandInstance);

            WeatherEventSavedData.get(world).setForecast(weatherContext.getWeatherForecast());
        } else {
            source.sendFailure(new TranslationTextComponent("betterweather.commands.weatherevent_missing", weatherEventKey));
            return 0;
        }
        return 1;
    }
}
package corgitaco.betterweather.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.common.weather.WeatherContext;
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
    public static final List<String> LENGTH_SUGGESTIONS = Arrays.asList(
            "1200", // a minute
            "6000", // 5 minutes
            "12000", // 10 minutes
            "36000" // 30 minutes
    );

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("setweather").then(
                Commands.argument("weather", StringArgumentType.string())
                        .suggests((ctx, sb) -> {
                            WeatherContext weatherEventContext = ((BetterWeatherWorldData) ctx.getSource().getLevel()).getWeatherEventContext();
                            return ISuggestionProvider.suggest(weatherEventContext != null ? weatherEventContext.getWeatherEvents().keySet().stream() : Arrays.stream(new String[]{WEATHER_NOT_ENABLED}), sb);
                        }).executes(cs -> betterWeatherSetSeason(cs.getSource(), cs.getArgument("weather", String.class),
                                12000)) // Default length to 10 minutes.
                        .then(
                                Commands.argument("length", IntegerArgumentType.integer())
                                        .suggests((ctx, sb) -> ISuggestionProvider.suggest(LENGTH_SUGGESTIONS.stream(), sb))
                                        .executes((cs) -> betterWeatherSetSeason(cs.getSource(), cs.getArgument("weather", String.class),
                                                cs.getArgument("length", int.class)))
                        )
        );
    }

    public static int betterWeatherSetSeason(CommandSource source, String weatherKey, int length) {
        if (weatherKey.equals(WEATHER_NOT_ENABLED)) {
            source.sendFailure(new TranslationTextComponent("commands.bw.setweather.no.weather.for.world"));
            return 0;
        }

        ServerWorld world = source.getLevel();
        WeatherContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();

        if (weatherEventContext != null) {
            if (weatherEventContext.getWeatherEvents().containsKey(weatherKey)) {
                weatherEventContext.setCurrentEvent(weatherKey);
                WeatherEvent currentEvent = weatherEventContext.getCurrentEvent();
                source.sendSuccess(currentEvent.successTranslationTextComponent(weatherKey), true);
            } else {
                source.sendFailure(new TranslationTextComponent("commands.bw.setweather.fail.no_weather_event", weatherKey));
                return 0;
            }
        }
        return 1;
    }
}

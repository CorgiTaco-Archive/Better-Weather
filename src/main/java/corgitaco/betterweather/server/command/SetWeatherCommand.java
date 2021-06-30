package corgitaco.betterweather.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.Arrays;

public class SetWeatherCommand {

    public static String weatherNotEnabled = "null";

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("setweather").then(Commands.argument("weather", StringArgumentType.string()).suggests((ctx, sb) -> {
            BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) ctx.getSource().getWorld()).getWeatherEventContext();
            return ISuggestionProvider.suggest(weatherEventContext != null ? weatherEventContext.getWeatherEvents().keySet().stream() : Arrays.stream(new String[]{weatherNotEnabled}), sb);
        }).then(Commands.argument("length", IntegerArgumentType.integer()).executes((cs) -> betterWeatherSetSeason(cs.getSource(), cs.getArgument("weather", String.class), cs.getArgument("length", int.class)))));
    }

    public static int betterWeatherSetSeason(CommandSource source, String weatherKey, int length) {
        if (weatherKey.equals(weatherNotEnabled)) {
            source.sendErrorMessage(new TranslationTextComponent("commands.bw.setweather.no.weather.for.world"));
            return 0;
        }

        ServerWorld world = source.getWorld();
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) world).getWeatherEventContext();

        if (weatherEventContext != null) {
            if (weatherEventContext.getWeatherEvents().containsKey(weatherKey)) {
                source.sendFeedback(weatherEventContext.weatherForcer(weatherKey, length, world).successTranslationTextComponent(weatherKey), true);
            } else {
                source.sendErrorMessage(new TranslationTextComponent("commands.bw.setweather.failed", weatherKey));
                return 0;
            }
        }
        return 1;
    }
}

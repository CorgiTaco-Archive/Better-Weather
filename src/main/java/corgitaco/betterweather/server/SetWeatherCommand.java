package corgitaco.betterweather.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;

public class SetWeatherCommand {

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        List<String> weatherTypes = new ArrayList<>();
        weatherTypes.add("acidrain");
        weatherTypes.add("blizzard");
        weatherTypes.add("clear");
        return Commands.literal("setweather").then(Commands.argument("weathertype", StringArgumentType.string()).suggests((ctx, sb) -> ISuggestionProvider.suggest(weatherTypes.stream(), sb))
                .executes((cs) -> betterWeatherSetWeatherType(cs.getSource().getWorld(), cs.getSource(), cs.getArgument("weathertype", String.class))));
    }

    public static int betterWeatherSetWeatherType(ServerWorld world, CommandSource source, String weatherType) {
        switch (weatherType) {
            case "acidrain":
                BetterWeather.weatherData.setEvent(WeatherEventSystem.ACID_RAIN);
                world.func_241113_a_(0, 6000, true, false);
                source.sendFeedback(new TranslationTextComponent("commands.bw.setweather.success.acidrain"), true);
                break;
            case "blizzard":
                BetterWeather.weatherData.setEvent(WeatherEventSystem.BLIZZARD);
                world.func_241113_a_(0, 6000, true, false);
                source.sendFeedback(new TranslationTextComponent("commands.bw.setweather.success.blizzard"), true);
                break;
            case "clear":
                BetterWeather.weatherData.setEvent(WeatherEventSystem.NONE);
                world.func_241113_a_(0, 6000, false, false);
                source.sendFeedback(new TranslationTextComponent("commands.bw.setweather.success.clear"), true);
                break;
            default:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setweather.failed", weatherType), true);
                break;
        }
        return 1;
    }
}

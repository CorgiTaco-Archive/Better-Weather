package corgitaco.betterweather.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import corgitaco.betterweather.BetterWeather;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class BetterWeatherCommand {

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        BetterWeather.LOGGER.debug("Registering BW commands...");
        List<String> weatherTypes = new ArrayList<>();
        weatherTypes.add("acidrain");
        weatherTypes.add("blizzard");
        weatherTypes.add("clear");
        LiteralCommandNode<CommandSource> source = dispatcher.register(
                Commands.literal(BetterWeather.MOD_ID)
                        .then(Commands.argument("weathertype", StringArgumentType.string()).suggests((ctx, sb) -> ISuggestionProvider.suggest(weatherTypes.stream(), sb))
                                .executes((cs) -> betterWeatherSetWeatherType(cs.getSource().getWorld(), cs.getSource(), cs.getArgument("weathertype", String.class)))));

        dispatcher.register(Commands.literal(BetterWeather.MOD_ID).redirect(source));
        BetterWeather.LOGGER.debug("Registered BW Commands!");
    }

    public static int betterWeatherSetWeatherType(World world, CommandSource source, String weatherType) {
        if (weatherType.equals("acidrain")) {
            BetterWeather.BetterWeatherEvents.weatherData.setBlizzard(false);
            BetterWeather.BetterWeatherEvents.weatherData.setAcidRain(true);
            world.getWorldInfo().setRaining(true);
            source.sendFeedback(new TranslationTextComponent("commands.bw.setweather.success", weatherType), true);
        }
        else if (weatherType.equals("blizzard")) {
            BetterWeather.BetterWeatherEvents.weatherData.setAcidRain(false);
            BetterWeather.BetterWeatherEvents.weatherData.setBlizzard(true);
            world.getWorldInfo().setRaining(true);
            source.sendFeedback(new TranslationTextComponent("commands.bw.setweather.success", weatherType), true);
        }
        else if (weatherType.equals("clear")) {
            BetterWeather.BetterWeatherEvents.weatherData.setAcidRain(false);
            BetterWeather.BetterWeatherEvents.weatherData.setBlizzard(false);
            world.getWorldInfo().setRaining(false);
            source.sendFeedback(new TranslationTextComponent("commands.bw.setweather.success", weatherType), true);
        }
        else {
            source.sendFeedback(new TranslationTextComponent("commands.bw.setweather.failed", weatherType), true);
        }
        return 1;
    }
}

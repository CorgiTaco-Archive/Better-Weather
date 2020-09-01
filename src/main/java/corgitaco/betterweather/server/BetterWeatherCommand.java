package corgitaco.betterweather.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.tree.LiteralCommandNode;
import corgitaco.betterweather.BetterWeather;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class BetterWeatherCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        BetterWeather.LOGGER.debug("Registering BW commands...");
        List<String> weatherTypes = new ArrayList<>();
        weatherTypes.add("acidrain");
        weatherTypes.add("blizzard");
        weatherTypes.add("clear");
        LiteralCommandNode<CommandSourceStack> source = dispatcher.register(
                Commands.literal(BetterWeather.MOD_ID)
                        .then(Commands.argument("weathertype", StringArgumentType.string()).suggests((ctx, sb) -> SharedSuggestionProvider.suggest(weatherTypes.stream(), sb))
                                .executes((cs) -> betterWeatherSetWeatherType(cs.getSource().getLevel(), cs.getSource(), cs.getArgument("weathertype", String.class)))));

        dispatcher.register(Commands.literal(BetterWeather.MOD_ID).redirect(source));
        BetterWeather.LOGGER.debug("Registered BW Commands!");
    }

    public static int betterWeatherSetWeatherType(Level world, CommandSourceStack source, String weatherType) {
        if (weatherType.equals("acidrain")) {
            BetterWeather.BetterWeatherEvents.weatherData.setBlizzard(false);
            BetterWeather.BetterWeatherEvents.weatherData.setAcidRain(true);
            world.getLevelData().setRaining(true);
            source.sendSuccess(new TranslatableComponent("commands.bw.setweather.success.acidrain"), true);
        } else if (weatherType.equals("blizzard")) {
            BetterWeather.BetterWeatherEvents.weatherData.setAcidRain(false);
            BetterWeather.BetterWeatherEvents.weatherData.setBlizzard(true);
            world.getLevelData().setRaining(true);
            source.sendSuccess(new TranslatableComponent("commands.bw.setweather.success.blizzard"), true);
        } else if (weatherType.equals("clear")) {
            BetterWeather.BetterWeatherEvents.weatherData.setAcidRain(false);
            BetterWeather.BetterWeatherEvents.weatherData.setBlizzard(false);
            world.getLevelData().setRaining(false);
            source.sendSuccess(new TranslatableComponent("commands.bw.setweather.success.clear"), true);
        } else {
            source.sendSuccess(new TranslatableComponent("commands.bw.setweather.failed", weatherType), true);
        }
        return 1;
    }
}

package corgitaco.betterweather.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.config.json.SeasonConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;

public class ConfigReloadCommand {
    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("reload").executes((cs) -> reloadCommand(cs.getSource()));
    }

    public static int reloadCommand(CommandSource source) {
        SeasonConfig.handleBWSeasonsConfig(BetterWeather.CONFIG_PATH.resolve(BetterWeather.MOD_ID + "-seasons.json"));
        BetterWeatherConfig.loadConfig(BetterWeather.CONFIG_PATH.resolve(BetterWeather.MOD_ID + "-common.toml"));
        source.sendFeedback(new TranslationTextComponent("commands.bw.reload.success"), true);
        return 1;
    }
}

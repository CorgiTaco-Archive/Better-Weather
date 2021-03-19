package corgitaco.betterweather.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherWorldData;
import corgitaco.betterweather.config.BetterWeatherConfig;
import corgitaco.betterweather.config.json.WeatherEventControllerConfig;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.util.RefreshRenderersPacket;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public class ConfigReloadCommand {
    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        return Commands.literal("reload").executes((cs) -> reloadCommand(cs.getSource()));
    }

    public static int reloadCommand(CommandSource source) {
        BetterWeatherConfig.loadConfig(BetterWeather.CONFIG_PATH.resolve(BetterWeather.MOD_ID + "-common.toml"));
        BetterWeather.BetterWeatherEvents.updateGeneralDataPacket(source.getWorld().getPlayers());

        if (!BetterWeather.useSeasons)
            WeatherEventControllerConfig.handleConfig(BetterWeather.CONFIG_PATH.resolve(BetterWeather.MOD_ID + "-weather-controller.json"));

        for (ServerWorld world : source.getWorld().getServer().getWorlds()) {
            SeasonContext seasonContext = ((BetterWeatherWorldData) world).getSeasonContext();
            if (seasonContext != null) {
                seasonContext.handleConfig();
                world.getPlayers().forEach(player -> NetworkHandler.sendToClient(player, new RefreshRenderersPacket()));
            }
        }


        source.sendFeedback(new TranslationTextComponent("commands.bw.reload.success"), true);
        return 1;
    }
}

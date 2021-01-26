package corgitaco.betterweather.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.BetterWeatherEntryPoint;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import corgitaco.betterweather.datastorage.BetterWeatherEventData;
import corgitaco.betterweather.datastorage.BetterWeatherSeasonData;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.OnCommandWeatherChangePacket;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

import java.util.List;
import java.util.stream.Collectors;

public class SetWeatherCommand {

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        List<String> weatherTypes = BetterWeatherEntryPoint.WEATHER_EVENTS.stream().map(WeatherEvent::getID).map(BetterWeatherID::toString).collect(Collectors.toList());

        return Commands.literal("setweather").then(Commands.argument("weathertype", StringArgumentType.string()).suggests((ctx, sb) -> ISuggestionProvider.suggest(weatherTypes.stream(), sb))
                .executes((cs) -> betterWeatherSetWeatherType(cs.getSource().getWorld(), cs.getSource(), cs.getArgument("weathertype", String.class))));
    }

    public static int betterWeatherSetWeatherType(ServerWorld world, CommandSource source, String weatherType) {
        WeatherEvent weatherEvent = WeatherEventSystem.WEATHER_EVENTS.get(new BetterWeatherID(weatherType));
        WeatherEvent previousWeatherEvent = WeatherData.currentWeatherEvent;
        if (weatherEvent != null) {
            BetterWeatherEventData.get(world).setEvent(weatherEvent.getID().toString());
            world.func_241113_a_(0, 6000, weatherEvent.getID() != WeatherEventSystem.CLEAR, false);

            source.getWorld().getPlayers().forEach(player -> {
                NetworkHandler.sendToClient(player, new WeatherEventPacket(BetterWeatherEventData.get(world).getEventString()));
                if (previousWeatherEvent != WeatherData.currentWeatherEvent)
                    NetworkHandler.sendToClient(player, new OnCommandWeatherChangePacket(previousWeatherEvent.getID().toString()));
            });

            source.sendFeedback(weatherEvent.successTranslationTextComponent(), true);

        } else {
            source.sendFeedback(new TranslationTextComponent("commands.bw.setweather.failed", weatherType), true);
        }
        return 1;
    }
}

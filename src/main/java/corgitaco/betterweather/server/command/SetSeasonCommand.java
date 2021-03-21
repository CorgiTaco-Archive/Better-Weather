package corgitaco.betterweather.server.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.api.season.Season;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.season.SeasonContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.TranslationTextComponent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SetSeasonCommand {

    public static ArgumentBuilder<CommandSource, ?> register(CommandDispatcher<CommandSource> dispatcher) {
        List<String> seasons = Arrays.stream(Season.Key.values()).map(Objects::toString).collect(Collectors.toList());
        List<String> phases = Arrays.stream(Season.Phase.values()).map(Objects::toString).collect(Collectors.toList());
        return Commands.literal("setseason").then(Commands.argument("season", StringArgumentType.string()).suggests((ctx, sb) -> ISuggestionProvider.suggest(seasons.stream(), sb))
                .then(Commands.argument("phase", StringArgumentType.string()).suggests((ctx, sb) -> ISuggestionProvider.suggest(phases.stream(), sb))
                        .executes((cs) -> betterWeatherSetSeason(cs.getSource(), cs.getArgument("season", String.class), cs.getArgument("phase", String.class)))));
    }

    public static int betterWeatherSetSeason(CommandSource source, String seasonKey, String phaseString) {
        SeasonContext seasonContext = ((BetterWeatherWorldData) source.getWorld()).getSeasonContext();
        if (seasonContext == null) {
            source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.fail.no_seasons"), true);
            return 0;
        }

        if (!Season.Key.hasType(seasonKey) || !Season.Phase.hasType(phaseString)) {
            return 0;
        }

        seasonContext.setSeason(source.getWorld().getPlayers(), Season.Key.valueOf(seasonKey), Season.Phase.valueOf(phaseString));
        source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success." + seasonKey.toLowerCase() + "_" + phaseString.toLowerCase()), true);
        return 1;
    }
}

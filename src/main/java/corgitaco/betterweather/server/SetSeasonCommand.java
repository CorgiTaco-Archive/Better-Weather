package corgitaco.betterweather.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.BetterWeatherWorldData;
import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.datastorage.SeasonSavedData;
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
        List<String> seasons = Arrays.stream(SeasonData.SeasonKey.values()).map(Objects::toString).collect(Collectors.toList());
        List<String> phases = Arrays.stream(SeasonData.Phase.values()).map(Objects::toString).collect(Collectors.toList());
        return Commands.literal("setseason").then(Commands.argument("season", StringArgumentType.string()).suggests((ctx, sb) -> ISuggestionProvider.suggest(seasons.stream(), sb))
                .then(Commands.argument("phase", StringArgumentType.string()).suggests((ctx, sb) -> ISuggestionProvider.suggest(phases.stream(), sb))
                .executes((cs) -> betterWeatherSetSeason(cs.getSource(), cs.getArgument("season", String.class), cs.getArgument("phase", String.class)))));
    }

    public static int betterWeatherSetSeason(CommandSource source, String weatherTypeString, String phaseString) {
        SeasonData.SeasonKey season = SeasonData.SeasonKey.valueOf(weatherTypeString);
        boolean failedFlag = false;
        switch (season) {
            case SPRING:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.spring_start"), true);
                break;
//            case SPRING_MID:
//                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.spring_mid"), true);
//                break;
//            case SPRING_END:
//                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.spring_end"), true);
//                break;
            case SUMMER:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.summer_start"), true);
                break;
//            case SUMMER_MID:
//                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.summer_mid"), true);
//                break;
//            case SUMMER_END:
//                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.summer_end"), true);
//                break;
            case AUTUMN:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.autumn_start"), true);
                break;
//            case AUTUMN_MID:
//                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.autumn_mid"), true);
//                break;
//            case AUTUMN_END:
//                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.autumn_end"), true);
//                break;
            case WINTER:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.winter_start"), true);
                break;
//            case WINTER_MID:
//                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.winter_mid"), true);
//                break;
//            case WINTER_END:
//                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.winter_end"), true);
//                break;
            default:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.failed", season.toString()), true);
                failedFlag = true;
                break;
        }
        if (!failedFlag) {
            SeasonContext seasonContext = ((BetterWeatherWorldData) source.getWorld()).getSeasonContext();

            if (seasonContext != null) {
                seasonContext.setSeason(source.getWorld().getPlayers(), season, SeasonData.Phase.valueOf(phaseString));
            }
        }
        return 1;
    }
}

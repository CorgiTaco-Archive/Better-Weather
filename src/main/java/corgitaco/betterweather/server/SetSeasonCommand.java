package corgitaco.betterweather.server;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.season.SeasonSystem;
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
        List<String> seasons = Arrays.stream(SeasonData.SubSeasonVal.values()).map(Objects::toString).collect(Collectors.toList());
        return Commands.literal("setseason").then(Commands.argument("season", StringArgumentType.string()).suggests((ctx, sb) -> ISuggestionProvider.suggest(seasons.stream(), sb))
                .executes((cs) -> betterWeatherSetSeason(cs.getSource(), cs.getArgument("season", String.class))));
    }

    public static int betterWeatherSetSeason(CommandSource source, String weatherTypeString) {
        SeasonData.SubSeasonVal subSeason = SeasonData.SubSeasonVal.valueOf(weatherTypeString);
        boolean failedFlag = false;
        switch (subSeason) {
            case SPRING_START:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.spring_start"), true);
                break;
            case SPRING_MID:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.spring_mid"), true);
                break;
            case SPRING_END:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.spring_end"), true);
                break;
            case SUMMER_START:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.summer_start"), true);
                break;
            case SUMMER_MID:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.summer_mid"), true);
                break;
            case SUMMER_END:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.summer_end"), true);
                break;
            case AUTUMN_START:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.autumn_start"), true);
                break;
            case AUTUMN_MID:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.autumn_mid"), true);
                break;
            case AUTUMN_END:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.autumn_end"), true);
                break;
            case WINTER_START:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.winter_start"), true);
                break;
            case WINTER_MID:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.winter_mid"), true);
                break;
            case WINTER_END:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.success.winter_end"), true);
                break;
            default:
                source.sendFeedback(new TranslationTextComponent("commands.bw.setseason.failed", subSeason.toString()), true);
                failedFlag = true;
                break;
        }
        if (!failedFlag) {
            BetterWeather.seasonData.setForced(true);
            BetterWeather.seasonData.setSeasonTime(SeasonSystem.getTimeInCycleForSubSeason(subSeason, BetterWeather.SEASON_CYCLE_LENGTH));
        }
        return 1;
    }
}

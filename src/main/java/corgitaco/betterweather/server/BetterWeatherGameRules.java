package corgitaco.betterweather.server;

import net.minecraft.world.GameRules;
import net.minecraftforge.fml.ModList;

public class BetterWeatherGameRules {

    public static final GameRules.RuleKey<GameRules.BooleanValue> DO_SEASON_CYCLE = register("doSeasonCycle", GameRules.Category.UPDATES, GameRules.BooleanValue.create(true));

    public static <T extends GameRules.RuleValue<T>> GameRules.RuleKey<T> register(String name, GameRules.Category category, GameRules.RuleType<T> type) {
        if (ModList.get().isLoaded("sereneseasons") && name.equals("doSeasonCycle"))
            name = "doBWSeasonCycle";

        GameRules.RuleKey<T> rulekey = new GameRules.RuleKey<>(name, category);
        GameRules.RuleType<?> ruletype = GameRules.GAME_RULES.put(rulekey, type);
        if (ruletype != null) {
            throw new IllegalStateException("Duplicate game rule registration for " + name);
        } else {
            return rulekey;
        }
    }

    public static void init() {
    }
}

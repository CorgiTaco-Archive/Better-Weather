package corgitaco.betterweather.mixin.access;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GameRules.class)
public interface GameRulesAccess {

    @Accessor("GAME_RULES")
    static Map<GameRules.RuleKey<?>, GameRules.RuleType<?>> getGameRules() {
        throw new Error("Mixin did not apply");
    }

}

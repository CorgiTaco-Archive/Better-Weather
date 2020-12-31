package corgitaco.betterweather.season.seasonoverrides;

import java.util.IdentityHashMap;

public class SeasonOverrideEntry {
    private final int priority;
    private final SeasonOverrideOption main;
    private final IdentityHashMap<Object, SeasonOverrideOption> subOverrides;

    public SeasonOverrideEntry(int priority, SeasonOverrideOption overrideOption, IdentityHashMap<Object, SeasonOverrideOption> subOverrides) {
        this.priority = priority;
        this.main = overrideOption;
        this.subOverrides = subOverrides;
    }

    public int getPriority() {
        return priority;
    }

    public SeasonOverrideOption getMain() {
        return main;
    }

    public IdentityHashMap<Object, SeasonOverrideOption> getSubOverrides() {
        return subOverrides;
    }
}
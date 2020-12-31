package corgitaco.betterweather.season.seasonoverrides;


import java.util.IdentityHashMap;

public class SeasonOverrides {

    private final IdentityHashMap<Object, SeasonOverrideEntry> overrides;


    public SeasonOverrides(IdentityHashMap<Object, SeasonOverrideEntry> overrides) {
        this.overrides = overrides;
    }

    public IdentityHashMap<Object, SeasonOverrideEntry> getOverrides() {
        return overrides;
    }
}

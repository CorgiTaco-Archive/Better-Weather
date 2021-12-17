package corgitaco.betterweather.util;

public interface DirtyTickTracker {

    boolean isTickDirty();

    void setTickDirty();
}
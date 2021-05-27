package corgitaco.betterweather.util.function;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface Builder<T> {

    @Contract("!null -> param1")
    @NotNull
    T build(T object) throws Exception;
}

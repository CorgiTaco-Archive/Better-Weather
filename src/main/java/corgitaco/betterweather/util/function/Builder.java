package corgitaco.betterweather.util.function;

@FunctionalInterface
public interface Builder<T> {

    void build(T object) throws Exception;
}

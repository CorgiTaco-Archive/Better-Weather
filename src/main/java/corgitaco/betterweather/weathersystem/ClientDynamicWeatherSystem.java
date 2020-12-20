package corgitaco.betterweather.weathersystem;

import corgitaco.betterweather.util.FastNoiseLite;

public class ClientDynamicWeatherSystem {

    private static FastNoiseLite noiseLite = null;







    public double sampledNoiseOutput(float x, float z, float gameTime, long seed) {
        setUpNoise(seed);

//        Minecraft.getInstance().world.se();

        return noiseLite.GetNoise(x, z, gameTime);
    }


    public static void setUpNoise(long seed) {
        if (noiseLite == null) {
            noiseLite = new FastNoiseLite((int) seed);
            noiseLite.SetNoiseType(FastNoiseLite.NoiseType.OpenSimplex2S);
        }
    }
}

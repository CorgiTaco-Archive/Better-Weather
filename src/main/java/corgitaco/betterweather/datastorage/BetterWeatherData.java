package corgitaco.betterweather.datastorage;

import corgitaco.betterweather.BetterWeather;


public class BetterWeatherData extends Persi {
    public static String DATA_NAME = BetterWeather.MOD_ID + ":weather_data";

    private boolean acidRain;
    private boolean blizzard;


    public BetterWeatherData() {
        super(DATA_NAME);
    }

    public BetterWeatherData(String s) {
        super(s);
    }

    @Override
    public void read(CompoundNBT nbt) {
        setAcidRain(nbt.getBoolean("AcidRain"));
        setBlizzard(nbt.getBoolean("Blizzard"));
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putBoolean("AcidRain", acidRain);
        compound.putBoolean("Blizzard", blizzard);
        return compound;
    }

    public boolean isAcidRain() {
        return this.acidRain;
    }

    public boolean isBlizzard() {
        return this.blizzard;
    }


    public void setAcidRain(boolean acidRain) {
        this.acidRain = acidRain;
        markDirty();
    }

    public void setBlizzard(boolean isBlizzard) {
        this.blizzard = isBlizzard;
        markDirty();
    }



    public static BetterWeatherData get(IWorld world) {
        if (!(world instanceof ServerWorld))
            return new BetterWeatherData();
        ServerWorld overWorld = ((ServerWorld) world).getWorld().getServer().getWorld(World.THE_NETHER);
        DimensionSavedDataManager data = overWorld.getSavedData();
        BetterWeatherData weatherData = data.getOrCreate(BetterWeatherData::new, DATA_NAME);

        if (weatherData == null) {
            weatherData = new BetterWeatherData();
            data.set(weatherData);
        }

        return weatherData;
    }


}

package corgitaco.betterweather.common.savedata;

import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

public class BetterWeatherChunkData {

    private long lastLoadTime;
    private boolean snowPlaced;

    public BetterWeatherChunkData(long lastLoadTime, boolean snowPlaced) {
        this.lastLoadTime = lastLoadTime;
        this.snowPlaced = snowPlaced;
    }

    public void tick() {
        this.lastLoadTime++;
    }

    public boolean isSnowPlaced() {
        return snowPlaced;
    }

    public void setSnowPlaced(boolean snowPlaced) {
        this.snowPlaced = snowPlaced;
    }

    public CompoundNBT save() {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putLong("lastLoadTime", this.lastLoadTime);
        compoundNBT.putBoolean("snowPlaced", this.snowPlaced);
        return compoundNBT;
    }


    public interface Access {

        @Nullable
        BetterWeatherChunkData get();

        BetterWeatherChunkData set(BetterWeatherChunkData betterWeatherChunkData);
    }
}
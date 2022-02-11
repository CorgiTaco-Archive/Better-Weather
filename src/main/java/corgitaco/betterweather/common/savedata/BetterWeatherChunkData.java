package corgitaco.betterweather.common.savedata;

import net.minecraft.nbt.CompoundNBT;

import javax.annotation.Nullable;

public class BetterWeatherChunkData {

    private long lastLoadTime;

    public BetterWeatherChunkData(long lastLoadTime) {
        this.lastLoadTime = lastLoadTime;
    }

    public void tick() {
        this.lastLoadTime++;
    }

    public CompoundNBT save() {
        CompoundNBT compoundNBT = new CompoundNBT();
        compoundNBT.putLong("lastLoadTime", this.lastLoadTime);
        return compoundNBT;
    }


    public interface Access {

        @Nullable
        BetterWeatherChunkData get();

        BetterWeatherChunkData set(BetterWeatherChunkData betterWeatherChunkData);
    }
}
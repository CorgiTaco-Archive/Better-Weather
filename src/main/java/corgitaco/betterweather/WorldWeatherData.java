package corgitaco.betterweather;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

public class WorldWeatherData extends WorldSavedData {
    private boolean acidRain;

    public WorldWeatherData() {
        super("BetterWeatherData");
    }

    @Override
    public void read(CompoundNBT nbt) {
        acidRain = nbt.getBoolean("AcidRain");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putBoolean("AcidRain", acidRain);
        return compound;
    }

    public boolean isAcidRain() {
        return this.acidRain;
    }

    public void setAcidRain(boolean acidRain) {
        this.acidRain = acidRain;
        markDirty();
    }
}

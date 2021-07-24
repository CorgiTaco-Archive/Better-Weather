package corgitaco.betterweather.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

public class WeatherVaneTileEntity extends TileEntity {

    public WeatherVaneTileEntity() {
        super(BetterWeatherBlockEntityTypes.WEATHER_VANE_TILE_ENTITY);
    }


    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        return super.write(compound);
    }
}

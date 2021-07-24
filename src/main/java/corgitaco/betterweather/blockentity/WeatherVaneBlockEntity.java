package corgitaco.betterweather.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;

public class WeatherVaneBlockEntity extends TileEntity implements ITickableTileEntity {

    public WeatherVaneBlockEntity() {
        super(BetterWeatherBlockEntityTypes.WEATHER_VANE_TILE_ENTITY);
    }

    private float rotationTarget;
    private float rotation;
    private float rotationSpeed;

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);
        rotation = nbt.getFloat("rotation");
        rotationTarget = nbt.getFloat("rotationTarget");
        rotationSpeed = nbt.getFloat("rotationSpeed");
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        compound.putFloat("rotation", rotation);
        compound.putFloat("rotationTarget", rotationTarget);
        compound.putFloat("rotationSpeed", rotationSpeed);
        return super.write(compound);
    }

    @Override
    public void tick() {
        if (Math.abs(rotationTarget - rotation) <= 0.01) {
            if (world.rand.nextDouble() < 0.05) {
                rotationTarget = (float) MathHelper.clampedLerp(0, Math.PI * 2, world.rand.nextDouble());
                rotationSpeed = (float) MathHelper.clampedLerp(0.004f, 0.01f, world.rand.nextDouble());
            }
        } else {
            if (rotationTarget > rotation) {
                rotation += rotationSpeed;
            } else {
                rotation -= rotationSpeed;
            }
        }
    }

    public float getRotation() {
        return rotation;
    }
}

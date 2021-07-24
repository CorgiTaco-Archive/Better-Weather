package corgitaco.betterweather.block;

import corgitaco.betterweather.blockentity.WeatherVaneTileEntity;
import net.minecraft.block.ContainerBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import org.jetbrains.annotations.Nullable;

public class WeatherVaneBlock extends ContainerBlock {

    protected WeatherVaneBlock(Properties builder) {
        super(builder);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new WeatherVaneTileEntity();
    }
}

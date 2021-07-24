package corgitaco.betterweather.block.weathervane;

import corgitaco.betterweather.blockentity.WeatherVaneBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("NullableProblems")
public class WeatherVane extends ContainerBlock {

    private final WeatherVaneType weatherVaneType;

    public WeatherVane(Properties builder, WeatherVaneType weatherVaneType) {
        super(builder);
        this.weatherVaneType = weatherVaneType;
    }


    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return new WeatherVaneBlockEntity();
    }

    public WeatherVaneType getWeatherVaneType() {
        return weatherVaneType;
    }
}

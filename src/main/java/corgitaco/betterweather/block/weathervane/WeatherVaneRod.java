package corgitaco.betterweather.block.weathervane;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.World;

@SuppressWarnings("NullableProblems")
public class WeatherVaneRod extends Block {

    private final WeatherVaneType weatherVaneType;

    public WeatherVaneRod(Properties properties, WeatherVaneType weatherVaneType) {
        super(properties);
        this.weatherVaneType = weatherVaneType;
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (!worldIn.isRemote) {
            BlockPos.Mutable mutable = new BlockPos.Mutable().setPos(pos);
            for (int findVane = 0; findVane < 100; findVane++) {
                Block block = worldIn.getBlockState(mutable.move(Direction.UP)).getBlock();
                if (block instanceof WeatherVaneRod) {
                    continue;
                }

                if (block instanceof WeatherVane) {
                    if (((WeatherVane) block).getWeatherVaneType().equals(this.weatherVaneType)) {
                        block.onBlockActivated(state, worldIn, pos, player, handIn, hit);
                        return ActionResultType.SUCCESS;
                    } else {
                    }
                } else {
                    return ActionResultType.PASS;
                }
            }
        }


        return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
    }

    public WeatherVaneType getWeatherVaneType() {
        return weatherVaneType;
    }
}

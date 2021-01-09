package corgitaco.betterweather;

import net.minecraft.tags.BlockTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;

public class BetterWeatherUtil {
    public static int removeLeavesFromHeightMap(World world, BlockPos pos) {
        BlockPos.Mutable heightMapOriginalPos = new BlockPos.Mutable(pos.getX(), world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos.getX(), pos.getZ()), pos.getZ());

        while(world.getBlockState(heightMapOriginalPos.down()).getBlock().isIn(BlockTags.LEAVES) || !world.getBlockState(heightMapOriginalPos.down()).getMaterial().blocksMovement() && world.getBlockState(heightMapOriginalPos.down()).getFluidState().isEmpty())
            heightMapOriginalPos.move(Direction.DOWN);

        return heightMapOriginalPos.getY();
    }

    public static int parseHexColor(String targetHexColor) {
        if (!targetHexColor.isEmpty()) {
            try {
                return (int) Long.parseLong(targetHexColor.replace("#", "").replace("0x", ""), 16);
            } catch (Exception e) {
                BetterWeather.LOGGER.error("\"" + targetHexColor + "\" was not a hex color value! | Using Defaults...");
            }
        }
        return Integer.MAX_VALUE;
    }

    public static boolean filterRegistryID(ResourceLocation id, Registry<?> registry, String registryTypeName) {
        if (registry.keySet().contains(id))
            return true;
        else {
            BetterWeather.LOGGER.error("\"" + id.toString() + "\" was not a registryID in the " + registryTypeName + "! Skipping entry...");
            return false;
        }
    }
}
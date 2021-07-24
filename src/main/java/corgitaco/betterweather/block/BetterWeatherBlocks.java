package corgitaco.betterweather.block;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

import java.util.ArrayList;
import java.util.List;

public class BetterWeatherBlocks {

    public static final List<Block> BLOCKS = new ArrayList<>();

    public static final WeatherVaneBlock WEATHER_VANE = register("weather_vane", new WeatherVaneBlock(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2.5f, 2.5f).harvestTool(ToolType.PICKAXE).harvestLevel(4)));

    public static <T extends Block> T register(String key, T block) {
        block.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, key));
        BLOCKS.add(block);
        return block;
    }
}

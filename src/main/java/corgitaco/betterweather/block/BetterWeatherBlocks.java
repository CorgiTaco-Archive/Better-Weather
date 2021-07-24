package corgitaco.betterweather.block;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.block.weathervane.WeatherVane;
import corgitaco.betterweather.block.weathervane.WeatherVaneRod;
import corgitaco.betterweather.block.weathervane.WeatherVaneType;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ToolType;

import java.util.ArrayList;
import java.util.List;

public class BetterWeatherBlocks {

    public static final List<Block> BLOCKS = new ArrayList<>();

    // public static final WeatherVaneBlock COPPER_WEATHER_VANE = register("weather_vane", new WeatherVaneBlock(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2.5f, 2.5f).harvestTool(ToolType.PICKAXE).harvestLevel(4).notSolid().setAllowsSpawn((state, reader, pos, type) -> false).setOpaque((state, reader, pos) -> false).setSuffocates((state, reader, pos) -> false).setBlocksVision((state, reader, pos) -> false), WeatherVaneType.COPPER));
    public static final WeatherVane IRON_WEATHER_VANE = register("iron_weather_vane", new WeatherVane(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2.5f, 2.5f).harvestTool(ToolType.PICKAXE).harvestLevel(4).notSolid().setAllowsSpawn((state, reader, pos, type) -> false).setOpaque((state, reader, pos) -> false).setSuffocates((state, reader, pos) -> false).setBlocksVision((state, reader, pos) -> false), WeatherVaneType.IRON));
    public static final WeatherVaneRod IRON_WEATHER_VANE_ROD = register("iron_weather_vane_rod", new WeatherVaneRod(AbstractBlock.Properties.create(Material.IRON).hardnessAndResistance(2.5f, 2.5f).harvestTool(ToolType.PICKAXE).harvestLevel(4).notSolid().setAllowsSpawn((state, reader, pos, type) -> false).setOpaque((state, reader, pos) -> false).setSuffocates((state, reader, pos) -> false).setBlocksVision((state, reader, pos) -> false), WeatherVaneType.IRON));



    public static <T extends Block> T register(String key, T block) {
        block.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, key));
        BLOCKS.add(block);
        return block;
    }
}

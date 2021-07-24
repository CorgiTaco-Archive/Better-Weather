package corgitaco.betterweather.item;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.block.BetterWeatherBlocks;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("deprecation")
public class BetterWeatherItems {

    public static final List<Item> ITEMS = new ArrayList<>();

    public static final Item WEATHER_VANE = register(new BlockItem(BetterWeatherBlocks.WEATHER_VANE, new Item.Properties().group(ItemGroup.MISC)));

    public static <T extends Item> T register(String key, T item) {
        item.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, key));
        ITEMS.add(item);
        return item;
    }

    public static <T extends BlockItem> T register(T item) {
        return register(Registry.BLOCK.getKey(item.getBlock()).getPath(), item);
    }

    public static <T extends Item> T register(Block block, T item) {
        return register(Registry.BLOCK.getKey(block).getPath(), item);
    }
}

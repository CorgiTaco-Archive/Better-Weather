package corgitaco.betterweather.blockentity;

import com.mojang.datafixers.types.Type;
import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.block.BetterWeatherBlocks;
import corgitaco.betterweather.mixin.access.BlockEntityTypeBuilderAccess;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.datafix.TypeReferences;

import java.util.ArrayList;
import java.util.List;

public class BetterWeatherBlockEntityTypes {

    public static final List<TileEntityType<?>> BLOCK_ENTITIES = new ArrayList<>();

    public static final TileEntityType<WeatherVaneTileEntity> WEATHER_VANE_TILE_ENTITY = register("weather_vane", TileEntityType.Builder.create(WeatherVaneTileEntity::new, BetterWeatherBlocks.WEATHER_VANE));


    private static <T extends TileEntity> TileEntityType<T> register(String key, TileEntityType.Builder<T> builder) {
        if (((BlockEntityTypeBuilderAccess) (Object) builder).getBlocks().isEmpty()) {
            BetterWeather.LOGGER.warn("Block entity type {} requires at least one valid block to be defined!", (Object) key);
        }

        Type<?> type = Util.attemptDataFix(TypeReferences.BLOCK_ENTITY, key);
        TileEntityType<T> blockEntityType = builder.build(type);
        blockEntityType.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, key));

        BLOCK_ENTITIES.add(blockEntityType);
        return blockEntityType;
    }
}

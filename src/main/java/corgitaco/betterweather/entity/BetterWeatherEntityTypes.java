package corgitaco.betterweather.entity;

import corgitaco.betterweather.BetterWeather;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class BetterWeatherEntityTypes {

    public static final List<EntityType<?>> ENTITY_TYPES = new ArrayList<>();
    public static final EntityType<TornadoEntity> TORNADO_ENTITY_TYPE = register("tornado", EntityType.Builder.create(TornadoEntity::new, EntityClassification.MISC).size(0, 0).trackingRange(16).updateInterval(10));

    private static <T extends Entity> EntityType<T> register(String key, EntityType.Builder<T> builder) {
        EntityType<T> type = builder.build(key);
        type.setRegistryName(new ResourceLocation(BetterWeather.MOD_ID, key));
        ENTITY_TYPES.add(type);
        return type;
    }
}

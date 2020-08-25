package corgitaco.betterweather.core;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.entity.TornadoEntity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class BWEntityRegistry {

    public static final EntityType<TornadoEntity> TORNADO = EntityType.Builder.<TornadoEntity>create(TornadoEntity::new, EntityClassification.MISC).size(0.1F, 0.1F).func_233606_a_(6).build(BetterWeather.MOD_ID + ":tornado");


    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        BetterWeather.LOGGER.debug("Better Weather: Registering entities...");
        event.getRegistry().registerAll(
                TORNADO.setRegistryName("tornado")
        );
        BetterWeather.LOGGER.info("Better Weather: Entities Registered!");
    }
}
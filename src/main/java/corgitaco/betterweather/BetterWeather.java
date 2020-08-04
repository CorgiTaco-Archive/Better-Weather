package corgitaco.betterweather;

import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("betterweather")
public class BetterWeather {
    public static Logger LOGGER = LogManager.getLogger();
    public static final String MOD_ID = "betterweather";

    public BetterWeather() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::commonSetup);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
    }

    public void commonSetup(FMLCommonSetupEvent event) {

    }

    public void clientSetup(FMLClientSetupEvent event) {

    }

    @Mod.EventBusSubscriber(modid = BetterWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class BetterWeatherEvents {

        @SubscribeEvent
        public static void worldTick(TickEvent.WorldTickEvent event) {

        }

        @SubscribeEvent
        public static void renderTickEvent(TickEvent.RenderTickEvent event) {

        }

        @SubscribeEvent
        public static void playerTickEvent(TickEvent.PlayerTickEvent event) {

        }
    }
}

package corgitaco.betterweather;

import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.server.BetterWeatherGameRules;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Path;

@Mod("betterweather")
public class BetterWeather {
    public static final String MOD_ID = "betterweather";
    public static final Path CONFIG_PATH = new File(String.valueOf(FMLPaths.CONFIGDIR.get().resolve(MOD_ID))).toPath();
    public static Logger LOGGER = LogManager.getLogger();
    public static boolean usingOptifine;

    public BetterWeather() {
        File dir = new File(CONFIG_PATH.toString());
        if (!dir.exists())
            dir.mkdir();

        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);
        bus.addListener(this::clientSetup);
        bus.addListener(this::lateSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(BetterWeatherGameRules::init);
        NetworkHandler.init();
    }

    private void clientSetup(FMLClientSetupEvent event) {

    }

    private void lateSetup(FMLLoadCompleteEvent event) {

    }
}

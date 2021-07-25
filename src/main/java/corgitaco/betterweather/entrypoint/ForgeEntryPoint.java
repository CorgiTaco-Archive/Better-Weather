package corgitaco.betterweather.entrypoint;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.block.BetterWeatherBlocks;
import corgitaco.betterweather.blockentity.BetterWeatherBlockEntityTypes;
import corgitaco.betterweather.data.network.NetworkHandler;
import corgitaco.betterweather.entity.BetterWeatherEntityTypes;
import corgitaco.betterweather.item.BetterWeatherItems;
import corgitaco.betterweather.server.BetterWeatherGameRules;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BetterWeather.MOD_ID)
@Mod.EventBusSubscriber(modid = BetterWeather.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ForgeEntryPoint {

    public ForgeEntryPoint() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(BetterWeatherGameRules::init);
        NetworkHandler.init();
        BetterWeather.registerWeatherEventCodecs();
        BetterWeather.registerDefaultEvents();
    }

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        BetterWeather.LOGGER.debug(BetterWeather.MOD_ID + ": Registering blocks...");
        BetterWeatherBlocks.BLOCKS.forEach(block -> event.getRegistry().register(block));
        BetterWeather.LOGGER.info(BetterWeather.MOD_ID + ": Blocks registered!");
    }

    @SubscribeEvent
    public static void registerItem(RegistryEvent.Register<Item> event) {
        BetterWeather.LOGGER.debug(BetterWeather.MOD_ID + ": Registering items...");
        BetterWeatherItems.ITEMS.forEach(block -> event.getRegistry().register(block));
        BetterWeather.LOGGER.info(BetterWeather.MOD_ID + ": Items registered!");
    }

    @SubscribeEvent
    public static void registerBlockEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        BetterWeather.LOGGER.debug(BetterWeather.MOD_ID + ": Registering block entities...");
        BetterWeatherBlockEntityTypes.BLOCK_ENTITIES.forEach(entityType -> event.getRegistry().register(entityType));
        BetterWeather.LOGGER.info(BetterWeather.MOD_ID + ": Block entities registered!");
    }

    @SubscribeEvent
    public static void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        BetterWeather.LOGGER.debug(BetterWeather.MOD_ID + ": Registering entities...");
        BetterWeatherEntityTypes.ENTITY_TYPES.forEach(entityType -> event.getRegistry().register(entityType));
        BetterWeather.LOGGER.info(BetterWeather.MOD_ID + ": Entities registered!");

    }
}

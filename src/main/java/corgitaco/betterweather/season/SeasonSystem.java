package corgitaco.betterweather.season;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherClientUtil;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.datastorage.BetterWeatherEventData;
import corgitaco.betterweather.datastorage.BetterWeatherSeasonData;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.SeasonPacket;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import corgitaco.betterweather.datastorage.network.packet.util.RefreshRenderersPacket;
import corgitaco.betterweather.helper.IsWeatherForced;
import corgitaco.betterweather.helpers.IBiomeUpdate;
import corgitaco.betterweather.server.BetterWeatherGameRules;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.ServerWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class SeasonSystem {

    private static SeasonData.SubSeasonVal privateSubSeasonVal;
    private static boolean isFadingOut = false;

    public static void updateSeasonTime(World world) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int currentSeasonTime = BetterWeatherSeasonData.get(world).getSeasonTime();
        if (world.getGameRules().getBoolean(BetterWeatherGameRules.DO_SEASON_CYCLE)) {
            if (currentSeasonTime > BetterWeather.SEASON_CYCLE_LENGTH)
                BetterWeatherSeasonData.get(world).setSeasonTime(0);
            else
                BetterWeatherSeasonData.get(world).setSeasonTime(currentSeasonTime + 1);
        }

        if (BetterWeatherSeasonData.get(world).getSeasonCycleLength() != BetterWeather.SEASON_CYCLE_LENGTH)
            BetterWeatherSeasonData.get(world).setSeasonCycleLength(BetterWeather.SEASON_CYCLE_LENGTH);

    }

    public static void updateSeasonPacket(List<ServerPlayerEntity> players, World world, boolean justJoined) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int currentSeasonTime = BetterWeatherSeasonData.get(world).getSeasonTime();

        SeasonData.SubSeasonVal subSeason = getSubSeasonFromTime(currentSeasonTime, world, BetterWeatherSeasonData.get(world).getSeasonCycleLength()).getSubSeasonVal();

        if (SeasonData.currentSubSeason != subSeason || BetterWeatherSeasonData.get(world).isForced() || justJoined) {
            BetterWeatherSeasonData.get(world).setSubseason(subSeason.toString());
//            ((IBiomeUpdate) ((ServerWorld) world)).updateBiomeData();

        }

        if (BetterWeatherSeasonData.get(world).getSeasonTime() % 1200 == 0 || BetterWeatherSeasonData.get(world).isForced() || justJoined) {
            players.forEach(player -> NetworkHandler.sendToClient(player, new SeasonPacket(BetterWeatherSeasonData.get(world).getSeasonTime(), BetterWeather.SEASON_CYCLE_LENGTH)));

            if (BetterWeatherSeasonData.get(world).isForced())
                BetterWeatherSeasonData.get(world).setForced(false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSeason(ClientWorld world) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

//        if (BetterWeatherSeasonData.get(world) == null) {
//            BetterWeatherClientUtil.printDebugWarning("bw.warn.seasondata");
//
//            BetterWeather.LOGGER.error("Season data was called to early, this should never happen...\nSetting season data to prevent further issues, bugs and client desync with the server is possible!");
//        }

        int currentSeasonTime = BetterWeatherSeasonData.get(world).getSeasonTime();

        SeasonData.SubSeasonVal subSeason = getSubSeasonFromTime(currentSeasonTime, world, BetterWeatherSeasonData.get(world).getSeasonCycleLength()).getSubSeasonVal();


        if (SeasonData.currentSubSeason != subSeason) {
            BetterWeatherSeasonData.get(world).setSubseason(subSeason.toString());
            Minecraft minecraft = Minecraft.getInstance();
            SeasonData.currentSubSeason = subSeason;
            minecraft.worldRenderer.loadRenderers();
        }
    }

    public static Season.SubSeason getSubSeasonFromTime(int seasonTime, World world, int seasonCycleLength) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int perSeasonTime = seasonCycleLength / 4;

        SeasonData.SeasonVal seasonVal = getSeasonFromTime(seasonTime, seasonCycleLength);
        if (SeasonData.currentSeason != seasonVal) {
            BetterWeatherSeasonData.get(world).setSeason(seasonVal.toString());
            SeasonData.currentSeason = seasonVal;
        }

        int perSeasonTime3rd = perSeasonTime / 3;

        int seasonOffset = perSeasonTime * seasonVal.ordinal();

        if (seasonTime < seasonOffset + perSeasonTime3rd)
            return Season.getSeasonFromEnum(seasonVal).getStart();
        else if (seasonTime < seasonOffset + (perSeasonTime3rd * 2))
            return Season.getSeasonFromEnum(seasonVal).getMid();
        else {
            return Season.getSeasonFromEnum(seasonVal).getEnd();
        }
    }

    public static void tickCropForBiomeBlockOrSeason(ServerWorld world, BlockPos posIn, Block block, BlockState self, CallbackInfo ci) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        Season.SubSeason subSeason = Season.getSubSeasonFromEnum(SeasonData.currentSubSeason);
        if (subSeason.getBiomeToOverrideStorage().isEmpty() && subSeason.getCropToMultiplierStorage().isEmpty()) {
            if (BlockTags.CROPS.contains(block) || BlockTags.BEE_GROWABLES.contains(block) || BlockTags.SAPLINGS.contains(block)) {
                cropTicker(world, posIn, block, subSeason, true, self, ci);
            }
        } else {
            if (BlockTags.CROPS.contains(block) || BlockTags.BEE_GROWABLES.contains(block) || BlockTags.SAPLINGS.contains(block)) {
                cropTicker(world, posIn, block, subSeason, false, self, ci);
            }
        }
    }

    private static void cropTicker(ServerWorld world, BlockPos posIn, Block block, Season.SubSeason subSeason, boolean useSeasonDefault, BlockState self, CallbackInfo ci) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        //Collect the crop multiplier for the given subseason.
        double cropGrowthMultiplier = subSeason.getCropGrowthChanceMultiplier(BetterWeather.biomeRegistryEarlyAccess.getKey(world.getBiome(posIn)), block, useSeasonDefault);
        if (cropGrowthMultiplier == 1)
            return;

        //Pretty self explanatory, basically run a chance on whether or not the crop will tick for this tick
        if (cropGrowthMultiplier < 1) {
            if (world.getRandom().nextDouble() < cropGrowthMultiplier) {
                block.randomTick(self, world, posIn, world.getRandom());
            } else
                ci.cancel();
        }
        //Here we gather a random number of ticks that this block will tick for this given tick.
        //We do a random.nextDouble() to determine if we get the ceil or floor value for the given crop growth multiplier.
        else if (cropGrowthMultiplier > 1) {
            int numberOfTicks = world.getRandom().nextInt((world.getRandom().nextDouble() + (cropGrowthMultiplier - 1) < cropGrowthMultiplier) ? (int) Math.ceil(cropGrowthMultiplier) : (int) cropGrowthMultiplier) + 1;
            for (int tick = 0; tick < numberOfTicks; tick++) {
                if (tick > 0) {
                    self = world.getBlockState(posIn);
                    block = self.getBlock();
                }

                block.randomTick(self, world, posIn, world.getRandom());
            }
        }
    }

    public static void rollWeatherEventChanceForSeason(Random random, ServerWorld world, boolean isRaining, boolean isThundering, ServerWorldInfo worldInfo, List<ServerPlayerEntity> players) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        Season.SubSeason subSeason = Season.getSubSeasonFromEnum(SeasonData.currentSubSeason);
        boolean isRainActive = isRaining || isThundering;

        if (privateSubSeasonVal == null) {
            privateSubSeasonVal = SeasonData.currentSubSeason;
        }

        boolean privateSeasonIsNotCacheSeasonFlag = privateSubSeasonVal != SeasonData.currentSubSeason;

        if (!isRainActive) {
            if (!BetterWeatherEventData.get(world).isModified() || privateSeasonIsNotCacheSeasonFlag) {
                worldInfo.setRainTime(BetterWeatherUtil.transformRainOrThunderTimeToCurrentSeason(worldInfo.getRainTime(), Season.getSubSeasonFromEnum(privateSubSeasonVal), subSeason));
                worldInfo.setThunderTime(BetterWeatherUtil.transformRainOrThunderTimeToCurrentSeason(worldInfo.getThunderTime(), Season.getSubSeasonFromEnum(privateSubSeasonVal), subSeason));
                BetterWeatherEventData.get(world).setModified(true);
            }
        } else {
            if (BetterWeatherEventData.get(world).isModified())
                BetterWeatherEventData.get(world).setModified(false);
        }


        if (world.rainingStrength == 0.0F) {
            if (isRainActive) {
                AtomicBoolean weatherEventWasSet = new AtomicBoolean(false);
                if (!BetterWeatherEventData.get(world).isWeatherForced()) { //If weather isn't forced, roll chance
                    subSeason.getWeatherEventController().forEach((event, chance) -> {
                        if (!event.equals(WeatherEventSystem.CLEAR.toString())) {
                            if (random.nextDouble() < chance) {
                                weatherEventWasSet.set(true);
                                BetterWeatherEventData.get(world).setEvent(event);
                            }
                        }
                    });
                    if (!weatherEventWasSet.get())
                        BetterWeatherEventData.get(world).setEvent(WeatherEventSystem.DEFAULT.toString());

                    players.forEach(player -> {
                        NetworkHandler.sendToClient(player, new WeatherEventPacket(BetterWeatherEventData.get(world).getEventString()));
                        if (WeatherData.currentWeatherEvent.refreshPlayerRenderer())
                            NetworkHandler.sendToClient(player, new RefreshRenderersPacket());
                    });
                }
            }
        } else {
            if (!isRainActive) {
                if (world.rainingStrength == 1.0F) {
                    isFadingOut = true;
                } else if (world.rainingStrength <= 0.011F && isFadingOut) {
                    boolean refreshRenderersPost = WeatherData.currentWeatherEvent.refreshPlayerRenderer();
                    BetterWeatherEventData.get(world).setEvent(WeatherEventSystem.CLEAR.toString());
                    ((IsWeatherForced) worldInfo).setWeatherForced(false);
                    BetterWeatherEventData.get(world).setWeatherForced(((IsWeatherForced) worldInfo).isWeatherForced());
                    players.forEach(player -> {
                        NetworkHandler.sendToClient(player, new WeatherEventPacket(BetterWeatherEventData.get(world).getEventString()));
                        if (refreshRenderersPost)
                            NetworkHandler.sendToClient(player, new RefreshRenderersPacket());
                    });

                    isFadingOut = false;
                }
            }
        }

        if (privateSeasonIsNotCacheSeasonFlag) {
            privateSubSeasonVal = SeasonData.currentSubSeason;
        }
    }

    public static int getTimeInCycleForSubSeason(SeasonData.SubSeasonVal subSeasonVal, int seasonCycleLength) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int perSubSeasonLength = seasonCycleLength / (SeasonData.SubSeasonVal.values().length);
        return perSubSeasonLength * subSeasonVal.ordinal();
    }

    public static SeasonData.SeasonVal getSeasonFromTime(int seasonTime, int seasonCycleLength) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int perSeasonTime = seasonCycleLength / 4;

        if (seasonTime < perSeasonTime) {
            return SeasonData.SeasonVal.SPRING;
        } else if (seasonTime < perSeasonTime * 2) {
            return SeasonData.SeasonVal.SUMMER;
        } else if (seasonTime < perSeasonTime * 3) {
            return SeasonData.SeasonVal.AUTUMN;
        } else
            return SeasonData.SeasonVal.WINTER;
    }
}
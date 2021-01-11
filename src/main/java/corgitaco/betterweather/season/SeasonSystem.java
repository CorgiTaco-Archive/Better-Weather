package corgitaco.betterweather.season;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.access.IsWeatherForced;
import corgitaco.betterweather.api.SeasonData;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherData;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.SeasonPacket;
import corgitaco.betterweather.datastorage.network.packet.WeatherEventPacket;
import corgitaco.betterweather.weatherevent.WeatherEventSystem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
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

    public static void updateSeasonTime() {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();
        if (currentSeasonTime > BetterWeather.SEASON_CYCLE_LENGTH)
            BetterWeather.seasonData.setSeasonTime(0);
        else
            BetterWeather.seasonData.setSeasonTime(currentSeasonTime + 1);

        if (BetterWeather.seasonData.getSeasonCycleLength() != BetterWeather.SEASON_CYCLE_LENGTH)
            BetterWeather.seasonData.setSeasonCycleLength(BetterWeather.SEASON_CYCLE_LENGTH);

    }

    public static void updateSeasonPacket(List<ServerPlayerEntity> players, World world, boolean justJoined) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        BetterWeather.setSeasonData(world);
        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();

        SeasonData.SubSeasonVal subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength()).getSubSeasonVal();

        if (SeasonData.currentSubSeason != subSeason || BetterWeather.seasonData.isForced() || justJoined) {
            BetterWeather.seasonData.setSubseason(subSeason.toString());
        }

        if (BetterWeather.seasonData.getSeasonTime() % 1200 == 0 || BetterWeather.seasonData.isForced() || justJoined) {
            players.forEach(player -> NetworkHandler.sendTo(player, new SeasonPacket(BetterWeather.seasonData.getSeasonTime(), BetterWeather.SEASON_CYCLE_LENGTH)));;

            if (BetterWeather.seasonData.isForced())
                BetterWeather.seasonData.setForced(false);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientSeason() {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();

        SeasonData.SubSeasonVal subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength()).getSubSeasonVal();


        if (SeasonData.currentSubSeason != subSeason) {
            BetterWeather.seasonData.setSubseason(subSeason.toString());
            Minecraft minecraft = Minecraft.getInstance();
            SeasonData.currentSubSeason = subSeason;
            minecraft.worldRenderer.loadRenderers();
        }
    }


    public static Season.SubSeason getSubSeasonFromTime(int seasonTime, int seasonCycleLength) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int perSeasonTime = seasonCycleLength / 4;

        SeasonData.SeasonVal seasonVal = getSeasonFromTime(seasonTime, seasonCycleLength);
        if (SeasonData.currentSeason != seasonVal) {
            BetterWeather.seasonData.setSeason(seasonVal.toString());
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
            if (BlockTags.CROPS.contains(block) || BlockTags.BEE_GROWABLES.contains(block)) {
                cropTicker(world, posIn, block, subSeason, true, self, ci);
            }
        } else {
            if (BlockTags.CROPS.contains(block) || BlockTags.BEE_GROWABLES.contains(block)) {
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
            ci.cancel();
            if (world.getRandom().nextDouble() < cropGrowthMultiplier) {
                block.randomTick(self, world, posIn, world.getRandom());
            }
        }
        //Here we gather a random number of ticks that this block will tick for this given tick.
        //We do a random.nextDouble() to determine if we get the ceil or floor value for the given crop growth multiplier.
        else if (cropGrowthMultiplier > 1) {
            ci.cancel();
            int numberOfTicks = world.getRandom().nextInt((world.getRandom().nextDouble() + (cropGrowthMultiplier - 1) < cropGrowthMultiplier) ? (int) Math.ceil(cropGrowthMultiplier) : (int) cropGrowthMultiplier) + 1;
            for (int tick = 0; tick < numberOfTicks; tick++) {
                block.randomTick(self, world, posIn, world.getRandom());
            }
        }
    }

    private static int tickCounter = 0;
    private static SeasonData.SubSeasonVal privateSubSeasonVal;

    public static void rollWeatherEventChanceForSeason(Random random, boolean isRaining, boolean isThundering, ServerWorldInfo worldInfo, List<ServerPlayerEntity> players) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        Season.SubSeason subSeason = Season.getSubSeasonFromEnum(SeasonData.currentSubSeason);
        boolean isRainActive = isRaining || isThundering;

        if (privateSubSeasonVal == null) {
            privateSubSeasonVal = SeasonData.currentSubSeason;
        }

        boolean privateSeasonIsNotCacheSeasonFlag = privateSubSeasonVal != SeasonData.currentSubSeason;

        if (!isRainActive) {
            if (!BetterWeather.weatherData.isModified() || privateSeasonIsNotCacheSeasonFlag) {
                worldInfo.setRainTime(BetterWeatherUtil.transformRainOrThunderTimeToCurrentSeason(worldInfo.getRainTime(), Season.getSubSeasonFromEnum(privateSubSeasonVal), subSeason));
                worldInfo.setThunderTime(BetterWeatherUtil.transformRainOrThunderTimeToCurrentSeason(worldInfo.getThunderTime(), Season.getSubSeasonFromEnum(privateSubSeasonVal), subSeason));
                BetterWeather.weatherData.setModified(true);
            }
        } else {
            if (BetterWeather.weatherData.isModified())
                BetterWeather.weatherData.setModified(false);
        }


        if (tickCounter == 0) {
            if (isRainActive) {
                AtomicBoolean weatherEventWasSet = new AtomicBoolean(false);
                if (!BetterWeather.weatherData.isWeatherForced()) { //If weather isn't forced, roll chance
                    subSeason.getWeatherEventController().forEach((event, chance) -> {
                        if (!event.equals(WeatherEventSystem.CLEAR.toString())) {
                            if (random.nextDouble() < chance) {
                                weatherEventWasSet.set(true);
                                BetterWeather.weatherData.setEvent(event);
                                worldInfo.setThundering(WeatherData.currentWeatherEvent.hasSkyDarkness());
                            }
                        }
                    });
                    if (!weatherEventWasSet.get())
                        BetterWeather.weatherData.setEvent(WeatherEventSystem.CLEAR.toString());
                    players.forEach(player -> NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEventString())));
                }
                tickCounter++;
            }
        } else {
            if (!isRainActive) {
                if (tickCounter > 0) {
                    BetterWeather.weatherData.setEvent(WeatherEventSystem.CLEAR.toString());
                    ((IsWeatherForced) worldInfo).setWeatherForced(false);
                    BetterWeather.weatherData.setWeatherForced(((IsWeatherForced) worldInfo).isWeatherForced());
                    players.forEach(player -> NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEventString())));
                    tickCounter = 0;
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
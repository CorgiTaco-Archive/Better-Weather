package corgitaco.betterweather.season;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.access.IsWeatherForced;
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
import net.minecraft.world.storage.IServerWorldInfo;
import net.minecraft.world.storage.ServerWorldInfo;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class SeasonSystem {

    public static SubSeasonVal cachedSubSeason = SubSeasonVal.SPRING_START;
    public static SeasonVal cachedSeason = SeasonVal.SPRING;

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

        SubSeasonVal subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength()).getSubSeasonVal();

        if (cachedSubSeason != subSeason || BetterWeather.seasonData.isForced() || justJoined) {
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

        SubSeasonVal subSeason = getSubSeasonFromTime(currentSeasonTime, BetterWeather.seasonData.getSeasonCycleLength()).getSubSeasonVal();


        if (cachedSubSeason != subSeason) {
            BetterWeather.seasonData.setSubseason(subSeason.toString());
            Minecraft minecraft = Minecraft.getInstance();
            cachedSubSeason = subSeason;
            minecraft.worldRenderer.loadRenderers();
        }
    }


    public static Season.SubSeason getSubSeasonFromTime(int seasonTime, int seasonCycleLength) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int perSeasonTime = seasonCycleLength / 4;

        SeasonVal seasonVal = getSeasonFromTime(seasonTime, seasonCycleLength);
        if (cachedSeason != seasonVal) {
            BetterWeather.seasonData.setSeason(seasonVal.toString());
            cachedSeason = seasonVal;
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

        Season.SubSeason subSeason = Season.getSubSeasonFromEnum(SeasonSystem.cachedSubSeason);
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
    private static SeasonSystem.SubSeasonVal privateSubSeasonVal;

    public static void rollWeatherEventChanceForSeason(Random random, boolean isRaining, boolean isThundering, ServerWorldInfo worldInfo, List<ServerPlayerEntity> players) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        Season.SubSeason subSeason = Season.getSubSeasonFromEnum(SeasonSystem.cachedSubSeason);
        boolean isRainActive = isRaining || isThundering;

        if (privateSubSeasonVal == null) {
            privateSubSeasonVal = SeasonSystem.cachedSubSeason;
        }

        boolean privateSeasonIsNotCacheSeasonFlag = privateSubSeasonVal != SeasonSystem.cachedSubSeason;

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
                        if (random.nextDouble() < chance) {
                            weatherEventWasSet.set(true);
                            BetterWeather.weatherData.setEvent(event);
                        }
                    });
                    if (!weatherEventWasSet.get())
                        BetterWeather.weatherData.setEvent(WeatherEventSystem.NONE);
                    players.forEach(player -> NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEvent())));
                }
                tickCounter++;
            }
        } else {
            if (!isRainActive) {
                if (tickCounter > 0) {
                    BetterWeather.weatherData.setEvent(WeatherEventSystem.NONE);
                    ((IsWeatherForced) worldInfo).setWeatherForced(false);
                    BetterWeather.weatherData.setWeatherForced(((IsWeatherForced) worldInfo).isWeatherForced());
                    players.forEach(player -> NetworkHandler.sendTo(player, new WeatherEventPacket(BetterWeather.weatherData.getEvent())));
                    tickCounter = 0;
                }
            }
        }

        if (privateSeasonIsNotCacheSeasonFlag) {
            privateSubSeasonVal = SeasonSystem.cachedSubSeason;
        }
    }

    public static int getTimeInCycleForSubSeason(SubSeasonVal subSeasonVal, int seasonCycleLength) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int perSubSeasonLength = seasonCycleLength / (SubSeasonVal.values().length);
        return perSubSeasonLength * subSeasonVal.ordinal();
    }

    public static SeasonVal getSeasonFromTime(int seasonTime, int seasonCycleLength) {
        if (!BetterWeather.useSeasons)
            throw new UnsupportedOperationException("Seasons are disabled in this instance!");

        int perSeasonTime = seasonCycleLength / 4;

        if (seasonTime < perSeasonTime) {
            return SeasonVal.SPRING;
        } else if (seasonTime < perSeasonTime * 2) {
            return SeasonVal.SUMMER;
        } else if (seasonTime < perSeasonTime * 3) {
            return SeasonVal.AUTUMN;
        } else
            return SeasonVal.WINTER;
    }

    public enum SeasonVal {
        SPRING,
        SUMMER,
        AUTUMN,
        WINTER;
    }

    public enum SubSeasonVal {
        SPRING_START,
        SPRING_MID,
        SPRING_END,

        SUMMER_START,
        SUMMER_MID,
        SUMMER_END,

        AUTUMN_START,
        AUTUMN_MID,
        AUTUMN_END,

        WINTER_START,
        WINTER_MID,
        WINTER_END;
    }
}
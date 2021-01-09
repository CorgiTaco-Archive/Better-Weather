package corgitaco.betterweather.season;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.datastorage.network.NetworkHandler;
import corgitaco.betterweather.datastorage.network.packet.SeasonPacket;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

public class BWSeasonSystem {

    public static SubSeasonVal cachedSubSeason = SubSeasonVal.SPRING_START;
    public static SeasonVal cachedSeason = SeasonVal.SPRING;

    public static void updateSeasonTime() {
        int currentSeasonTime = BetterWeather.seasonData.getSeasonTime();
        if (currentSeasonTime > BetterWeather.SEASON_CYCLE_LENGTH)
            BetterWeather.seasonData.setSeasonTime(0);
        else
            BetterWeather.seasonData.setSeasonTime(currentSeasonTime + 1);

        if (BetterWeather.seasonData.getSeasonCycleLength() != BetterWeather.SEASON_CYCLE_LENGTH)
            BetterWeather.seasonData.setSeasonCycleLength(BetterWeather.SEASON_CYCLE_LENGTH);

    }

    public static void updateSeasonPacket(List<ServerPlayerEntity> players, World world, boolean justJoined) {
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
        Season.SubSeason subSeason = Season.getSubSeasonFromEnum(BWSeasonSystem.cachedSubSeason);
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

    public static int getTimeInCycleForSubSeason(SubSeasonVal subSeasonVal, int seasonCycleLength) {
        int perSubSeasonLength = seasonCycleLength / (SubSeasonVal.values().length);
        return perSubSeasonLength * subSeasonVal.ordinal();
    }

    public static SeasonVal getSeasonFromTime(int seasonTime, int seasonCycleLength) {
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
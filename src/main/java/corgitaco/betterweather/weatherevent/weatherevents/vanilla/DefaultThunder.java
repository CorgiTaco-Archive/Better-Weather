package corgitaco.betterweather.weatherevent.weatherevents.vanilla;

import corgitaco.betterweather.BetterWeather;
import corgitaco.betterweather.BetterWeatherUtil;
import corgitaco.betterweather.api.weatherevent.BetterWeatherID;
import corgitaco.betterweather.api.weatherevent.WeatherEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.LightningBoltEntity;
import net.minecraft.entity.passive.horse.SkeletonHorseEntity;
import net.minecraft.profiler.IProfiler;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.GameRules;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.server.ChunkHolder;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.List;

public class DefaultThunder extends WeatherEvent {
    public DefaultThunder() {
        super(new BetterWeatherID(BetterWeather.MOD_ID, "DEFAULT_THUNDER"), 0.4);
    }

    @Override
    public void worldTick(ServerWorld world, int tickSpeed, long worldTime, Iterable<ChunkHolder> loadedChunks) {
        loadedChunks.forEach(chunk -> {
            ChunkPos chunkpos = chunk.getPosition();
            boolean flag = world.isRaining();
            int i = chunkpos.getXStart();
            int j = chunkpos.getZStart();
            IProfiler iprofiler = world.getProfiler();
            iprofiler.startSection("thunder");
            if (flag && world.isThundering() && world.rand.nextInt(100000) == 0) {
                BlockPos blockpos = adjustPosToNearbyEntity(world.getBlockRandomPos(i, 0, j, 15), world);
                if (world.isRainingAt(blockpos)) {
                    DifficultyInstance difficultyinstance = world.getDifficultyForLocation(blockpos);
                    boolean flag1 = world.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING) && world.rand.nextDouble() < (double) difficultyinstance.getAdditionalDifficulty() * 0.01D;
                    if (flag1) {
                        SkeletonHorseEntity skeletonhorseentity = EntityType.SKELETON_HORSE.create(world);
                        skeletonhorseentity.setTrap(true);
                        skeletonhorseentity.setGrowingAge(0);
                        skeletonhorseentity.setPosition(blockpos.getX(), blockpos.getY(), blockpos.getZ());
                        world.addEntity(skeletonhorseentity);
                    }

                    LightningBoltEntity lightningboltentity = EntityType.LIGHTNING_BOLT.create(world);
                    lightningboltentity.moveForced(Vector3d.copyCenteredHorizontally(blockpos));
                    lightningboltentity.setEffectOnly(flag1);
                    world.addEntity(lightningboltentity);
                }
            }
        });
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc, int postClientTicksLeft) {

    }

    @Override
    public boolean renderWeather(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z) {
        return false;
    }



    protected BlockPos adjustPosToNearbyEntity(BlockPos pos, ServerWorld world) {
        BlockPos blockpos = world.getHeight(Heightmap.Type.MOTION_BLOCKING, pos);
        AxisAlignedBB axisalignedbb = (new AxisAlignedBB(blockpos, new BlockPos(blockpos.getX(), world.getHeight(), blockpos.getZ()))).grow(3.0D);
        List<LivingEntity> list = world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb, (entity) -> entity != null && entity.isAlive() && world.canSeeSky(entity.getPosition()));
        if (!list.isEmpty()) {
            return list.get(world.rand.nextInt(list.size())).getPosition();
        } else {
            if (blockpos.getY() == -1) {
                blockpos = blockpos.up(2);
            }
            return blockpos;
        }
    }

    @Override
    public Color modifySkyColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return BetterWeatherUtil.blendColor(returnColor, BetterWeatherUtil.DEFAULT_THUNDER_SKY, rainStrength);
    }

    @Override
    public Color modifyCloudColor(Color returnColor, float rainStrength) {
        return BetterWeatherUtil.blendColor(returnColor, BetterWeatherUtil.DEFAULT_THUNDER_CLOUDS, rainStrength);
    }

    @Override
    public Color modifyFogColor(Color biomeColor, Color returnColor, @Nullable Color seasonTargetColor, float rainStrength) {
        return BetterWeatherUtil.blendColor(returnColor, BetterWeatherUtil.DEFAULT_THUNDER_FOG, rainStrength);
    }

    @Override
    public float skyOpacity() {
        return super.skyOpacity();
    }
}

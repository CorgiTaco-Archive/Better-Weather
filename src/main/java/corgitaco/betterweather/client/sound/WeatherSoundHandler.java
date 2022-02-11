package corgitaco.betterweather.client.sound;

import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventAudio;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.common.weather.WeatherContext;
import corgitaco.betterweather.util.BetterWeatherWorldData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.IAmbientSoundHandler;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;

public class WeatherSoundHandler implements IAmbientSoundHandler {

    private final ClientPlayerEntity player;
    private final SoundHandler soundHandler;
    private final BiomeManager biomeManager;
    private final ClientWorld world;
    private WeatherSoundHandler.Sound currentSound;

    public WeatherSoundHandler(ClientPlayerEntity player, SoundHandler soundHandler, BiomeManager biomeManager) {
        this.player = player;
        this.soundHandler = soundHandler;
        this.biomeManager = biomeManager;
        this.world = player.clientLevel;
    }

    @Override
    public void tick() {
        WeatherContext weatherEventContext = ((BetterWeatherWorldData) player.clientLevel).getWeatherContext();
        if (weatherEventContext == null) {
            return;
        }

        WeatherEvent currentEvent = weatherEventContext.getCurrentEvent();
        WeatherEventClientSettings clientSettings = currentEvent.getClientSettings();
        if (clientSettings instanceof WeatherEventAudio) {
            Biome currentBiome = biomeManager.getBiome(player.blockPosition());
            if (currentEvent.isValidBiome(currentBiome)) {
                if (this.currentSound == null) {
                    this.currentSound = new WeatherSoundHandler.Sound(((WeatherEventAudio) clientSettings).getSound(), this.world, ((WeatherEventAudio) clientSettings).getVolume(), ((WeatherEventAudio) clientSettings).getPitch());
                    this.soundHandler.play(this.currentSound);
                }

                if (world.rainLevel == 1.0) {
                    this.currentSound.fadeInSound();
                }
            } else {
                endAudio();
            }
        } else {
            endAudio();
        }
    }

    private void endAudio() {
        if (this.currentSound != null) {
            this.currentSound.fadeOutSound();
            if (this.currentSound.isStopped()) {
                this.currentSound = null;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Sound extends TickableSound {
        private static final Vector3d[] vector3ds = vectorDistribution(1000);

        private final ClientWorld world;
        private final float maxVolume;

        private int fadeSpeed;
        private int fadeInTicks;

        private boolean overrideRainStrength = false;
        private float decreasedVolume;

        private int fadeLimit = 40;


        public Sound(SoundEvent sound, ClientWorld world, float volume, float pitch) {
            super(sound, SoundCategory.WEATHER);
            this.world = world;
            this.looping = true;
            this.delay = 0;
            this.volume = volume;
            this.pitch = pitch;
            this.maxVolume = volume;

        }

        /**
         * Python script for visualising where the sound is being sampled from
         * <p>
         * import math
         * <p>
         * from matplotlib import pyplot
         * from mpl_toolkits.mplot3d import Axes3D
         * <p>
         * import matplotlib.pyplot as plt
         * from mpl_toolkits.mplot3d import Axes3D
         * fig = plt.figure()
         * ax = fig.add_subplot(111, projection='3d')
         * <p>
         * <p>
         * def fibonacci_sphere(samples=1):
         * <p>
         * points = []
         * phi = math.pi * (3. - math.sqrt(5.))  # golden angle in radians
         * <p>
         * for i in range(int(samples/4), samples):
         * y = 1 - (i / float(samples - 1)) * 2  # y goes from 1 to -1
         * radius = math.sqrt(1 - y * y)  # radius at y
         * <p>
         * theta = phi * i  # golden angle increment
         * <p>
         * x = math.cos(theta) * radius
         * z = math.sin(theta) * radius
         * <p>
         * points.append((x, y, z))
         * <p>
         * return points
         * <p>
         * xvals = []
         * yvals = []
         * zvals = []
         * for point in fibonacci_sphere(10000):
         * xvals.append(point[0])
         * yvals.append(point[1])
         * zvals.append(point[2])
         * <p>
         * fig = pyplot.figure()
         * ax = Axes3D(fig)
         * <p>
         * ax.scatter(xvals, yvals, zvals)
         * pyplot.show()
         */
        private static Vector3d[] vectorDistribution(int samples) {
            List<Vector3d> points = new ArrayList<>();
            double phi = Math.PI * (3. - Math.sqrt(5.));  // golden angle in radians

            for (int i = samples / 4; i < samples; i++) {
                double y = 1 - (i / (float) (samples - 1)) * 2; //y goes from 1 to -1
                double radius = Math.sqrt(1 - y * y); //radius at y

                double theta = phi * i; //golden angle increment

                double x = Math.cos(theta) * radius;
                double z = Math.sin(theta) * radius;

                points.add(new Vector3d(x, -y, z));
            }
            return points.toArray(new Vector3d[0]);
        }

        public void tick() {
            if (volume == 0) {
                this.stop();
            }

            Vector3d startPos = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
            ClientPlayerEntity player = Minecraft.getInstance().player;
            byte brightness = (byte) (this.world.getBrightness(LightType.SKY, new BlockPos(startPos.x, startPos.y, startPos.z)));
            int vectorDistance = 35;
            double maxDistanceNormalised = 0; //average sample distance

            //Cast ray in every direction sampled
            for (int i = 0, vector3dsLength = vector3ds.length; i < vector3dsLength; i++) {
                Vector3d endPos = startPos.add(vector3ds[i].scale(vectorDistance));
                BlockRayTraceResult result = world.clip(new RayTraceContext(startPos, endPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, null));

                if (result.getType() == RayTraceResult.Type.MISS) {
                    maxDistanceNormalised += maxVolume; //if miss, assume max volume
                }

                Vector3d hitVec = result.getLocation();
                Vector3d dif = startPos.subtract(hitVec);
                float distance = (float) dif.length() / vectorDistance; //normalised distance
                //distance is weighted such that longer distance count more, based on skylight brightness
                maxDistanceNormalised += MathHelper.lerp(brightness / 15f, startPos.y < world.getSeaLevel() ? 0.0000000000001 : distance, Math.pow(distance, 1 / 4f /*Controls the weighting based on distance*/));
            }
            this.decreasedVolume = (float) (maxDistanceNormalised / vector3ds.length) * (player.isEyeInFluid(FluidTags.WATER) || player.isEyeInFluid(FluidTags.LAVA) ? 0.2F : 1);


            this.fadeInTicks += this.fadeSpeed;
            this.volume = MathHelper.clamp(world.getRainLevel(Minecraft.getInstance().getFrameTime()), 0.0F, decreasedVolume);
        }

        public void fadeOutSound() {
            this.fadeInTicks = Math.min(this.fadeInTicks, fadeLimit);
            this.fadeSpeed = -1;
        }

        public void fadeInSound() {
            this.fadeInTicks = Math.max(0, this.fadeInTicks);
            this.fadeSpeed = 1;
        }
    }
}

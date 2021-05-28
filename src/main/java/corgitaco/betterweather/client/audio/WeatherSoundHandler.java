package corgitaco.betterweather.client.audio;

import corgitaco.betterweather.api.weather.WeatherEvent;
import corgitaco.betterweather.api.weather.WeatherEventAudio;
import corgitaco.betterweather.api.weather.WeatherEventClientSettings;
import corgitaco.betterweather.helpers.BetterWeatherWorldData;
import corgitaco.betterweather.weather.BWWeatherEventContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.IAmbientSoundHandler;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
        this.world = player.worldClient;
    }

    @Override
    public void tick() {
        BWWeatherEventContext weatherEventContext = ((BetterWeatherWorldData) player.worldClient).getWeatherEventContext();
        if (weatherEventContext == null) {
            return;
        }

        WeatherEvent currentEvent = weatherEventContext.getCurrentEvent();
        WeatherEventClientSettings clientSettings = currentEvent.getClientSettings();
        if (clientSettings instanceof WeatherEventAudio) {
            Biome currentBiome = biomeManager.getBiome(player.getPosition());
            if (currentEvent.biomeConditionPasses(this.player.world.func_241828_r().getRegistry(Registry.BIOME_KEY).getOptionalKey(currentBiome).get(), currentBiome)) {
                if (this.currentSound == null) {
                    this.currentSound = new WeatherSoundHandler.Sound(((WeatherEventAudio) clientSettings).getSound(), this.world, ((WeatherEventAudio) clientSettings).getVolume(), ((WeatherEventAudio) clientSettings).getPitch());
                    this.soundHandler.play(this.currentSound);
                }

                if (world.rainingStrength == 1.0) {
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
            if (this.currentSound.isDonePlaying()) {
                this.currentSound = null;
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static class Sound extends TickableSound {
        private final ClientWorld world;
        private final float maxVolume;

        private int fadeSpeed;
        private int fadeInTicks;

        private boolean overrideRainStrength = false;

        public Sound(SoundEvent sound, ClientWorld world, float volume, float pitch) {
            super(sound, SoundCategory.WEATHER);
            this.world = world;
            this.repeat = true;
            this.repeatDelay = 0;
            this.volume = volume;
            this.pitch = pitch;
            this.maxVolume = volume;
        }

        public void tick() {
            if (volume == 0.0) {
                this.finishPlaying();
            }

            if (overrideRainStrength) {
                if (this.fadeInTicks < 0) {
                    this.finishPlaying();
                }

                this.fadeInTicks += this.fadeSpeed;
                this.volume = MathHelper.clamp((float) this.fadeInTicks / 40.0F, 0.0F, maxVolume);

                if (this.volume == maxVolume) {
                    overrideRainStrength = false;
                }
            } else {
                this.volume = MathHelper.clamp(world.getRainStrength(Minecraft.getInstance().getRenderPartialTicks()), 0.0F, maxVolume);
            }
        }

        public void fadeOutSound() {
            this.overrideRainStrength = true;
            this.fadeInTicks = Math.min(this.fadeInTicks, 40);
            this.fadeSpeed = -1;
        }

        public void fadeInSound() {
            this.overrideRainStrength = true;
            this.fadeInTicks = Math.max(0, this.fadeInTicks);
            this.fadeSpeed = 1;
        }
    }
}

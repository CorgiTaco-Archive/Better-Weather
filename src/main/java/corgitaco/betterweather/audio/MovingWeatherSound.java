package corgitaco.betterweather.audio;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LightType;

public class MovingWeatherSound extends TickableSound {
    private final float originalVolume;

    public MovingWeatherSound(SoundEvent soundIn, int replayRate, SoundCategory categoryIn, BlockPos initialPosition, float originalVolume, float pitch) {
        super(soundIn, categoryIn);
        this.x = initialPosition.getX();
        this.y = initialPosition.getY();
        this.z = initialPosition.getZ();
        this.originalVolume = originalVolume;
        this.volume = originalVolume;
        this.pitch = pitch;
        this.repeat = true;
        this.repeatDelay = replayRate;
    }


    @Override
    public void tick() {
        Minecraft mc = Minecraft.getInstance();
        BlockPos livePosition = mc.gameRenderer.getActiveRenderInfo().getBlockPos();
        this.x = livePosition.getX();
        this.y = livePosition.getY();
        this.z = livePosition.getZ();
        changeVolumeDynamically(mc, livePosition);
    }


    public void changeVolumeDynamically(Minecraft mc, BlockPos livePosition) {
        float partialTicks = mc.isGamePaused() ? mc.renderPartialTicksPaused : mc.timer.renderPartialTicks;
        float fade = mc.world.getRainStrength(partialTicks);
        float finalVolume = this.originalVolume;

        IsInsideAudioHelper isInsideAudioHelper = new IsInsideAudioHelper(livePosition, mc, this.originalVolume);
        finalVolume = isInsideAudioHelper.getFinalVolume();


        //Check if the player is underwater then chop the noise volume in half(essentially muffling it)
        if (mc.world.getBlockState(livePosition).getBlock() == Blocks.WATER && mc.world.getBlockState(livePosition).getFluidState().getLevel() >= 6)
            finalVolume = finalVolume / 2;





        float clampedFinalVolume = MathHelper.clamp(finalVolume, 0.01F, 1.0F);
        this.volume = fade * clampedFinalVolume;
        if (this.volume == 0.0F) {
            this.finishPlaying();
        }
    }

    public static class IsInsideAudioHelper {
        private final float originalVolume;
        private final float finalVolume;

        private IsInsideAudioHelper(BlockPos playerLivePos, Minecraft mc, float originalVolume) {
            this.originalVolume = originalVolume;
            int lightLevel = mc.world.getLightFor(LightType.SKY, playerLivePos);
            this.finalVolume = lightLevel * 0.02F;

        }


        public float getFinalVolume() {
            return MathHelper.clamp(finalVolume, 0.01F, originalVolume);
        }
    }
}

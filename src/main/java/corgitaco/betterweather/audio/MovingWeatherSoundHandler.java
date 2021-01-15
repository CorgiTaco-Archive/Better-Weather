package corgitaco.betterweather.audio;

import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.Heightmap;

public class MovingWeatherSoundHandler extends TickableSound {
    private final float originalVolume;

    public MovingWeatherSoundHandler(SoundEvent soundIn, int replayRate, SoundCategory categoryIn, BlockPos initialPosition, float originalVolume, float pitch) {
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
        int motionBlockingY = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, livePosition.getX(), livePosition.getZ());
        float partialTicks = mc.isGamePaused() ? mc.renderPartialTicksPaused : mc.timer.renderPartialTicks;
        float fade = mc.world.getRainStrength(partialTicks);


        float finalVolume = this.originalVolume;
        float playerHeightToMotionBlockingHeightDifference = (motionBlockingY - livePosition.getY()) * 0.02F;
        float heightMapCalculatedVolume = this.volume - (playerHeightToMotionBlockingHeightDifference + 0.5F);
        //Implement a protection to prevent the sound from stopping when it reaches volume 0.0F.
        if (livePosition.getY() < motionBlockingY) {
            finalVolume = heightMapCalculatedVolume;
        }

        //Check if the player is underwater then chop the noise volume in half(essentially muffling it)
        if (mc.world.getBlockState(livePosition).getBlock() == Blocks.WATER && mc.world.getBlockState(livePosition).getFluidState().getLevel() >= 6)
            finalVolume = finalVolume / 2;

        float clampedFinalVolume = MathHelper.clamp(finalVolume, 0.05F, 1.0F);
        this.volume = fade * clampedFinalVolume;
        if (this.volume == 0.0F) {
            this.finishPlaying();
        }
    }
}

package corgitaco.betterweather.audio;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.TickableSound;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.common.Tags;

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


        public static final ObjectOpenHashSet<ITag<Block>> NON_CEILING = Util.make(new ObjectOpenHashSet<>(), (set) -> {
            set.add(BlockTags.LEAVES);
            set.add(BlockTags.FENCE_GATES);
            set.add(BlockTags.FENCES);
            set.add(BlockTags.WALLS);

            // Forge tags
            set.add(Tags.Blocks.GLASS_PANES);
            set.add(Tags.Blocks.CHESTS);
            set.add(Tags.Blocks.FENCES);
            set.add(Tags.Blocks.FENCE_GATES);
        });

        private final BlockPos playerLivePos;
        private final Minecraft mc;
        private final float originalVolume;
        private float finalVolume = 1.0F;

        private IsInsideAudioHelper(BlockPos playerLivePos, Minecraft mc, float originalVolume) {
            this.playerLivePos = playerLivePos;
            this.mc = mc;
            this.originalVolume = originalVolume;

            BlockPos.Mutable mutable = new BlockPos.Mutable();

            int checkRange = 4;
            int checkXPosRange = 4;
            int checkZPosRange = 4;
            int checkXNegRange = 4;
            int checkZNegRange = 4;
            boolean foundSky;

            for (int x = -checkRange; x <= checkRange; x++) {
                for (int z = -checkRange; z <= checkRange; z++) {
                    mutable.setPos(playerLivePos).move(x, 0, z);
                    int heightMapY = mc.world.getHeight(Heightmap.Type.MOTION_BLOCKING, mutable.getX(), mutable.getZ());
                    foundSky = heightMapY < playerLivePos.getY();

                    if (!foundSky) {
                        this.finalVolume -= 0.03F;
                    } else {
                        this.finalVolume += 0.015F;
                    }
                }
            }
        }


        public float getFinalVolume() {
            return MathHelper.clamp(finalVolume * originalVolume, 0.01F, originalVolume);
        }

        private static boolean actsAsCeiling( final BlockState state) {
            // If it doesn't block movement it doesn't count as a ceiling.
            if (!state.getMaterial().blocksMovement())
                return false;

            // Test the block tags in our NON_CEILING set to see if any match
            final Block block = state.getBlock();
            for (final ITag<Block> tag : NON_CEILING) {
                if (tag.contains(block))
                    return false;
            }
            return true;
        }
    }
}

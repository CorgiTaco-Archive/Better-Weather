package corgitaco.betterweather.weather.event.client;

import corgitaco.betterweather.api.client.WeatherEventClient;
import corgitaco.betterweather.graphics.Graphics;
import corgitaco.betterweather.weather.event.client.settings.RainClientSettings;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CampfireBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.settings.ParticleStatus;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.FluidState;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;

import java.util.Random;
import java.util.function.Predicate;

public class RainClient extends WeatherEventClient<RainClientSettings> {

    protected final ResourceLocation rainTexture;
    protected final ResourceLocation snowTexture;
    private final float[] rainSizeX = new float[1024];
    private final float[] rainSizeZ = new float[1024];
    private int rainSoundTime;

    public RainClient(RainClientSettings clientSettings) {
        super(clientSettings);
        this.rainTexture = clientSettings.rainTexture;
        this.snowTexture = clientSettings.snowTexture;

        for (int i = 0; i < 32; ++i) {
            for (int j = 0; j < 32; ++j) {
                float f = (float) (j - 16);
                float f1 = (float) (i - 16);
                float f2 = MathHelper.sqrt(f * f + f1 * f1);
                this.rainSizeX[i << 5 | j] = -f1 / f2;
                this.rainSizeZ[i << 5 | j] = f / f2;
            }
        }
    }

    @Override
    public boolean renderWeatherShaders() {
        return false;
    }

    @Override
    public boolean renderWeatherLegacy(Minecraft mc, ClientWorld world, LightTexture lightTexture, int ticks, float partialTicks, double x, double y, double z, Predicate<Biome> biomePredicate) {
        renderVanillaWeather(mc, partialTicks, x, y, z, lightTexture, rainSizeX, rainSizeZ, this.rainTexture, this.snowTexture, ticks, biomePredicate);
        return true;
    }

    @Override
    public boolean weatherParticlesAndSound(ActiveRenderInfo renderInfo, Minecraft mc, float ticks, Predicate<Biome> validBiomes) {
        float particleStrength = mc.world.getRainStrength(1.0F) / (Minecraft.isFancyGraphicsEnabled() ? 1.0F : 2.0F);
        if (!(particleStrength <= 0.0F)) {
            Random random = new Random((long) ticks * 312987231L);
            IWorldReader worldReader = mc.world;
            BlockPos blockpos = new BlockPos(renderInfo.getProjectedView());
            BlockPos blockpos1 = null;
            int particleCount = (int)(100.0F * particleStrength * particleStrength) / (mc.gameSettings.particles == ParticleStatus.DECREASED ? 2 : 1);

            for(int particleCounter = 0; particleCounter < particleCount; ++particleCounter) {
                int randomAddX = random.nextInt(21) - 10;
                int randomAddZ = random.nextInt(21) - 10;
                BlockPos motionBlockingHeightMinus1 = worldReader.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos.add(randomAddX, 0, randomAddZ)).down();
                Biome biome = worldReader.getBiome(motionBlockingHeightMinus1);
                if (!validBiomes.test(biome)) {
                    continue;
                }

                if (motionBlockingHeightMinus1.getY() > 0 && motionBlockingHeightMinus1.getY() <= blockpos.getY() + 10 && motionBlockingHeightMinus1.getY() >= blockpos.getY() - 10 && biome.getPrecipitation() == Biome.RainType.RAIN && biome.getTemperature(motionBlockingHeightMinus1) >= 0.15F) {
                    blockpos1 = motionBlockingHeightMinus1;
                    if (mc.gameSettings.particles == ParticleStatus.MINIMAL) {
                        break;
                    }

                    double randDouble = random.nextDouble();
                    double randDouble2 = random.nextDouble();
                    BlockState blockstate = worldReader.getBlockState(motionBlockingHeightMinus1);
                    FluidState fluidstate = worldReader.getFluidState(motionBlockingHeightMinus1);
                    VoxelShape voxelshape = blockstate.getCollisionShapeUncached(worldReader, motionBlockingHeightMinus1);
                    double voxelShapeMax = voxelshape.max(Direction.Axis.Y, randDouble, randDouble2);
                    double fluidstateActualHeight = fluidstate.getActualHeight(worldReader, motionBlockingHeightMinus1);
                    double particleMaxAddedY = Math.max(voxelShapeMax, fluidstateActualHeight);
                    addParticlesToWorld(mc, motionBlockingHeightMinus1, randDouble, randDouble2, blockstate, fluidstate, particleMaxAddedY);
                }
            }

            if (blockpos1 != null && random.nextInt(3) < this.rainSoundTime++) {
                this.rainSoundTime = 0;
                if (blockpos1.getY() > blockpos.getY() + 1 && worldReader.getHeight(Heightmap.Type.MOTION_BLOCKING, blockpos).getY() > MathHelper.floor((float)blockpos.getY())) {
                    mc.world.playSound(blockpos1, SoundEvents.WEATHER_RAIN_ABOVE, SoundCategory.WEATHER, 0.1F, 0.5F, false);
                } else {
                    mc.world.playSound(blockpos1, SoundEvents.WEATHER_RAIN, SoundCategory.WEATHER, 0.2F, 1.0F, false);
                }
            }
        }
        return true;
    }

    protected void addParticlesToWorld(Minecraft mc, BlockPos motionBlockingHeightMinus1, double randDouble, double randDouble2, BlockState blockstate, FluidState fluidstate, double particleMaxAddedY) {
        IParticleData iparticledata = !fluidstate.isTagged(FluidTags.LAVA) && !blockstate.matchesBlock(Blocks.MAGMA_BLOCK) && !CampfireBlock.isLit(blockstate) ? ParticleTypes.RAIN : ParticleTypes.SMOKE;
        mc.world.addParticle(iparticledata, (double) motionBlockingHeightMinus1.getX() + randDouble, (double) motionBlockingHeightMinus1.getY() + particleMaxAddedY, (double) motionBlockingHeightMinus1.getZ() + randDouble2, 0.0D, 0.0D, 0.0D);
    }

    @Override
    public boolean drippingLeaves() {
        return true;
    }

    @Override
    public void clientTick(ClientWorld world, int tickSpeed, long worldTime, Minecraft mc, Predicate<Biome> biomePredicate) {

    }
}

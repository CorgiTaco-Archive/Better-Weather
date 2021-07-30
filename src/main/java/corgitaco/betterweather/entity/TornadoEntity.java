package corgitaco.betterweather.entity;

import corgitaco.betterweather.entity.tornado.NoiseWormPathGenerator;
import corgitaco.betterweather.util.noise.FastNoise;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TornadoEntity extends Entity {

    private final List<StateRotatable> capturedStates = new ArrayList<>();
    private int lifeSpan;
    private int nodeIDX;

    private float rotation;
    private float rotationSpeed = 0.2F;
    private double lerp;
    private NoiseWormPathGenerator pathGenerator;

    public TornadoEntity(World worldIn, double x, double y, double z) {
        super(BetterWeatherEntityTypes.TORNADO_ENTITY_TYPE, worldIn);
    }

    public TornadoEntity(EntityType<TornadoEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);

        lifeSpan = worldIn.rand.nextInt(10000) + 1000;
        this.ignoreFrustumCheck = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (pathGenerator == null) {
            this.pathGenerator = new NoiseWormPathGenerator(createNoise(new Random((long) (this.getPosX() + this.getPosY() + this.getPosZ())).nextInt()), this.getPosition(), (isInvalid) -> false, 5000);
        }

        rotationPitch += 6.0f;

        if (rotationPitch > 360.0) {
            rotationPitch = 0;
        }

        NoiseWormPathGenerator.Node node = this.pathGenerator.getNodes().get(nodeIDX);

        BlockPos.Mutable pos = node.getPos();
        NoiseWormPathGenerator.Node targetNode = this.pathGenerator.getNodes().get(nodeIDX + 1);

        BlockPos.Mutable targetPos = targetNode.getPos();

        double x = MathHelper.lerp(MathHelper.clamp(this.lerp, 0.0, 1.0), pos.getX(), targetPos.getX());
        double z = MathHelper.lerp(MathHelper.clamp(this.lerp, 0.0, 1.0), pos.getZ(), targetPos.getZ());
        this.forceSetPosition(x, world.getHeight(Heightmap.Type.MOTION_BLOCKING, (int) Math.round(x), (int) Math.round(z)), z);

        if ((int) this.lerp == 1) {
            nodeIDX++;
            this.lerp = 0.0;
        } else {
            this.lerp += 0.1;
        }


        if (nodeIDX == this.pathGenerator.getNodes().size() - 1) {
            this.remove();
        }
    }

    @Override
    protected void registerData() {
    }

    @Override
    public void read(CompoundNBT compound) {
        super.read(compound);
        this.nodeIDX = compound.getInt("nodeidx");
        this.lerp = compound.getDouble("lerp");
        this.pathGenerator = NoiseWormPathGenerator.read((CompoundNBT) compound.get("pathGenerator"));
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {

    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

    }

    @Override
    public boolean writeUnlessPassenger(CompoundNBT compound) {
        compound.putInt("nodeidx", this.nodeIDX);
        compound.putDouble("lerp", this.lerp);
        compound.put("pathGenerator", this.pathGenerator.write());
        return super.writeUnlessPassenger(compound);
    }

    // Forge -----------------------------------
    @Override
    public void deserializeNBT(CompoundNBT nbt) {
        readAdditional(nbt);
        super.deserializeNBT(nbt);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT compoundNBT = super.serializeNBT();
        writeAdditional(compoundNBT);
        return compoundNBT;
    }
    // -----------------------------------------


    public List<StateRotatable> getCapturedStates() {
        return capturedStates;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = 64.0D * getRenderDistanceWeight();
        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return super.getRenderBoundingBox();
    }

    @Override
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }



    public float getRotation() {
        return rotation;
    }


    private static FastNoise createNoise(int seed) {
        FastNoise noise = new FastNoise(seed);
        noise.SetDomainWarpType(FastNoise.DomainWarpType.BasicGrid);
        noise.SetNoiseType(FastNoise.NoiseType.OpenSimplex2);
        return noise;
    }

    public static class StateRotatable {

        private final BlockState state;
        private float xOffset;
        private float yOffset;
        private float zOffset;
        private float rotationDegrees;

        public StateRotatable(BlockState state, float xOffset, float yOffset, float zOffset, float rotationDegrees) {
            this.state = state;
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.rotationDegrees = rotationDegrees;
        }


        public BlockState getState() {
            return state;
        }

        public float getXOffset() {
            return xOffset;
        }

        public void setXDegrees(float xDegrees) {
            this.xOffset = xDegrees;
        }

        public float getYOffset() {
            return yOffset;
        }

        public void setYDegrees(float yDegrees) {
            this.yOffset = yDegrees;
        }

        public float getZOffset() {
            return zOffset;
        }

        public void setZDegrees(float zDegrees) {
            this.zOffset = zDegrees;
        }

        public float getRotationDegrees() {
            return rotationDegrees;
        }

        public void setRotationDegrees(float rotationDegrees) {
            this.rotationDegrees = rotationDegrees;
        }
    }
}

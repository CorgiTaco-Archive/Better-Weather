package corgitaco.betterweather.entity;

import corgitaco.betterweather.util.MutableVec3d;
import net.minecraft.block.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.ArrayList;
import java.util.List;

public class TornadoEntity extends Entity {

    private final List<StateRotatable> capturedStates = new ArrayList<>();
    private final int range;
    private int lifeSpan;

    private float rotation;
    private float rotationSpeed = 0.2F;

    public TornadoEntity(EntityType<?> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        range = worldIn.rand.nextInt(50) + 150;
        lifeSpan = worldIn.rand.nextInt(10000) + 1000;
        this.ignoreFrustumCheck = true;
    }

    @Override
    public void tick() {
        super.tick();

        rotation += 6.0f;

        if (rotation > 360.0) {
            rotation = 0;
        }

        BlockPos.Mutable mutable = new BlockPos.Mutable().setPos(this.getPosition());

        for (int i = 0; i < capturedStates.size(); i++) {
            StateRotatable capturedState = capturedStates.get(i);
            double randXZDegrees = MathHelper.clampedLerp(0.4, 0.8, world.rand.nextDouble());



            capturedState.worldPos.y += 0.01;

            capturedState.setRotationDegrees(capturedState.rotationDegrees > 360.0 ? 0 : (capturedState.rotationDegrees += 5.0F));


        }


        int limit = 5;
        if (capturedStates.size() > limit) {
            return;
        }

        for (int x = -range; x < range; x++) {
            for (int z = -range; z < range; z++) {
                mutable.setAndOffset(this.getPosition(), x, 0, z);
                int height = world.getHeight(Heightmap.Type.MOTION_BLOCKING, mutable.getX(), mutable.getZ()) - 1;
                mutable.setY(height);

                BlockState state = world.getBlockState(mutable);
                Block block = state.getBlock();

                if ((block instanceof LeavesBlock || block instanceof SnowyDirtBlock) && rand.nextInt(1000) == 0) {
                    if (capturedStates.size() > limit) {
                        return;
                    }

                    capturedStates.add(new StateRotatable(state, new MutableVec3d(mutable.getX(), mutable.getY(), mutable.getZ()), 0));
                    world.setBlockState(mutable, Blocks.AIR.getDefaultState());
                }
            }
        }
    }

    public static double angleBetween2Points(double endX, double endZ, double startX, double startZ) {
        return Math.atan2(endX - startX, endZ - startZ);
    }


    @Override
    protected void registerData() {
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
//        for (INBT states : compound.getList("states", 10)) {
//            capturedStates.add(NBTUtil.readBlockState((CompoundNBT) states));
//        }
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {
        ListNBT states = new ListNBT();
//        for (BlockState capturedState : capturedStates) {
//           states.add(NBTUtil.writeBlockState(capturedState));
//        }
//        compound.put("states", states);
    }

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

    public static class StateRotatable {

        private final BlockState state;
        private final MutableVec3d worldPos;
        private float rotationDegrees;

        public StateRotatable(BlockState state, MutableVec3d worldPos, float rotationDegrees) {
            this.state = state;
            this.worldPos = worldPos;
            this.rotationDegrees = rotationDegrees;
        }


        public MutableVec3d getWorldPos() {
            return worldPos;
        }

        public double getXDistanceFrom(double x) {
           return worldPos.x - x;
        }

        public double getYDistanceFrom(double y) {
            return worldPos.y - y;
        }

        public double getZDistanceFrom(double z) {
            return worldPos.z - z;
        }

        public BlockState getState() {
            return state;
        }

        public float getRotationDegrees() {
            return rotationDegrees;
        }

        public void setRotationDegrees(float rotationDegrees) {
            this.rotationDegrees = rotationDegrees;
        }
    }
}

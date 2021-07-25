package corgitaco.betterweather.entity;

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

            if (capturedState.xOffset < 20) {
                capturedState.setXDegrees(capturedState.xOffset += randXZDegrees);
            }

            double randYDegrees = MathHelper.clampedLerp(0.05, 1.2, world.rand.nextDouble());

            if (capturedState.yOffset > 10) {

                double x = (double) this.getPosition().getX() + capturedState.xOffset;
                double y = (double) this.getPosition().getY() + capturedState.yOffset;
                double z = (double) this.getPosition().getZ() + capturedState.zOffset;
                if (!world.isRemote) {
                    FallingBlockEntity fallingblockentity = new FallingBlockEntity(world, x, y, z, Blocks.DIAMOND_BLOCK.getDefaultState());
                    world.addEntity(fallingblockentity);

                    world.addEntity(new FireworkRocketEntity(world, x, y, z, new ItemStack(Items.FIREWORK_ROCKET)));
                }
                capturedStates.remove(i);
                continue;
            }


            capturedState.setYDegrees(capturedState.yOffset += randYDegrees);
            if (capturedState.zOffset < 20) {

                capturedState.setZDegrees(capturedState.zOffset += randXZDegrees);
            }

            capturedState.setRotationDegrees(capturedState.rotationDegrees > 360.0 ? 0 : (capturedState.rotationDegrees += 6.0F));

        }


        int limit = 500;
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

                    capturedStates.add(new StateRotatable(state, rand.nextFloat(), rand.nextFloat(), rand.nextFloat(), (float) MathHelper.clampedLerp(0.0, 360.0F, rand.nextFloat())));
                    world.setBlockState(mutable, Blocks.AIR.getDefaultState());
                }
            }
        }
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

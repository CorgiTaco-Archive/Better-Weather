package corgitaco.betterweather.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.monster.BlazeEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class TornadoEntity extends BlazeEntity {
    public TornadoEntity(EntityType<? extends BlazeEntity> type, World world) {
        super(type, world);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }

    //func_233666_p_ ---> registerAttributes()
    public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
        return MonsterEntity.func_234295_eP_().createMutableAttribute(Attributes.ATTACK_DAMAGE, 6.0D).createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.23F).createMutableAttribute(Attributes.FOLLOW_RANGE, 48.0D);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.WEATHER;
    }

    @Override
    public float getBrightness() {
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable(this.getPosX(), 0.0D, this.getPosZ());
        if (this.world.isBlockLoaded(blockpos$mutable)) {
            blockpos$mutable.setY(MathHelper.floor(this.getPosYEye()));
            return this.world.getBrightness(blockpos$mutable);
        } else {
            return 0.0F;
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 0.0F));
        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setCallsForHelp());
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
    }

    @Override
    public boolean isWaterSensitive() {
        return false;
    }

    @Override
    protected void registerData() {
        super.registerData();
    }


}

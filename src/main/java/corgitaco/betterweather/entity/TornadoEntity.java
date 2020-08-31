//package corgitaco.betterweather.entity;
//
//import net.minecraft.entity.AgeableEntity;
//import net.minecraft.entity.EntityType;
//import net.minecraft.entity.ai.attributes.AttributeModifierMap;
//import net.minecraft.entity.ai.attributes.Attributes;
//import net.minecraft.entity.ai.goal.*;
//import net.minecraft.entity.monster.MonsterEntity;
//import net.minecraft.entity.passive.AnimalEntity;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.util.SoundCategory;
//import net.minecraft.util.SoundEvent;
//import net.minecraft.world.World;
//import net.minecraft.world.server.ServerLevel;
//
//import javax.annotation.Nullable;
//
//public class TornadoEntity extends AnimalEntity {
//
//    public TornadoEntity(EntityType<? extends AnimalEntity> type, Level worldIn) {
//        super(type, worldIn);
//    }
//
//    @Override
//    protected SoundEvent getAmbientSound() {
//        return super.getAmbientSound();
//    }
//
//    //func_233666_p_ ---> registerAttributes()
//    public static AttributeModifierMap.MutableAttribute setCustomAttributes() {
//        return MonsterEntity.func_234295_eP_().createMutableAttribute(Attributes.ATTACK_DAMAGE, 6.0D).createMutableAttribute(Attributes.MOVEMENT_SPEED, (double)0.23F).createMutableAttribute(Attributes.FOLLOW_RANGE, 48.0D);
//    }
//
//    @Override
//    public SoundCategory getSoundCategory() {
//        return SoundCategory.WEATHER;
//    }
//
//    @Override
//    public float getBrightness() {
//        return super.getBrightness();
//    }
//
//    @Override
//    protected void registerGoals() {
//        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1.0D));
//        this.goalSelector.addGoal(7, new WaterAvoidingRandomWalkingGoal(this, 1.0D, 0.0F));
//        this.goalSelector.addGoal(8, new LookAtGoal(this, PlayerEntity.class, 8.0F));
//        this.goalSelector.addGoal(8, new LookRandomlyGoal(this));
//        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setCallsForHelp());
//        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, PlayerEntity.class, true));
//    }
//
//    @Override
//    public boolean isWaterSensitive() {
//        return false;
//    }
//
//    @Nullable
//    @Override
//    public AgeableEntity func_241840_a(ServerLevel p_241840_1_, AgeableEntity p_241840_2_) {
//        return null;
//    }
//
//    @Override
//    protected void registerData() {
//        super.registerData();
//    }
//
//
//}

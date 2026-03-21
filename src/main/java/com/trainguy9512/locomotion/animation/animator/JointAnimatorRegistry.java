package com.trainguy9512.locomotion.animation.animator;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.animation.animator.block_entity.BlockEntityJointAnimator;
import com.trainguy9512.locomotion.animation.animator.entity.EntityJointAnimator;
import com.trainguy9512.locomotion.animation.animator.entity.LivingEntityJointAnimator;
import net.minecraft.client.player.LocalPlayer;
//? if >= 1.21.9 {
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
//?}
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;

public class JointAnimatorRegistry {

    //? if >= 1.21.9 {
    private static final HashMap<EntityType<?>, EntityJointAnimator<?, ?>> THIRD_PERSON_ENTITY_JOINT_ANIMATORS = Maps.newHashMap();
    //?} else {
    /*private static final HashMap<EntityType<?>, EntityJointAnimator<?>> THIRD_PERSON_ENTITY_JOINT_ANIMATORS = Maps.newHashMap();*/
    //?}
    private static final HashMap<BlockEntityType<?>, BlockEntityJointAnimator<?>> BLOCK_ENTITY_JOINT_ANIMATORS = Maps.newHashMap();

    //? if >= 1.21.9 {
    private static LivingEntityJointAnimator<LocalPlayer, AvatarRenderState> FIRST_PERSON_PLAYER_JOINT_ANIMATOR = null;
    //?} else {
    /*private static LivingEntityJointAnimator<LocalPlayer> FIRST_PERSON_PLAYER_JOINT_ANIMATOR = null;*/
    //?}

    private JointAnimatorRegistry(){

    }

    /**
     * Registers a joint animator for use on third person living entities.
     * @param type                      Type of entity associated with the living entity
     * @param jointAnimator             Newly constructed entity joint animator object
     */
    //? if >= 1.21.9 {
    public static <T extends Entity> void registerEntityJointAnimator(EntityType<T> type, EntityJointAnimator<T, ?> jointAnimator){
        THIRD_PERSON_ENTITY_JOINT_ANIMATORS.put(type, jointAnimator);
    }
    //?} else {
    /*public static <T extends Entity> void registerEntityJointAnimator(EntityType<T> type, EntityJointAnimator<T> jointAnimator){
        THIRD_PERSON_ENTITY_JOINT_ANIMATORS.put(type, jointAnimator);
    }*/
    //?}

    /**
     * Registers a joint animator for use on block entities.
     * @param type                      Type of block entity
     * @param jointAnimator             Newly constructed block entity joint animator
     */
    public static <T extends BlockEntity> void registerBlockEntityJointAnimator(BlockEntityType<T> type, BlockEntityJointAnimator<T> jointAnimator){
        BLOCK_ENTITY_JOINT_ANIMATORS.put(type, jointAnimator);
    }

    //? if >= 1.21.9 {
    public static void registerFirstPersonPlayerJointAnimator(LivingEntityJointAnimator<LocalPlayer, AvatarRenderState> firstPersonPlayerJointAnimator){
        FIRST_PERSON_PLAYER_JOINT_ANIMATOR = firstPersonPlayerJointAnimator;
    }
    //?} else {
    /*public static void registerFirstPersonPlayerJointAnimator(LivingEntityJointAnimator<LocalPlayer> firstPersonPlayerJointAnimator){
        FIRST_PERSON_PLAYER_JOINT_ANIMATOR = firstPersonPlayerJointAnimator;
    }*/
    //?}


    @SuppressWarnings("unchecked")
    //? if >= 1.21.9 {
    public static <T extends Entity> Optional<EntityJointAnimator<T, ?>> getThirdPersonJointAnimator(T entity){
        return Optional.ofNullable((EntityJointAnimator<T, ?>) THIRD_PERSON_ENTITY_JOINT_ANIMATORS.get(entity.getType()));
    }
    //?} else {
    /*public static <T extends Entity> Optional<EntityJointAnimator<T>> getThirdPersonJointAnimator(T entity){
        return Optional.ofNullable((EntityJointAnimator<T>) THIRD_PERSON_ENTITY_JOINT_ANIMATORS.get(entity.getType()));
    }*/
    //?}

    @SuppressWarnings("unchecked")
    public static <T extends BlockEntity> Optional<BlockEntityJointAnimator<T>> getBlockEntityJointAnimator(BlockEntityType<T> type){
        return Optional.ofNullable((BlockEntityJointAnimator<T>) BLOCK_ENTITY_JOINT_ANIMATORS.get(type));
    }

    //? if >= 1.21.9 {
    public static Optional<LivingEntityJointAnimator<LocalPlayer, AvatarRenderState>> getFirstPersonPlayerJointAnimator(){
        return Optional.ofNullable(FIRST_PERSON_PLAYER_JOINT_ANIMATOR);
    }
    //?} else {
    /*public static Optional<LivingEntityJointAnimator<LocalPlayer>> getFirstPersonPlayerJointAnimator(){
        return Optional.ofNullable(FIRST_PERSON_PLAYER_JOINT_ANIMATOR);
    }*/
    //?}

    public static Set<BlockEntityType<?>> getRegisteredBlockEntities() {
        return BLOCK_ENTITY_JOINT_ANIMATORS.keySet();
    }
}

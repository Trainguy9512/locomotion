package com.trainguy9512.locomotion.animation.animator;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.animation.animator.entity.EntityJointAnimator;
import com.trainguy9512.locomotion.animation.animator.entity.LivingEntityJointAnimator;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashMap;
import java.util.Optional;

public class JointAnimatorRegistry {

    private static final HashMap<EntityType<?>, EntityJointAnimator<?, ?>> THIRD_PERSON_ENTITY_JOINT_ANIMATORS = Maps.newHashMap();
    private static LivingEntityJointAnimator<LocalPlayer, AvatarRenderState> FIRST_PERSON_PLAYER_JOINT_ANIMATOR = null;

    private JointAnimatorRegistry(){

    }

    /**
     * Registers a joint animator for use on third person living entities.
     * @param entityType                Type of entity associated with the living entity
     * @param entityJointAnimator       Newly constructed entity joint animator object
     */
    public static <T extends Entity> void registerEntityJointAnimator(EntityType<T> entityType, EntityJointAnimator<T, ?> entityJointAnimator){
        THIRD_PERSON_ENTITY_JOINT_ANIMATORS.put(entityType, entityJointAnimator);
    }


    public static void registerFirstPersonPlayerJointAnimator(LivingEntityJointAnimator<LocalPlayer, AvatarRenderState> firstPersonPlayerJointAnimator){
        FIRST_PERSON_PLAYER_JOINT_ANIMATOR = firstPersonPlayerJointAnimator;
    }

    /**
     * Returns the entity joint animator for the provided entity type. If the entity type does not have a joint animator registered, null is returned.
     * @param entity                    Entity
     * @return                          Entity joint animator
     */
    @SuppressWarnings("unchecked")
    public static <T extends Entity> Optional<EntityJointAnimator<T, ?>> getThirdPersonJointAnimator(T entity){
        return Optional.ofNullable((EntityJointAnimator<T, ?>) THIRD_PERSON_ENTITY_JOINT_ANIMATORS.get(entity.getType()));
    }

    /**
     * Returns the first person player joint animator, if it has been registered. If not, it returns null.
     */
    public static Optional<LivingEntityJointAnimator<LocalPlayer, AvatarRenderState>> getFirstPersonPlayerJointAnimator(){
        return Optional.ofNullable(FIRST_PERSON_PLAYER_JOINT_ANIMATOR);
    }
}

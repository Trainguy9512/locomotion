package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.entity.LivingEntityJointAnimator;
import com.trainguy9512.locomotion.animation.data.*;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Set;

public class FirstPersonJointAnimator implements LivingEntityJointAnimator<LocalPlayer, AvatarRenderState> {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/FPJointAnimator");

    public static final String ROOT_JOINT = "root_jnt";
    public static final String CAMERA_JOINT = "camera_jnt";
    public static final String ARM_BUFFER_JOINT = "arm_buffer_jnt";
    public static final String RIGHT_ARM_BUFFER_JOINT = "arm_R_buffer_jnt";
    public static final String RIGHT_ARM_JOINT = "arm_R_jnt";
    public static final String RIGHT_HAND_JOINT = "hand_R_jnt";
    public static final String RIGHT_ITEM_JOINT = "item_R_jnt";
    public static final String LEFT_ARM_BUFFER_JOINT = "arm_L_buffer_jnt";
    public static final String LEFT_ARM_JOINT = "arm_L_jnt";
    public static final String LEFT_HAND_JOINT = "hand_L_jnt";
    public static final String LEFT_ITEM_JOINT = "item_L_jnt";

    public static final String IS_USING_PROPERTY_ATTRIBUTE = "is_using_property";
    public static final String USE_DURATION_PROPERTY_ATTRIBUTE = "use_duration_property";
    public static final String CROSSBOW_PULL_PROPERTY_ATTRIBUTE = "crossbow_pull_property";

    public static final Set<String> RIGHT_SIDE_JOINTS = Set.of(
            RIGHT_ARM_BUFFER_JOINT,
            RIGHT_ARM_JOINT,
            RIGHT_HAND_JOINT,
            RIGHT_ITEM_JOINT
    );

    public static final Set<String> LEFT_SIDE_JOINTS = Set.of(
            LEFT_ARM_BUFFER_JOINT,
            LEFT_ARM_JOINT,
            LEFT_HAND_JOINT,
            LEFT_ITEM_JOINT
    );

    public static final BlendMask LEFT_SIDE_MASK = BlendMask.builder()
            .defineForMultipleJoints(LEFT_SIDE_JOINTS, 1)
            .build();

    public static final BlendMask RIGHT_SIDE_MASK = BlendMask.builder()
            .defineForMultipleJoints(RIGHT_SIDE_JOINTS, 1)
            .build();

    public static final BlendMask CAMERA_MASK = BlendMask.builder()
            .defineForJoint(CAMERA_JOINT, 1f)
            .build();

    public static final BlendMask ARMS_ONLY_MASK = BlendMask.builder()
            .defineForJoint(RIGHT_ARM_BUFFER_JOINT, 1f)
            .defineForJoint(RIGHT_ARM_JOINT, 1f)
            .defineForJoint(LEFT_ARM_BUFFER_JOINT, 1f)
            .defineForJoint(LEFT_ARM_JOINT, 1f)
            .build();

    @Override
    public void postProcessModelParts(EntityModel<AvatarRenderState> entityModel, AvatarRenderState entityRenderState) {
    }

    @Override
    public Identifier getJointSkeleton() {
        return Identifier.fromNamespaceAndPath(LocomotionMain.MOD_ID, "skeletons/entity/player/first_person.json");
    }

    @Override
    public PoseCalculationFrequency getPoseCalulationFrequency() {
        return PoseCalculationFrequency.CALCULATE_EVERY_FRAME;
    }

    public static final String MAIN_HAND_POSE_CACHE = "main_hand_pose";
    public static final String OFF_HAND_POSE_CACHE = "off_hand_pose";

    @Override
    public PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {

        PoseFunction<LocalSpacePose> mainHandPose = FirstPersonHandPoseSwitching.constructPoseFunction(cachedPoseContainer, InteractionHand.MAIN_HAND);
        PoseFunction<LocalSpacePose> offHandPose = FirstPersonHandPoseSwitching.constructPoseFunction(cachedPoseContainer, InteractionHand.OFF_HAND);
        cachedPoseContainer.register(MAIN_HAND_POSE_CACHE, mainHandPose, true);
        cachedPoseContainer.register(OFF_HAND_POSE_CACHE, MirrorFunction.of(offHandPose), true);

        // Getting the additive camera pose from the off hand
        PoseFunction<LocalSpacePose> composedCameraPoseFunction = ApplyAdditiveFunction.of(
                cachedPoseContainer.getOrThrow(MAIN_HAND_POSE_CACHE),
                MakeDynamicAdditiveFunction.of(
                        cachedPoseContainer.getOrThrow(OFF_HAND_POSE_CACHE),
                        EmptyPoseFunction.of(false)
                ));

        PoseFunction<LocalSpacePose> pose = BlendPosesFunction.builder(cachedPoseContainer.getOrThrow(MAIN_HAND_POSE_CACHE))
                .addBlendInput(cachedPoseContainer.getOrThrow(OFF_HAND_POSE_CACHE), evaluationState -> 1f, LEFT_SIDE_MASK)
                .addBlendInput(composedCameraPoseFunction, evaluationState -> 1f, CAMERA_MASK)
                .build();

        // Offsetting the hands from the config on the X axis prior to the two handed animations.
        pose = FirstPersonArmOffset.constructWithArmXOffset(pose);

        // Blending in the two handed animations.
        pose = FirstPersonTwoHandedActions.constructPoseFunction(pose, cachedPoseContainer);

        // Offsetting the hands from the config on the Y and Z axis after to the two handed animations.
        pose = FirstPersonArmOffset.constructWithArmYZOffset(pose);

        // Offsetting the hands based on the shield state machine. Also offsetting the hands for attack animations.
        pose = FirstPersonShield.constructWithHandsOffsetByShield(cachedPoseContainer, pose);
        pose = FirstPersonAttackAnimations.constructWithOffsetOffHandAttack(pose);

        // Punch mining animation
        pose = FirstPersonMining.constructWithPunchMiningPoseFunction(pose);

        // Adding in the movement animations.
        pose = FirstPersonMovement.constructWithMovementAnimations(pose, cachedPoseContainer);

        // Master camera shake intensity
        pose = BlendPosesFunction.builder(pose)
                .addBlendInput(
                        EmptyPoseFunction.of(),
                        functionEvaluationState -> 1 - LocomotionMain.CONFIG.data().firstPersonPlayer.cameraShakeMasterIntensity,
                        FirstPersonJointAnimator.CAMERA_MASK
                )
                .build();

        // Mirroring the pose function if left handed
        pose = MirrorFunction.of(pose, context -> Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT);


        pose = JointTransformerFunction.localOrParentSpaceBuilder(pose, ARM_BUFFER_JOINT)
                        .setTranslation(
                                context -> context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.MOVEMENT_DIRECTION_OFFSET, context.partialTicks()).mul(1.5f, new Vector3f()),
                                JointChannel.TransformType.ADD,
                                JointChannel.TransformSpace.COMPONENT
                        )
                        .setRotationEuler(
                                context -> {
                                    Vector3f main_rotation = context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.CAMERA_ROTATION_DAMPING, context.partialTicks());
                                    float z_rotation = context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.CAMERA_Z_ROTATION_DAMPING, context.partialTicks());
                                    return new Vector3f(main_rotation.x, main_rotation.y, z_rotation).mul(-0.15f, -0.15f, -0.08f, new Vector3f());
                                },
                                JointChannel.TransformType.ADD,
                                JointChannel.TransformSpace.COMPONENT
                        )
                        .setWeight(interpolationContext -> LocomotionMain.CONFIG.data().firstPersonPlayer.enableCameraRotationDamping ? 1f : 0f)
                        .build();

        // Scaling the whole arms based on whether a spyglass is being used or not.
        pose = FirstPersonSpyglass.getHiddenArmsSpyglassPose(pose);


        return pose;
    }

    @Override
    public void extractAnimationData(LocalPlayer player, OnTickDriverContainer driverContainer, MontageManager montageManager){

        this.extractMovementPoseData(player, driverContainer);
        this.extractItemData(player, driverContainer);

        //? if >= 1.21.5 {
        driverContainer.getDriver(FirstPersonDrivers.HOTBAR_SLOT).setValue(player.getInventory().getSelectedSlot());
        //?} else {
        /*driverContainer.getDriver(HOTBAR_SLOT).setValue(dataReference.getInventory().selected);*/
        //?}

        //? if >= 1.21.11 {
        FirstPersonSpear.extractSpearData(player, driverContainer, montageManager);
        //? }

        this.extractAttackConditionData(player, driverContainer);
        this.handleMontagesFromTriggerDrivers(player, driverContainer, montageManager);
        this.extractInteractionHandData(player, driverContainer, montageManager);
        this.extractDampedCameraData(player, driverContainer, montageManager);
    }

    public void extractMovementPoseData(LocalPlayer player, OnTickDriverContainer driverContainer) {
        driverContainer.getDriver(FirstPersonDrivers.MODIFIED_WALK_SPEED).setValue(player.walkAnimation.speed());
        driverContainer.getDriver(FirstPersonDrivers.HORIZONTAL_MOVEMENT_SPEED).setValue(new Vector3f((float) (player.getX() - player.xo), 0.0f, (float) (player.getZ() - player.zo)).length());
        driverContainer.getDriver(FirstPersonDrivers.VERTICAL_MOVEMENT_SPEED).setValue((float) (player.getY() - player.yo));

        driverContainer.getDriver(FirstPersonDrivers.IS_IN_RIPTIDE).setValue(player.isAutoSpinAttack());
        driverContainer.getDriver(FirstPersonDrivers.IS_MOVING).setValue(player.input.keyPresses.forward() || player.input.keyPresses.backward() || player.input.keyPresses.left() || player.input.keyPresses.right());
        driverContainer.getDriver(FirstPersonDrivers.IS_SPRINTING).setValue(player.isSprinting());
        driverContainer.getDriver(FirstPersonDrivers.IS_ON_GROUND).setValue(player.onGround());
        driverContainer.getDriver(FirstPersonDrivers.IS_JUMPING).setValue(player.input.keyPresses.jump());
        driverContainer.getDriver(FirstPersonDrivers.IS_CROUCHING).setValue(player.isCrouching());
        driverContainer.getDriver(FirstPersonDrivers.IS_UNDERWATER).setValue(player.isUnderWater() || (player.isInWater() && !player.onGround()));
        driverContainer.getDriver(FirstPersonDrivers.IS_PASSENGER).setValue(player.isPassenger());
        driverContainer.getDriver(FirstPersonDrivers.HAS_SCREEN_OPEN).setValue(Minecraft.getInstance().screen != null);

        boolean isSwimmingUnderwater = player.getPose() == Pose.SWIMMING
                && player.isInWater()
                && !driverContainer.getDriverValue(FirstPersonDrivers.IS_USING_MAIN_HAND_ITEM)
                && !driverContainer.getDriverValue(FirstPersonDrivers.IS_USING_OFF_HAND_ITEM)
                && !player.isAutoSpinAttack();
        driverContainer.getDriver(FirstPersonDrivers.IS_SWIMMING_UNDERWATER).setValue(isSwimmingUnderwater);
    }

    public void extractItemData(LocalPlayer player, OnTickDriverContainer driverContainer) {
        for (InteractionHand hand : InteractionHand.values()) {
            VariableDriver<ItemStack> itemDriver = driverContainer.getDriver(FirstPersonDrivers.getItemDriver(hand));
            VariableDriver<ItemStack> itemCopyReferenceDriver = driverContainer.getDriver(FirstPersonDrivers.getItemCopyReferenceDriver(hand));
            ItemStack itemInHand = player.getItemInHand(hand);
            itemDriver.setValue(itemInHand);
            itemCopyReferenceDriver.setValue(itemInHand.copy());
        }
    }

    public void handleMontagesFromTriggerDrivers(LocalPlayer player, OnTickDriverContainer driverContainer, MontageManager montageManager) {
        driverContainer.getDriver(FirstPersonDrivers.HAS_DROPPED_ITEM).runAndConsumeIfTriggered(() -> {
            montageManager.playMontage(FirstPersonMontages.USE_MAIN_HAND_MONTAGE);
        });
        driverContainer.getDriver(FirstPersonDrivers.HAS_ATTACKED).runAndConsumeIfTriggered(() -> {
            FirstPersonAttackAnimations.tryPlayingAttackAnimation(driverContainer, montageManager);
        });
        if (driverContainer.getDriver(FirstPersonDrivers.IS_MINING).getCurrentValue()) {
            montageManager.interruptMontagesInSlot(FirstPersonMontages.MAIN_HAND_ATTACK_SLOT, Transition.builder(TimeSpan.ofTicks(2)).build());
        }
        driverContainer.getDriver(FirstPersonDrivers.HAS_BLOCKED_ATTACK).runAndConsumeIfTriggered(() -> montageManager.playMontage(FirstPersonMontages.SHIELD_BLOCK_IMPACT_MONTAGE));
        driverContainer.getDriver(FirstPersonDrivers.LAST_USED_SWING_TIME).setValue(player.swingTime);


        // Consuming the item swap trigger driver
        driverContainer.getDriver(FirstPersonDrivers.HAS_SWAPPED_ITEMS).runAndConsumeIfTriggered(() -> {});


    }

    public void extractAttackConditionData(LocalPlayer player, OnTickDriverContainer driverContainer) {
        boolean meetsCriticalAttackConditions = player.fallDistance > 0.0
                && !player.onGround()
                && !player.onClimbable()
                && !player.isInWater()
                && !player.isMobilityRestricted()
                && !player.isPassenger()
                && !player.isSprinting();

        boolean meetsSprintAttackConditions = driverContainer.getDriver(FirstPersonDrivers.IS_SPRINTING).getPreviousValue();
        if (meetsCriticalAttackConditions) {
            meetsSprintAttackConditions = false;
        }
        driverContainer.getDriver(FirstPersonDrivers.MEETS_CRITICAL_ATTACK_CONDITIONS).setValue(meetsCriticalAttackConditions);
        driverContainer.getDriver(FirstPersonDrivers.MEETS_SPRINT_ATTACK_CONDITIONS).setValue(meetsSprintAttackConditions);
    }

    public void extractInteractionHandData(LocalPlayer dataReference, OnTickDriverContainer driverContainer, MontageManager montageManager) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemInHand = driverContainer.getDriverValue(FirstPersonDrivers.getItemDriver(hand));
            ItemStack renderedItemInHand = driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));

            FirstPersonUseAnimations.playUseAnimationIfTriggered(driverContainer, montageManager, hand);
            FirstPersonItemUpdateAnimations.testForAndPlayItemUpdateAnimations(driverContainer, montageManager, hand);

//            if (itemInHand.getUseAnimation() == ItemUseAnimation.CROSSBOW && renderedItemInHand.getUseAnimation() == ItemUseAnimation.CROSSBOW) {
//                if (itemInHand.has(DataComponents.CHARGED_PROJECTILES) && renderedItemInHand.has(DataComponents.CHARGED_PROJECTILES)) {
//                    if (itemInHand.get(DataComponents.CHARGED_PROJECTILES).isEmpty() && !renderedItemInHand.get(DataComponents.CHARGED_PROJECTILES).isEmpty()) {
//                        if (driverContainer.getDriver(FirstPersonDrivers.getHasInteractedWithDriver(hand)).hasBeenTriggered()) {
//                            montageManager.playMontage(FirstPersonMontages.getCrossbowFireMontage(hand));
//                            FirstPersonDrivers.updateRenderedItem(driverContainer, hand);
//                        }
//                    }
//                }
//            }
//            driverContainer.getDriver(FirstPersonDrivers.getHasInteractedWithDriver(hand)).runIfTriggered(() -> FirstPersonMontages.playUseMontage(driverContainer, montageManager, hand));


            driverContainer.getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).setValue(false);
            if (dataReference.isUsingItem() && dataReference.getUsedItemHand() == hand) {
                driverContainer.getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).setValue(true);
                montageManager.interruptMontagesInSlot(FirstPersonMontages.getAttackSlot(hand), Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.SINE_IN_OUT).build());
                driverContainer.getDriver(FirstPersonDrivers.LAST_USED_HAND).setValue(hand);
                driverContainer.getDriver(FirstPersonDrivers.PROJECTILE_ITEM).setValue(dataReference.getProjectile(itemInHand));
                if (itemInHand.getUseAnimation() == ItemUseAnimation.CROSSBOW) {
                    float chargeTime = EnchantmentHelper.modifyCrossbowChargingTime(itemInHand, dataReference, 1.25f);
                    float chargeSpeedMultiplier = 1.25f / chargeTime;
                    driverContainer.getDriver(FirstPersonDrivers.CROSSBOW_RELOAD_SPEED).setValue(chargeSpeedMultiplier);
                }
            }

            // Is item on cooldown
            ItemStack renderedItem = driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
            boolean isItemOnCooldown = dataReference.getCooldowns().isOnCooldown(renderedItem);
            driverContainer.getDriver(FirstPersonDrivers.getItemOnCooldownDriver(hand)).setValue(isItemOnCooldown);

        }
    }

//    public void extractHandSwapData(LocalPlayer dataReference, OnTickDriverContainer driverContainer, MontageManager montageManager) {
//        ItemStack currentMainHandItem = driverContainer.getDriver(FirstPersonDrivers.getItemDriver(InteractionHand.MAIN_HAND)).getCurrentValue();
//        ItemStack previousOffHandItem = driverContainer.getDriver(FirstPersonDrivers.getItemDriver(InteractionHand.OFF_HAND)).getPreviousValue();
//
//        ItemStack currentOffHandItem = driverContainer.getDriver(FirstPersonDrivers.getItemDriver(InteractionHand.OFF_HAND)).getCurrentValue();
//        ItemStack previousMainHandItem = driverContainer.getDriver(FirstPersonDrivers.getItemDriver(InteractionHand.MAIN_HAND)).getPreviousValue();
//
//        boolean haveOppositesMatched = currentMainHandItem == previousOffHandItem;
//        boolean haveItemsChanged = currentOffHandItem != currentMainHandItem;
//
//        if (haveOppositesMatched && haveItemsChanged) {
//            driverContainer.getDriver(FirstPersonDrivers.HAS_SWAPPED_ITEMS).trigger();
//            LocomotionMain.DEBUG_LOGGER.info("ffffffffff");
//        }
//    }

    public void extractDampedCameraData(LocalPlayer dataReference, OnTickDriverContainer driverContainer, MontageManager montageManager) {
        Vector3f velocity = new Vector3f((float) (dataReference.getX() - dataReference.xo), (float) (dataReference.getY() - dataReference.yo), (float) (dataReference.getZ() - dataReference.zo));
        // We don't want vertical velocity to be factored into the movement direction offset as much as the horizontal velocity.
        velocity.mul(1, 0f, 1).mul(dataReference.isSprinting() ? 4f : 3f).min(new Vector3f(1)).max(new Vector3f(-1));
        driverContainer.getDriver(FirstPersonDrivers.DAMPED_VELOCITY).setValue(velocity);

        Vector3f dampedVelocity = new Vector3f(driverContainer.getDriverValue(FirstPersonDrivers.DAMPED_VELOCITY));
        Quaternionf rotation = new Quaternionf().rotationYXZ(Mth.PI - dataReference.getYRot() * Mth.DEG_TO_RAD, -dataReference.getXRot() * Mth.DEG_TO_RAD, 0.0F);
        Vector3f movementDirection = new Vector3f(
                dampedVelocity.dot(new Vector3f(1, 0, 0).rotate(rotation)),
                dampedVelocity.dot(new Vector3f(0, 1, 0).rotate(rotation)),
                dampedVelocity.dot(new Vector3f(0, 0, -1).rotate(rotation))
        );

        // Disable movement direction offset if mounted
        if (driverContainer.getDriverValue(FirstPersonDrivers.IS_PASSENGER)) {
            movementDirection.set(0);
        }

        driverContainer.getDriver(FirstPersonDrivers.MOVEMENT_DIRECTION_OFFSET).setValue(movementDirection);
        driverContainer.getDriver(FirstPersonDrivers.CAMERA_ROTATION_DAMPING).setValue(new Vector3f(dataReference.getXRot(), dataReference.getYRot(), dataReference.getYRot()).mul(Mth.DEG_TO_RAD));
        driverContainer.getDriver(FirstPersonDrivers.CAMERA_Z_ROTATION_DAMPING).setValue(driverContainer.getDriverValue(FirstPersonDrivers.CAMERA_ROTATION_DAMPING).y);

        // Camera rotation X
        driverContainer.getDriver(FirstPersonDrivers.CAMERA_ROTATION_X).setValue(dataReference.getXRot());
//        LocomotionMain.DEBUG_LOGGER.info(dataReference.getXRot());
    }
}

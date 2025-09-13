package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.entity.LivingEntityJointAnimator;
import com.trainguy9512.locomotion.animation.data.*;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Set;

public class FirstPersonJointAnimator implements LivingEntityJointAnimator<LocalPlayer, PlayerRenderState> {

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

    @Override
    public void postProcessModelParts(EntityModel<PlayerRenderState> entityModel, PlayerRenderState entityRenderState) {
    }

    @Override
    public ResourceLocation getJointSkeleton() {
        return ResourceLocation.fromNamespaceAndPath(LocomotionMain.MOD_ID, "skeletons/entity/player/first_person.json");
    }

    @Override
    public PoseCalculationFrequency getPoseCalulationFrequency() {
        return PoseCalculationFrequency.CALCULATE_EVERY_FRAME;
    }

    public static final String ADDITIVE_GROUND_MOVEMENT_CACHE = "additive_ground_movement";

    @Override
    public PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {
        cachedPoseContainer.register(ADDITIVE_GROUND_MOVEMENT_CACHE, com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonAdditiveMovement.constructPoseFunction(cachedPoseContainer), false);

        PoseFunction<LocalSpacePose> mainHandPose = FirstPersonHandPose.constructPoseFunction(cachedPoseContainer, InteractionHand.MAIN_HAND);
        PoseFunction<LocalSpacePose> offHandPose = FirstPersonHandPose.constructPoseFunction(cachedPoseContainer, InteractionHand.OFF_HAND);

        PoseFunction<LocalSpacePose> combinedHandPoseFunction = BlendPosesFunction.builder(mainHandPose)
                .addBlendInput(MirrorFunction.of(offHandPose), evaluationState -> 1f, LEFT_SIDE_MASK)
                .build();

        PoseFunction<LocalSpacePose> twoHandedActionPoseFunction = FirstPersonTwoHandedActions.constructPoseFunction(combinedHandPoseFunction, cachedPoseContainer);

        PoseFunction<LocalSpacePose> handPoseWithAdditive = ApplyAdditiveFunction.of(twoHandedActionPoseFunction, cachedPoseContainer.getOrThrow(ADDITIVE_GROUND_MOVEMENT_CACHE));

        PoseFunction<LocalSpacePose> mirroredBasedOnHandednessPose = MirrorFunction.of(handPoseWithAdditive, context -> Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT);

        PoseFunction<LocalSpacePose> movementDirectionOffsetTransformer =
                JointTransformerFunction.localOrParentSpaceBuilder(mirroredBasedOnHandednessPose, ARM_BUFFER_JOINT)
                        .setTranslation(
                                context -> context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.MOVEMENT_DIRECTION_OFFSET, context.partialTicks()).mul(1.5f, new Vector3f()),
                                JointChannel.TransformType.ADD,
                                JointChannel.TransformSpace.COMPONENT
                        )
                        .setRotationEuler(
                                context -> context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.CAMERA_ROTATION_DAMPING, context.partialTicks()).mul(-0.15f, -0.15f, 0, new Vector3f()),
                                JointChannel.TransformType.ADD,
                                JointChannel.TransformSpace.COMPONENT
                        )
                        .setWeight(interpolationContext -> LocomotionMain.CONFIG.data().firstPersonPlayer.enableCameraRotationDamping ? 1f : 0f)
                        .build();

        return movementDirectionOffsetTransformer;
    }

    @Override
    public void extractAnimationData(LocalPlayer dataReference, OnTickDriverContainer driverContainer, MontageManager montageManager){

        driverContainer.getDriver(FirstPersonDrivers.MODIFIED_WALK_SPEED).setValue(dataReference.walkAnimation.speed());
        driverContainer.getDriver(FirstPersonDrivers.HORIZONTAL_MOVEMENT_SPEED).setValue(new Vector3f((float) (dataReference.getX() - dataReference.xo), 0.0f, (float) (dataReference.getZ() - dataReference.zo)).length());
        driverContainer.getDriver(FirstPersonDrivers.VERTICAL_MOVEMENT_SPEED).setValue((float) (dataReference.getY() - dataReference.yo));

        // DEBUG ITEMS
//        updateRenderedItem(driverContainer, InteractionHand.MAIN_HAND);
//        updateRenderedItem(driverContainer, InteractionHand.OFF_HAND);
//        LocomotionMain.LOGGER.info("-------------------");
//        LocomotionMain.LOGGER.info(driverContainer.getDriverValue(HAS_USED_MAIN_HAND_ITEM));
//        LocomotionMain.LOGGER.info(dataReference.getMainHandItem());
//        LocomotionMain.LOGGER.info(dataReference.getMainHandItem().getCount());

        driverContainer.getDriver(FirstPersonDrivers.MAIN_HAND_ITEM).setValue(dataReference.getMainHandItem());
        driverContainer.getDriver(FirstPersonDrivers.OFF_HAND_ITEM).setValue(dataReference.getOffhandItem());

        //? if >= 1.21.5 {
        driverContainer.getDriver(FirstPersonDrivers.HOTBAR_SLOT).setValue(dataReference.getInventory().getSelectedSlot());
        //? } else {
        /*driverContainer.getDriver(HOTBAR_SLOT).setValue(dataReference.getInventory().selected);*/
        //? }


        driverContainer.getDriver(FirstPersonDrivers.HAS_DROPPED_ITEM).runIfTriggered(() -> montageManager.playMontage(FirstPersonMontages.USE_MAIN_HAND_MONTAGE, driverContainer));
        driverContainer.getDriver(FirstPersonDrivers.HAS_ATTACKED).runIfTriggered(() -> {
            MontageConfiguration montageConfiguration = driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_POSE).attackMontage;
            if (montageConfiguration != null) {
                montageManager.playMontage(montageConfiguration, driverContainer);
            }
        });
        driverContainer.getDriver(FirstPersonDrivers.HAS_BLOCKED_ATTACK).runIfTriggered(() -> montageManager.playMontage(FirstPersonMontages.SHIELD_BLOCK_IMPACT_MONTAGE, driverContainer));

        for (InteractionHand interactionHand : InteractionHand.values()) {
            ItemStack itemInHand = driverContainer.getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand));
            ItemStack renderedItemInHand = driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand));

            driverContainer.getDriver(FirstPersonDrivers.getHasUsedItemDriver(interactionHand)).runIfTriggered(() -> {
                montageManager.playMontage(interactionHand == InteractionHand.MAIN_HAND ? FirstPersonMontages.USE_MAIN_HAND_MONTAGE : FirstPersonMontages.USE_OFF_HAND_MONTAGE, driverContainer);
                if (FirstPersonHandPose.fromItemStack(driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand))) == FirstPersonHandPose.fromItemStack(driverContainer.getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand)))) {
                    FirstPersonDrivers.updateRenderedItem(driverContainer, interactionHand);
                }
            });
            if (itemInHand.getUseAnimation() == ItemUseAnimation.CROSSBOW && renderedItemInHand.getUseAnimation() == ItemUseAnimation.CROSSBOW) {
                if (itemInHand.has(DataComponents.CHARGED_PROJECTILES) && renderedItemInHand.has(DataComponents.CHARGED_PROJECTILES)) {
                    if (itemInHand.get(DataComponents.CHARGED_PROJECTILES).isEmpty() && !renderedItemInHand.get(DataComponents.CHARGED_PROJECTILES).isEmpty()) {
                        if (driverContainer.getDriver(FirstPersonDrivers.getHasInteractedWithDriver(interactionHand)).hasBeenTriggered()) {
                            montageManager.playMontage(FirstPersonMontages.getCrossbowFireMontage(interactionHand), driverContainer);
                            FirstPersonDrivers.updateRenderedItem(driverContainer, interactionHand);
                        }
                    }
                }
            }
            driverContainer.getDriver(FirstPersonDrivers.getHasInteractedWithDriver(interactionHand)).runIfTriggered(() -> {});


            driverContainer.getDriver(FirstPersonDrivers.getUsingItemDriver(interactionHand)).setValue(false);
            if (dataReference.isUsingItem() && dataReference.getUsedItemHand() == interactionHand) {
                driverContainer.getDriver(FirstPersonDrivers.getUsingItemDriver(interactionHand)).setValue(true);
                montageManager.interruptMontagesInSlot(FirstPersonMontages.getAttackSlot(interactionHand), Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.SINE_IN_OUT).build());
                driverContainer.getDriver(FirstPersonDrivers.LAST_USED_HAND).setValue(interactionHand);
                driverContainer.getDriver(FirstPersonDrivers.PROJECTILE_ITEM).setValue(dataReference.getProjectile(itemInHand));
                if (itemInHand.getUseAnimation() == ItemUseAnimation.CROSSBOW) {
                    float chargeTime = EnchantmentHelper.modifyCrossbowChargingTime(itemInHand, dataReference, 1.25f);
                    float chargeSpeedMultiplier = 1.25f / chargeTime;
                    driverContainer.getDriver(FirstPersonDrivers.CROSSBOW_RELOAD_SPEED).setValue(chargeSpeedMultiplier);
                }
            }
        }


        driverContainer.getDriver(FirstPersonDrivers.IS_MAIN_HAND_ON_COOLDOWN).setValue(dataReference.getCooldowns().isOnCooldown(driverContainer.getDriverValue(FirstPersonDrivers.RENDERED_MAIN_HAND_ITEM)));
        driverContainer.getDriver(FirstPersonDrivers.IS_OFF_HAND_ON_COOLDOWN).setValue(dataReference.getCooldowns().isOnCooldown(driverContainer.getDriverValue(FirstPersonDrivers.RENDERED_OFF_HAND_ITEM)));

        if (driverContainer.getDriver(FirstPersonDrivers.IS_MINING).getCurrentValue()) {
            montageManager.interruptMontagesInSlot(FirstPersonMontages.MAIN_HAND_ATTACK_SLOT, Transition.builder(TimeSpan.ofTicks(2)).build());
        }

        driverContainer.getDriver(FirstPersonDrivers.IS_MOVING).setValue(dataReference.input.keyPresses.forward() || dataReference.input.keyPresses.backward() || dataReference.input.keyPresses.left() || dataReference.input.keyPresses.right());
        driverContainer.getDriver(FirstPersonDrivers.IS_GROUNDED).setValue(dataReference.onGround());
        driverContainer.getDriver(FirstPersonDrivers.IS_JUMPING).setValue(dataReference.input.keyPresses.jump());
        driverContainer.getDriver(FirstPersonDrivers.IS_CROUCHING).setValue(dataReference.isCrouching());

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
        driverContainer.getDriver(FirstPersonDrivers.MOVEMENT_DIRECTION_OFFSET).setValue(movementDirection);
        driverContainer.getDriver(FirstPersonDrivers.CAMERA_ROTATION_DAMPING).setValue(new Vector3f(dataReference.getXRot(), dataReference.getYRot(), dataReference.getYRot()).mul(Mth.DEG_TO_RAD));

        // Camera rotation X
        driverContainer.getDriver(FirstPersonDrivers.CAMERA_ROTATION_X).setValue(dataReference.getXRot());
//        LocomotionMain.DEBUG_LOGGER.info(dataReference.getXRot());
    }
}

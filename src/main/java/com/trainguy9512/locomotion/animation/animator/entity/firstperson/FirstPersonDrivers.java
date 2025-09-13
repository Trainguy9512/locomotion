package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.driver.*;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.util.Easing;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;

public class FirstPersonDrivers {

    public static final DriverKey<SpringDriver<Vector3f>> DAMPED_VELOCITY = DriverKey.of("damped_velocity", () -> SpringDriver.ofVector3f(0.8f, 0.6f, 1f, Vector3f::new, false));
    public static final DriverKey<VariableDriver<Vector3f>> MOVEMENT_DIRECTION_OFFSET = DriverKey.of("movement_direction_offset", () -> VariableDriver.ofVector(Vector3f::new));
    public static final DriverKey<SpringDriver<Vector3f>> CAMERA_ROTATION_DAMPING = DriverKey.of("camera_rotation_damping", () -> SpringDriver.ofVector3f(LocomotionMain.CONFIG.data().firstPersonPlayer.cameraRotationStiffnessFactor, LocomotionMain.CONFIG.data().firstPersonPlayer.cameraRotationDampingFactor, 1f, Vector3f::new, true));
    public static final DriverKey<VariableDriver<Float>> CAMERA_ROTATION_X = DriverKey.of("camera_rotation_x", () -> VariableDriver.ofFloat(() -> 0f));

    public static final DriverKey<VariableDriver<Integer>> HOTBAR_SLOT = DriverKey.of("hotbar_slot", () -> VariableDriver.ofConstant(() -> 0));
    public static final DriverKey<VariableDriver<ItemStack>> MAIN_HAND_ITEM = DriverKey.of("main_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> OFF_HAND_ITEM = DriverKey.of("off_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> RENDERED_MAIN_HAND_ITEM = DriverKey.of("rendered_main_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> RENDERED_OFF_HAND_ITEM = DriverKey.of("rendered_off_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<FirstPersonHandPose>> MAIN_HAND_POSE = DriverKey.of("main_hand_pose", () -> VariableDriver.ofConstant(() -> FirstPersonHandPose.EMPTY));
    public static final DriverKey<VariableDriver<FirstPersonHandPose>> OFF_HAND_POSE = DriverKey.of("off_hand_pose", () -> VariableDriver.ofConstant(() -> FirstPersonHandPose.EMPTY));
    public static final DriverKey<VariableDriver<FirstPersonGenericItemPose>> MAIN_HAND_GENERIC_ITEM_POSE = DriverKey.of("main_hand_generic_item_pose", () -> VariableDriver.ofConstant(() -> FirstPersonGenericItemPose.DEFAULT_2D_ITEM));
    public static final DriverKey<VariableDriver<FirstPersonGenericItemPose>> OFF_HAND_GENERIC_ITEM_POSE = DriverKey.of("off_hand_generic_item_pose", () -> VariableDriver.ofConstant(() -> FirstPersonGenericItemPose.DEFAULT_2D_ITEM));

    public static final DriverKey<VariableDriver<FirstPersonTwoHandedActions.TwoHandedActionStates>> CURRENT_TWO_HANDED_OVERRIDE_STATE = DriverKey.of("current_two_handed_override_state", () -> VariableDriver.ofConstant(() -> FirstPersonTwoHandedActions.TwoHandedActionStates.NORMAL));

    public static final DriverKey<VariableDriver<Float>> HORIZONTAL_MOVEMENT_SPEED = DriverKey.of("horizontal_movement_speed", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Float>> VERTICAL_MOVEMENT_SPEED = DriverKey.of("vertical_movement_speed", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Float>> MODIFIED_WALK_SPEED = DriverKey.of("modified_walk_speed", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Boolean>> IS_MOVING = DriverKey.of("is_moving", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_GROUNDED = DriverKey.of("is_grounded", () -> VariableDriver.ofBoolean(() -> true));
    public static final DriverKey<VariableDriver<Boolean>> IS_JUMPING = DriverKey.of("is_jumping", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_CROUCHING = DriverKey.of("is_crouching", () -> VariableDriver.ofBoolean(() -> false));

    public static final DriverKey<VariableDriver<Boolean>> IS_MINING = DriverKey.of("is_mining", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<TriggerDriver> HAS_ATTACKED = DriverKey.of("has_attacked", TriggerDriver::of);
    public static final DriverKey<TriggerDriver> HAS_USED_MAIN_HAND_ITEM = DriverKey.of("has_used_main_hand_item", () -> TriggerDriver.of(2));
    public static final DriverKey<TriggerDriver> HAS_USED_OFF_HAND_ITEM = DriverKey.of("has_used_off_hand_item", () -> TriggerDriver.of(2));
    public static final DriverKey<TriggerDriver> HAS_INTERACTED_WITH_MAIN_HAND = DriverKey.of("has_interacted_with_main_hand_item", () -> TriggerDriver.of(2));
    public static final DriverKey<TriggerDriver> HAS_INTERACTED_WITH_OFF_HAND = DriverKey.of("has_interacted_with_off_hand_item", () -> TriggerDriver.of(2));
    public static final DriverKey<TriggerDriver> HAS_BLOCKED_ATTACK = DriverKey.of("has_blocked_attack", TriggerDriver::of);
    public static final DriverKey<TriggerDriver> HAS_DROPPED_ITEM = DriverKey.of("has_dropped_item", TriggerDriver::of);

    public static final DriverKey<VariableDriver<Boolean>> IS_USING_MAIN_HAND_ITEM = DriverKey.of("is_using_main_hand_item", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_USING_OFF_HAND_ITEM = DriverKey.of("is_using_off_hand_item", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_MAIN_HAND_ON_COOLDOWN = DriverKey.of("is_main_hand_on_cooldown", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_OFF_HAND_ON_COOLDOWN = DriverKey.of("is_off_hand_on_cooldown", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<InteractionHand>> LAST_USED_HAND = DriverKey.of("last_used_hand", () -> VariableDriver.ofConstant(() -> InteractionHand.MAIN_HAND));

    public static final DriverKey<VariableDriver<ItemStack>> PROJECTILE_ITEM = DriverKey.of("projectile_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<Float>> CROSSBOW_RELOAD_SPEED = DriverKey.of("crossbow_reload_speed", () -> VariableDriver.ofFloat(() -> 1f));
    public static final DriverKey<VariableDriver<Float>> ITEM_CONSUMPTION_SPEED = DriverKey.of("item_consumption_speed", () -> VariableDriver.ofFloat(() -> 1f));

    public static void updateRenderedItem(OnTickDriverContainer driverContainer, InteractionHand interactionHand) {
        driverContainer.getDriver(getRenderedItemDriver(interactionHand)).setValue(driverContainer.getDriverValue(getItemDriver(interactionHand)).copy());
        FirstPersonHandPose handPose = FirstPersonHandPose.fromItemStack(driverContainer.getDriver(getRenderedItemDriver(interactionHand)).getCurrentValue());
        driverContainer.getDriver(getHandPoseDriver(interactionHand)).setValue(handPose);
        if (handPose == FirstPersonHandPose.GENERIC_ITEM) {
            driverContainer.getDriver(getGenericItemPoseDriver(interactionHand)).setValue(FirstPersonGenericItemPose.fromItemStack(driverContainer.getDriver(getRenderedItemDriver(interactionHand)).getCurrentValue()));
        } else {
            driverContainer.getDriver(getGenericItemPoseDriver(interactionHand)).setValue(FirstPersonGenericItemPose.DEFAULT_2D_ITEM);
        }
    }

    public static void updateRenderedItemIfNoTwoHandOverrides(OnTickDriverContainer driverContainer, InteractionHand interactionHand) {
        if (driverContainer.getDriverValue(CURRENT_TWO_HANDED_OVERRIDE_STATE).isOverriding()) {
            updateRenderedItem(driverContainer, interactionHand);
        }
    }

    public static DriverKey<VariableDriver<FirstPersonHandPose>> getHandPoseDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> MAIN_HAND_POSE;
            case OFF_HAND -> OFF_HAND_POSE;
        };
    }

    public static DriverKey<VariableDriver<FirstPersonGenericItemPose>> getGenericItemPoseDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> MAIN_HAND_GENERIC_ITEM_POSE;
            case OFF_HAND -> OFF_HAND_GENERIC_ITEM_POSE;
        };
    }

    public static DriverKey<VariableDriver<ItemStack>> getItemDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> MAIN_HAND_ITEM;
            case OFF_HAND -> OFF_HAND_ITEM;
        };
    }

    public static DriverKey<VariableDriver<ItemStack>> getRenderedItemDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> RENDERED_MAIN_HAND_ITEM;
            case OFF_HAND -> RENDERED_OFF_HAND_ITEM;
        };
    }

    public static DriverKey<VariableDriver<Boolean>> getUsingItemDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> IS_USING_MAIN_HAND_ITEM;
            case OFF_HAND -> IS_USING_OFF_HAND_ITEM;
        };
    }

    public static DriverKey<TriggerDriver> getHasUsedItemDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> HAS_USED_MAIN_HAND_ITEM;
            case OFF_HAND -> HAS_USED_OFF_HAND_ITEM;
        };
    }

    public static DriverKey<TriggerDriver> getHasInteractedWithDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> HAS_INTERACTED_WITH_MAIN_HAND;
            case OFF_HAND -> HAS_INTERACTED_WITH_OFF_HAND;
        };
    }
}
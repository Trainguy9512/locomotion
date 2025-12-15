package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.driver.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.Objects;

public class FirstPersonDrivers {

    public static final DriverKey<SpringDriver<Vector3f>> DAMPED_VELOCITY = DriverKey.of("damped_velocity", () -> SpringDriver.ofVector3f(0.8f, 0.6f, 1f, Vector3f::new, false));
    public static final DriverKey<VariableDriver<Vector3f>> MOVEMENT_DIRECTION_OFFSET = DriverKey.of("movement_direction_offset", () -> VariableDriver.ofVector(Vector3f::new));
    public static final DriverKey<SpringDriver<Vector3f>> CAMERA_ROTATION_DAMPING = DriverKey.of("camera_rotation_damping", () -> SpringDriver.ofVector3f(LocomotionMain.CONFIG.data().firstPersonPlayer.cameraRotationStiffnessFactor, LocomotionMain.CONFIG.data().firstPersonPlayer.cameraRotationDampingFactor, 1f, Vector3f::new, true));
    public static final DriverKey<SpringDriver<Float>> CAMERA_Z_ROTATION_DAMPING = DriverKey.of("camera_z_rotation_damping", () -> SpringDriver.ofFloat(0.4f, 0.7f, 1, () -> 0f, true));
    public static final DriverKey<VariableDriver<Float>> CAMERA_ROTATION_X = DriverKey.of("camera_rotation_x", () -> VariableDriver.ofFloat(() -> 0f));

    public static final DriverKey<VariableDriver<Integer>> HOTBAR_SLOT = DriverKey.of("hotbar_slot", () -> VariableDriver.ofConstant(() -> 0));
    public static final DriverKey<VariableDriver<ItemStack>> MAIN_HAND_ITEM = DriverKey.of("main_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> OFF_HAND_ITEM = DriverKey.of("off_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> MAIN_HAND_ITEM_COPY_REFERENCE = DriverKey.of("main_hand_item_copy_reference", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> OFF_HAND_ITEM_COPY_REFERENCE = DriverKey.of("off_hand_item_copy_reference", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> RENDERED_MAIN_HAND_ITEM = DriverKey.of("rendered_main_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> RENDERED_OFF_HAND_ITEM = DriverKey.of("rendered_off_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<Identifier>> MAIN_HAND_POSE = DriverKey.of("main_hand_pose", () -> VariableDriver.ofConstant(() -> FirstPersonHandPoses.EMPTY_MAIN_HAND));
    public static final DriverKey<VariableDriver<Identifier>> OFF_HAND_POSE = DriverKey.of("off_hand_pose", () -> VariableDriver.ofConstant(() -> FirstPersonHandPoses.EMPTY_OFF_HAND));
    public static final DriverKey<VariableDriver<Identifier>> MAIN_HAND_GENERIC_ITEM_POSE = DriverKey.of("main_hand_generic_item_pose", () -> VariableDriver.ofConstant(() -> FirstPersonGenericItems.GENERIC_2D_ITEM));
    public static final DriverKey<VariableDriver<Identifier>> OFF_HAND_GENERIC_ITEM_POSE = DriverKey.of("off_hand_generic_item_pose", () -> VariableDriver.ofConstant(() -> FirstPersonGenericItems.GENERIC_2D_ITEM));

    public static final DriverKey<VariableDriver<String>> CURRENT_TWO_HANDED_OVERRIDE_STATE = DriverKey.of("current_two_handed_override_state", () -> VariableDriver.ofConstant(() -> FirstPersonTwoHandedActions.TWO_HANDED_ACTION_NORMAL_STATE));

    public static final DriverKey<VariableDriver<Float>> HORIZONTAL_MOVEMENT_SPEED = DriverKey.of("horizontal_movement_speed", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Float>> VERTICAL_MOVEMENT_SPEED = DriverKey.of("vertical_movement_speed", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Float>> MODIFIED_WALK_SPEED = DriverKey.of("modified_walk_speed", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Boolean>> IS_MOVING = DriverKey.of("is_moving", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_ON_GROUND = DriverKey.of("is_grounded", () -> VariableDriver.ofBoolean(() -> true));
    public static final DriverKey<VariableDriver<Boolean>> IS_JUMPING = DriverKey.of("is_jumping", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_CROUCHING = DriverKey.of("is_crouching", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_UNDERWATER = DriverKey.of("is_underwater", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_SWIMMING_UNDERWATER = DriverKey.of("is_swimming_underwater", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_PASSENGER = DriverKey.of("is_passenger", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> HAS_SCREEN_OPEN = DriverKey.of("has_screen_open", () -> VariableDriver.ofBoolean(() -> false));

    public static final DriverKey<VariableDriver<Boolean>> IS_MINING = DriverKey.of("is_mining", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<TriggerDriver> HAS_ATTACKED = DriverKey.of("has_attacked", TriggerDriver::of);
    public static final DriverKey<TriggerDriver> HAS_BLOCKED_ATTACK = DriverKey.of("has_blocked_attack", TriggerDriver::of);
    public static final DriverKey<TriggerDriver> HAS_DROPPED_ITEM = DriverKey.of("has_dropped_item", TriggerDriver::of);
    public static final DriverKey<TriggerDriver> HAS_SWAPPED_ITEMS = DriverKey.of("has_swapped_items", () -> TriggerDriver.of(3));

    public static final DriverKey<TriggerDriver> HAS_USED_MAIN_HAND_ITEM = DriverKey.of("has_used_main_hand_item", () -> TriggerDriver.of(2));
    public static final DriverKey<TriggerDriver> HAS_USED_OFF_HAND_ITEM = DriverKey.of("has_used_off_hand_item", () -> TriggerDriver.of(2));
    public static final DriverKey<VariableDriver<InteractionHand>> LAST_USED_HAND = DriverKey.of("last_used_hand", () -> VariableDriver.ofConstant(() -> InteractionHand.MAIN_HAND));
    public static final DriverKey<VariableDriver<Integer>> LAST_USED_SWING_TIME = DriverKey.of("last_used_swing_time", () -> VariableDriver.ofConstant(() -> 0));
    public static final DriverKey<VariableDriver<Boolean>> LAST_USED_SWING_FROM_CLIENT = DriverKey.of("last_used_swing_from_client", () -> VariableDriver.ofConstant(() -> false));
    public static final DriverKey<VariableDriver<EntityType<?>>> LAST_USED_TARGET_ENTITY = DriverKey.of("last_used_target_entity", () -> VariableDriver.ofConstant(() -> EntityType.COW));
    public static final DriverKey<VariableDriver<BlockState>> LAST_USED_TARGET_BLOCK_STATE = DriverKey.of("last_used_target_block_state", () -> VariableDriver.ofConstant(Blocks.AIR::defaultBlockState));
    public static final DriverKey<VariableDriver<FirstPersonUseAnimations.UseAnimationType>> LAST_USE_TYPE = DriverKey.of("last_use_type", () -> VariableDriver.ofConstant(() -> FirstPersonUseAnimations.UseAnimationType.USE_ITEM_ON_BLOCK));

    public static final DriverKey<VariableDriver<Boolean>> IS_USING_MAIN_HAND_ITEM = DriverKey.of("is_using_main_hand_item", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_USING_OFF_HAND_ITEM = DriverKey.of("is_using_off_hand_item", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_MAIN_HAND_ON_COOLDOWN = DriverKey.of("is_main_hand_on_cooldown", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_OFF_HAND_ON_COOLDOWN = DriverKey.of("is_off_hand_on_cooldown", () -> VariableDriver.ofBoolean(() -> false));

    public static final DriverKey<VariableDriver<ItemStack>> PROJECTILE_ITEM = DriverKey.of("projectile_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<Float>> CROSSBOW_RELOAD_SPEED = DriverKey.of("crossbow_reload_speed", () -> VariableDriver.ofFloat(() -> 1f));
    public static final DriverKey<VariableDriver<Float>> ITEM_CONSUMPTION_SPEED = DriverKey.of("item_consumption_speed", () -> VariableDriver.ofFloat(() -> 1f));
    public static final DriverKey<VariableDriver<Boolean>> IS_IN_RIPTIDE = DriverKey.of("is_in_riptide", () -> VariableDriver.ofBoolean(() -> false));

    public static void updateRenderedItem(OnTickDriverContainer driverContainer, InteractionHand hand) {
        ItemStack newRenderedItem = driverContainer.getDriverValue(getItemDriver(hand)).copy();
        driverContainer.getDriver(getRenderedItemDriver(hand)).setValue(newRenderedItem);
        Identifier handPose = FirstPersonHandPoses.testForNextHandPose(newRenderedItem, hand);
        driverContainer.getDriver(getHandPoseDriver(hand)).setValue(handPose);
        if (handPose == FirstPersonHandPoses.GENERIC_ITEM) {
            Identifier genericItemPose = FirstPersonGenericItems.getConfigurationFromItem(newRenderedItem);
            driverContainer.getDriver(getGenericItemPoseDriver(hand)).setValue(genericItemPose);
        } else {
            driverContainer.getDriver(getGenericItemPoseDriver(hand)).setValue(FirstPersonGenericItems.getFallback());
        }
    }

    public static void updateRenderedItemIfNoTwoHandOverrides(OnTickDriverContainer driverContainer, InteractionHand hand) {
        if (Objects.equals(driverContainer.getDriverValue(CURRENT_TWO_HANDED_OVERRIDE_STATE), FirstPersonTwoHandedActions.TWO_HANDED_ACTION_NORMAL_STATE)) {
            updateRenderedItem(driverContainer, hand);
        }
    }

    public static DriverKey<VariableDriver<Identifier>> getHandPoseDriver(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> MAIN_HAND_POSE;
            case OFF_HAND -> OFF_HAND_POSE;
        };
    }

    public static DriverKey<VariableDriver<Identifier>> getGenericItemPoseDriver(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> MAIN_HAND_GENERIC_ITEM_POSE;
            case OFF_HAND -> OFF_HAND_GENERIC_ITEM_POSE;
        };
    }

    public static DriverKey<VariableDriver<Boolean>> getItemOnCooldownDriver(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> IS_MAIN_HAND_ON_COOLDOWN;
            case OFF_HAND -> IS_OFF_HAND_ON_COOLDOWN;
        };
    }

    public static DriverKey<VariableDriver<ItemStack>> getItemDriver(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> MAIN_HAND_ITEM;
            case OFF_HAND -> OFF_HAND_ITEM;
        };
    }

    public static DriverKey<VariableDriver<ItemStack>> getItemCopyReferenceDriver(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> MAIN_HAND_ITEM_COPY_REFERENCE;
            case OFF_HAND -> OFF_HAND_ITEM_COPY_REFERENCE;
        };
    }

    public static DriverKey<VariableDriver<ItemStack>> getRenderedItemDriver(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> RENDERED_MAIN_HAND_ITEM;
            case OFF_HAND -> RENDERED_OFF_HAND_ITEM;
        };
    }

    public static DriverKey<VariableDriver<Boolean>> getUsingItemDriver(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> IS_USING_MAIN_HAND_ITEM;
            case OFF_HAND -> IS_USING_OFF_HAND_ITEM;
        };
    }

    public static DriverKey<TriggerDriver> getHasUsedItemDriver(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> HAS_USED_MAIN_HAND_ITEM;
            case OFF_HAND -> HAS_USED_OFF_HAND_ITEM;
        };
    }
}
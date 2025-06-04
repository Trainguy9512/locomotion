package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.world.InteractionHand;


public class FirstPersonMontages {

    public static final String MAIN_HAND_ATTACK_SLOT = "main_hand_attack";
    public static final String OFF_HAND_ATTACK_SLOT = "off_hand_attack";
    public static final String SHIELD_BLOCK_SLOT = "shield_block";

    public static final MontageConfiguration HAND_TOOL_ATTACK_PICKAXE_MONTAGE = MontageConfiguration.builder("hand_tool_attack_pickaxe", FirstPersonAnimationSequences.HAND_TOOL_ATTACK)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(8))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(1)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(12)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(driverContainer -> {
                FirstPersonHandPose handPose = driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_POSE);
                if (handPose == FirstPersonHandPose.GENERIC_ITEM) {
                    return driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_GENERIC_ITEM_POSE).basePoseLocation;
                }
                return handPose.basePoseLocation;
            })
            .build();


    public static final MontageConfiguration USE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("hand_use_main_hand", FirstPersonAnimationSequences.HAND_TOOL_USE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(5))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(16)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(driverContainer -> {
                FirstPersonHandPose handPose = driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_POSE);
                if (handPose == FirstPersonHandPose.GENERIC_ITEM) {
                    return driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_GENERIC_ITEM_POSE).basePoseLocation;
                }
                return handPose.basePoseLocation;
            })
            .build();
    public static final MontageConfiguration USE_OFF_HAND_MONTAGE = MontageConfiguration.builder("hand_use_off_hand", FirstPersonAnimationSequences.HAND_TOOL_USE)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(5))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(16)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(driverContainer -> {
                FirstPersonHandPose handPose = driverContainer.getDriverValue(FirstPersonDrivers.OFF_HAND_POSE);
                if (handPose == FirstPersonHandPose.GENERIC_ITEM) {
                    return driverContainer.getDriverValue(FirstPersonDrivers.OFF_HAND_GENERIC_ITEM_POSE).basePoseLocation;
                }
                return handPose.basePoseLocation;
            })
            .build();
    public static final MontageConfiguration SHIELD_BLOCK_IMPACT_MONTAGE = MontageConfiguration.builder("shield_block_impact", FirstPersonAnimationSequences.HAND_SHIELD_IMPACT)
            .playsInSlot(SHIELD_BLOCK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(5))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_IN_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(8)).setEasement(Easing.SINE_IN_OUT).build())
            .build();


    public static final MontageConfiguration CROSSBOW_FIRE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("crossbow_fire_main_hand", FirstPersonAnimationSequences.HAND_CROSSBOW_FIRE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(20)).setEasement(Easing.SINE_IN_OUT).build())
            .build();
    public static final MontageConfiguration CROSSBOW_FIRE_OFF_HAND_MONTAGE = MontageConfiguration.builder("crossbow_fire_off_hand", FirstPersonAnimationSequences.HAND_CROSSBOW_FIRE)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(20)).setEasement(Easing.SINE_IN_OUT).build())
            .build();
    public static MontageConfiguration getCrossbowFireMontage(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> CROSSBOW_FIRE_MAIN_HAND_MONTAGE;
            case OFF_HAND -> CROSSBOW_FIRE_OFF_HAND_MONTAGE;
        };
    }
}
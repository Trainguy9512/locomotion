package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.world.InteractionHand;


public class FirstPersonMontages {

    public static final String MAIN_HAND_ATTACK_SLOT = "main_hand_attack";
    public static final String OFF_HAND_ATTACK_SLOT = "off_hand_attack";
    public static final String SHIELD_BLOCK_SLOT = "shield_block";

    public static String getAttackSlot(InteractionHand interactionHand) {
        return interactionHand == InteractionHand.MAIN_HAND ? MAIN_HAND_ATTACK_SLOT : OFF_HAND_ATTACK_SLOT;
    }

    public static final MontageConfiguration HAND_TOOL_ATTACK_PICKAXE_MONTAGE = MontageConfiguration.builder("hand_tool_attack_pickaxe", FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_ATTACK)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(40)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(driverContainer -> {
                FirstPersonHandPose handPose = driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_POSE);
                if (handPose == FirstPersonHandPose.GENERIC_ITEM) {
                    return driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_GENERIC_ITEM_POSE).basePoseLocation;
                }
                return handPose.basePoseLocation;
            })
            .build();
    public static final MontageConfiguration HAND_TOOL_ATTACK_AXE_MONTAGE = MontageConfiguration.builder("hand_tool_attack_axe", FirstPersonAnimationSequences.HAND_TOOL_AXE_ATTACK)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(15)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration HAND_MACE_ATTACK_MONTAGE = MontageConfiguration.builder("hand_mace_attack", FirstPersonAnimationSequences.HAND_MACE_ATTACK)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration HAND_TRIDENT_JAB_MONTAGE = MontageConfiguration.builder("hand_trident_jab", FirstPersonAnimationSequences.HAND_TRIDENT_JAB)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
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

    public static final MontageConfiguration USE_OFF_HAND_MONTAGE = USE_MAIN_HAND_MONTAGE.makeBuilderCopy("hand_use_off_hand", USE_MAIN_HAND_MONTAGE.animationSequence())
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
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

    public static final MontageConfiguration AXE_SCRAPE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("axe_scrape_main_hand", FirstPersonAnimationSequences.HAND_TOOL_AXE_SCRAPE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration AXE_SCRAPE_OFF_HAND_MONTAGE = AXE_SCRAPE_MAIN_HAND_MONTAGE.makeBuilderCopy("axe_scrape_off_hand", FirstPersonAnimationSequences.HAND_TOOL_AXE_SCRAPE)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();

    public static final MontageConfiguration HOE_TILL_MAIN_HAND_MONTAGE = MontageConfiguration.builder("hoe_till_main_hand", FirstPersonAnimationSequences.HAND_TOOL_HOE_TILL)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration HOE_TILL_OFF_HAND_MONTAGE = AXE_SCRAPE_MAIN_HAND_MONTAGE.makeBuilderCopy("hoe_till_off_hand", FirstPersonAnimationSequences.HAND_TOOL_HOE_TILL)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();

    public static final MontageConfiguration SHOVEL_FLATTEN_MAIN_HAND_MONTAGE = MontageConfiguration.builder("shovel_flatten_main_hand", FirstPersonAnimationSequences.HAND_TOOL_SHOVEL_FLATTEN)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration SHOVEL_FLATTEN_OFF_HAND_MONTAGE = AXE_SCRAPE_MAIN_HAND_MONTAGE.makeBuilderCopy("shoven_flatten_off_hand", FirstPersonAnimationSequences.HAND_TOOL_SHOVEL_FLATTEN)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();

    public static final MontageConfiguration SHEARS_USE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("shears_use_main_hand", FirstPersonAnimationSequences.HAND_GENERIC_ITEM_SHEARS_USE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration SHEARS_USE_OFF_HAND_MONTAGE = AXE_SCRAPE_MAIN_HAND_MONTAGE.makeBuilderCopy("shears_use_off_hand", FirstPersonAnimationSequences.HAND_GENERIC_ITEM_SHEARS_USE)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();

    public static final MontageConfiguration CROSSBOW_FIRE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("crossbow_fire_main_hand", FirstPersonAnimationSequences.HAND_CROSSBOW_FIRE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(20)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    public static final MontageConfiguration CROSSBOW_FIRE_OFF_HAND_MONTAGE = CROSSBOW_FIRE_MAIN_HAND_MONTAGE.makeBuilderCopy("crossbow_fire_off_hand", FirstPersonAnimationSequences.HAND_CROSSBOW_FIRE)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .build();

    public static MontageConfiguration getCrossbowFireMontage(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> CROSSBOW_FIRE_MAIN_HAND_MONTAGE;
            case OFF_HAND -> CROSSBOW_FIRE_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getUseAnimationMontage(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> USE_MAIN_HAND_MONTAGE;
            case OFF_HAND -> USE_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getAxeScrapeMontage(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> AXE_SCRAPE_MAIN_HAND_MONTAGE;
            case OFF_HAND -> AXE_SCRAPE_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getHoeTillMontage(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> HOE_TILL_MAIN_HAND_MONTAGE;
            case OFF_HAND -> HOE_TILL_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getShovelFlattenMontage(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> SHOVEL_FLATTEN_MAIN_HAND_MONTAGE;
            case OFF_HAND -> SHOVEL_FLATTEN_OFF_HAND_MONTAGE;
        };
    }

    public static MontageConfiguration getShearsUseMontage(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> SHEARS_USE_MAIN_HAND_MONTAGE;
            case OFF_HAND -> SHEARS_USE_OFF_HAND_MONTAGE;
        };
    }

    public static void playAttackMontage(OnTickDriverContainer driverContainer, MontageManager montageManager) {
        FirstPersonHandPose firstPersonHandPose = driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_POSE);
        MontageConfiguration montage = firstPersonHandPose.getAttackMontage(driverContainer);
        if (montage != null) {
            montageManager.playMontage(montage);
        }
    }
}
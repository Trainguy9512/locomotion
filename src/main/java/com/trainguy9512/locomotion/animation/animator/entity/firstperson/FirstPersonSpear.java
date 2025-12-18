package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageSlotFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.KineticWeapon;

import java.util.Set;

public class FirstPersonSpear {

    public static PoseFunction<LocalSpacePose> constructSpearPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {
        PoseFunction<LocalSpacePose> pose;

        pose = constructChargePoseFunction(cachedPoseContainer, hand);
        pose = constructWithSpearImpact(pose);

        return pose;
    }

    private static PoseFunction<LocalSpacePose> constructWithSpearImpact(PoseFunction<LocalSpacePose> inputPose) {
        PoseFunction<LocalSpacePose> basePose = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SPEAR_CHARGE_POSE_1).build();
        PoseFunction<LocalSpacePose> pose = MontageSlotFunction.of(basePose, FirstPersonMontages.SPEAR_CHARGE_SLOT);
        pose = MakeDynamicAdditiveFunction.of(pose, basePose);
        pose = ApplyAdditiveFunction.of(inputPose, pose);
        return pose;
    }

    public static String CHARGE_IDLE_STATE = "idle";
    public static String CHARGE_ENTER_STATE = "enter";
    public static String CHARGE_STAGE_1_STATE = "stage_1";
    public static String CHARGE_STAGE_1_TO_2_STATE = "stage_1_to_2";
    public static String CHARGE_STAGE_2_STATE = "stage_2";
    public static String CHARGE_STAGE_2_TO_3_STATE = "stage_2_to_3";
    public static String CHARGE_STAGE_3_STATE = "stage_3";
    public static String CHARGE_EXIT_STATE = "exit";

    private static PoseFunction<LocalSpacePose> constructChargePoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {

        PoseFunction<LocalSpacePose> chargeIdlePose = FirstPersonMining.constructMainHandPickaxeMiningPoseFunction(cachedPoseContainer, hand);
        PoseFunction<LocalSpacePose> chargeEnterPose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SPEAR_CHARGE_ENTER).build();
        PoseFunction<LocalSpacePose> chargeExitPose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SPEAR_CHARGE_EXIT).build();
        PoseFunction<LocalSpacePose> chargeStage1Pose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SPEAR_CHARGE_POSE_1).build();
        PoseFunction<LocalSpacePose> chargeStage2Pose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SPEAR_CHARGE_POSE_2).build();
        PoseFunction<LocalSpacePose> chargeStage3Pose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SPEAR_CHARGE_POSE_3).build();
        PoseFunction<LocalSpacePose> chargeStage1To2Pose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SPEAR_CHARGE_WEAKEN_1).build();
        PoseFunction<LocalSpacePose> chargeStage2To3Pose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_SPEAR_CHARGE_WEAKEN_2).build();

        PoseFunction<LocalSpacePose> chargeStateMachinePose;
        chargeStateMachinePose = StateMachineFunction.builder(evaluationState -> CHARGE_IDLE_STATE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(CHARGE_IDLE_STATE, chargeIdlePose)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(CHARGE_ENTER_STATE, chargeEnterPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_1_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_1_STATE, chargeStage1Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_1_TO_2_STATE)
                                .isTakenIfTrue(FirstPersonSpear::spearCanNoLongerDismount)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_1_TO_2_STATE, chargeStage1To2Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_2_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_2_STATE, chargeStage2Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_2_TO_3_STATE)
                                .isTakenIfTrue(FirstPersonSpear::spearCanNoLongerKnockback)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_2_TO_3_STATE, chargeStage2To3Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_STAGE_3_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHARGE_STAGE_3_STATE, chargeStage3Pose)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(CHARGE_EXIT_STATE, chargeExitPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHARGE_IDLE_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                CHARGE_STAGE_1_STATE,
                                CHARGE_STAGE_2_STATE,
                                CHARGE_STAGE_3_STATE,
                                CHARGE_STAGE_1_TO_2_STATE,
                                CHARGE_STAGE_2_TO_3_STATE,
                                CHARGE_ENTER_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(CHARGE_EXIT_STATE)
                                .isTakenIfTrue(FirstPersonSpear::spearCanNoLongerDamage)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(50)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                CHARGE_EXIT_STATE,
                                CHARGE_IDLE_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(CHARGE_ENTER_STATE)
                                .isTakenIfTrue(context -> isUsingSpear(context, hand))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .setPriority(60)
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .build();
        return chargeStateMachinePose;
    }

    public static boolean isUsingSpear(StateTransition.TransitionContext context, InteractionHand hand) {
        boolean isUsing = context.driverContainer().getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand));
        boolean handPoseIsSpear = context.driverContainer().getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand)) == FirstPersonHandPoses.SPEAR;
        boolean spearCanDamage = context.driverContainer().getDriverValue(FirstPersonDrivers.SPEAR_CAN_DAMAGE);
        return isUsing && handPoseIsSpear && spearCanDamage;
    }

    public static boolean spearCanNoLongerDismount(StateTransition.TransitionContext context) {
        boolean spearCanDismount = context.driverContainer().getDriverValue(FirstPersonDrivers.SPEAR_CAN_DISMOUNT);
        return !spearCanDismount;
    }

    public static boolean spearCanNoLongerKnockback(StateTransition.TransitionContext context) {
        boolean spearCanKnockback = context.driverContainer().getDriverValue(FirstPersonDrivers.SPEAR_CAN_KNOCKBACK);
        return !spearCanKnockback;
    }

    public static boolean spearCanNoLongerDamage(StateTransition.TransitionContext context) {
        boolean spearCanDamage = context.driverContainer().getDriverValue(FirstPersonDrivers.SPEAR_CAN_DAMAGE);
        return !spearCanDamage;
    }

    public static void extractSpearData(LocalPlayer player, OnTickDriverContainer driverContainer, MontageManager montageManager) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack itemStack = player.getItemInHand(hand);
            KineticWeapon kineticWeapon = itemStack.get(DataComponents.KINETIC_WEAPON);
            if (kineticWeapon == null) {
                continue;
            }
            int spearUseDuration = itemStack.getUseDuration(player) - (player.getUseItemRemainingTicks() + 1);
            int delayTicks = kineticWeapon.delayTicks();
            driverContainer.getDriver(FirstPersonDrivers.SPEAR_CAN_DISMOUNT).setValue(spearUseDuration < kineticWeapon.dismountConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0).floatValue() - delayTicks);
            driverContainer.getDriver(FirstPersonDrivers.SPEAR_CAN_KNOCKBACK).setValue(spearUseDuration < kineticWeapon.knockbackConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0).floatValue() - delayTicks);
            driverContainer.getDriver(FirstPersonDrivers.SPEAR_CAN_DAMAGE).setValue(spearUseDuration < kineticWeapon.damageConditions().map(KineticWeapon.Condition::maxDurationTicks).orElse(0).floatValue() - delayTicks);

            int ticksSinceLastSpearImpact = (int) player.getTicksSinceLastKineticHitFeedback(0);
            if (ticksSinceLastSpearImpact == 1) {
                montageManager.playMontage(FirstPersonMontages.SPEAR_CHARGE_IMPACT_MONTAGE);
            }
        }
    }

}

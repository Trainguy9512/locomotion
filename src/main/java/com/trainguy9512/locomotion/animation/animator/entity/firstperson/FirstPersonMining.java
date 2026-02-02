package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonGenericItems;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonHandPoseSwitching;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonHandPoses;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.*;
import com.trainguy9512.locomotion.animation.util.Easing;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Set;
import java.util.function.Function;

public class FirstPersonMining {

    public static final String MINING_IDLE_STATE = "idle";
    public static final String MINING_SWING_STATE = "swing";
    public static final String MINING_FINISH_STATE = "finish";

    public static PoseFunction<LocalSpacePose> constructMiningPoseFunction(
            PoseFunction<LocalSpacePose> idlePoseFunction,
            PoseFunction<LocalSpacePose> swingPoseFunction,
            PoseFunction<LocalSpacePose> finishPoseFunction,
            Transition miningToFinishTiming,
            Transition idleToMiningTiming
    ) {
        return constructMiningPoseFunction(
                idlePoseFunction,
                swingPoseFunction,
                finishPoseFunction,
                miningToFinishTiming,
                idleToMiningTiming,
                FirstPersonHandPoseSwitching.constructCurrentBasePoseFunction(InteractionHand.MAIN_HAND),
                true
        );

    }

    public static PoseFunction<LocalSpacePose> constructMiningPoseFunction(
            PoseFunction<LocalSpacePose> idlePoseFunction,
            PoseFunction<LocalSpacePose> swingPoseFunction,
            PoseFunction<LocalSpacePose> finishPoseFunction,
            Transition miningToFinishTiming,
            Transition idleToMiningTiming,
            PoseFunction<LocalSpacePose> basePoseFunction,
            boolean makeAdditive
    ) {

        PoseFunction<LocalSpacePose> stateMachine = StateMachineFunction.builder(context -> MINING_IDLE_STATE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(MINING_IDLE_STATE, idlePoseFunction)
                        .addOutboundTransition(StateTransition.builder(MINING_SWING_STATE)
                                .isTakenIfTrue(FirstPersonMining::isMining)
                                .setTiming(idleToMiningTiming)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(MINING_SWING_STATE, swingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MINING_FINISH_STATE)
                                .isTakenIfTrue(
                                        FirstPersonMining::isNoLongerMining
//                                        StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING.and(FirstPersonMining::isNoLongerMining)
                                )
                                .setTiming(miningToFinishTiming)
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(MINING_FINISH_STATE, finishPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MINING_IDLE_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setPriority(50)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(5)).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(MINING_SWING_STATE)
                                .isTakenIfTrue(FirstPersonMining::isMining)
                                .setCanInterruptOtherTransitions(false)
                                .setPriority(60)
                                .setTiming(idleToMiningTiming)
                                .build())
                        .build())
                .build();

        PoseFunction<LocalSpacePose> pose = stateMachine;
        if (makeAdditive) {
            pose = MakeDynamicAdditiveFunction.of(
                    stateMachine,
                    idlePoseFunction
            );
            pose = ApplyAdditiveFunction.of(basePoseFunction, pose);
        }
        return pose;

    }

    public static Function<PoseTickEvaluationContext, Float> getMiningPlayRateFunction(float baseMultiplier) {
        return context -> baseMultiplier * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier;
    }

    public static PoseFunction<LocalSpacePose> constructPickaxeMiningPoseFunction() {
        return constructPickaxeMiningPoseFunction(FirstPersonHandPoseSwitching.constructCurrentBasePoseFunction(InteractionHand.MAIN_HAND));
    }

    public static PoseFunction<LocalSpacePose> constructPickaxeMiningPoseFunction(PoseFunction<LocalSpacePose> basePose) {
        return FirstPersonMining.constructMiningPoseFunction(
                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_MINE_SWING)
                        .setLooping(true)
                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(10))
                        .setPlayRate(getMiningPlayRateFunction(1.1f))
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(4))
                        .build(),
                Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.CUBIC_IN_OUT).build(),
                Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.CUBIC_OUT).build(),
                basePose,
                true);
    }

    public static PoseFunction<LocalSpacePose> constructAxeMiningPoseFunction() {
        return FirstPersonMining.constructMiningPoseFunction(
                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_AXE_MINE_SWING)
                        .setLooping(true)
                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(0))
                        .setPlayRate(getMiningPlayRateFunction(1.1f))
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(4))
                        .build(),
                Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.CUBIC_IN_OUT).build(),
                Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.CUBIC_OUT).build());
    }

    public static PoseFunction<LocalSpacePose> constructShovelMiningPoseFunction() {
        return FirstPersonMining.constructMiningPoseFunction(
                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SHOVEL_MINE_SWING)
                        .setLooping(true)
                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(0))
                        .setPlayRate(getMiningPlayRateFunction(1.1f))
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(4))
                        .build(),
                Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.CUBIC_IN_OUT).build(),
                Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.CUBIC_OUT).build());
    }

    public static PoseFunction<LocalSpacePose> constructEmptyHandMiningPoseFunction() {
        PoseFunction<LocalSpacePose> idlePoseFunction = FirstPersonHandPoseSwitching.constructCurrentBasePoseFunction(InteractionHand.MAIN_HAND);

        PoseFunction<LocalSpacePose> mineSwingPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_SWING)
                .setLooping(true)
                .setPlayRate(getMiningPlayRateFunction(1.5f))
                .build();
        mineSwingPoseFunction = BlendPosesFunction.builder(idlePoseFunction)
                .addBlendInput(mineSwingPoseFunction, context -> 1f, BlendMask.builder()
                        .defineForJoint(FirstPersonJointAnimator.RIGHT_ARM_JOINT, 1f)
                        .build())
                .build();

        PoseFunction<LocalSpacePose> mineFinishPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
                .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(4))
                .isAdditive(true, SequenceReferencePoint.END)
                .build();
        mineFinishPoseFunction = ApplyAdditiveFunction.of(
                idlePoseFunction,
                mineFinishPoseFunction
        );


        return FirstPersonMining.constructMiningPoseFunction(
                idlePoseFunction,
                mineSwingPoseFunction,
                mineFinishPoseFunction,
                Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.CUBIC_IN_OUT).build(),
                Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.CUBIC_OUT).build(),
                FirstPersonHandPoseSwitching.constructCurrentBasePoseFunction(InteractionHand.MAIN_HAND),
                false);
    }


    public static final String PUNCH_MINING_IDLE_STATE = "idle";
    public static final String PUNCH_MINING_SWING_A_STATE = "swing_a";
    public static final String PUNCH_MINING_SWING_B_STATE = "swing_b";
    public static final String PUNCH_MINING_FINISH_STATE = "finish";

    public static PoseFunction<LocalSpacePose> constructWithPunchMiningPoseFunction(PoseFunction<LocalSpacePose> inputPose) {

        PoseFunction<LocalSpacePose> swingAPose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_SWING)
                .setLooping(false)
                .setPlayRate(context -> 1.3f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                .build();
        PoseFunction<LocalSpacePose> swingBPose = MirrorFunction.of(swingAPose);
        PoseFunction<LocalSpacePose> finishPose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_FINISH).build();

        PoseFunction<LocalSpacePose> miningStateMachine;
        miningStateMachine = StateMachineFunction.builder(context -> PUNCH_MINING_IDLE_STATE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(PUNCH_MINING_IDLE_STATE, inputPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(PUNCH_MINING_SWING_A_STATE)
                                .isTakenIfTrue(FirstPersonMining::isPunchMining)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(2))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(FirstPersonMining::clearOffHandOfItems)
                                .setCanInterruptOtherTransitions(true)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(PUNCH_MINING_SWING_A_STATE, swingAPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(PUNCH_MINING_SWING_B_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(3))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(PUNCH_MINING_SWING_B_STATE, swingBPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(PUNCH_MINING_SWING_A_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(3))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(PUNCH_MINING_FINISH_STATE, finishPose)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(PUNCH_MINING_IDLE_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(PUNCH_MINING_IDLE_STATE)
                                .isTakenIfTrue(FirstPersonMining::shouldInterruptFinishAnimation)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(PUNCH_MINING_SWING_A_STATE)
                                .isTakenIfTrue(FirstPersonMining::isPunchMining)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(2))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(FirstPersonMining::clearOffHandOfItems)
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                        PUNCH_MINING_SWING_A_STATE,
                        PUNCH_MINING_SWING_B_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(PUNCH_MINING_FINISH_STATE)
                                .isTakenIfTrue(FirstPersonMining::isNoLongerPunchMining)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(8))
                                        .setEasement(Easing.CUBIC_OUT)
                                        .build())
                                .build())
                        .build())
                .build();

        return miningStateMachine;

    }

    public static boolean isMining(StateTransitionContext context) {
        return context.getDriverValue(FirstPersonDrivers.IS_MINING);
    }

    public static boolean isNoLongerMining(StateTransitionContext context) {
        return !isMining(context);
    }

    public static boolean isPunchMining(StateTransitionContext context) {
        boolean isMainHandInEmptyPose = context.getDriverValue(FirstPersonDrivers.MAIN_HAND_POSE) == FirstPersonHandPoses.EMPTY_MAIN_HAND;
        boolean isMainHandEmpty = context.getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM).isEmpty();
        return isMining(context) && isMainHandInEmptyPose && isMainHandEmpty;
    }

    public static boolean isNoLongerPunchMining(StateTransitionContext context) {
        boolean isMainHandEmpty = context.getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM).isEmpty();
        return isNoLongerMining(context) || !isMainHandEmpty;
    }

    public static boolean shouldInterruptFinishAnimation(StateTransitionContext context) {
        boolean hasAttacked = context.getDriverValue(FirstPersonDrivers.HAS_ATTACKED);
        boolean hasUsedItem = context.getDriverValue(FirstPersonDrivers.IS_USING_MAIN_HAND_ITEM);
        hasUsedItem = hasUsedItem || context.getDriverValue(FirstPersonDrivers.IS_USING_OFF_HAND_ITEM);
        hasUsedItem = hasUsedItem || context.getDriverValue(FirstPersonDrivers.HAS_USED_MAIN_HAND_ITEM);
        hasUsedItem = hasUsedItem || context.getDriverValue(FirstPersonDrivers.HAS_USED_OFF_HAND_ITEM);
        return hasAttacked || hasUsedItem;
    }

    public static void clearOffHandOfItems(PoseTickEvaluationContext context) {
        context.getDriver(FirstPersonDrivers.RENDERED_OFF_HAND_ITEM).setValue(ItemStack.EMPTY);
        context.getDriver(FirstPersonDrivers.OFF_HAND_POSE).setValue(FirstPersonHandPoses.EMPTY_OFF_HAND);
        context.getDriver(FirstPersonDrivers.OFF_HAND_GENERIC_ITEM_POSE).setValue(FirstPersonGenericItems.getFallback());
    }
}
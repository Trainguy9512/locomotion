package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.Set;

public class FirstPersonMining {

    public static final String MINING_IDLE_STATE = "idle";
    public static final String MINING_SWING_STATE = "swing";
    public static final String MINING_FINISH_STATE = "finish";

    public static PoseFunction<LocalSpacePose> constructMiningPoseFunction(
            PoseFunction<LocalSpacePose> idlePoseFunction,
            PoseFunction<LocalSpacePose> swingPoseFunction,
            PoseFunction<LocalSpacePose> finishPoseFunction,
            Transition idleToMiningTiming
    ) {
        return StateMachineFunction.builder(evaluationState -> MINING_IDLE_STATE)
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
                                        StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING.and(FirstPersonMining::isNoLongerMining)
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(MINING_FINISH_STATE, finishPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MINING_IDLE_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setPriority(50)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(20)).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(MINING_SWING_STATE)
                                .isTakenIfTrue(FirstPersonMining::isMining)
                                .setCanInterruptOtherTransitions(false)
                                .setPriority(60)
                                .setTiming(idleToMiningTiming)
                                .build())
                        .build())
                .build();
    }

    public static PoseFunction<LocalSpacePose> makeMainHandMiningPoseFunction(
            InteractionHand hand,
            PoseFunction<LocalSpacePose> miningStateMachine,
            Identifier miningStateMachineBasePose
    ) {
        PoseFunction<LocalSpacePose> basePoseFunction = FirstPersonHandPoseSwitching.constructCurrentBasePoseFunction(hand);
        return switch (hand) {
            case MAIN_HAND -> {
                PoseFunction<LocalSpacePose> pose;
                pose = MakeDynamicAdditiveFunction.of(
                        miningStateMachine,
                        SequenceEvaluatorFunction.builder(miningStateMachineBasePose).build()
                );
                pose = ApplyAdditiveFunction.of(basePoseFunction, pose);
                yield pose;
            }
            case OFF_HAND -> basePoseFunction;
        };
    }

    public static PoseFunction<LocalSpacePose> constructPickaxeMiningPoseFunction() {
        return FirstPersonMining.constructMiningPoseFunction(
                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_MINE_SWING)
                        .setLooping(true)
                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(16))
                        .setPlayRate(evaluationState -> 1.75f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_MINE_FINISH).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build());
    }

    public static PoseFunction<LocalSpacePose> constructMainHandPickaxeMiningPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {
        PoseFunction<LocalSpacePose> miningStateMachine = constructPickaxeMiningPoseFunction();
        return makeMainHandMiningPoseFunction(
                hand,
                miningStateMachine,
                FirstPersonAnimationSequences.HAND_TOOL_POSE
        );
    }

    public static PoseFunction<LocalSpacePose> constructEmptyHandMiningPoseFunction() {
        return FirstPersonMining.constructMiningPoseFunction(
                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_SWING)
                        .setLooping(true)
                        .setPlayRate(evaluationState -> 1.1f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_FINISH).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_IN_OUT).build());
    }

    public static PoseFunction<LocalSpacePose> constructMainHandEmptyHandMiningPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {
        PoseFunction<LocalSpacePose> miningStateMachine = constructEmptyHandMiningPoseFunction();
        return makeMainHandMiningPoseFunction(
                hand,
                miningStateMachine,
                FirstPersonAnimationSequences.HAND_EMPTY_POSE
        );
    }


    public static final String PUNCH_MINING_IDLE_STATE = "idle";
    public static final String PUNCH_MINING_SWING_A_STATE = "swing_a";
    public static final String PUNCH_MINING_SWING_B_STATE = "swing_b";
    public static final String PUNCH_MINING_FINISH_STATE = "finish";

    public static PoseFunction<LocalSpacePose> constructWithPunchMiningPoseFunction(PoseFunction<LocalSpacePose> inputPose) {

        PoseFunction<LocalSpacePose> swingAPose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_SWING)
                .setLooping(false)
                .setPlayRate(evaluationState -> 1.3f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                .build();
        PoseFunction<LocalSpacePose> swingBPose = MirrorFunction.of(swingAPose);
        PoseFunction<LocalSpacePose> finishPose = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_FINISH).build();

        PoseFunction<LocalSpacePose> miningStateMachine;
        miningStateMachine = StateMachineFunction.builder(evaluationState -> PUNCH_MINING_IDLE_STATE)
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

    public static boolean isMining(StateTransition.TransitionContext context) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.IS_MINING);
    }

    public static boolean isNoLongerMining(StateTransition.TransitionContext context) {
        return !isMining(context);
    }

    public static boolean isPunchMining(StateTransition.TransitionContext context) {
        boolean isMainHandInEmptyPose = context.driverContainer().getDriverValue(FirstPersonDrivers.MAIN_HAND_POSE) == FirstPersonHandPoses.EMPTY_MAIN_HAND;
        boolean isMainHandEmpty = context.driverContainer().getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM).isEmpty();
        return isMining(context) && isMainHandInEmptyPose && isMainHandEmpty;
    }

    public static boolean isNoLongerPunchMining(StateTransition.TransitionContext context) {
        boolean isMainHandEmpty = context.driverContainer().getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM).isEmpty();
        return isNoLongerMining(context) || !isMainHandEmpty;
    }

    public static boolean shouldInterruptFinishAnimation(StateTransition.TransitionContext context) {
        boolean hasAttacked = context.driverContainer().getDriverValue(FirstPersonDrivers.HAS_ATTACKED);
        boolean hasUsedItem = context.driverContainer().getDriverValue(FirstPersonDrivers.IS_USING_MAIN_HAND_ITEM);
        hasUsedItem = hasUsedItem || context.driverContainer().getDriverValue(FirstPersonDrivers.IS_USING_OFF_HAND_ITEM);
        hasUsedItem = hasUsedItem || context.driverContainer().getDriverValue(FirstPersonDrivers.HAS_USED_MAIN_HAND_ITEM);
        hasUsedItem = hasUsedItem || context.driverContainer().getDriverValue(FirstPersonDrivers.HAS_USED_OFF_HAND_ITEM);
        return hasAttacked || hasUsedItem;
    }

    public static void clearOffHandOfItems(PoseFunction.FunctionEvaluationState evaluationState) {
        evaluationState.driverContainer().getDriver(FirstPersonDrivers.RENDERED_OFF_HAND_ITEM).setValue(ItemStack.EMPTY);
        evaluationState.driverContainer().getDriver(FirstPersonDrivers.OFF_HAND_POSE).setValue(FirstPersonHandPoses.EMPTY_OFF_HAND);
        evaluationState.driverContainer().getDriver(FirstPersonDrivers.OFF_HAND_GENERIC_ITEM_POSE).setValue(FirstPersonGenericItems.getFallback());
    }
}
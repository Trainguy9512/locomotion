package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceEvaluatorFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.State;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;

public class FirstPersonMining {

    public static PoseFunction<LocalSpacePose> constructPoseFunction(
            CachedPoseContainer cachedPoseContainer,
            PoseFunction<LocalSpacePose> idlePoseFunction,
            PoseFunction<LocalSpacePose> swingPoseFunction,
            PoseFunction<LocalSpacePose> finishPoseFunction,
            Transition idleToMiningTiming
    ) {
        return StateMachineFunction.builder(evaluationState -> MiningStates.IDLE)
                .resetsUponRelevant(true)
                .defineState(State.builder(MiningStates.IDLE, idlePoseFunction)
                        .addOutboundTransition(StateTransition.builder(MiningStates.SWING)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_MINING))
                                .setTiming(idleToMiningTiming)
                                .build())
                        .build())
                .defineState(State.builder(MiningStates.SWING, swingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MiningStates.FINISH)
                                .isTakenIfTrue(
                                        StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING.and(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_MINING).negate())
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(State.builder(MiningStates.FINISH, finishPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MiningStates.IDLE)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setPriority(50)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(20)).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(MiningStates.SWING)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_MINING).and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setPriority(60)
                                .setTiming(idleToMiningTiming)
                                .build())
                        .build())
                .build();
    }

    enum MiningStates {
        IDLE,
        SWING,
        FINISH
    }

    public static PoseFunction<LocalSpacePose> makePickaxeMiningPoseFunction(CachedPoseContainer cachedPoseContainer) {
        return FirstPersonMining.constructPoseFunction(
                cachedPoseContainer,
                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_MINE_SWING)
                        .looping(true)
                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(16))
                        .setPlayRate(evaluationState -> 1.25f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_MINE_FINISH).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build());
    }
}
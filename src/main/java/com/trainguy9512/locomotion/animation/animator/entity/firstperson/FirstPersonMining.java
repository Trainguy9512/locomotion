package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;

public class FirstPersonMining {

    public static final String MINING_IDLE_STATE = "idle";
    public static final String MINING_SWING_STATE = "swing";
    public static final String MINING_FINISH_STATE = "finish";

    private static PoseFunction<LocalSpacePose> constructMiningPoseFunction(
            PoseFunction<LocalSpacePose> idlePoseFunction,
            PoseFunction<LocalSpacePose> swingPoseFunction,
            PoseFunction<LocalSpacePose> finishPoseFunction,
            Transition idleToMiningTiming
    ) {
        return StateMachineFunction.builder(evaluationState -> MINING_IDLE_STATE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(MINING_IDLE_STATE, idlePoseFunction)
                        .addOutboundTransition(StateTransition.builder(MINING_SWING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_MINING))
                                .setTiming(idleToMiningTiming)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(MINING_SWING_STATE, swingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MINING_FINISH_STATE)
                                .isTakenIfTrue(
                                        StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING.and(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_MINING).negate())
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
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_MINING).and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setPriority(60)
                                .setTiming(idleToMiningTiming)
                                .build())
                        .build())
                .build();
    }

    public static Identifier getCurrentBasePose(PoseFunction.FunctionInterpolationContext context, InteractionHand interactionHand) {
        Identifier currentHandPoseIdentifier = context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.getHandPoseDriver(interactionHand), context.partialTicks());
        if (currentHandPoseIdentifier == FirstPersonHandPoses.GENERIC_ITEM) {
            Identifier currentGenericItemPoseIdentifier = context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(interactionHand), context.partialTicks());
            FirstPersonGenericItems.GenericItemPoseDefinition definition = FirstPersonGenericItems.getOrThrowFromIdentifier(currentHandPoseIdentifier);
            return definition.basePoseSequence();
        } else {
            FirstPersonHandPoses.HandPoseDefinition definition = FirstPersonHandPoses.getOrThrowFromIdentifier(currentHandPoseIdentifier);
            return definition.basePoseSequence();
        }
    }

    public static PoseFunction<LocalSpacePose> makeMainHandMiningPoseFunction(
            InteractionHand interactionHand,
            PoseFunction<LocalSpacePose> miningStateMachine,
            Identifier miningStateMachineBasePose
    ) {
        PoseFunction<LocalSpacePose> basePoseFunction = SequenceEvaluatorFunction.builder(context -> getCurrentBasePose(context, interactionHand)).build();
        return switch (interactionHand) {
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

    public static PoseFunction<LocalSpacePose> makeMainHandPickaxeMiningPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        PoseFunction<LocalSpacePose> miningStateMachine = FirstPersonMining.constructMiningPoseFunction(
                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_MINE_SWING)
                        .setLooping(true)
                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(16))
                        .setPlayRate(evaluationState -> 1.75f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_MINE_FINISH).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build());
        return makeMainHandMiningPoseFunction(
                interactionHand,
                miningStateMachine,
                FirstPersonAnimationSequences.HAND_TOOL_POSE
        );
    }

    public static PoseFunction<LocalSpacePose> makePickaxeMiningPoseFunction(CachedPoseContainer cachedPoseContainer) {
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
}
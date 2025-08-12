package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.State;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.world.InteractionHand;

import java.util.Set;

public class FirstPersonSword {

    public static PoseFunction<LocalSpacePose> handSwordPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> StateMachineFunction.builder(evaluationState -> SwordSwingStates.IDLE)
                    .resetsUponRelevant(true)
                    .defineState(State.builder(SwordSwingStates.IDLE, FirstPersonHandPose.SWORD.getMiningStateMachine(cachedPoseContainer, interactionHand))
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_LEFT)
                                    .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .build())
                            .build())
                    .defineState(State.builder(SwordSwingStates.SWING_LEFT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_DOWN).build())
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_RIGHT)
                                    .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .build())
                            .build())
                    .defineState(State.builder(SwordSwingStates.SWING_RIGHT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_SWING_DOWN).build())
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_LEFT)
                                    .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .build())
                            .build())
                    .addStateAlias(StateAlias.builder(
                                    Set.of(
                                            SwordSwingStates.SWING_LEFT,
                                            SwordSwingStates.SWING_RIGHT
                                    ))
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.IDLE)
                                    .isTakenIfMostRelevantAnimationPlayerFinishing(0)
                                    .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                    .build())
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.IDLE)
                                    .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_MINING))
                                    .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build())
                                    .build())
                            .build())
                    .build();
            case OFF_HAND -> FirstPersonHandPose.SWORD.getMiningStateMachine(cachedPoseContainer, interactionHand);
        };
    }

    enum SwordSwingStates {
        IDLE,
        SWING_LEFT,
        SWING_RIGHT
    }
}
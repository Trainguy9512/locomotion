package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.ApplyAdditiveFunction;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceReferencePoint;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

import java.util.Objects;
import java.util.Set;

public class FirstPersonDrinking {

    public static final String DRINKING_IDLE_STATE = "idle";
    public static final String DRINKING_BEGIN_STATE = "drinking_begin";
    public static final String DRINKING_LOOP_STATE = "drinking_loop";
    public static final String DRINKING_FINISHED_STATE = "drinking_finished";

    public static PoseFunction<LocalSpacePose> constructWithDrinkingStateMachine(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand, PoseFunction<LocalSpacePose> idlePoseFunction) {
        PoseFunction<LocalSpacePose> drinkingLoopPoseFunction = ApplyAdditiveFunction.of(
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_PROGRESS)
                        .setPlayRate(evaluationState -> evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.ITEM_CONSUMPTION_SPEED))
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_LOOP)
                        .setLooping(true)
                        .setPlayRate(1f)
                        .isAdditive(true, SequenceReferencePoint.BEGINNING)
                        .build()
        );

        // Eating pose functions
        PoseFunction<LocalSpacePose> eatingLoopPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_EAT_LOOP)
                .setPlayRate(1.5f)
                .setLooping(true)
                .build();
        PoseFunction<LocalSpacePose> eatingBeginPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_EAT_BEGIN).build();

        return StateMachineFunction.builder(evaluationState -> DRINKING_IDLE_STATE)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(DRINKING_IDLE_STATE, idlePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                // Drinking
                .defineState(StateDefinition.builder(DRINKING_BEGIN_STATE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_BEGIN)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(DRINKING_LOOP_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(DRINKING_LOOP_STATE, drinkingLoopPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(DRINKING_FINISHED_STATE, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_FINISH)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(DRINKING_IDLE_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        DRINKING_BEGIN_STATE,
                                        DRINKING_LOOP_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(DRINKING_FINISHED_STATE)
                                .isTakenIfTrue(context -> !isDrinking(context, interactionHand))
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItem(evaluationState.driverContainer(), interactionHand))
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        DRINKING_FINISHED_STATE,
                                        DRINKING_IDLE_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(DRINKING_BEGIN_STATE)
                                .isTakenIfTrue(context -> isDrinking(context, interactionHand))
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(evaluationState -> updateConsumptionSpeed(evaluationState, interactionHand))
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        DRINKING_BEGIN_STATE,
                                        DRINKING_LOOP_STATE,
                                        DRINKING_FINISHED_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(DRINKING_IDLE_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.IS_MINING))
                                .setCanInterruptOtherTransitions(true)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(evaluationState -> updateConsumptionSpeed(evaluationState, interactionHand))
                                .build())
                        .build())
                .build();
    }

    public static void updateConsumptionSpeed(PoseFunction.FunctionEvaluationState evaluationState, InteractionHand interactionHand) {
        ItemStack item = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand));
        if (!item.has(DataComponents.CONSUMABLE)) {
            return;
        }
        float speed = Objects.requireNonNull(item.get(DataComponents.CONSUMABLE)).consumeSeconds();
        speed = 1f / Math.max(speed, 0.1f);
        evaluationState.driverContainer().getDriver(FirstPersonDrivers.ITEM_CONSUMPTION_SPEED).setValue(speed);
    }

    private static boolean isDrinking(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        OnTickDriverContainer driverContainer = context.driverContainer();
        if (!driverContainer.getDriverValue(FirstPersonDrivers.getUsingItemDriver(interactionHand))) {
            return false;
        }
        return driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand)).getUseAnimation() == ItemUseAnimation.DRINK;
    }
}

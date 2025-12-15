package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageSlotFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

import java.util.Objects;
import java.util.Set;

public class FirstPersonHandPoseSwitching {

    public static String getEntryHandPoseState(PoseFunction.FunctionEvaluationState evaluationState, InteractionHand hand) {
        Identifier handPoseIdentifier = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand));
        FirstPersonHandPoses.HandPoseDefinition definition = FirstPersonHandPoses.getOrThrowFromIdentifier(handPoseIdentifier);
        return definition.stateIdentifier();
    }

    public static final String HAND_POSE_DROPPING_LAST_ITEM_STATE = "dropping_last_item";
    public static final String HAND_POSE_USING_LAST_ITEM_STATE = "using_last_item";
    public static final String HAND_POSE_THROWING_TRIDENT_STATE = "throwing_trident";

    public static PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {

        StateMachineFunction.Builder stateMachineBuilder = StateMachineFunction.builder(evaluationState -> getEntryHandPoseState(evaluationState, hand));
        stateMachineBuilder.resetsUponRelevant(true);
        StateAlias.Builder fromLowerAliasBuilder = StateAlias.builder(Set.of());

        for (Identifier handPoseIdentifier : FirstPersonHandPoses.getRegisteredHandPoseDefinitions()) {
            FirstPersonHandPoses.HandPoseDefinition handPose = FirstPersonHandPoses.getOrThrowFromIdentifier(handPoseIdentifier);
            defineStatesForHandPose(cachedPoseContainer, hand, stateMachineBuilder, fromLowerAliasBuilder, handPoseIdentifier, handPose);
        }

        stateMachineBuilder.addStateAlias(fromLowerAliasBuilder.build());
        defineExtraStates(stateMachineBuilder, hand);

        PoseFunction<LocalSpacePose> pose = stateMachineBuilder.build();
        // Camera shake intensity for item interaction animations
        pose = BlendPosesFunction.builder(pose)
                .addBlendInput(
                        EmptyPoseFunction.of(),
                        functionEvaluationState -> 1 - LocomotionMain.CONFIG.data().firstPersonPlayer.cameraShakeItemInteractionIntensity,
                        FirstPersonJointAnimator.CAMERA_MASK
                )
                .build();

        return pose;
    }

    public static PoseFunction<LocalSpacePose> constructCurrentBasePoseFunction(InteractionHand hand) {
        return SequenceEvaluatorFunction.builder(context -> getCurrentBasePoseForAdditive(context, hand)).build();
    }

    public static Identifier getCurrentBasePoseForAdditive(PoseFunction.FunctionInterpolationContext context, InteractionHand hand) {
        Identifier currentHandPoseIdentifier = context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.getHandPoseDriver(hand), context.partialTicks());
        if (currentHandPoseIdentifier == FirstPersonHandPoses.GENERIC_ITEM) {
            Identifier currentGenericItemPoseIdentifier = context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(hand), context.partialTicks());
            FirstPersonGenericItems.GenericItemPoseDefinition definition = FirstPersonGenericItems.getOrThrowFromIdentifier(currentGenericItemPoseIdentifier);
            return definition.basePoseSequence();
        } else {
            FirstPersonHandPoses.HandPoseDefinition definition = FirstPersonHandPoses.getOrThrowFromIdentifier(currentHandPoseIdentifier);
            return definition.basePoseSequence();
        }
    }

    private static boolean shouldTransitionToThisRaiseState(
            StateTransition.TransitionContext context,
            Identifier handPoseIdentifier,
            InteractionHand hand
    ) {
        ItemStack currentItemStack = context.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(hand));
        Identifier handPoseFromCurrentItemStack = FirstPersonHandPoses.testForNextHandPose(currentItemStack, hand);
        return handPoseIdentifier == handPoseFromCurrentItemStack;
    }

    public static void defineStatesForHandPose(
            CachedPoseContainer cachedPoseContainer,
            InteractionHand hand,
            StateMachineFunction.Builder stateMachineBuilder,
            StateAlias.Builder fromLowerAliasBuilder,
            Identifier handPoseIdentifier,
            FirstPersonHandPoses.HandPoseDefinition handPose
    ) {
        String poseState = handPose.stateIdentifier();
        String lowerState = handPose.getLowerStateIdentifier();
        String raiseState = handPose.getRaiseStateIdentifier();

        PoseFunction<LocalSpacePose> posePoseFunction;
        posePoseFunction = handPose.poseFunctionProvider().apply(cachedPoseContainer, hand);
        posePoseFunction = MontageSlotFunction.of(posePoseFunction, FirstPersonMontages.getAttackSlot(hand));

        PoseFunction<LocalSpacePose> raisePoseFunction;
        raisePoseFunction = SequencePlayerFunction.builder(handPose.raiseSequence()).isAdditive(true, SequenceReferencePoint.END).build();
        raisePoseFunction = ApplyAdditiveFunction.of(constructCurrentBasePoseFunction(hand), raisePoseFunction);
        raisePoseFunction = MontageSlotFunction.of(raisePoseFunction, FirstPersonMontages.getAttackSlot(hand));

        PoseFunction<LocalSpacePose> lowerPoseFunction;
        lowerPoseFunction = SequencePlayerFunction.builder(handPose.lowerSequence()).isAdditive(true, SequenceReferencePoint.BEGINNING).build();
        lowerPoseFunction = ApplyAdditiveFunction.of(constructCurrentBasePoseFunction(hand), lowerPoseFunction);

        stateMachineBuilder
                .defineState(StateDefinition.builder(poseState, posePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(lowerState, lowerPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(raiseState, raisePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(poseState)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(handPose.raiseToPoseTransition())
                                .build())
                        .addOutboundTransition(StateTransition.builder(poseState)
                                .isTakenIfTrue(context -> shouldSkipRaiseAnimation(context, hand))
                                .setTiming(Transition.builder(TimeSpan.ofTicks(3)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        poseState,
                                        raiseState
                                ))
                        .addOutboundTransition(StateTransition.builder(lowerState)
                                .isTakenIfTrue(context -> shouldTakeHardSwitchTransition(context, hand))
                                .setTiming(handPose.poseToLowerTransition())
                                .setPriority(50)
                                .build())
                        .addOutboundTransition(StateTransition.builder(HAND_POSE_DROPPING_LAST_ITEM_STATE)
                                .isTakenIfTrue(context -> shouldTakeDropLastItemTransition(context, hand))
                                .setPriority(80)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(evaluationState.driverContainer(), hand))
                                .bindToOnTransitionTaken(evaluationState -> clearMontagesInAttackSlot(evaluationState, hand))
                                .build())
                        .addOutboundTransition(StateTransition.builder(HAND_POSE_USING_LAST_ITEM_STATE)
                                .isTakenIfTrue(context -> shouldTakeUseLastItemTransition(context, hand))
                                .setPriority(70)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(evaluationState.driverContainer(), hand))
                                .bindToOnTransitionTaken(evaluationState -> clearMontagesInAttackSlot(evaluationState, hand))
                                .build())
                        .addOutboundTransition(StateTransition.builder(HAND_POSE_THROWING_TRIDENT_STATE)
                                .isTakenIfTrue(context -> shouldTakeThrowTridentTransition(context, hand))
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
                                .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(evaluationState.driverContainer(), hand))
                                .bindToOnTransitionTaken(evaluationState -> clearMontagesInAttackSlot(evaluationState, hand))
                                .build())
                        .build());
        fromLowerAliasBuilder
                .addOriginatingState(lowerState)
                .addOutboundTransition(StateTransition.builder(raiseState)
                        .setTiming(Transition.INSTANT)
                        .isTakenIfTrue(StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING
                                .and(context -> shouldTransitionToThisRaiseState(context, handPoseIdentifier, hand))
                        )
                        .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(evaluationState.driverContainer(), hand))
                        .bindToOnTransitionTaken(evaluationState -> clearMontagesInAttackSlot(evaluationState, hand))
                        .build());
    }

    public static void defineExtraStates(StateMachineFunction.Builder stateMachineBuilder, InteractionHand hand) {
        FirstPersonHandPoses.HandPoseDefinition emptyHandPose = FirstPersonHandPoses.getOrThrowFromIdentifier(FirstPersonHandPoses.getEmptyHandPose(hand));

        PoseFunction<LocalSpacePose> useLastItemPoseFunction;
        useLastItemPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_USE).isAdditive(true, SequenceReferencePoint.END).build();
        useLastItemPoseFunction = ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(emptyHandPose.basePoseSequence()).build(), useLastItemPoseFunction);

        PoseFunction<LocalSpacePose> dropLastItemPoseFunction;
        dropLastItemPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_USE).isAdditive(true, SequenceReferencePoint.END).build();
        dropLastItemPoseFunction = ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(emptyHandPose.basePoseSequence()).build(), dropLastItemPoseFunction);

        PoseFunction<LocalSpacePose> throwingTridentPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_RELEASE_THROW).build();

        stateMachineBuilder.defineState(StateDefinition.builder(HAND_POSE_DROPPING_LAST_ITEM_STATE, dropLastItemPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(HAND_POSE_USING_LAST_ITEM_STATE, useLastItemPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(StateDefinition.builder(HAND_POSE_THROWING_TRIDENT_STATE, throwingTridentPoseFunction)
                        .addOutboundTransition(StateTransition.builder(emptyHandPose.getRaiseStateIdentifier())
                                .isTakenOnAnimationFinished(0)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                HAND_POSE_DROPPING_LAST_ITEM_STATE,
                                HAND_POSE_USING_LAST_ITEM_STATE
                        ))
                        .addOutboundTransition(StateTransition.builder(emptyHandPose.stateIdentifier())
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(emptyHandPose.stateIdentifier())
                                .isTakenIfTrue(context -> shouldCancelLastItemAnimation(context, hand))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.05f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build());
    }

    private static void clearMontagesInAttackSlot(PoseFunction.FunctionEvaluationState evaluationState, InteractionHand hand) {
        evaluationState.montageManager().interruptMontagesInSlot(FirstPersonMontages.getAttackSlot(hand), Transition.INSTANT);
    }


    private static boolean shouldCancelLastItemAnimation(StateTransition.TransitionContext context, InteractionHand hand) {
        if (context.driverContainer().getDriverValue(FirstPersonDrivers.getHasUsedItemDriver(hand))) {
            if (context.timeElapsedInCurrentState().inTicks() > 2) {
                return true;
            }
        }
        if (hand == InteractionHand.MAIN_HAND) {
            if (context.driverContainer().getDriverValue(FirstPersonDrivers.HAS_ATTACKED)) {
                return true;
            }
            if (context.driverContainer().getDriverValue(FirstPersonDrivers.IS_MINING)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasItemChanged(StateTransition.TransitionContext context, InteractionHand hand) {
        ItemStack itemPreviousTick = context.driverContainer().getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
        ItemStack itemCurrentTick = context.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(hand));
        if (!itemPreviousTick.is(itemCurrentTick.getItem())) {
            return true;
        }
        for (TypedDataComponent<?> dataComponent : itemCurrentTick.getComponents()) {
            if (dataComponent.type() == DataComponents.DAMAGE) {
                continue;
            }
            if (!itemPreviousTick.getComponents().has(dataComponent.type())) {
                return true;
            }
            if (!Objects.equals(itemPreviousTick.get(dataComponent.type()), dataComponent.value())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isNewItemEmpty(StateTransition.TransitionContext context, InteractionHand hand) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(hand)).isEmpty();
    }

    private static boolean isOldItemEmpty(StateTransition.TransitionContext context, InteractionHand hand) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand)).isEmpty();
    }

    private static boolean hasSelectedHotbarSlotChanged(StateTransition.TransitionContext context, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(FirstPersonDrivers.HOTBAR_SLOT).hasValueChanged();
    }

    private static boolean areAnyTwoHandedOverridesActive(StateTransition.TransitionContext context) {
        return !Objects.equals(context.driverContainer().getDriverValue(FirstPersonDrivers.CURRENT_TWO_HANDED_OVERRIDE_STATE), FirstPersonTwoHandedActions.TWO_HANDED_ACTION_NORMAL_STATE);
    }

    private static boolean shouldTakeHardSwitchTransition(StateTransition.TransitionContext context, InteractionHand hand) {
        if (areAnyTwoHandedOverridesActive(context)) {
            return false;
        }
        // Duct-tape solution for hand pose functions like consumables not being able to update the rendered item before the hard switch condition is updated.
        if (context.driverContainer().getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).getPreviousValue()) {
            ItemUseAnimation useAnimation = context.driverContainer().getDriver(FirstPersonDrivers.getItemCopyReferenceDriver(hand)).getPreviousValue().getUseAnimation();
            if (useAnimation == ItemUseAnimation.EAT) {
                return false;
            }
            if (useAnimation == ItemUseAnimation.DRINK) {
                return false;
            }
        }
        if (hasItemChanged(context, hand)) {
            return true;
        }
        if (hasSelectedHotbarSlotChanged(context, hand)) {
            if (!isNewItemEmpty(context, hand) || !isOldItemEmpty(context, hand)) {
                return true;
            }
        }
        return false;
    }

    private static boolean shouldTakeDropLastItemTransition(StateTransition.TransitionContext context, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return false;
        }
        if (!isNewItemEmpty(context, hand)) {
            return false;
        }
        return context.driverContainer().getDriver(FirstPersonDrivers.HAS_DROPPED_ITEM).hasBeenTriggered();
    }

    private static boolean shouldTakeUseLastItemTransition(StateTransition.TransitionContext context, InteractionHand hand) {
        // Required conditions for the "use last item" transition
        if (!isNewItemEmpty(context, hand)) {
            return false;
        }
        if (isOldItemEmpty(context, hand)) {
            return false;
        }
        // If any of these conditions are met, use the "use last item" transition
        if (context.driverContainer().getDriver(FirstPersonDrivers.getHasUsedItemDriver(hand)).hasBeenTriggered()) {
            return true;
        }
        return hand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(FirstPersonDrivers.HAS_ATTACKED).hasBeenTriggered();
    }

    private static boolean shouldTakeThrowTridentTransition(StateTransition.TransitionContext context, InteractionHand hand) {
        // Required conditions for the "throw trident" transition
        // Don't play the throw animation if the new item is not empty
        if (!isNewItemEmpty(context, hand)) {
            return false;
        }
        // Don't play the throw animation if the old item is empty
        if (isOldItemEmpty(context, hand)) {
            return false;
        }
        // Don't play the throw animation if the hotbar slot just changed
        if (hasSelectedHotbarSlotChanged(context, hand)) {
            return false;
        }
        // Don't play the throw animation if the player has just swapped items
        if (context.driverContainer().getDriver(FirstPersonDrivers.HAS_SWAPPED_ITEMS).hasBeenTriggered()) {
            return false;
        }
        // Don't play the throw animation if the player has a screen open
        if (context.driverContainer().getDriverValue(FirstPersonDrivers.HAS_SCREEN_OPEN)) {
            return false;
        }
//        if (!context.driverContainer().getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).getPreviousValue()) {
//            return false;
//        }
        // If any of these conditions are met, use the "throw trident" transition
        boolean previousItemWasTrident = context.driverContainer().getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand)) == FirstPersonHandPoses.TRIDENT;
        boolean wasJustUsingItem = context.driverContainer().getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).getPreviousValue();
        if (previousItemWasTrident) {
            return true;
        }
        return false;
    }

    private static boolean shouldSkipRaiseAnimation(StateTransition.TransitionContext context, InteractionHand hand) {
        if (context.driverContainer().getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand))) {
            return true;
        }
        if (context.driverContainer().getDriverValue(FirstPersonDrivers.getHasUsedItemDriver(hand))) {
            return true;
        }
        if (hand == InteractionHand.MAIN_HAND) {
            if (context.driverContainer().getDriverValue(FirstPersonDrivers.IS_MINING)) {
                return true;
            }
            if (context.driverContainer().getDriverValue(FirstPersonDrivers.HAS_ATTACKED)) {
                return true;
            }
        }
        return false;
    }
}

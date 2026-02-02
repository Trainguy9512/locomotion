package com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.*;
import com.trainguy9512.locomotion.animation.data.PoseCalculationContext;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageSlotFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.*;
import com.trainguy9512.locomotion.animation.util.Easing;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;

import java.util.Objects;
import java.util.Set;

public class FirstPersonHandPoseSwitching {

    public static String getEntryHandPoseState(PoseTickEvaluationContext context, InteractionHand hand) {
//        if (hand == InteractionHand.OFF_HAND) {
//            return FirstPersonHandPoses.getOrThrowFromIdentifier(FirstPersonHandPoses.EMPTY_OFF_HAND).getLowerStateIdentifier();
//        }

        Identifier handPoseIdentifier = context.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand));
        FirstPersonHandPoses.HandPoseDefinition definition = FirstPersonHandPoses.getOrThrowFromIdentifier(handPoseIdentifier);
        return definition.stateIdentifier();
    }

    public static final String HAND_POSE_DROPPING_LAST_ITEM_STATE = "dropping_last_item";
    public static final String HAND_POSE_USING_LAST_ITEM_STATE = "using_last_item";
    public static final String HAND_POSE_THROWING_TRIDENT_STATE = "throwing_trident";

    public static PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand hand) {

        StateMachineFunction.Builder stateMachineBuilder = StateMachineFunction.builder(context -> getEntryHandPoseState(context, hand));
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
                        context -> 1 - LocomotionMain.CONFIG.data().firstPersonPlayer.cameraShakeItemInteractionIntensity,
                        FirstPersonJointAnimator.CAMERA_MASK
                )
                .build();

        return pose;
    }

    public static PoseFunction<LocalSpacePose> constructCurrentBasePoseFunction(InteractionHand hand) {
        return SequenceEvaluatorFunction.builder(context -> getCurrentBasePoseForAdditive(context, hand)).build();
    }

    public static Identifier getCurrentBasePoseForAdditive(PoseCalculationContext context, InteractionHand hand) {
        Identifier currentHandPoseIdentifier = context.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand));
        FirstPersonHandPoses.HandPoseDefinition definition = FirstPersonHandPoses.getOrThrowFromIdentifier(currentHandPoseIdentifier);
        return definition.currentBasePoseSupplier().apply(context, hand);
    }



    private static boolean shouldChooseThisHandPose(
            StateTransitionContext context,
            Identifier handPoseIdentifier,
            InteractionHand hand
    ) {
        ItemStack currentItemStack = context.getDriverValue(FirstPersonDrivers.getItemDriver(hand));
        Identifier handPoseFromCurrentItemStack = FirstPersonHandPoses.testForNextHandPose(currentItemStack, hand);
        return handPoseIdentifier == handPoseFromCurrentItemStack;
    }

    private static boolean shouldTransitionToThisRaiseState(
            StateTransitionContext context,
            Identifier handPoseIdentifier,
            InteractionHand hand
    ) {
        if (!StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING.test(context)) {
            return false;
        }
        return shouldChooseThisHandPose(context, handPoseIdentifier, hand);
    }

    private static boolean shouldSkipStraightToPoseStateForAttack(
            StateTransitionContext context,
            Identifier handPoseIdentifier,
            InteractionHand hand
    ) {
        if (hand == InteractionHand.OFF_HAND) {
            return false;
        }
        if (!context.getDriverValue(FirstPersonDrivers.HAS_ATTACKED)) {
            return false;
        }
        return shouldChooseThisHandPose(context, handPoseIdentifier, hand);
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

        PoseFunction<LocalSpacePose> basePoseFunction = handPose.constructBasePoseFunction(hand);

        PoseFunction<LocalSpacePose> miningPoseFunction = switch (hand) {
            case MAIN_HAND -> handPose.miningPoseFunctionSupplier().get();
            case OFF_HAND -> basePoseFunction;
        };

        PoseFunction<LocalSpacePose> posePoseFunction;
        posePoseFunction = handPose.poseFunctionSupplier().constructHandPose(cachedPoseContainer, hand, miningPoseFunction);
        posePoseFunction = MontageSlotFunction.of(posePoseFunction, FirstPersonMontages.getAttackSlot(hand));

        PoseFunction<LocalSpacePose> raisePoseFunction;
        raisePoseFunction = SequencePlayerFunction.builder(handPose.raiseSequence()).isAdditive(true, SequenceReferencePoint.END).build();
        raisePoseFunction = ApplyAdditiveFunction.of(basePoseFunction, raisePoseFunction);
        raisePoseFunction = MontageSlotFunction.of(raisePoseFunction, FirstPersonMontages.getAttackSlot(hand));

        PoseFunction<LocalSpacePose> lowerPoseFunction;
        lowerPoseFunction = SequencePlayerFunction.builder(handPose.lowerSequence()).isAdditive(true, SequenceReferencePoint.BEGINNING).build();
        lowerPoseFunction = ApplyAdditiveFunction.of(basePoseFunction, lowerPoseFunction);

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
                                .bindToOnTransitionTaken(context -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(context, hand))
                                .bindToOnTransitionTaken(context -> clearMontagesInAttackSlot(context, hand))
                                .build())
                        .addOutboundTransition(StateTransition.builder(HAND_POSE_USING_LAST_ITEM_STATE)
                                .isTakenIfTrue(context -> shouldTakeUseLastItemTransition(context, hand))
                                .setPriority(70)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                .bindToOnTransitionTaken(context -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(context, hand))
                                .bindToOnTransitionTaken(context -> clearMontagesInAttackSlot(context, hand))
                                .build())
                        .addOutboundTransition(StateTransition.builder(HAND_POSE_THROWING_TRIDENT_STATE)
                                .isTakenIfTrue(context -> shouldTakeThrowTridentTransition(context, hand))
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
                                .bindToOnTransitionTaken(context -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(context, hand))
                                .bindToOnTransitionTaken(context -> clearMontagesInAttackSlot(context, hand))
                                .build())
                        .build());
        fromLowerAliasBuilder
                .addOriginatingState(lowerState)
                .addOutboundTransition(StateTransition.builder(raiseState)
                        .setTiming(Transition.INSTANT)
                        .isTakenIfTrue(context -> shouldTransitionToThisRaiseState(context, handPoseIdentifier, hand))
                        .bindToOnTransitionTaken(context -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(context, hand))
                        .bindToOnTransitionTaken(context -> clearMontagesInAttackSlot(context, hand))
                        .build())
                .addOutboundTransition(StateTransition.builder(poseState)
                        .setTiming(Transition.INSTANT)
                        .isTakenIfTrue(context -> shouldSkipStraightToPoseStateForAttack(context, handPoseIdentifier, hand))
                        .bindToOnTransitionTaken(context -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(context, hand))
                        .build());;
    }

    public static void defineExtraStates(StateMachineFunction.Builder stateMachineBuilder, InteractionHand hand) {
        FirstPersonHandPoses.HandPoseDefinition emptyHandPose = FirstPersonHandPoses.getOrThrowFromIdentifier(FirstPersonHandPoses.getEmptyHandPose(hand));

        PoseFunction<LocalSpacePose> useLastItemPoseFunction;
        useLastItemPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_USE).isAdditive(true, SequenceReferencePoint.END).build();
        useLastItemPoseFunction = ApplyAdditiveFunction.of(emptyHandPose.constructBasePoseFunction(hand), useLastItemPoseFunction);

        PoseFunction<LocalSpacePose> dropLastItemPoseFunction;
        dropLastItemPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_USE).isAdditive(true, SequenceReferencePoint.END).build();
        dropLastItemPoseFunction = ApplyAdditiveFunction.of(emptyHandPose.constructBasePoseFunction(hand), dropLastItemPoseFunction);

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

    private static void clearMontagesInAttackSlot(PoseTickEvaluationContext context, InteractionHand hand) {
        context.montageManager().interruptMontagesInSlot(FirstPersonMontages.getAttackSlot(hand), Transition.INSTANT);
    }


    private static boolean shouldCancelLastItemAnimation(StateTransitionContext context, InteractionHand hand) {
        if (context.getDriverValue(FirstPersonDrivers.getHasUsedItemDriver(hand))) {
            if (context.timeElapsedInCurrentState().inTicks() > 2) {
                return true;
            }
        }
        if (hand == InteractionHand.MAIN_HAND) {
            if (context.getDriverValue(FirstPersonDrivers.HAS_ATTACKED)) {
                return true;
            }
            if (context.getDriverValue(FirstPersonDrivers.IS_MINING)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasItemChanged(StateTransitionContext context, InteractionHand hand) {
        ItemStack itemPreviousTick = context.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
        ItemStack itemCurrentTick = context.getDriverValue(FirstPersonDrivers.getItemDriver(hand));
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

    private static boolean isNewItemEmpty(StateTransitionContext context, InteractionHand hand) {
        return context.getDriverValue(FirstPersonDrivers.getItemDriver(hand)).isEmpty();
    }

    private static boolean isOldItemEmpty(StateTransitionContext context, InteractionHand hand) {
        return context.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand)).isEmpty();
    }

    private static boolean hasSelectedHotbarSlotChanged(StateTransitionContext context, InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND && context.getDriver(FirstPersonDrivers.HOTBAR_SLOT).hasValueChanged();
    }

    private static boolean areAnyTwoHandedOverridesActive(StateTransitionContext context) {
        return !Objects.equals(context.getDriverValue(FirstPersonDrivers.CURRENT_TWO_HANDED_OVERRIDE_STATE), FirstPersonTwoHandedActions.TWO_HANDED_ACTION_NORMAL_STATE);
    }

    private static boolean shouldTakeHardSwitchTransition(StateTransitionContext context, InteractionHand hand) {
        if (areAnyTwoHandedOverridesActive(context)) {
            return false;
        }
        // Duct-tape solution for hand pose functions like consumables not being able to update the rendered item before the hard switch condition is updated.
        if (context.getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).getPreviousValue()) {
            ItemUseAnimation useAnimation = context.getDriver(FirstPersonDrivers.getItemCopyReferenceDriver(hand)).getPreviousValue().getUseAnimation();
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

    private static boolean shouldTakeDropLastItemTransition(StateTransitionContext context, InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND) {
            return false;
        }
        if (!isNewItemEmpty(context, hand)) {
            return false;
        }
        return context.getDriver(FirstPersonDrivers.HAS_DROPPED_ITEM).hasBeenTriggered();
    }

    private static boolean shouldTakeUseLastItemTransition(StateTransitionContext context, InteractionHand hand) {
        // Required conditions for the "use last item" transition
        if (!isNewItemEmpty(context, hand)) {
            return false;
        }
        if (isOldItemEmpty(context, hand)) {
            return false;
        }
        // If any of these conditions are met, use the "use last item" transition
        if (context.getDriver(FirstPersonDrivers.getHasUsedItemDriver(hand)).hasBeenTriggered()) {
            return true;
        }
        return hand == InteractionHand.MAIN_HAND && context.getDriver(FirstPersonDrivers.HAS_ATTACKED).hasBeenTriggered();
    }

    private static boolean shouldTakeThrowTridentTransition(StateTransitionContext context, InteractionHand hand) {
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
        if (context.getDriver(FirstPersonDrivers.HAS_SWAPPED_ITEMS).hasBeenTriggered()) {
            return false;
        }
        // Don't play the throw animation if the player has a screen open
        if (context.getDriverValue(FirstPersonDrivers.HAS_SCREEN_OPEN)) {
            return false;
        }
//        if (!context.getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).getPreviousValue()) {
//            return false;
//        }
        // If any of these conditions are met, use the "throw trident" transition
        boolean previousItemWasTrident = context.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand)) == FirstPersonHandPoses.TRIDENT;
        boolean wasJustUsingItem = context.getDriver(FirstPersonDrivers.getUsingItemDriver(hand)).getPreviousValue();
        if (previousItemWasTrident) {
            return true;
        }
        return false;
    }

    private static boolean shouldSkipRaiseAnimation(StateTransitionContext context, InteractionHand hand) {
        if (context.getDriverValue(FirstPersonDrivers.getUsingItemDriver(hand))) {
            return true;
        }
        if (context.getDriverValue(FirstPersonDrivers.getHasUsedItemDriver(hand))) {
            return true;
        }
        if (hand == InteractionHand.MAIN_HAND) {
            if (context.getDriverValue(FirstPersonDrivers.IS_MINING)) {
                return true;
            }
            if (context.getDriverValue(FirstPersonDrivers.HAS_ATTACKED)) {
                return true;
            }
        }
        return false;
    }
}

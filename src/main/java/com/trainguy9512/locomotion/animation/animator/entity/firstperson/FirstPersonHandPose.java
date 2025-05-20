package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.TriggerDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageSlotFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.State;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public enum FirstPersonHandPose {
    EMPTY (HandPoseStates.EMPTY_RAISE, HandPoseStates.EMPTY_LOWER, HandPoseStates.EMPTY, FirstPersonAnimationSequences.HAND_EMPTY_POSE, FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE),
    GENERIC_ITEM (HandPoseStates.GENERIC_ITEM_RAISE, HandPoseStates.GENERIC_ITEM_LOWER, HandPoseStates.GENERIC_ITEM, FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE, FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE),
    TOOL (HandPoseStates.TOOL_RAISE, HandPoseStates.TOOL_LOWER, HandPoseStates.TOOL, FirstPersonAnimationSequences.HAND_TOOL_POSE, FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE),
    SWORD (HandPoseStates.SWORD_RAISE, HandPoseStates.SWORD_LOWER, HandPoseStates.SWORD, FirstPersonAnimationSequences.HAND_TOOL_POSE, null),
    SHIELD (HandPoseStates.SHIELD_RAISE, HandPoseStates.SHIELD_LOWER, HandPoseStates.SHIELD, FirstPersonAnimationSequences.HAND_SHIELD_POSE, FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE),
    BOW (HandPoseStates.BOW_RAISE, HandPoseStates.BOW_LOWER, HandPoseStates.BOW, FirstPersonAnimationSequences.HAND_BOW_POSE, FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE),
    CROSSBOW (HandPoseStates.CROSSBOW_RAISE, HandPoseStates.CROSSBOW_LOWER, HandPoseStates.CROSSBOW, FirstPersonAnimationSequences.HAND_CROSSBOW_POSE, FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE);

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/FPJointAnimator/HandPose");

    public final HandPoseStates raisingState;
    public final HandPoseStates loweringState;
    public final HandPoseStates poseState;
    public final ResourceLocation basePoseLocation;
    public final MontageConfiguration attackMontage;

    FirstPersonHandPose(HandPoseStates raisingState, HandPoseStates loweringState, HandPoseStates poseState, ResourceLocation basePoseLocation, MontageConfiguration attackMontage) {
        this.raisingState = raisingState;
        this.loweringState = loweringState;
        this.poseState = poseState;
        this.basePoseLocation = basePoseLocation;
        this.attackMontage = attackMontage;
    }

    private static final List<TagKey<Item>> TOOL_ITEM_TAGS = List.of(
            ItemTags.PICKAXES,
            ItemTags.AXES,
            ItemTags.SHOVELS,
            ItemTags.HOES
    );

    public static FirstPersonHandPose fromItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return EMPTY;
        }
        if (itemStack.getUseAnimation() == ItemUseAnimation.CROSSBOW) {
            return CROSSBOW;
        }
        if (itemStack.getUseAnimation() == ItemUseAnimation.BOW) {
            return BOW;
        }
        if (itemStack.getUseAnimation() == ItemUseAnimation.BLOCK) {
            return SHIELD;
        }
        if (itemStack.is(ItemTags.SWORDS)) {
            return SWORD;
        }
        for (TagKey<Item> tag : TOOL_ITEM_TAGS) {
            if (itemStack.is(tag)) {
                return TOOL;
            }
        }
        return GENERIC_ITEM;
    }

    public PoseFunction<LocalSpacePose> getMiningStateMachine(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> switch (this) {
                case TOOL -> FirstPersonMining.constructPoseFunction(
                        cachedPoseContainer,
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_MINE_SWING)
                                .looping(true)
                                .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(16))
                                .setPlayRate(evaluationState -> 1.15f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                                .build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_PICKAXE_MINE_FINISH).build(),
                        Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build());
                default -> ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(this.basePoseLocation).build(), MakeDynamicAdditiveFunction.of(
                        FirstPersonMining.constructPoseFunction(
                                cachedPoseContainer,
                                SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build(),
                                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_SWING)
                                        .looping(true)
                                        .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(20))
                                        .setPlayRate(evaluationState -> 1.35f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                                        .build(),
                                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_FINISH).build(),
                                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()),
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build()));
            };
            case OFF_HAND -> SequenceEvaluatorFunction.builder(this.basePoseLocation).build();
        };
    }

    public enum HandPoseStates {
        DROPPING_LAST_ITEM,
        USING_LAST_ITEM,
        EMPTY,
        EMPTY_RAISE,
        EMPTY_LOWER,
        GENERIC_ITEM,
        GENERIC_ITEM_RAISE,
        GENERIC_ITEM_LOWER,
        TOOL,
        TOOL_RAISE,
        TOOL_LOWER,
        SWORD,
        SWORD_RAISE,
        SWORD_LOWER,
        SHIELD,
        SHIELD_RAISE,
        SHIELD_LOWER,
        BOW,
        BOW_RAISE,
        BOW_LOWER,
        CROSSBOW,
        CROSSBOW_RAISE,
        CROSSBOW_LOWER
    }

    public static PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {

        StateMachineFunction.Builder<HandPoseStates> handPoseStateMachineBuilder = switch (interactionHand) {
            case MAIN_HAND -> StateMachineFunction.builder(evaluationState -> evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.MAIN_HAND_POSE).poseState);
            case OFF_HAND -> StateMachineFunction.builder(evaluationState -> evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.OFF_HAND_POSE).poseState);
        };

        handPoseStateMachineBuilder.resetsUponRelevant(true);
        StateAlias.Builder<HandPoseStates> fromLoweringAliasBuilder = StateAlias.builder(Set.of(HandPoseStates.EMPTY_LOWER));

        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.GENERIC_ITEM,
                FirstPersonGenericItemPose.constructPoseFunction(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(context -> context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(interactionHand), 1).basePoseLocation).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(context -> context.driverContainer().getInterpolatedDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(interactionHand), 1).basePoseLocation).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.TOOL,
                FirstPersonHandPose.TOOL.getMiningStateMachine(cachedPoseContainer, interactionHand),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_LOWER).build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_RAISE).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.SWORD,
                FirstPersonSword.handSwordPoseFunction(cachedPoseContainer, interactionHand),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_LOWER).build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_RAISE).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.SHIELD,
                FirstPersonShield.handShieldPoseFunction(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SHIELD_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.BOW,
                FirstPersonHandPose.BOW.getMiningStateMachine(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_BOW_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_BOW_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.CROSSBOW,
                FirstPersonHandPose.CROSSBOW.getMiningStateMachine(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_CROSSBOW_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_CROSSBOW_RAISE).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.EMPTY,
                switch (interactionHand) {
                    case MAIN_HAND -> FirstPersonHandPose.EMPTY.getMiningStateMachine(cachedPoseContainer, interactionHand);
                    case OFF_HAND -> SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_LOWERED).build();
                },
                switch (interactionHand) {
                    case MAIN_HAND -> SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_LOWER).build();
                    case OFF_HAND -> SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_LOWERED).build();
                },
                switch (interactionHand) {
                    case MAIN_HAND -> SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_RAISE).build();
                    case OFF_HAND -> SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_LOWERED).build();
                },
                switch (interactionHand) {
                    case MAIN_HAND -> Transition.builder(TimeSpan.of60FramesPerSecond(7)).setEasement(Easing.SINE_IN_OUT).build();
                    case OFF_HAND -> Transition.INSTANT;
                },
                switch (interactionHand) {
                    case MAIN_HAND -> Transition.builder(TimeSpan.of60FramesPerSecond(18)).setEasement(Easing.SINE_IN_OUT).build();
                    case OFF_HAND -> Transition.INSTANT;
                }
        );
        handPoseStateMachineBuilder.addStateAlias(fromLoweringAliasBuilder.build())
                .defineState(State.builder(HandPoseStates.DROPPING_LAST_ITEM,
                                ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build(), MakeDynamicAdditiveFunction.of(SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_USE).build(), SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build()))
                        )
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.EMPTY)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(HandPoseStates.USING_LAST_ITEM,
                                ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build(), MakeDynamicAdditiveFunction.of(SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_USE).build(), SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build()))
                        )
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.EMPTY)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build());


        return handPoseStateMachineBuilder.build();
    }

    public static void addStatesForHandPose(
            StateMachineFunction.Builder<HandPoseStates> stateMachineBuilder,
            StateAlias.Builder<HandPoseStates> fromLoweringAliasBuilder,
            InteractionHand interactionHand,
            FirstPersonHandPose handPose,
            PoseFunction<LocalSpacePose> posePoseFunction,
            PoseFunction<LocalSpacePose> loweringPoseFunction,
            PoseFunction<LocalSpacePose> raisingPoseFunction,
            Transition poseToLoweringTiming,
            Transition raisingToPoseTiming
    ) {
        DriverKey<TriggerDriver> hasUsedItemDriver = switch (interactionHand) {
            case MAIN_HAND -> FirstPersonDrivers.HAS_USED_MAIN_HAND_ITEM;
            case OFF_HAND -> FirstPersonDrivers.HAS_USED_OFF_HAND_ITEM;
        };

        Predicate<StateTransition.TransitionContext> itemHasChanged = context -> {
            ItemStack itemPreviousTick = context.driverContainer().getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand));
            ItemStack itemCurrentTick = context.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand));
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
        };
        Predicate<StateTransition.TransitionContext> hotbarHasChanged = context -> interactionHand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(FirstPersonDrivers.HOTBAR_SLOT).hasValueChanged();
        Predicate<StateTransition.TransitionContext> newItemIsEmpty = context -> context.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand)).isEmpty();
        Predicate<StateTransition.TransitionContext> oldItemIsEmpty = context -> context.driverContainer().getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand)).isEmpty();
        Predicate<StateTransition.TransitionContext> noTwoHandedOverrides = context -> context.driverContainer().getDriverValue(FirstPersonDrivers.CURRENT_TWO_HANDED_OVERRIDE_STATE) == FirstPersonTwoHandedActions.TwoHandedActionStates.NORMAL;

        Predicate<StateTransition.TransitionContext> hardSwitchCondition = (hotbarHasChanged.and(newItemIsEmpty.and(oldItemIsEmpty).negate()).or(itemHasChanged)).and(noTwoHandedOverrides);
        Predicate<StateTransition.TransitionContext> dropLastItemCondition = newItemIsEmpty.and(context -> interactionHand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(FirstPersonDrivers.HAS_DROPPED_ITEM).hasBeenTriggered());
        Predicate<StateTransition.TransitionContext> useLastItemCondition = itemHasChanged.and(newItemIsEmpty).and(context -> context.driverContainer().getDriver(hasUsedItemDriver).hasBeenTriggered() || (interactionHand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(FirstPersonDrivers.HAS_ATTACKED).hasBeenTriggered()));

        Predicate<StateTransition.TransitionContext> skipRaiseAnimationCondition = StateTransition.booleanDriverPredicate(FirstPersonDrivers.getUsingItemDriver(interactionHand));
        if (interactionHand == InteractionHand.MAIN_HAND) {
            skipRaiseAnimationCondition = skipRaiseAnimationCondition.or(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_MINING)).or(StateTransition.booleanDriverPredicate(FirstPersonDrivers.HAS_ATTACKED)).or(StateTransition.booleanDriverPredicate(FirstPersonDrivers.HAS_USED_MAIN_HAND_ITEM));
        }
        Consumer<PoseFunction.FunctionEvaluationState> updateRenderedItem = evaluationState -> {
            if (evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.CURRENT_TWO_HANDED_OVERRIDE_STATE) == FirstPersonTwoHandedActions.TwoHandedActionStates.NORMAL) {
                FirstPersonDrivers.updateRenderedItem(evaluationState.driverContainer(), interactionHand);
            }
        };
        Consumer<PoseFunction.FunctionEvaluationState> clearAttackMontages = evaluationState -> {
            LOGGER.info("interrupted, {}, {}", evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.HAS_USED_MAIN_HAND_ITEM), evaluationState.currentTick());
            evaluationState.montageManager().interruptMontagesInSlot(FirstPersonMontages.MAIN_HAND_ATTACK_SLOT, Transition.INSTANT);
        };

        stateMachineBuilder
                .defineState(State.builder(handPose.poseState, MontageSlotFunction.of(posePoseFunction, interactionHand == InteractionHand.MAIN_HAND ? FirstPersonMontages.MAIN_HAND_ATTACK_SLOT : FirstPersonMontages.OFF_HAND_ATTACK_SLOT))
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(handPose.loweringState, loweringPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(handPose.raisingState, MontageSlotFunction.of(raisingPoseFunction, interactionHand == InteractionHand.MAIN_HAND ? FirstPersonMontages.MAIN_HAND_ATTACK_SLOT : FirstPersonMontages.OFF_HAND_ATTACK_SLOT))
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(handPose.poseState)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1f)
                                .setTiming(raisingToPoseTiming)
                                .build())
                        .addOutboundTransition(StateTransition.builder(handPose.poseState)
                                .isTakenIfTrue(skipRaiseAnimationCondition)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(3)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        handPose.poseState,
                                        handPose.raisingState
                                ))
                        .addOutboundTransition(StateTransition.builder(handPose.loweringState)
                                .isTakenIfTrue(hardSwitchCondition)
                                .setTiming(poseToLoweringTiming)
                                .setPriority(50)
                                .build())
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.DROPPING_LAST_ITEM)
                                .isTakenIfTrue(dropLastItemCondition)
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                .bindToOnTransitionTaken(updateRenderedItem)
                                .bindToOnTransitionTaken(clearAttackMontages)
                                .build())
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.USING_LAST_ITEM)
                                .isTakenIfTrue(useLastItemCondition)
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                .bindToOnTransitionTaken(updateRenderedItem)
                                .bindToOnTransitionTaken(clearAttackMontages)
                                .build())
                        .build());
        fromLoweringAliasBuilder
                .addOriginatingState(handPose.loweringState)
                .addOutboundTransition(StateTransition.builder(handPose.raisingState)
                        .setTiming(Transition.INSTANT)
                        .isTakenIfTrue(StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING
                                .and(context -> FirstPersonHandPose.fromItemStack(context.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand))) == handPose)
                        )
                        .bindToOnTransitionTaken(updateRenderedItem)
                        .bindToOnTransitionTaken(clearAttackMontages)
                        .build());
    }
}
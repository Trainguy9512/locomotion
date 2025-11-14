package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

public enum FirstPersonHandPose {
    EMPTY (
            HandPoseStates.EMPTY_RAISE,
            HandPoseStates.EMPTY_LOWER,
            HandPoseStates.EMPTY,
            FirstPersonAnimationSequences.HAND_EMPTY_POSE
    ),
    GENERIC_ITEM (
            HandPoseStates.GENERIC_ITEM_RAISE,
            HandPoseStates.GENERIC_ITEM_LOWER,
            HandPoseStates.GENERIC_ITEM,
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE
    ),
    TOOL (
            HandPoseStates.TOOL_RAISE,
            HandPoseStates.TOOL_LOWER,
            HandPoseStates.TOOL,
            FirstPersonAnimationSequences.HAND_TOOL_POSE
    ),
    SWORD (
            HandPoseStates.SWORD_RAISE,
            HandPoseStates.SWORD_LOWER,
            HandPoseStates.SWORD,
            FirstPersonAnimationSequences.HAND_TOOL_POSE
    ),
    SHIELD (
            HandPoseStates.SHIELD_RAISE,
            HandPoseStates.SHIELD_LOWER,
            HandPoseStates.SHIELD,
            FirstPersonAnimationSequences.HAND_SHIELD_POSE
    ),
    BOW (
            HandPoseStates.BOW_RAISE,
            HandPoseStates.BOW_LOWER,
            HandPoseStates.BOW,
            FirstPersonAnimationSequences.HAND_BOW_POSE
    ),
    CROSSBOW (
            HandPoseStates.CROSSBOW_RAISE,
            HandPoseStates.CROSSBOW_LOWER,
            HandPoseStates.CROSSBOW,
            FirstPersonAnimationSequences.HAND_CROSSBOW_POSE
    ),
    TRIDENT (
            HandPoseStates.TRIDENT_RAISE,
            HandPoseStates.TRIDENT_LOWER,
            HandPoseStates.TRIDENT,
            FirstPersonAnimationSequences.HAND_TRIDENT_POSE
    ),
    BRUSH (
            HandPoseStates.BRUSH_RAISE,
            HandPoseStates.BRUSH_LOWER,
            HandPoseStates.BRUSH,
            FirstPersonAnimationSequences.HAND_BRUSH_POSE
    ),
    MACE (
            HandPoseStates.MACE_RAISE,
            HandPoseStates.MACE_LOWER,
            HandPoseStates.MACE,
            FirstPersonAnimationSequences.HAND_MACE_POSE
    ),
    SPYGLASS (
            HandPoseStates.SPYGLASS_RAISE,
            HandPoseStates.SPYGLASS_LOWER,
            HandPoseStates.SPYGLASS,
            FirstPersonAnimationSequences.HAND_SPYGLASS_POSE
    ),
    MAP (
            HandPoseStates.MAP_RAISE,
            HandPoseStates.MAP_LOWER,
            HandPoseStates.MAP,
            FirstPersonAnimationSequences.HAND_MAP_SINGLE_HAND_POSE
    );

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/FPJointAnimator/HandPose");

    public final HandPoseStates raisingState;
    public final HandPoseStates loweringState;
    public final HandPoseStates poseState;
    public final ResourceLocation basePoseLocation;

    FirstPersonHandPose(
            HandPoseStates raisingState,
            HandPoseStates loweringState,
            HandPoseStates poseState,
            ResourceLocation basePoseLocation
    ) {
        this.raisingState = raisingState;
        this.loweringState = loweringState;
        this.poseState = poseState;
        this.basePoseLocation = basePoseLocation;
    }

    public static final List<TagKey<Item>> TOOL_ITEM_TAGS = List.of(
            ItemTags.PICKAXES,
            ItemTags.AXES,
            ItemTags.SHOVELS,
            ItemTags.HOES
    );

    public static Map<ItemUseAnimation, FirstPersonHandPose> POSES_BY_USE_ANIMATION = Maps.newHashMap();

    static {
        POSES_BY_USE_ANIMATION.put(ItemUseAnimation.BRUSH, BRUSH);
        POSES_BY_USE_ANIMATION.put(ItemUseAnimation.SPEAR, TRIDENT);
        POSES_BY_USE_ANIMATION.put(ItemUseAnimation.CROSSBOW, CROSSBOW);
        POSES_BY_USE_ANIMATION.put(ItemUseAnimation.BOW, BOW);
        POSES_BY_USE_ANIMATION.put(ItemUseAnimation.BLOCK, SHIELD);
        POSES_BY_USE_ANIMATION.put(ItemUseAnimation.SPYGLASS, SPYGLASS);
    }

    public static FirstPersonHandPose fromItemStack(ItemStack itemStack) {
        if (itemStack.isEmpty()) {
            return EMPTY;
        }
        ItemUseAnimation useAnimation = itemStack.getUseAnimation();
        if (POSES_BY_USE_ANIMATION.containsKey(useAnimation)) {
            return POSES_BY_USE_ANIMATION.get(useAnimation);
        }
        if (itemStack.has(DataComponents.MAP_ID)) {
            return MAP;
        }
        if (itemStack.is(ItemTags.MACE_ENCHANTABLE)) {
            return MACE;
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

    public static MontageConfiguration getToolMontage(OnTickDriverContainer driverContainer) {
        if (driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM).is(ItemTags.AXES)) {
            return FirstPersonMontages.HAND_TOOL_ATTACK_AXE_MONTAGE;
        }
        return FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE;
    }

    /**
     * Returns the attack montage for this hand pose.
     */
    @Nullable
    public MontageConfiguration getAttackMontage(OnTickDriverContainer driverContainer) {
        if (this == SWORD) {
            return null;
        }
        if (this == TRIDENT) {
            return FirstPersonMontages.HAND_TRIDENT_JAB_MONTAGE;
        }
        if (this == MACE) {
            return FirstPersonMontages.HAND_MACE_ATTACK_MONTAGE;
        }
        if (driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM).is(ItemTags.AXES)) {
            return FirstPersonMontages.HAND_TOOL_ATTACK_AXE_MONTAGE;
        }
        return FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE;
    }

    public PoseFunction<LocalSpacePose> getMiningStateMachine(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> switch (this) {
                case TOOL -> FirstPersonMining.makePickaxeMiningPoseFunction(cachedPoseContainer);
                default -> ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(this.basePoseLocation).build(), MakeDynamicAdditiveFunction.of(
                        FirstPersonMining.makePickaxeMiningPoseFunction(cachedPoseContainer),
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build()));
            };
            case OFF_HAND -> SequenceEvaluatorFunction.builder(this.basePoseLocation).build();
        };
    }

    public enum HandPoseStates {
        DROPPING_LAST_ITEM,
        USING_LAST_ITEM,
        THROWING_TRIDENT,
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
        CROSSBOW_LOWER,
        TRIDENT,
        TRIDENT_RAISE,
        TRIDENT_LOWER,
        BRUSH,
        BRUSH_RAISE,
        BRUSH_LOWER,
        MACE,
        MACE_RAISE,
        MACE_LOWER,
        SPYGLASS,
        SPYGLASS_RAISE,
        SPYGLASS_LOWER,
        MAP,
        MAP_RAISE,
        MAP_LOWER
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
                FirstPersonHandPose.TRIDENT,
                FirstPersonTrident.handTridentPoseFunction(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.BRUSH,
                FirstPersonBrush.handBrushPoseFunction(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_BRUSH_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_BRUSH_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.MACE,
                FirstPersonMace.handMacePoseFunction(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_MACE_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_MACE_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.SPYGLASS,
                FirstPersonSpyglass.handSpyglassPoseFunction(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SPYGLASS_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_SPYGLASS_POSE).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                FirstPersonHandPose.MAP,
                FirstPersonHandPose.MAP.getMiningStateMachine(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonHandPose.MAP.basePoseLocation).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(FirstPersonHandPose.MAP.basePoseLocation).build(),
                        SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
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
                        .build())
                .defineState(State.builder(HandPoseStates.USING_LAST_ITEM,
                                ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build(), MakeDynamicAdditiveFunction.of(SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_USE).build(), SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build()))
                        )
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(HandPoseStates.THROWING_TRIDENT, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TRIDENT_RELEASE_THROW).build())
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.EMPTY_RAISE)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(0)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .addStateAlias(StateAlias.builder(Set.of(
                                HandPoseStates.DROPPING_LAST_ITEM,
                                HandPoseStates.USING_LAST_ITEM
                        ))
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.EMPTY)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.EMPTY)
                                .isTakenIfTrue(context -> shouldCancelLastItemAnimation(context, interactionHand))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.05f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build());

        PoseFunction<LocalSpacePose> pose = handPoseStateMachineBuilder.build();
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

    private static boolean shouldCancelLastItemAnimation(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        if (context.driverContainer().getDriverValue(FirstPersonDrivers.getHasUsedItemDriver(interactionHand))) {
            if (context.timeElapsedInCurrentState().inTicks() > 2) {
                return true;
            }
        }
        if (interactionHand == InteractionHand.MAIN_HAND) {
            if (context.driverContainer().getDriverValue(FirstPersonDrivers.HAS_ATTACKED)) {
                return true;
            }
            if (context.driverContainer().getDriverValue(FirstPersonDrivers.IS_MINING)) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasItemChanged(StateTransition.TransitionContext context, InteractionHand interactionHand) {
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
    }

    private static boolean isNewItemEmpty(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand)).isEmpty();
    }

    private static boolean isOldItemEmpty(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand)).isEmpty();
    }

    private static boolean hasSelectedHotbarSlotChanged(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        return interactionHand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(FirstPersonDrivers.HOTBAR_SLOT).hasValueChanged();
    }

    private static boolean areAnyTwoHandedOverridesActive(StateTransition.TransitionContext context) {
        return context.driverContainer().getDriverValue(FirstPersonDrivers.CURRENT_TWO_HANDED_OVERRIDE_STATE) != FirstPersonTwoHandedActions.TwoHandedActionStates.NORMAL;
    }

    private static boolean shouldTakeHardSwitchTransition(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        if (areAnyTwoHandedOverridesActive(context)) {
            return false;
        }
        // Duct-tape solution for hand pose functions like consumables not being able to update the rendered item before the hard switch condition is updated.
        if (context.driverContainer().getDriver(FirstPersonDrivers.getUsingItemDriver(interactionHand)).getPreviousValue()) {
            ItemUseAnimation useAnimation = context.driverContainer().getDriver(FirstPersonDrivers.getItemDriver(interactionHand)).getPreviousValue().getUseAnimation();
            if (useAnimation == ItemUseAnimation.EAT) {
                return false;
            }
            if (useAnimation == ItemUseAnimation.DRINK) {
                return false;
            }
        }
        if (hasItemChanged(context, interactionHand)) {
            return true;
        } else {
            if (!hasSelectedHotbarSlotChanged(context, interactionHand)) {
                return false;
            }
            if (!isNewItemEmpty(context, interactionHand)) {
                return false;
            }
            return !isOldItemEmpty(context, interactionHand);
        }
    }

    private static boolean shouldTakeDropLastItemTransition(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        if (interactionHand != InteractionHand.MAIN_HAND) {
            return false;
        }
        if (!isNewItemEmpty(context, interactionHand)) {
            return false;
        }
        return context.driverContainer().getDriver(FirstPersonDrivers.HAS_DROPPED_ITEM).hasBeenTriggered();
    }

    private static boolean shouldTakeUseLastItemTransition(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        // Required conditions for the "use last item" transition
        if (!isNewItemEmpty(context, interactionHand)) {
            return false;
        }
        if (isOldItemEmpty(context, interactionHand)) {
            return false;
        }
        // If any of these conditions are met, use the "use last item" transition
        if (context.driverContainer().getDriver(FirstPersonDrivers.getHasUsedItemDriver(interactionHand)).hasBeenTriggered()) {
            return true;
        }
        if (context.driverContainer().getDriver(FirstPersonDrivers.getHasInteractedWithDriver(interactionHand)).hasBeenTriggered()) {
            return true;
        }
        return interactionHand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(FirstPersonDrivers.HAS_ATTACKED).hasBeenTriggered();
    }

    private static boolean shouldTakeThrowTridentTransition(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        // Required conditions for the "throw trident" transition
        // Don't play the throw animation if the new item is not empty
        if (!isNewItemEmpty(context, interactionHand)) {
            return false;
        }
        // Don't play the throw animation if the old item is empty
        if (isOldItemEmpty(context, interactionHand)) {
            return false;
        }
        // Don't play the throw animation if the hotbar slot just changed
        if (hasSelectedHotbarSlotChanged(context, interactionHand)) {
            return false;
        }
        // Don't play the throw animation if the player has just swapped items
        if (context.driverContainer().getDriver(FirstPersonDrivers.HAS_SWAPPED_ITEMS).hasBeenTriggered()) {
            return false;
        }
//        if (!context.driverContainer().getDriver(FirstPersonDrivers.getUsingItemDriver(interactionHand)).getPreviousValue()) {
//            return false;
//        }
        // If any of these conditions are met, use the "throw trident" transition
        if (context.driverContainer().getDriver(FirstPersonDrivers.getRenderedItemDriver(interactionHand)).getCurrentValue().getUseAnimation() == ItemUseAnimation.SPEAR) {
            return true;
        }
        return false;
    }

    private static boolean shouldSkipRaiseAnimation(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        if (context.driverContainer().getDriverValue(FirstPersonDrivers.getUsingItemDriver(interactionHand))) {
            return true;
        }
        if (context.driverContainer().getDriverValue(FirstPersonDrivers.getHasUsedItemDriver(interactionHand))) {
            return true;
        }
        if (interactionHand == InteractionHand.MAIN_HAND) {
            if (context.driverContainer().getDriverValue(FirstPersonDrivers.IS_MINING)) {
                return true;
            }
            if (context.driverContainer().getDriverValue(FirstPersonDrivers.HAS_ATTACKED)) {
                return true;
            }
        }
        return false;
    }

    private static void clearMontagesInAttackSlot(PoseFunction.FunctionEvaluationState evaluationState, InteractionHand interactionHand) {
        evaluationState.montageManager().interruptMontagesInSlot(FirstPersonMontages.getAttackSlot(interactionHand), Transition.INSTANT);
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
                                .isTakenIfTrue(context -> shouldSkipRaiseAnimation(context, interactionHand))
                                .setTiming(Transition.builder(TimeSpan.ofTicks(3)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        handPose.poseState,
                                        handPose.raisingState
                                ))
                        .addOutboundTransition(StateTransition.builder(handPose.loweringState)
                                .isTakenIfTrue(context -> shouldTakeHardSwitchTransition(context, interactionHand))
                                .setTiming(poseToLoweringTiming)
                                .setPriority(50)
                                .build())
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.DROPPING_LAST_ITEM)
                                .isTakenIfTrue(context -> shouldTakeDropLastItemTransition(context, interactionHand))
                                .setPriority(80)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(evaluationState.driverContainer(), interactionHand))
                                .bindToOnTransitionTaken(evaluationState -> clearMontagesInAttackSlot(evaluationState, interactionHand))
                                .build())
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.USING_LAST_ITEM)
                                .isTakenIfTrue(context -> shouldTakeUseLastItemTransition(context, interactionHand))
                                .setPriority(70)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(evaluationState.driverContainer(), interactionHand))
                                .bindToOnTransitionTaken(evaluationState -> clearMontagesInAttackSlot(evaluationState, interactionHand))
                                .build())
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.THROWING_TRIDENT)
                                .isTakenIfTrue(context -> shouldTakeThrowTridentTransition(context, interactionHand))
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(1)).build())
                                .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(evaluationState.driverContainer(), interactionHand))
                                .bindToOnTransitionTaken(evaluationState -> clearMontagesInAttackSlot(evaluationState, interactionHand))
                                .build())
                        .build());
        fromLoweringAliasBuilder
                .addOriginatingState(handPose.loweringState)
                .addOutboundTransition(StateTransition.builder(handPose.raisingState)
                        .setTiming(Transition.INSTANT)
                        .isTakenIfTrue(StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING
                                .and(context -> FirstPersonHandPose.fromItemStack(context.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand))) == handPose)
                        )
                        .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItemIfNoTwoHandOverrides(evaluationState.driverContainer(), interactionHand))
                        .bindToOnTransitionTaken(evaluationState -> clearMontagesInAttackSlot(evaluationState, interactionHand))
                        .build());
    }
}
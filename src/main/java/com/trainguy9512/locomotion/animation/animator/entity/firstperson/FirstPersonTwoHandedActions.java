package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.joint.skeleton.BlendProfile;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.MirrorFunction;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.component.ChargedProjectiles;

import java.util.Set;
import java.util.function.Predicate;

public class FirstPersonTwoHandedActions {

    public static PoseFunction<LocalSpacePose> constructPoseFunction(PoseFunction<LocalSpacePose> normalPoseFunction, CachedPoseContainer cachedPoseContainer) {
        StateMachineFunction.Builder builder = StateMachineFunction.builder(FirstPersonTwoHandedActions::getTwoHandedEntryState)
                .bindDriverToCurrentActiveState(FirstPersonDrivers.CURRENT_TWO_HANDED_OVERRIDE_STATE)
                .defineState(StateDefinition.builder(TWO_HANDED_ACTION_NORMAL_STATE, normalPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build());
        defineBowStatesForHand(builder, InteractionHand.MAIN_HAND);
        defineBowStatesForHand(builder, InteractionHand.OFF_HAND);
        defineCrossbowStatesForHand(builder, InteractionHand.MAIN_HAND);
        defineCrossbowStatesForHand(builder, InteractionHand.OFF_HAND);
        return builder.build();
    }

    public static void defineBowStatesForHand(StateMachineFunction.Builder stateMachineBuilder, InteractionHand interactionHand) {
        InteractionHand oppositeHand = interactionHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        String bowPullState = getBowPullState(interactionHand);
        String bowReleaseState = getBowReleaseState(interactionHand);

        PoseFunction<LocalSpacePose> pullPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_BOW_PULL)
                .bindToTimeMarker("arrow_placed_in_bow", evaluationState -> {
                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderedItemDriver(oppositeHand)).setValue(ItemStack.EMPTY);
                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getHandPoseDriver(oppositeHand)).setValue(FirstPersonHandPoses.EMPTY);
                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getGenericItemPoseDriver(oppositeHand)).setValue(FirstPersonGenericItems.getFallback());
                })
                .bindToTimeMarker("get_new_arrow", evaluationState -> {
                })
                .build();
        PoseFunction<LocalSpacePose> releasePoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_BOW_RELEASE)
                .build();

        BlendProfile releaseBlendProfile = BlendProfile.builder()
                .defineForCustomAttribute(FirstPersonJointAnimator.IS_USING_PROPERTY_ATTRIBUTE, 0)
                .defineForCustomAttribute(FirstPersonJointAnimator.USE_DURATION_PROPERTY_ATTRIBUTE, 0)
                .build();

        if (interactionHand == InteractionHand.OFF_HAND) {
            pullPoseFunction = MirrorFunction.of(pullPoseFunction);
            releasePoseFunction = MirrorFunction.of(releasePoseFunction);
        }

        BlendProfile blendOffhandArrowMoreQuickly = BlendProfile.builder().defineForJoint(FirstPersonJointAnimator.LEFT_HAND_JOINT, 0.2f).build();
        if (interactionHand == InteractionHand.OFF_HAND) {
            blendOffhandArrowMoreQuickly = blendOffhandArrowMoreQuickly.getMirrored();
        }

        stateMachineBuilder.defineState(StateDefinition.builder(bowPullState, pullPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(bowReleaseState)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.getUsingItemDriver(interactionHand)).negate().and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .bindToOnTransitionTaken(evaluationState -> {
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderedItemDriver(oppositeHand)).setValue(ItemStack.EMPTY);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getHandPoseDriver(oppositeHand)).setValue(FirstPersonHandPoses.EMPTY);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getGenericItemPoseDriver(oppositeHand)).setValue(FirstPersonGenericItems.getFallback());
                                })
                                .setTiming(Transition.builder(TimeSpan.ofTicks(1f)).setBlendProfile(releaseBlendProfile).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(bowReleaseState, releasePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TWO_HANDED_ACTION_NORMAL_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        TWO_HANDED_ACTION_NORMAL_STATE,
                                        bowReleaseState
                                ))
                        .addOutboundTransition(StateTransition.builder(bowPullState)
                                .isTakenIfTrue(
                                        StateTransition.takeIfBooleanDriverTrue(FirstPersonDrivers.getUsingItemDriver(interactionHand))
                                                .and(transitionContext -> transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.getHandPoseDriver(interactionHand)) == FirstPersonHandPoses.BOW)
                                                .and(transitionContext -> transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand)).getUseAnimation() == ItemUseAnimation.BOW)
                                )
                                .bindToOnTransitionTaken(evaluationState -> {
                                    ItemStack projectileStack = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.PROJECTILE_ITEM);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderedItemDriver(oppositeHand)).setValue(projectileStack);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getGenericItemPoseDriver(oppositeHand)).setValue(FirstPersonGenericItems.getConfigurationFromItem(projectileStack));
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getHandPoseDriver(oppositeHand)).setValue(FirstPersonHandPoses.GENERIC_ITEM);
                                })
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(12))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .setBlendProfile(blendOffhandArrowMoreQuickly)
                                        .build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        bowPullState,
                                        bowReleaseState
                                ))
                        .addOutboundTransition(StateTransition.builder(TWO_HANDED_ACTION_NORMAL_STATE)
                                .isTakenIfTrue(transitionContext -> transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand)).getUseAnimation() != ItemUseAnimation.BOW)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build()
                );
    }

    public static void defineCrossbowStatesForHand(StateMachineFunction.Builder stateMachineBuilder, InteractionHand interactionHand) {
        InteractionHand oppositeHand = interactionHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        String crossbowReloadState = getCrossbowReloadState(interactionHand);
        String crossbowFinishReloadState = getCrossbowFinishReloadState(interactionHand);

        PoseFunction<LocalSpacePose> crossbowReloadPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_CROSSBOW_RELOAD)
                .setPlayRate(evaluationState -> evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.CROSSBOW_RELOAD_SPEED))
                .build();
        PoseFunction<LocalSpacePose> crossbowFinishReloadPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_CROSSBOW_RELOAD_FINISH)
                .build();

        Predicate<StateTransition.TransitionContext> isReloadingEmptyCrossbow = transitionContext -> {
//            if (!transitionContext.driverContainer().getDriver(FirstPersonDrivers.getUsingItemDriver(interactionHand)).getCurrentValue()) {
//                return false;
//            }
            if (!transitionContext.driverContainer().getDriver(FirstPersonDrivers.getUsingItemDriver(interactionHand)).getPreviousValue()) {
                return false;
            }

            if (transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.getHandPoseDriver(interactionHand)) != FirstPersonHandPoses.CROSSBOW) {
                return false;
            }
            if (transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand)).getUseAnimation() != ItemUseAnimation.CROSSBOW) {
                return false;
            }
            ChargedProjectiles chargedProjectiles = transitionContext.driverContainer().getDriver(FirstPersonDrivers.getItemDriver(interactionHand)).getCurrentValue().get(DataComponents.CHARGED_PROJECTILES);
            if (chargedProjectiles == null) {
                return false;
            }
            if (!chargedProjectiles.isEmpty()) {
                return false;
            }
            return true;
        };

        if (interactionHand == InteractionHand.OFF_HAND) {
            crossbowReloadPoseFunction = MirrorFunction.of(crossbowReloadPoseFunction);
            crossbowFinishReloadPoseFunction = MirrorFunction.of(crossbowFinishReloadPoseFunction);
        }

        stateMachineBuilder.defineState(StateDefinition.builder(crossbowReloadState, crossbowReloadPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(crossbowFinishReloadState)
                                .isTakenIfTrue(isReloadingEmptyCrossbow.negate())
                                .bindToOnTransitionTaken(evaluationState -> {
                                    FirstPersonDrivers.updateRenderedItem(evaluationState.driverContainer(), interactionHand);
                                })
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(crossbowFinishReloadState, crossbowFinishReloadPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TWO_HANDED_ACTION_NORMAL_STATE)
                                .isTakenOnAnimationFinished(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        TWO_HANDED_ACTION_NORMAL_STATE
                                ))
                        .addOutboundTransition(StateTransition.builder(crossbowReloadState)
                                .isTakenIfTrue(isReloadingEmptyCrossbow)
                                .bindToOnTransitionTaken(evaluationState -> {
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderedItemDriver(oppositeHand)).setValue(ItemStack.EMPTY);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getHandPoseDriver(oppositeHand)).setValue(FirstPersonHandPoses.EMPTY);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getGenericItemPoseDriver(oppositeHand)).setValue(FirstPersonGenericItems.getFallback());
                                })
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(12))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        crossbowReloadState,
                                        crossbowFinishReloadState
                                ))
                        .addOutboundTransition(StateTransition.builder(TWO_HANDED_ACTION_NORMAL_STATE)
                                .isTakenIfTrue(transitionContext -> transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand)).getUseAnimation() != ItemUseAnimation.CROSSBOW)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                .bindToOnTransitionTaken(evaluationState -> {
                                })
                                .build())
                        .build()
                );
    }

    public static final String TWO_HANDED_ACTION_NORMAL_STATE = "normal";
    public static final String TWO_HANDED_ACTION_BOW_PULL_MAIN_HAND_STATE = "bow_pull_main_hand";
    public static final String TWO_HANDED_ACTION_BOW_PULL_OFF_HAND_STATE = "bow_pull_off_hand";
    public static final String TWO_HANDED_ACTION_BOW_RELEASE_MAIN_HAND_STATE = "bow_release_main_hand";
    public static final String TWO_HANDED_ACTION_BOW_RELEASE_OFF_HAND_STATE = "bow_release_off_hand";
    public static final String TWO_HANDED_ACTION_CROSSBOW_RELOAD_MAIN_HAND_STATE = "crossbow_reload_main_hand";
    public static final String TWO_HANDED_ACTION_CROSSBOW_RELOAD_MAIN_OFF_STATE = "crossbow_reload_off_hand";
    public static final String TWO_HANDED_ACTION_CROSSBOW_FINISH_RELOAD_MAIN_HAND_STATE = "crossbow_finish_reload_main_hand";
    public static final String TWO_HANDED_ACTION_CROSSBOW_FINISH_RELOAD_OFF_HAND_STATE = "crossbow_finish_reload_off_hand";

    private static String getTwoHandedEntryState(PoseFunction.FunctionEvaluationState evaluationState) {
        return TWO_HANDED_ACTION_NORMAL_STATE;
    }

    private static String getBowPullState(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> TWO_HANDED_ACTION_BOW_PULL_MAIN_HAND_STATE;
            case OFF_HAND -> TWO_HANDED_ACTION_BOW_PULL_OFF_HAND_STATE;
        };
    }

    private static String getBowReleaseState(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> TWO_HANDED_ACTION_BOW_RELEASE_MAIN_HAND_STATE;
            case OFF_HAND -> TWO_HANDED_ACTION_BOW_RELEASE_OFF_HAND_STATE;
        };
    }

    private static String getCrossbowReloadState(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> TWO_HANDED_ACTION_CROSSBOW_RELOAD_MAIN_HAND_STATE;
            case OFF_HAND -> TWO_HANDED_ACTION_CROSSBOW_RELOAD_MAIN_OFF_STATE;
        };
    }

    private static String getCrossbowFinishReloadState(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> TWO_HANDED_ACTION_CROSSBOW_FINISH_RELOAD_MAIN_HAND_STATE;
            case OFF_HAND -> TWO_HANDED_ACTION_CROSSBOW_FINISH_RELOAD_OFF_HAND_STATE;
        };
    }
}
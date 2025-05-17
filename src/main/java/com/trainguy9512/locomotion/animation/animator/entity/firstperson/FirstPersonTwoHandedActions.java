package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.animation.joint.skeleton.BlendProfile;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.MirrorFunction;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Set;

public class FirstPersonTwoHandedActions {

    public static PoseFunction<LocalSpacePose> constructPoseFunction(PoseFunction<LocalSpacePose> normalPoseFunction, CachedPoseContainer cachedPoseContainer) {
        StateMachineFunction.Builder<TwoHandedActionStates> builder = StateMachineFunction.builder(evaluationState -> TwoHandedActionStates.NORMAL)
                .bindDriverToCurrentActiveState(FirstPersonDrivers.CURRENT_TWO_HANDED_OVERRIDE_STATE)
                .defineState(State.builder(TwoHandedActionStates.NORMAL, normalPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build());
        defineBowStatesForHand(builder, InteractionHand.MAIN_HAND);
        defineBowStatesForHand(builder, InteractionHand.OFF_HAND);
        return builder.build();
    }

    public static void defineBowStatesForHand(StateMachineFunction.Builder<TwoHandedActionStates> stateMachineBuilder, InteractionHand interactionHand) {
        InteractionHand oppositeHand = interactionHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        TwoHandedActionStates bowPullState = switch (interactionHand) {
            case MAIN_HAND -> TwoHandedActionStates.BOW_PULL_MAIN_HAND;
            case OFF_HAND -> TwoHandedActionStates.BOW_PULL_OFF_HAND;
        };
        TwoHandedActionStates bowReleaseState = switch (interactionHand) {
            case MAIN_HAND -> TwoHandedActionStates.BOW_RELEASE_MAIN_HAND;
            case OFF_HAND -> TwoHandedActionStates.BOW_RELEASE_OFF_HAND;
        };
        PoseFunction<LocalSpacePose> pullPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_BOW_PULL)
                .bindToTimeMarker("arrow_placed_in_bow", evaluationState -> {
                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderedItemDriver(oppositeHand)).setValue(ItemStack.EMPTY);
                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getHandPoseDriver(oppositeHand)).setValue(FirstPersonHandPose.EMPTY);
                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderItemAsStaticDriver(interactionHand)).setValue(false);
                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getGenericItemPoseDriver(oppositeHand)).setValue(FirstPersonGenericItemPose.DEFAULT_2D_ITEM);
                })
                .bindToTimeMarker("get_new_arrow", evaluationState -> {
                })
                .build();
        PoseFunction<LocalSpacePose> releasePoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_BOW_RELEASE)
                .build();

        if (interactionHand == InteractionHand.OFF_HAND) {
            pullPoseFunction = MirrorFunction.of(pullPoseFunction);
            releasePoseFunction = MirrorFunction.of(releasePoseFunction);
        }

        BlendProfile blendOffhandArrowMoreQuickly = BlendProfile.builder().defineForJoint(FirstPersonJointAnimator.LEFT_HAND_JOINT, 0.2f).build();
        if (interactionHand == InteractionHand.OFF_HAND) {
            blendOffhandArrowMoreQuickly = blendOffhandArrowMoreQuickly.getMirrored();
        }

        stateMachineBuilder.defineState(State.builder(bowPullState, pullPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(bowReleaseState)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.getUsingItemDriver(interactionHand)).negate().and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .bindToOnTransitionTaken(evaluationState -> {
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderedItemDriver(oppositeHand)).setValue(ItemStack.EMPTY);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getHandPoseDriver(oppositeHand)).setValue(FirstPersonHandPose.EMPTY);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderItemAsStaticDriver(interactionHand)).setValue(false);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getGenericItemPoseDriver(oppositeHand)).setValue(FirstPersonGenericItemPose.DEFAULT_2D_ITEM);
                                })
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(bowReleaseState, releasePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TwoHandedActionStates.NORMAL)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        TwoHandedActionStates.NORMAL,
                                        bowReleaseState
                                ))
                        .addOutboundTransition(StateTransition.builder(bowPullState)
                                .isTakenIfTrue(
                                        StateTransition.booleanDriverPredicate(FirstPersonDrivers.getUsingItemDriver(interactionHand))
                                                .and(transitionContext -> transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.getHandPoseDriver(interactionHand)) == FirstPersonHandPose.BOW)
                                                .and(transitionContext -> transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand)).is(Items.BOW))
                                )
                                .bindToOnTransitionTaken(evaluationState -> {
                                    ItemStack projectileStack = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.PROJECTILE_ITEM);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderedItemDriver(oppositeHand)).setValue(projectileStack);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getGenericItemPoseDriver(oppositeHand)).setValue(FirstPersonGenericItemPose.fromItemStack(projectileStack));
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getHandPoseDriver(oppositeHand)).setValue(FirstPersonHandPose.GENERIC_ITEM);
                                    evaluationState.driverContainer().getDriver(FirstPersonDrivers.getRenderItemAsStaticDriver(interactionHand)).setValue(true);
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
                        .addOutboundTransition(StateTransition.builder(TwoHandedActionStates.NORMAL)
                                .isTakenIfTrue(transitionContext -> !transitionContext.driverContainer().getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand)).is(Items.BOW))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build()
                );
    }

    public enum TwoHandedActionStates {
        NORMAL,
        BOW_PULL_MAIN_HAND,
        BOW_PULL_OFF_HAND,
        BOW_RELEASE_MAIN_HAND,
        BOW_RELEASE_OFF_HAND
    }
}
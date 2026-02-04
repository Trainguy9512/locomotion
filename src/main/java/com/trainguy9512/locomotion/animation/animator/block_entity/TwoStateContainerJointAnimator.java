package com.trainguy9512.locomotion.animation.animator.block_entity;

import com.trainguy9512.locomotion.animation.data.DriverGetter;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequenceEvaluatorFunction;
import com.trainguy9512.locomotion.animation.pose.function.SequencePlayerFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateDefinition;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface TwoStateContainerJointAnimator<B extends BlockEntity> extends BlockEntityJointAnimator<B> {

    DriverKey<VariableDriver<Float>> CONTAINER_OPENNESS = DriverKey.of("container_openness", () -> VariableDriver.ofFloat(() -> 0f));
    DriverKey<VariableDriver<Boolean>> CONTAINER_IS_OPEN = DriverKey.of("container_is_open", () -> VariableDriver.ofBoolean(() -> false));

    float getOpenProgress(B blockEntity);

    Identifier getOpenAnimationSequence();

    Identifier getCloseAnimationSequence();

    @Override
    default void extractAnimationData(B blockEntity, DriverGetter dataContainer, MontageManager montageManager) {
        this.extractContainerOpennessData(blockEntity, dataContainer);
    }

    default void extractContainerOpennessData(B blockEntity, DriverGetter dataContainer) {
        dataContainer.getDriver(CONTAINER_OPENNESS).setValue(this.getOpenProgress(blockEntity));

        float currentShulkerBoxOpenness = dataContainer.getDriver(CONTAINER_OPENNESS).getCurrentValue();
        float previousShulkerBoxOpenness = dataContainer.getDriver(CONTAINER_OPENNESS).getPreviousValue();

        boolean shulkerBoxIsOpen = false;
        if (currentShulkerBoxOpenness >= 1f) {
            shulkerBoxIsOpen = true;
        } else if (currentShulkerBoxOpenness != previousShulkerBoxOpenness) {
            if (currentShulkerBoxOpenness > previousShulkerBoxOpenness) {
                shulkerBoxIsOpen = true;
            }
        }
        dataContainer.getDriver(CONTAINER_IS_OPEN).setValue(shulkerBoxIsOpen);
    }

    @Override
    default PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {
        return this.makeContainerOpenClosePoseFunction();
    }

    String CONTAINER_CLOSED_STATE = "closed";
    String CONTAINER_OPENING_STATE = "opening";
    String CONTAINER_OPEN_STATE = "open";
    String CONTAINER_CLOSING_STATE = "closing";

    private static String getInitialContainerState(DriverGetter driverGetter) {
        return driverGetter.getDriverValue(CONTAINER_IS_OPEN) ? CONTAINER_OPEN_STATE : CONTAINER_CLOSED_STATE;
    }

    default PoseFunction<LocalSpacePose> makeContainerOpenClosePoseFunction() {


        PoseFunction<LocalSpacePose> chestClosedPoseFunction = SequenceEvaluatorFunction.builder(this.getOpenAnimationSequence()).build();
        PoseFunction<LocalSpacePose> chestOpeningPoseFunction = SequencePlayerFunction.builder(this.getOpenAnimationSequence()).build();
        PoseFunction<LocalSpacePose> chestOpenPoseFunction = SequenceEvaluatorFunction.builder(this.getCloseAnimationSequence()).build();
        PoseFunction<LocalSpacePose> chestClosingPoseFunction = SequencePlayerFunction.builder(this.getCloseAnimationSequence()).build();

        PoseFunction<LocalSpacePose> shulkerBoxStateMachine;
        shulkerBoxStateMachine = StateMachineFunction.builder(TwoStateContainerJointAnimator::getInitialContainerState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(CONTAINER_CLOSED_STATE, chestClosedPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CONTAINER_OPENING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CONTAINER_IS_OPEN))
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CONTAINER_OPENING_STATE, chestOpeningPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CONTAINER_OPEN_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(CONTAINER_CLOSING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CONTAINER_IS_OPEN).negate())
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CONTAINER_OPEN_STATE, chestOpenPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CONTAINER_CLOSING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CONTAINER_IS_OPEN).negate())
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CONTAINER_CLOSING_STATE, chestClosingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CONTAINER_CLOSED_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(CONTAINER_OPENING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CONTAINER_IS_OPEN))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .build();
        return shulkerBoxStateMachine;
    }
}

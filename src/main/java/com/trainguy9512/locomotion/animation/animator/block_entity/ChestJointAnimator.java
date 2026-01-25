package com.trainguy9512.locomotion.animation.animator.block_entity;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
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
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;

public class ChestJointAnimator<T extends LidBlockEntity> implements BlockEntityJointAnimator<T> {

    public static final Identifier CHEST_SKELETON = LocomotionMain.makeIdentifier("skeletons/block_entity/chest.json");

    public static final Identifier CHEST_OPEN_SEQUENCE = LocomotionMain.makeIdentifier("sequences/block_entity/chest/open.json");
    public static final Identifier CHEST_CLOSE_SEQUENCE = LocomotionMain.makeIdentifier("sequences/block_entity/chest/close.json");

    public static final DriverKey<VariableDriver<Float>> CHEST_OPENNESS = DriverKey.of("chest_openness", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Boolean>> CHEST_IS_OPEN = DriverKey.of("chest_is_open", () -> VariableDriver.ofBoolean(() -> false));

    @Override
    public Identifier getJointSkeleton() {
        return CHEST_SKELETON;
    }

    @Override
    public void extractAnimationData(ChestBlockEntity chest, OnTickDriverContainer dataContainer, MontageManager montageManager) {
        dataContainer.getDriver(CHEST_OPENNESS).setValue(chest.getOpenNess(1));

        float currentChestOpenness = dataContainer.getDriver(CHEST_OPENNESS).getCurrentValue();
        float previousChestOpenness = dataContainer.getDriver(CHEST_OPENNESS).getPreviousValue();

        boolean chestIsOpen = false;
        if (currentChestOpenness >= 1f) {
            chestIsOpen = true;
        } else if (currentChestOpenness != previousChestOpenness) {
            if (currentChestOpenness > previousChestOpenness) {
                chestIsOpen = true;
            }
        }
        dataContainer.getDriver(CHEST_IS_OPEN).setValue(chestIsOpen);
    }

    private static final String CHEST_CLOSED_STATE = "closed";
    private static final String CHEST_OPENING_STATE = "opening";
    private static final String CHEST_OPEN_STATE = "open";
    private static final String CHEST_CLOSING_STATE = "closing";

    private static String getInitialChestState(PoseFunction.FunctionEvaluationState evaluationState) {
        return CHEST_CLOSED_STATE;
    }

    @Override
    public PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {

        PoseFunction<LocalSpacePose> chestClosedPoseFunction = SequenceEvaluatorFunction.builder(CHEST_OPEN_SEQUENCE).build();
        PoseFunction<LocalSpacePose> chestOpeningPoseFunction = SequencePlayerFunction.builder(CHEST_OPEN_SEQUENCE).build();
        PoseFunction<LocalSpacePose> chestOpenPoseFunction = SequenceEvaluatorFunction.builder(CHEST_CLOSE_SEQUENCE).build();
        PoseFunction<LocalSpacePose> chestClosingPoseFunction = SequencePlayerFunction.builder(CHEST_CLOSE_SEQUENCE).build();

        PoseFunction<LocalSpacePose> chestOpenStateMachine;
        chestOpenStateMachine = StateMachineFunction.builder(ChestJointAnimator::getInitialChestState)
                .resetsUponRelevant(true)
                .defineState(StateDefinition.builder(CHEST_CLOSED_STATE, chestClosedPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHEST_OPENING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CHEST_IS_OPEN))
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHEST_OPENING_STATE, chestOpeningPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHEST_OPEN_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(CHEST_CLOSING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CHEST_IS_OPEN).negate())
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHEST_OPEN_STATE, chestOpenPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHEST_CLOSING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CHEST_IS_OPEN).negate())
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .build())
                        .build())
                .defineState(StateDefinition.builder(CHEST_CLOSING_STATE, chestClosingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(CHEST_CLOSED_STATE)
                                .isTakenOnAnimationFinished(1f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(CHEST_OPENING_STATE)
                                .isTakenIfTrue(StateTransition.takeIfBooleanDriverTrue(CHEST_IS_OPEN))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).build())
                                .setCanInterruptOtherTransitions(false)
                                .build())
                        .build())
                .build();

        return chestOpenStateMachine;
    }

    @Override
    public PoseCalculationFrequency getPoseCalulationFrequency() {
        return PoseCalculationFrequency.CALCULATE_EVERY_FRAME;
    }
}

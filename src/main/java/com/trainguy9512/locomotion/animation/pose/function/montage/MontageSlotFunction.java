package com.trainguy9512.locomotion.animation.pose.function.montage;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public record MontageSlotFunction(PoseFunction<LocalSpacePose> input, String slot) implements PoseFunction<LocalSpacePose> {

    public static MontageSlotFunction of(PoseFunction<LocalSpacePose> inputPose, String slot) {
        return new MontageSlotFunction(inputPose, slot);
    }

    @Override
    public @NotNull LocalSpacePose compute(FunctionInterpolationContext context) {
        return context.montageManager().getLayeredSlotPose(this.input.compute(context), this.slot, context.driverContainer().getJointSkeleton(), context.partialTicks());
    }

    @Override
    public void tick(FunctionEvaluationState evaluationState) {
        if (!evaluationState.montageManager().areAnyMontagesInSlotFullyOverriding(this.slot)) {
            this.input.tick(evaluationState);
        }
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return MontageSlotFunction.of(this.input.wrapUnique(), this.slot);
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : this.input.searchDownChainForMostRelevant(findCondition);
    }
}

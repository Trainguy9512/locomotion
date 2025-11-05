package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public class EmptyPoseFunction implements PoseFunction<LocalSpacePose> {

    public static EmptyPoseFunction of() {
        return new EmptyPoseFunction();
    }

    @Override
    public @NotNull LocalSpacePose compute(FunctionInterpolationContext context) {
        return LocalSpacePose.of(context.driverContainer().getJointSkeleton());
    }

    @Override
    public void tick(FunctionEvaluationState evaluationState) {

    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return of();
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : Optional.empty();
    }
}

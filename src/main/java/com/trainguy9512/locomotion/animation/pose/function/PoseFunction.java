package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.data.PoseCalculationContext;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.pose.Pose;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public interface PoseFunction<P extends Pose> {

    /**
     * Computes and returns an animation pose using its inputs.
     * @param context           Interpolation context, containing the driver container, partial ticks float, and elapsed game time for calculating values every frame.
     * @implNote                Called every frame for joint animators that compute a new pose every frame, or once per tick.
     */
    @NotNull P compute(PoseCalculationContext context);

    /**
     * Updates the function and then updates the function's inputs.
     * @param context   Current state of the evaluation, with the data container as well as
     * @implNote                If an input is deemed irrelevant, or not necessary during pose calculation, the input does not need to be ticked.
     * @implNote                Called once per tick, with the assumption that per-frame values can be interpolated.
     */
    void tick(PoseTickEvaluationContext context);

    /**
     * Recursive method that creates and returns a new copy of the function with its inputs also copied.
     * This ensures that no pose function is referenced twice, besides cached pose functions.
     * If a pose were referenced as an input twice, then it would tick and compute twice, which can lead to undesirable results.
     * @implNote                Called after the joint animator's pose function is constructed.
     * @return                  Clean copy of the pose function with its inputs being clean copies
     */
    PoseFunction<P> wrapUnique();

    /**
     * Recursive method that goes down the chain of pose functions returns the most relevant function that matches the condition predicate.
     * <p>
     * If this pose function does not pass the test, or it is set to be ignored for relevancy tests,
     * then call this method for all inputs in order of most to least relevant.
     * If this pose function is the end of a chain and does not pass the test, then return null.
     *
     * @return                  Most pose function that passes the test, if it exists in this part of the chain.
     */
    Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition);

}

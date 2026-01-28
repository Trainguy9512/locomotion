package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.data.PoseCalculationDataContainer;
import com.trainguy9512.locomotion.animation.pose.Pose;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public interface PoseFunction<P extends Pose> {

    /**
     * Computes and returns an animation pose using its inputs.
     * @param context           Interpolation context, containing the driver container, partial ticks float, and elapsed game time for calculating values every frame.
     * @implNote                Called every frame for joint animators that compute a new pose every frame, or once per tick.
     */
    @NotNull P compute(PoseFunction.FunctionInterpolationContext context);

    /**
     * Updates the function and then updates the function's inputs.
     * @param evaluationState   Current state of the evaluation, with the data container as well as
     * @implNote                If an input is deemed irrelevant, or not necessary during pose calculation, the input does not need to be ticked.
     * @implNote                Called once per tick, with the assumption that per-frame values can be interpolated.
     */
    void tick(FunctionEvaluationState evaluationState);

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

    record FunctionEvaluationState(OnTickDriverContainer driverContainer, MontageManager montageManager, boolean resetting, long currentTick) {

        public static FunctionEvaluationState of(OnTickDriverContainer driverContainer, MontageManager montageManager, boolean resetting, long currentTick) {
            return new FunctionEvaluationState(driverContainer, montageManager, resetting, currentTick);
        }

        /**
         * Creates a copy of the evaluation state that is marked for a hard reset.
         *
         * <p>A hard reset is an animation reset that immediately resets with no blending.</p>
         */
        public FunctionEvaluationState markedForReset() {
            return FunctionEvaluationState.of(this.driverContainer, this.montageManager, true, this.currentTick);
        }

        public FunctionEvaluationState cleared() {
            return FunctionEvaluationState.of(this.driverContainer, this.montageManager, false, this.currentTick);
        }

        /**
         * Runs the provided function if this evaluation state is marked for hard reset.
         */
        public void ifMarkedForReset(Runnable runnable) {
            if (this.resetting) {
                runnable.run();
            }
        }
    }

    record FunctionInterpolationContext(PoseCalculationDataContainer driverContainer, MontageManager montageManager, float partialTicks, TimeSpan gameTime) {
        public static FunctionInterpolationContext of(PoseCalculationDataContainer dataContainer, MontageManager montageManager, float partialTicks, TimeSpan gameTime){
            return new FunctionInterpolationContext(dataContainer, montageManager, partialTicks, gameTime);
        }
    }
}

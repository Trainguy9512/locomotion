package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.data.PoseCalculationContext;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Takes an input pose and mirrors it.
 */
public class MirrorFunction implements PoseFunction<LocalSpacePose> {

    private final PoseFunction<LocalSpacePose> input;
    private final Function<PoseCalculationContext, Boolean> enabledFunction;

    public MirrorFunction(PoseFunction<LocalSpacePose> input, Function<PoseCalculationContext, Boolean> enabledFunction) {
        this.input = input;
        this.enabledFunction = enabledFunction;
    }

    public static MirrorFunction of(PoseFunction<LocalSpacePose> input, Function<PoseCalculationContext, Boolean> mirrorFunction) {
        return new MirrorFunction(input, mirrorFunction);
    }

    public static MirrorFunction of(PoseFunction<LocalSpacePose> input) {
        return new MirrorFunction(input, context -> true);
    }

    @Override
    public @NotNull LocalSpacePose compute(PoseCalculationContext context) {
        if (this.enabledFunction.apply(context)) {
            return input.compute(context).mirrored();
        } else {
            return input.compute(context);
        }
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {
        this.input.tick(context);
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return MirrorFunction.of(this.input.wrapUnique(), this.enabledFunction);
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : this.input.searchDownChainForMostRelevant(findCondition);
    }
}

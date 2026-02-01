package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.sequence.AnimationSequence;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class SequenceEvaluatorFunction implements PoseFunction<LocalSpacePose> {

    private final Function<FunctionInterpolationContext, Identifier> animationSequenceFunction;
    private final Function<FunctionInterpolationContext, TimeSpan> sequenceTimeFunction;

    private SequenceEvaluatorFunction(Function<FunctionInterpolationContext, Identifier> animationSequenceFunction, Function<FunctionInterpolationContext, TimeSpan> sequenceTimeFunction) {
        this.animationSequenceFunction = animationSequenceFunction;
        this.sequenceTimeFunction = sequenceTimeFunction;
    }

    public static Builder builder(Function<FunctionInterpolationContext, Identifier> animationSequenceFunction) {
        return new Builder(animationSequenceFunction);
    }

    public static Builder builder(Identifier animationSequence) {
        return builder(context -> animationSequence);
    }

    @Override
    public @NotNull LocalSpacePose compute(FunctionInterpolationContext context) {
        TimeSpan time = this.sequenceTimeFunction.apply(context);
        Identifier sequence = this.animationSequenceFunction.apply(context);
        return AnimationSequence.samplePose(
                context.driverContainer().getJointSkeleton(),
                sequence,
                time,
                true
        );
    }

    @Override
    public void tick(FunctionEvaluationState evaluationState) {

    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return new SequenceEvaluatorFunction(this.animationSequenceFunction, this.sequenceTimeFunction);
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : Optional.empty();
    }

    public static class Builder {
        private final Function<FunctionInterpolationContext, Identifier> animationSequenceFunction;
        private Function<FunctionInterpolationContext, TimeSpan> sequenceTimeFunction;

        public Builder(Function<FunctionInterpolationContext, Identifier> animationSequenceFunction) {
            this.animationSequenceFunction = animationSequenceFunction;
            this.sequenceTimeFunction = context -> TimeSpan.ZERO;
        }

        public Builder evaluatesPoseAt(Function<FunctionInterpolationContext, TimeSpan> sequenceTimeFunction) {
            this.sequenceTimeFunction = sequenceTimeFunction;
            return this;
        }

        public Builder evaluatesPoseAt(TimeSpan sequenceTime) {
            this.sequenceTimeFunction = context -> sequenceTime;
            return this;
        }

        public SequenceEvaluatorFunction build() {
            return new SequenceEvaluatorFunction(this.animationSequenceFunction, this.sequenceTimeFunction);
        }
    }
}

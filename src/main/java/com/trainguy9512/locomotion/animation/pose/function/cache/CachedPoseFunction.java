package com.trainguy9512.locomotion.animation.pose.function.cache;

import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.AnimationPlayer;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

public class CachedPoseFunction implements PoseFunction<LocalSpacePose> {

    private PoseFunction<LocalSpacePose> input;
    private final boolean resetsUponRelevant;

    LocalSpacePose poseCache;
    boolean hasTickedAlready;
    private long lastUpdateTick;

    private CachedPoseFunction(PoseFunction<LocalSpacePose> input, boolean resetsUponRelevant) {
        this.input = input;
        this.resetsUponRelevant = resetsUponRelevant;
        this.poseCache = null;
        this.hasTickedAlready = false;
    }

    protected static CachedPoseFunction of(PoseFunction<LocalSpacePose> input, boolean resetsUponRelevant) {
        return new CachedPoseFunction(input, resetsUponRelevant);
    }

    @Override
    public @NotNull LocalSpacePose compute(FunctionInterpolationContext context) {
        if (this.poseCache == null) {
            this.poseCache = this.input.compute(context);
        }
        return LocalSpacePose.of(this.poseCache);
    }

    @Override
    public void tick(FunctionEvaluationState evaluationState) {
        if (!this.hasTickedAlready) {
            if (evaluationState.currentTick() - 1 > this.lastUpdateTick && this.resetsUponRelevant) {
                this.input.tick(evaluationState.cleared().markedForReset());
            } else {
                this.input.tick(evaluationState.cleared());
            }
            this.lastUpdateTick = evaluationState.currentTick();
            this.hasTickedAlready = true;
        }
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        this.input = input.wrapUnique();
        return this;
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : Optional.empty();
    }

    public void clearCache() {
        this.poseCache = null;
        this.hasTickedAlready = false;
    }
}

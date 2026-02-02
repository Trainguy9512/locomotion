package com.trainguy9512.locomotion.animation.pose.function.cache;

import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.data.PoseCalculationContext;
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
    public @NotNull LocalSpacePose compute(PoseCalculationContext context) {
        if (this.poseCache == null) {
            this.poseCache = this.input.compute(context);
        }
        return LocalSpacePose.of(this.poseCache);
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {
        if (!this.hasTickedAlready) {
            if (context.currentTick() - 1 > this.lastUpdateTick && this.resetsUponRelevant) {
                this.input.tick(context.cleared().markedForReset());
            } else {
                this.input.tick(context.cleared());
            }
            this.lastUpdateTick = context.currentTick();
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

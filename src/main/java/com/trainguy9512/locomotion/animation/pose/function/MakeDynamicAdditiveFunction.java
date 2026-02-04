package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.data.PoseCalculationContext;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Pose function that creates an additive animation pose by subtracting a base pose from the desired additive pose.
 */
public class MakeDynamicAdditiveFunction implements PoseFunction<LocalSpacePose> {

    private final PoseFunction<LocalSpacePose> additivePoseInput;
    private final PoseFunction<LocalSpacePose> basePoseInput;

    public MakeDynamicAdditiveFunction(PoseFunction<LocalSpacePose> additivePoseInput, PoseFunction<LocalSpacePose> basePoseInput) {
        this.additivePoseInput = additivePoseInput;
        this.basePoseInput = basePoseInput;
    }

    public static MakeDynamicAdditiveFunction of(PoseFunction<LocalSpacePose> additivePoseInput, PoseFunction<LocalSpacePose> basePoseInput) {
        return new MakeDynamicAdditiveFunction(additivePoseInput, basePoseInput);
    }

    @Override
    public @NotNull LocalSpacePose compute(PoseCalculationContext context) {
        LocalSpacePose additivePose = this.additivePoseInput.compute(context);
        LocalSpacePose additivePoseReference = this.basePoseInput.compute(context);

        additivePoseReference.invert();
        additivePose.multiply(additivePoseReference, JointChannel.TransformSpace.COMPONENT);

        return additivePose;
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {
        this.additivePoseInput.tick(context);
        this.basePoseInput.tick(context);
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return new MakeDynamicAdditiveFunction(this.additivePoseInput.wrapUnique(), this.basePoseInput.wrapUnique());
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        // Test this pose function first
        if (findCondition.test(this)) {
            return Optional.of(this);
        }
        // Test the additive pose input first. If it does not have a relevant animation player, then test the base pose input.
        Optional<PoseFunction<?>> test = this.additivePoseInput.searchDownChainForMostRelevant(findCondition);
        if (test.isPresent()) {
            return test;
        }
        return this.basePoseInput.searchDownChainForMostRelevant(findCondition);
    }
}

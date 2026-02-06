package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.data.DriverGetter;
import com.trainguy9512.locomotion.animation.data.PoseCalculationContext;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.util.Easing;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class BlendPosesByBooleanFunction implements PoseFunction<LocalSpacePose> {

    private final PoseFunction<LocalSpacePose> inputPoseTrue;
    private final PoseFunction<LocalSpacePose> inputPoseFalse;
    private final Predicate<DriverGetter> booleanGetter;
    private final Easing blendEasement;
    private final TimeSpan falseToTrueDuration;
    private final TimeSpan trueToFalseDuration;
    private final boolean skipEvaluationIfFullyBlended;

    private final VariableDriver<Float> blendValueDriver = VariableDriver.ofFloat(() -> 0f);
    private long lastUpdateTick = 0;

    public BlendPosesByBooleanFunction(
            PoseFunction<LocalSpacePose> inputPoseTrue,
            PoseFunction<LocalSpacePose> inputPoseFalse,
            Predicate<DriverGetter> booleanGetter,
            Easing blendEasement,
            TimeSpan falseToTrueDuration,
            TimeSpan trueToFalseDuration,
            boolean skipEvaluationIfFullyBlended
    ) {
        this.inputPoseTrue = inputPoseTrue;
        this.inputPoseFalse = inputPoseFalse;
        this.booleanGetter = booleanGetter;
        this.blendEasement = blendEasement;
        this.falseToTrueDuration = falseToTrueDuration;
        this.trueToFalseDuration = trueToFalseDuration;
        this.skipEvaluationIfFullyBlended = skipEvaluationIfFullyBlended;
    }

    @Override
    public @NotNull LocalSpacePose compute(PoseCalculationContext context) {
        float interpolatedBlendValue = this.blendValueDriver.getInterpolatedValue(context.partialTicks());
        interpolatedBlendValue = this.blendEasement.ease(interpolatedBlendValue);

        LocalSpacePose poseTrue = this.inputPoseTrue.compute(context);
        LocalSpacePose poseFalse = this.inputPoseFalse.compute(context);

        return poseFalse.interpolated(poseTrue, interpolatedBlendValue);
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {
        boolean isResetting = context.currentTick() - 1 > this.lastUpdateTick;
        this.lastUpdateTick = context.currentTick();
        if (context.resetting()) {
            isResetting = true;
        }


        boolean currentBooleanValue = this.booleanGetter.test(context);


        if (!isResetting) {
            this.blendValueDriver.pushCurrentToPrevious();
            float currentBlendValue = this.blendValueDriver.getCurrentValue();

            // Getting the increment
            float increment;
            if (currentBooleanValue) {
                increment = 1f / this.falseToTrueDuration.inTicks();
            } else {
                increment = 1f / -this.trueToFalseDuration.inTicks();
            }

            currentBlendValue += increment;
            currentBlendValue = Mth.clamp(currentBlendValue, 0, 1);

            this.blendValueDriver.setValue(currentBlendValue);
        } else {
            this.blendValueDriver.setValue(currentBooleanValue ? 1f : 0f);
            this.blendValueDriver.pushCurrentToPrevious();
            this.blendValueDriver.setValue(currentBooleanValue ? 1f : 0f);
        }

        boolean shouldTickFalsePose = true;
        boolean shouldTickTruePose = true;

        if (this.skipEvaluationIfFullyBlended) {
            if (this.blendValueDriver.getPreviousValue() == 1 && this.blendValueDriver.getCurrentValue() == 1) {
                shouldTickFalsePose = false;
            }
            if (this.blendValueDriver.getPreviousValue() == 0 && this.blendValueDriver.getCurrentValue() == 0) {
                shouldTickTruePose = false;
            }
        }

        if (shouldTickFalsePose) {
            this.inputPoseFalse.tick(context);
        }
        if (shouldTickTruePose) {
            this.inputPoseTrue.tick(context);
        }
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return new BlendPosesByBooleanFunction(
                this.inputPoseTrue.wrapUnique(),
                this.inputPoseFalse.wrapUnique(),
                this.booleanGetter,
                this.blendEasement,
                this.falseToTrueDuration,
                this.trueToFalseDuration,
                this.skipEvaluationIfFullyBlended
        );
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return Optional.empty();
    }

    public static Builder builder(PoseFunction<LocalSpacePose> inputPoseFalse, PoseFunction<LocalSpacePose> inputPoseTrue){
        return new Builder(inputPoseFalse, inputPoseTrue);
    }

    public static class Builder {

        private final PoseFunction<LocalSpacePose> inputPoseTrue;
        private final PoseFunction<LocalSpacePose> inputPoseFalse;
        private Predicate<DriverGetter> booleanGetter;
        private Easing blendEasement;
        private TimeSpan falseToTrueDuration;
        private TimeSpan trueToFalseDuration;
        private boolean skipEvaluationIfFullyBlended;

        private Builder(PoseFunction<LocalSpacePose> inputPoseFalse, PoseFunction<LocalSpacePose> inputPoseTrue){
            this.inputPoseTrue = inputPoseTrue;
            this.inputPoseFalse = inputPoseFalse;
            this.booleanGetter = driverGetter -> false;
            this.blendEasement = Easing.LINEAR;
            this.falseToTrueDuration = TimeSpan.ofSeconds(0.3f);
            this.trueToFalseDuration = TimeSpan.ofSeconds(0.3f);
            this.skipEvaluationIfFullyBlended = true;
        }

        public <D extends VariableDriver<Boolean>> Builder setBooleanDriver(DriverKey<D> booleanDriver) {
            this.booleanGetter = driverGetter -> driverGetter.getDriverValue(booleanDriver);
            return this;
        }

        public Builder setPredicate(Predicate<DriverGetter> predicate) {
            this.booleanGetter = predicate;
            return this;
        }

        public Builder setBlendEasement(Easing easement) {
            this.blendEasement = easement;
            return this;
        }

        public Builder setFalseToTrueDuration(TimeSpan duration) {
            this.falseToTrueDuration = duration;
            return this;
        }

        public Builder setTrueToFalseDuration(TimeSpan duration) {
            this.trueToFalseDuration = duration;
            return this;
        }

        public Builder setSkipEvaluationIfFullyBlended(boolean skipEvaluationIfFullyBlended) {
            this.skipEvaluationIfFullyBlended = skipEvaluationIfFullyBlended;
            return this;
        }

        public BlendPosesByBooleanFunction build(){
            return new BlendPosesByBooleanFunction(
                    this.inputPoseTrue,
                    this.inputPoseFalse,
                    this.booleanGetter,
                    this.blendEasement,
                    this.falseToTrueDuration,
                    this.trueToFalseDuration,
                    this.skipEvaluationIfFullyBlended
            );
        }
    }
}

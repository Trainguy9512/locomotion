package com.trainguy9512.locomotion.animation.pose.function;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class BlendPosesFunction implements PoseFunction<LocalSpacePose> {

    private final PoseFunction<LocalSpacePose> basePoseFunction;
    private final Map<BlendInput, VariableDriver<Float>> inputs;

    public BlendPosesFunction(PoseFunction<LocalSpacePose> basePoseFunction, Map<BlendInput, VariableDriver<Float>> inputs){
        this.basePoseFunction = basePoseFunction;
        this.inputs = inputs;
    }

    @Override
    public @NotNull LocalSpacePose compute(FunctionInterpolationContext context) {
        LocalSpacePose pose = this.basePoseFunction.compute(context);
        for(BlendInput blendInput : this.inputs.keySet()) {
            float weight = this.inputs.get(blendInput).getValueInterpolated(context.partialTicks());
            if(weight != 0f){
                pose = pose.interpolated(blendInput.inputFunction.compute(context), weight, blendInput.blendMask);
            }
        }
        return pose;
    }

    @Override
    public void tick(FunctionEvaluationState evaluationState) {
        this.basePoseFunction.tick(evaluationState);
        this.inputs.forEach((blendInput, weightDriver) -> {
            weightDriver.pushCurrentToPrevious();
            float weight = blendInput.weightFunction.apply(evaluationState);
            weightDriver.setValue(weight);

            if(weight != 0f) {
                blendInput.inputFunction.tick(evaluationState);
            }
        });
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        Builder builder = BlendPosesFunction.builder(this.basePoseFunction.wrapUnique());
        for(BlendInput blendInput : this.inputs.keySet()){
            builder.addBlendInput(blendInput.inputFunction.wrapUnique(), blendInput.weightFunction, blendInput.blendMask);
        }
        return builder.build();
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        // Test this pose function first
        if (findCondition.test(this)) {
            return Optional.of(this);
        }
        List<Optional<PoseFunction<?>>> blendAnimationPlayers = new ArrayList<>();
        this.inputs.forEach(((blendInput, weightDriver) -> {
            if (weightDriver.getCurrentValue() >= 0.5f) {
                blendAnimationPlayers.add(blendInput.inputFunction.searchDownChainForMostRelevant(findCondition));
            }
        }));
        if (!blendAnimationPlayers.isEmpty()) {
            return blendAnimationPlayers.getLast().isPresent() ?
                    blendAnimationPlayers.getLast() :
                    this.basePoseFunction.searchDownChainForMostRelevant(findCondition);
        } else {
            return this.basePoseFunction.searchDownChainForMostRelevant(findCondition);
        }
    }


    public static Builder builder(PoseFunction<LocalSpacePose> basePoseFunction){
        return new Builder(basePoseFunction);
    }

    public static class Builder {

        private final PoseFunction<LocalSpacePose> baseFunction;
        private final Map<BlendInput, VariableDriver<Float>> inputs;

        private Builder(PoseFunction<LocalSpacePose> baseFunction){
            this.baseFunction = baseFunction;
            this.inputs = Maps.newHashMap();
        }

        public Builder addBlendInput(PoseFunction<LocalSpacePose> inputFunction, Function<FunctionEvaluationState, Float> weightFunction, @Nullable BlendMask blendMask){
            this.inputs.put(new BlendInput(inputFunction, weightFunction, blendMask), VariableDriver.ofFloat(() -> 0f));
            return this;
        }

        public Builder addBlendInput(PoseFunction<LocalSpacePose> inputFunction, Function<FunctionEvaluationState, Float> weightFunction){
            return this.addBlendInput(inputFunction, weightFunction, null);
        }

        public BlendPosesFunction build(){
            return new BlendPosesFunction(this.baseFunction, this.inputs);
        }
    }

    public record BlendInput(
            PoseFunction<LocalSpacePose> inputFunction,
            Function<FunctionEvaluationState, Float> weightFunction,
            @Nullable BlendMask blendMask
    ) {

    }
}

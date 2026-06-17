package com.trainguy9512.locomotion.animation.pose.function;

import com.trainguy9512.locomotion.animation.data.PoseCalculationContext;
import com.trainguy9512.locomotion.animation.data.PoseTickEvaluationContext;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.Pose;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

public class CustomAttributeModifierFunction implements PoseFunction<LocalSpacePose> {

    private final PoseFunction<LocalSpacePose> input;
    private final String customAttributeName;
    private final Function<PoseCalculationContext, Float> customAttributeValueFunction;

    public CustomAttributeModifierFunction(PoseFunction<LocalSpacePose> input, String customAttributeName, Function<PoseCalculationContext, Float> customAttributeValueFunction) {
        this.input = input;
        this.customAttributeName = customAttributeName;
        this.customAttributeValueFunction = customAttributeValueFunction;
    }

    public static CustomAttributeModifierFunction of(PoseFunction<LocalSpacePose> input, String customAttributeName, Function<PoseCalculationContext, Float> customAttributeValueFunction) {
        return new CustomAttributeModifierFunction(input, customAttributeName, customAttributeValueFunction);
    }

    @Override
    public @NotNull LocalSpacePose compute(PoseCalculationContext context) {
        LocalSpacePose pose = this.input.compute(context);
        float value = this.customAttributeValueFunction.apply(context);
        pose.loadCustomAttributeValue(this.customAttributeName, value);
        return pose;
    }

    @Override
    public void tick(PoseTickEvaluationContext context) {
        this.input.tick(context);
    }

    @Override
    public PoseFunction<LocalSpacePose> wrapUnique() {
        return CustomAttributeModifierFunction.of(this.input.wrapUnique(), this.customAttributeName, this.customAttributeValueFunction);
    }

    @Override
    public Optional<PoseFunction<?>> searchDownChainForMostRelevant(Predicate<PoseFunction<?>> findCondition) {
        return findCondition.test(this) ? Optional.of(this) : this.input.searchDownChainForMostRelevant(findCondition);
    }
}

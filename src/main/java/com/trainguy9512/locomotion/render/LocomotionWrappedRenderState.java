package com.trainguy9512.locomotion.render;

import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LocomotionWrappedRenderState<D> {

    private final D innerValue;
    private final AnimationDataContainer dataContainer;

    private LocomotionWrappedRenderState(D innerValue, @Nullable AnimationDataContainer dataContainer) {
        this.innerValue = innerValue;
        this.dataContainer = dataContainer;
    }

    public static <D> LocomotionWrappedRenderState<D> of(D innerValue, @Nullable AnimationDataContainer dataContainer) {
        return new LocomotionWrappedRenderState<>(innerValue, dataContainer);
    }

    public D getInnerValue() {
        return this.innerValue;
    }

    public Optional<AnimationDataContainer> getDataContainer() {
        return Optional.ofNullable(this.dataContainer);
    }
}

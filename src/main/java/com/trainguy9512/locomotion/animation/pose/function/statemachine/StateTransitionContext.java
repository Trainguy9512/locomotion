package com.trainguy9512.locomotion.animation.pose.function.statemachine;

import com.trainguy9512.locomotion.animation.data.DriverGetter;
import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.AnimationPlayer;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.util.TimeSpan;

import java.util.Optional;

public record StateTransitionContext(
        @Deprecated
        DriverGetter driverContainer,
        TimeSpan timeElapsedInCurrentState,
        float currentStateWeight,
        float previousStateWeight,
        PoseFunction<LocalSpacePose> currentStateInput,
        TimeSpan transitionDuration
) implements DriverGetter {

    public Optional<AnimationPlayer> findMostRelevantAnimationPlayer() {
        Optional<PoseFunction<?>> foundPoseFunction = this.currentStateInput.searchDownChainForMostRelevant(poseFunction -> poseFunction instanceof AnimationPlayer);
        return foundPoseFunction.map(poseFunction -> (AnimationPlayer) poseFunction);
    }

    @Override
    @SuppressWarnings("deprecated")
    public <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey) {
        return this.driverContainer.getDriver(driverKey);
    }
}

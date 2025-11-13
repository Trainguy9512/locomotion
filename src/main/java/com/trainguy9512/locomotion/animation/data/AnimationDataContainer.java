package com.trainguy9512.locomotion.animation.data;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.animation.animator.JointAnimator;
import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.resource.LocomotionResources;
import com.trainguy9512.locomotion.util.Interpolator;
import com.trainguy9512.locomotion.util.TimeSpan;

import java.util.Map;

public class AnimationDataContainer implements PoseCalculationDataContainer, OnTickDriverContainer {

    private final Map<DriverKey<? extends Driver<?>>, Driver<?>> drivers;
    private final CachedPoseContainer savedCachedPoseContainer;
    private final PoseFunction<LocalSpacePose> poseFunction;
    private final MontageManager montageManager;

    private final JointSkeleton jointSkeleton;
    private final DriverKey<VariableDriver<LocalSpacePose>> perTickCalculatedPoseDriverKey;
    private final DriverKey<VariableDriver<Long>> gameTimeTicksDriverKey;

    private AnimationDataContainer(JointAnimator<?> jointAnimator) {
        this.drivers = Maps.newHashMap();
        this.savedCachedPoseContainer = CachedPoseContainer.of();
        this.poseFunction = jointAnimator.constructPoseFunction(savedCachedPoseContainer).wrapUnique();
        this.montageManager = MontageManager.of(this);

        this.jointSkeleton = LocomotionResources.getOrThrowJointSkeleton(jointAnimator.getJointSkeleton());
        this.perTickCalculatedPoseDriverKey = DriverKey.of("per_tick_calculated_pose", () -> VariableDriver.ofInterpolatable(() -> LocalSpacePose.of(jointSkeleton), Interpolator.LOCAL_SPACE_POSE));
        this.gameTimeTicksDriverKey = DriverKey.of("game_time", () -> VariableDriver.ofConstant(() -> 0L));
        this.tick();
    }

    public static AnimationDataContainer of(JointAnimator<?> jointAnimator) {
        return new AnimationDataContainer(jointAnimator);
    }

    public void preTick() {
        this.drivers.values().forEach(Driver::pushCurrentToPrevious);
    }

    public void tick() {
        this.montageManager.tick();
        this.drivers.values().forEach(Driver::tick);
        this.getDriver(this.gameTimeTicksDriverKey).setValue(this.getDriver(this.gameTimeTicksDriverKey).getCurrentValue() + 1);
        this.poseFunction.tick(PoseFunction.FunctionEvaluationState.of(
                this,
                this.montageManager,
                false,
                this.getDriver(this.gameTimeTicksDriverKey).getCurrentValue()
        ));
    }

    public void postTick() {
        this.drivers.values().forEach(Driver::postTick);
    }

    public LocalSpacePose computePose(float partialTicks) {
        this.savedCachedPoseContainer.clearCaches();
        return this.poseFunction.compute(PoseFunction.FunctionInterpolationContext.of(
                this,
                this.montageManager,
                partialTicks,
                TimeSpan.ofTicks(this.getInterpolatedDriverValue(gameTimeTicksDriverKey, 1) + partialTicks)
        ));
    }

    @Override
    public JointSkeleton getJointSkeleton() {
        return this.jointSkeleton;
    }

    public DriverKey<VariableDriver<LocalSpacePose>> getPerTickCalculatedPoseDriverKey() {
        return this.perTickCalculatedPoseDriverKey;
    }

    public MontageManager getMontageManager() {
        return this.montageManager;
    }

    public Map<DriverKey<? extends Driver<?>>, Driver<?>> getAllDrivers() {
        return this.drivers;
    }

    @Override
    public <D, R extends Driver<D>> D getInterpolatedDriverValue(DriverKey<R> driverKey, float partialTicks) {
        return this.getDriver(driverKey).getValueInterpolated(partialTicks);
    }

    @Override
    public <D, R extends Driver<D>> D getDriverValue(DriverKey<R> driverKey) {
        return this.getInterpolatedDriverValue(driverKey, 1);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey) {
        return (R) this.drivers.computeIfAbsent(driverKey, DriverKey::createInstance);
    }
}

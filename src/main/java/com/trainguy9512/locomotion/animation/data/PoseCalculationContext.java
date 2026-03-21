package com.trainguy9512.locomotion.animation.data;

import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.animation.util.TimeSpan;

import java.util.function.Function;

public record PoseCalculationContext (
        @Deprecated
        DriverGetter dataContainer,
        JointSkeleton jointSkeleton,
        MontageManager montageManager,
        float partialTicks,
        TimeSpan gameTime
) implements DriverGetter {

    public <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey) {
        return this.dataContainer.getDriver(driverKey);
    }

    @Override
    public <D, R extends Driver<D>> D getDriverValue(DriverKey<R> driverKey) {
        return this.getDriver(driverKey).getInterpolatedValue(this.partialTicks);
    }
}

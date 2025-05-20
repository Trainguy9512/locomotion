package com.trainguy9512.locomotion.animation.data;

import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;

/**
 * Interface for retrieving the interpolated value of a driver during the sampling stage.
 */
public interface PoseCalculationDataContainer {

    /**
     * Returns the interpolated value of an animation driver.
     * @param driverKey         {@link DriverKey <>} of the driver to return a value for.
     * @param partialTicks      Percentage of a tick since the previous tick.
     * @return                  Interpolated value
     */
    public <D, R extends Driver<D>> D getInterpolatedDriverValue(DriverKey<R> driverKey, float partialTicks);

    /**
     * Returns the joint skeleton for the data container.
     */
    public JointSkeleton getJointSkeleton();

}

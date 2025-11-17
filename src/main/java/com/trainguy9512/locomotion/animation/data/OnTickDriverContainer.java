package com.trainguy9512.locomotion.animation.data;

import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.Driver;

public interface OnTickDriverContainer {
    /**
     * Returns the value of an animation driver at the current tick.
     * @param driverKey         {@link DriverKey <>} of the driver to return a value for.
     * @return                  Interpolated value
     */
    public <D, R extends Driver<D>> D getDriverValue(DriverKey<R> driverKey);

    public <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey);

    public long getCurrentTick();
}

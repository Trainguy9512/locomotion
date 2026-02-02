package com.trainguy9512.locomotion.animation.data;

import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;

import java.util.function.Function;

public interface DriverGetter {

    default <D, R extends Driver<D>> D getDriverValue(DriverKey<R> driverKey) {
        return this.getDriver(driverKey).getCurrentValue();
    }

    <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey);
}

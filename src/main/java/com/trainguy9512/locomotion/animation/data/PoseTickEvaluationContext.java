package com.trainguy9512.locomotion.animation.data;

import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;

import java.util.function.Function;

public record PoseTickEvaluationContext(
        @Deprecated
        DriverGetter driverContainer,
        MontageManager montageManager,
        boolean resetting,
        long currentTick
) implements DriverGetter {

    /**
     * Creates a copy of the evaluation state that is marked for a hard reset.
     *
     * <p>A hard reset is an animation reset that immediately resets with no blending.</p>
     */
    public PoseTickEvaluationContext markedForReset() {
        return new PoseTickEvaluationContext(this.driverContainer, this.montageManager, true, this.currentTick);
    }

    public PoseTickEvaluationContext cleared() {
        return new PoseTickEvaluationContext(this.driverContainer, this.montageManager, false, this.currentTick);
    }

    /**
     * Runs the provided function if this evaluation state is marked for hard reset.
     */
    public void ifMarkedForReset(Runnable runnable) {
        if (this.resetting) {
            runnable.run();
        }
    }

    @Override
    public <D, R extends Driver<D>> R getDriver(DriverKey<R> driverKey) {
        return this.driverContainer.getDriver(driverKey);
    }

    public <D, R extends VariableDriver<D>> void setVariableDriverValue(DriverKey<R> driverKey, D newValue) {
        this.getDriver(driverKey).setValue(newValue);
    }
}

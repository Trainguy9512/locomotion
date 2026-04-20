package com.trainguy9512.locomotion.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LocomotionEventBus {
    private static LocomotionEventBus INSTANCE;

    public static LocomotionEventBus getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new LocomotionEventBus();
        }
        return INSTANCE;
    }

    private final List<Consumer<FirstPersonRenderEvents.BeforeArmRender>> beforeArmRenderListeners = new ArrayList<>();
    private final List<Consumer<FirstPersonRenderEvents.AfterArmRender>> afterArmRenderListeners = new ArrayList<>();
    private final List<Consumer<FirstPersonRenderEvents.BeforeItemRender>> beforeItemRenderListeners = new ArrayList<>();
    private final List<Consumer<FirstPersonRenderEvents.AfterItemRender>> afterItemRenderListeners = new ArrayList<>();
    private final List<Consumer<FirstPersonRenderEvents.BeforeFirstPersonRender>> beforeFirstPersonRenderListeners = new ArrayList<>();
    private final List<Consumer<FirstPersonRenderEvents.AfterFirstPersonRender>> afterFirstPersonRenderListeners = new ArrayList<>();

    // Registrar listeners
    public void registerBeforeArmRender(Consumer<FirstPersonRenderEvents.BeforeArmRender> listener) {
        beforeArmRenderListeners.add(listener);
    }

    public void registerAfterArmRender(Consumer<FirstPersonRenderEvents.AfterArmRender> listener) {
        afterArmRenderListeners.add(listener);
    }

    public void registerBeforeItemRender(Consumer<FirstPersonRenderEvents.BeforeItemRender> listener) {
        beforeItemRenderListeners.add(listener);
    }

    public void registerAfterItemRender(Consumer<FirstPersonRenderEvents.AfterItemRender> listener) {
        afterItemRenderListeners.add(listener);
    }

    public void registerBeforeFirstPersonRender(Consumer<FirstPersonRenderEvents.BeforeFirstPersonRender> listener) {
        beforeFirstPersonRenderListeners.add(listener);
    }

    public void registerAfterFirstPersonRender(Consumer<FirstPersonRenderEvents.AfterFirstPersonRender> listener) {
        afterFirstPersonRenderListeners.add(listener);
    }

    public boolean fireBeforeArmRender(FirstPersonRenderEvents.BeforeArmRender event) {
        for (Consumer<FirstPersonRenderEvents.BeforeArmRender> listener : beforeArmRenderListeners) {
            listener.accept(event);
            if (event.isCancelled()) return true;
        }
        return false;
    }

    public void fireAfterArmRender(FirstPersonRenderEvents.AfterArmRender event) {
        for (Consumer<FirstPersonRenderEvents.AfterArmRender> listener : afterArmRenderListeners) {
            listener.accept(event);
        }
    }

    public boolean fireBeforeItemRender(FirstPersonRenderEvents.BeforeItemRender event) {
        for (Consumer<FirstPersonRenderEvents.BeforeItemRender> listener : beforeItemRenderListeners) {
            listener.accept(event);
            if (event.isCancelled()) return true;
        }
        return false;
    }

    public void fireAfterItemRender(FirstPersonRenderEvents.AfterItemRender event) {
        for (Consumer<FirstPersonRenderEvents.AfterItemRender> listener : afterItemRenderListeners) {
            listener.accept(event);
        }
    }

    public boolean fireBeforeFirstPersonRender(FirstPersonRenderEvents.BeforeFirstPersonRender event) {
        for (Consumer<FirstPersonRenderEvents.BeforeFirstPersonRender> listener : beforeFirstPersonRenderListeners) {
            listener.accept(event);
            if (event.isCancelled()) return true;
        }
        return false;
    }

    public void fireAfterFirstPersonRender(FirstPersonRenderEvents.AfterFirstPersonRender event) {
        for (Consumer<FirstPersonRenderEvents.AfterFirstPersonRender> listener : afterFirstPersonRenderListeners) {
            listener.accept(event);
        }
    }
}
package com.trainguy9512.locomotion.animation.pose.function;

public enum SequenceReferencePoint {
    BEGINNING(0),
    END(1);

    private final float progressThroughSequence;

    SequenceReferencePoint(float progressThroughSequence) {
        this.progressThroughSequence = progressThroughSequence;
    }

    public float getProgressThroughSequence() {
        return this.progressThroughSequence;
    }
}

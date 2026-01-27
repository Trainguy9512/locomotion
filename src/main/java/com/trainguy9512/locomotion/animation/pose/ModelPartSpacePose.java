package com.trainguy9512.locomotion.animation.pose;

public class ModelPartSpacePose extends Pose {

    protected ModelPartSpacePose(Pose pose) {
        super(pose);
    }

    static ModelPartSpacePose of(Pose pose) {
        return new ModelPartSpacePose(pose);
    }
}

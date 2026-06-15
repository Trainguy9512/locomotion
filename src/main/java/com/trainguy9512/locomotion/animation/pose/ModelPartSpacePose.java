package com.trainguy9512.locomotion.animation.pose;

import com.trainguy9512.locomotion.access.MatrixModelPart;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;

import java.util.function.Function;

public class ModelPartSpacePose extends Pose {

    protected ModelPartSpacePose(Pose pose) {
        super(pose);
    }

    static ModelPartSpacePose of(Pose pose) {
        return new ModelPartSpacePose(pose);
    }

    public <S> void setupAnimOnModel(Model<S> model) {
        model.resetPose();

        JointSkeleton jointSkeleton = this.getJointSkeleton();

        Function<String, ModelPart> partLookup = model.root().createPartLookup();
        jointSkeleton.getJoints().forEach(joint -> {
            String modelPartIdentifier = jointSkeleton.getJointConfiguration(joint).modelPartIdentifier();
            if (modelPartIdentifier != null) {
                ModelPart modelPart = partLookup.apply(modelPartIdentifier);
                if (modelPart != null) {
                    ((MatrixModelPart)(Object) modelPart).locomotion$setMatrix(this.getJointChannel(joint).getTransform());
                }
            }
        });
    }
}

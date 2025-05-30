package com.trainguy9512.locomotion.animation.joint.skeleton;

import java.util.Map;

public abstract class SkeletonPropertyDefinition<D> {

    protected final Map<String, D> jointProperties;
    protected final Map<String, D> customAttributeProperties;
    protected final boolean isMirrored;
    protected final D defaultValue;

    protected SkeletonPropertyDefinition(Map<String, D> jointProperties, Map<String, D> customAttributeProperties, boolean mirrored, D defaultValue) {
        this.jointProperties = jointProperties;
        this.customAttributeProperties = customAttributeProperties;
        this.isMirrored = mirrored;
        this.defaultValue = defaultValue;
    }

    public D getJointProperty(String jointName, JointSkeleton skeleton) {
        if (!skeleton.containsJoint(jointName)) {
            return this.defaultValue;
        }
        if (this.isMirrored) {
            jointName = skeleton.getJointConfiguration(jointName).mirrorJoint();
        }
        return this.jointProperties.getOrDefault(jointName, this.defaultValue);
    }

    public D getCustomAttributeProperty(String customAttributeName, JointSkeleton skeleton) {
        if (!skeleton.containsCustomAttribute(customAttributeName)) {
            return this.defaultValue;
        }
        return this.customAttributeProperties.getOrDefault(customAttributeName, this.defaultValue);
    }

    public abstract SkeletonPropertyDefinition<D> getMirrored();
}

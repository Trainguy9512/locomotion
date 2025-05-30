package com.trainguy9512.locomotion.animation.joint.skeleton;

import com.google.common.collect.Maps;
import net.minecraft.util.Mth;

import java.util.Map;
import java.util.Set;

/**
 * Configuration that allows individual joints to blend faster than others through duration multipliers.
 * @param jointDurationMultipliers      Map of joints to duration multipliers. Duration multipliers can be anywhere between 0 and 1.
 * @param isMirrored                    Whether the blend profile will be mirrored or not, used by {@link BlendProfile#ofMirrored}
 */
public class BlendProfile extends SkeletonPropertyDefinition<Float> {

    private BlendProfile(Map<String, Float> jointProperties, Map<String, Float> customAttributeProperties, boolean mirrored) {
        super(jointProperties, customAttributeProperties, mirrored, 1f);
    }

    @Override
    public BlendProfile getMirrored() {
        return new BlendProfile(this.jointProperties, this.customAttributeProperties, !this.isMirrored);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private final Map<String, Float> jointDurationMultipliers;
        private final Map<String, Float> customAttributeDurationMultipliers;

        public Builder() {
            this.jointDurationMultipliers = Maps.newHashMap();
            this.customAttributeDurationMultipliers = Maps.newHashMap();
        }

        /**
         * Defines a duration multiplier for the provided joint.
         * @param jointName             Name of the joint
         * @param durationMultiplier    Float value between 0 and 1.
         */
        public Builder defineForJoint(String jointName, float durationMultiplier) {
            this.jointDurationMultipliers.put(jointName, Mth.clamp(durationMultiplier, 0.001f, 1f));
            return this;
        }

        /**
         * Defines a duration multiplier for multiple joints.
         * @param jointNames            Set of joint names to assign the duration multiplier.
         * @param durationMultiplier    Float value between 0 and 1.
         */
        public Builder defineForMultipleJoints(Set<String> jointNames, float durationMultiplier) {
            jointNames.forEach(jointName -> this.defineForJoint(jointName, durationMultiplier));
            return this;
        }

        /**
         * Defines a weight for the provided custom attribute.
         * @param customAttributeName   Name of the custom attribute
         * @param weight                Weight value between 0 and 1.
         */
        public Builder defineForCustomAttribute(String customAttributeName, float weight) {
            this.customAttributeDurationMultipliers.put(customAttributeName, Mth.clamp(weight, 0f, 1f));
            return this;
        }

        /**
         * Defines a duration multiplier for multiple custom attributes.
         * @param customAttributeNames  Set of custom attribute names to assign the provided weight to.
         * @param weight                Weight value between 0 and 1.
         */
        public Builder defineForMultipleCustomAttributes(Set<String> customAttributeNames, float weight) {
            customAttributeNames.forEach(jointName -> this.defineForJoint(jointName, weight));
            return this;
        }

        public BlendProfile build() {
            return new BlendProfile(this.jointDurationMultipliers, this.customAttributeDurationMultipliers, false);
        }
    }
}

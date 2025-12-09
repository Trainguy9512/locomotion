package com.trainguy9512.locomotion.animation.sequence;

import com.google.common.collect.Maps;
import com.mojang.math.Axis;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.resource.LocomotionResources;
import com.trainguy9512.locomotion.util.Interpolator;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Timeline;
import net.minecraft.resources.Identifier;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public record AnimationSequence(
        Map<String, Timeline<Vector3f>> translationTimelines,
        Map<String, Timeline<Quaternionf>> rotationTimelines,
        Map<String, Timeline<Vector3f>> scaleTimelines,
        Map<String, Timeline<Boolean>> visibilityTimelines,
        Map<String, Timeline<Float>> customAttributeTimelines,
        Map<String, List<TimeSpan>> timeMarkers,
        Identifier jointSkeletonLocation,
        TimeSpan length
) {

    /**
     * Creates an animation pose from a point in time within the provided animation sequence
     * @param jointSkeleton         Template joint skeleton
     * @param sequenceLocation      Animation sequence resource location
     * @param time                  Point of time in the animation to get.
     * @param looping               Whether the animation should be looped or not.
     * @return                      New animation pose
     */
    public static LocalSpacePose samplePose(JointSkeleton jointSkeleton, Identifier sequenceLocation, TimeSpan time, boolean looping) {
        AnimationSequence animationSequence = LocomotionResources.getOrThrowAnimationSequence(sequenceLocation);
        LocalSpacePose pose = LocalSpacePose.of(jointSkeleton);
        for (String joint : jointSkeleton.getJoints()) {
            JointChannel channel = JointChannel.ofTranslationRotationScaleQuaternion(
                    animationSequence.translationTimelines().get(joint).getValueAtTime(time.inSeconds(), looping),
                    animationSequence.rotationTimelines().get(joint).getValueAtTime(time.inSeconds(), looping),
                    animationSequence.scaleTimelines().get(joint).getValueAtTime(time.inSeconds(), looping),
                    animationSequence.visibilityTimelines().get(joint).getValueAtTime(time.inSeconds(), looping)
            );
            pose.setJointChannel(joint, channel);
        }
        for (String customAttribute : animationSequence.customAttributeTimelines.keySet()) {
            pose.loadCustomAttributeValue(customAttribute, animationSequence.customAttributeTimelines.get(customAttribute).getValueAtTime(time.inSeconds()));
        }
        return pose;
    }

    public boolean containsTimelinesForJoint(String joint) {
        return this.translationTimelines().containsKey(joint) && this.rotationTimelines().containsKey(joint) && this.scaleTimelines().containsKey(joint) && this.visibilityTimelines().containsKey(joint);
    }

    /**
     * Returns a set of marker identifiers within the specified time range.
     *
     * @param start  Start time
     * @param end    End time
     * @param looped Whether the time range should be looped based on the sequence's length
     * @return Set of marker identifiers.
     */
    public Set<String> getMarkersInRange(TimeSpan start, TimeSpan end, boolean looped) {
        float startSeconds = looped ? start.inSeconds() % this.length.inSeconds() : start.inSeconds();
        float endSeconds = looped ? end.inSeconds() % this.length.inSeconds() : end.inSeconds();
        Set<String> markersToReturn = new HashSet<>();
        this.timeMarkers.forEach((identifier, times) -> times.forEach(markerTime -> {
            float markerTimeSeconds = markerTime.inSeconds();

            boolean isRangeWrappedAroundLoop = endSeconds < startSeconds;
            if (isRangeWrappedAroundLoop && (markerTimeSeconds <= endSeconds || markerTimeSeconds > startSeconds)) {
                markersToReturn.add(identifier);
            } else if (markerTimeSeconds > startSeconds && markerTimeSeconds <= endSeconds) {
                markersToReturn.add(identifier);
            }
        }));
        return markersToReturn;
    }

    public AnimationSequence getBaked() {
        Builder bakedSequenceBuilder = AnimationSequence.builder(this.length, this.jointSkeletonLocation);
        JointSkeleton jointSkeleton = LocomotionResources.getOrThrowJointSkeleton(this.jointSkeletonLocation);
        for (String joint : jointSkeleton.getJoints()) {
            if (this.translationTimelines.containsKey(joint)) {
                bakedSequenceBuilder.putJointTranslationTimeline(joint, this.translationTimelines.get(joint));
                bakedSequenceBuilder.putJointRotationTimeline(joint, this.rotationTimelines.get(joint));
                bakedSequenceBuilder.putJointScaleTimeline(joint, this.scaleTimelines.get(joint));
                bakedSequenceBuilder.putJointVisibilityTimeline(joint, this.visibilityTimelines.get(joint));
            } else {
                bakedSequenceBuilder.putJointTranslationTimeline(joint, Timeline.of(Interpolator.VECTOR_FLOAT, this.length.inSeconds()).addKeyframe(0, new Vector3f(0, 0, 0)));
                bakedSequenceBuilder.putJointRotationTimeline(joint, Timeline.of(Interpolator.QUATERNION, this.length.inSeconds()).addKeyframe(0, Axis.XP.rotation(0f)));
                bakedSequenceBuilder.putJointScaleTimeline(joint, Timeline.of(Interpolator.VECTOR_FLOAT, this.length.inSeconds()).addKeyframe(0, new Vector3f(0, 0, 0)));
                bakedSequenceBuilder.putJointVisibilityTimeline(joint, Timeline.of(Interpolator.BOOLEAN_KEYFRAME, this.length.inSeconds()).addKeyframe(0, true));
            }
        }
        for (String timeMarker : this.timeMarkers.keySet()) {
            for (TimeSpan time : this.timeMarkers.get(timeMarker)) {
                bakedSequenceBuilder.putTimeMarker(timeMarker, time);
            }
        }
        for (String customAttribute : jointSkeleton.getCustomAttributeDefaults().keySet()) {
            if (this.customAttributeTimelines.containsKey(customAttribute)) {
                bakedSequenceBuilder.putCustomAttributeTimeline(customAttribute, this.customAttributeTimelines.get(customAttribute));
            }
        }
        return bakedSequenceBuilder.build();
    }

    public static Builder builder(TimeSpan frameLength, Identifier jointSkeletonLocation) {
        return new Builder(frameLength, jointSkeletonLocation);
    }

    public static class Builder {
        private final Map<String, Timeline<Vector3f>> translationTimelines;
        private final Map<String, Timeline<Quaternionf>> rotationTimelines;
        private final Map<String, Timeline<Vector3f>> scaleTimelines;
        private final Map<String, Timeline<Boolean>> visibilityTimelines;
        private final Map<String, Timeline<Float>> customAttributeTimelines;
        private final Map<String, List<TimeSpan>> timeMarkers;
        private final Identifier jointSkeletonLocation;
        private final TimeSpan length;

        protected Builder(TimeSpan length, Identifier jointSkeletonLocation) {
            this.translationTimelines = Maps.newHashMap();
            this.rotationTimelines = Maps.newHashMap();
            this.scaleTimelines = Maps.newHashMap();
            this.visibilityTimelines = Maps.newHashMap();
            this.customAttributeTimelines = Maps.newHashMap();
            this.timeMarkers = Maps.newHashMap();
            this.jointSkeletonLocation = jointSkeletonLocation;
            this.length = length;
        }

        public Builder putJointTranslationTimeline(String jointName, Timeline<Vector3f> timeline) {
            this.translationTimelines.put(jointName, timeline);
            return this;
        }

        public Builder putJointRotationTimeline(String jointName, Timeline<Quaternionf> timeline) {
            this.rotationTimelines.put(jointName, timeline);
            return this;
        }

        public Builder putJointScaleTimeline(String jointName, Timeline<Vector3f> timeline) {
            this.scaleTimelines.put(jointName, timeline);
            return this;
        }

        public Builder putJointVisibilityTimeline(String jointName, Timeline<Boolean> timeline) {
            this.visibilityTimelines.put(jointName, timeline);
            return this;
        }

        public Builder putCustomAttributeTimeline(String customAttributeName, Timeline<Float> timeline) {
            this.customAttributeTimelines.put(customAttributeName, timeline);
            return this;
        }

        public Builder putTimeMarker(String identifier, TimeSpan time) {
            if (!this.timeMarkers.containsKey(identifier)) {
                this.timeMarkers.put(identifier, new ArrayList<>());
            }
            this.timeMarkers.get(identifier).add(time);
            return this;
        }

        public AnimationSequence build() {
            return new AnimationSequence(
                    this.translationTimelines,
                    this.rotationTimelines,
                    this.scaleTimelines,
                    this.visibilityTimelines,
                    this.customAttributeTimelines,
                    this.timeMarkers,
                    this.jointSkeletonLocation,
                    this.length
            );
        }

    }
}

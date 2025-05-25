package com.trainguy9512.locomotion.animation.sequence;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Timeline;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public record AnimationSequence(
        Map<String, Timeline<Vector3f>> translationTimelines,
        Map<String, Timeline<Quaternionf>> rotationTimelines,
        Map<String, Timeline<Vector3f>> scaleTimelines,
        Map<String, Timeline<Boolean>> visibilityTimelines,
        Map<String, List<TimeSpan>> timeMarkers,
        TimeSpan length
) {

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

    public static Builder builder(TimeSpan frameLength) {
        return new Builder(frameLength);
    }

    public static class Builder {
        private final Map<String, Timeline<Vector3f>> translationTimelines;
        private final Map<String, Timeline<Quaternionf>> rotationTimelines;
        private final Map<String, Timeline<Vector3f>> scaleTimelines;
        private final Map<String, Timeline<Boolean>> visibilityTimelines;
        private final Map<String, List<TimeSpan>> timeMarkers;
        private final TimeSpan length;

        protected Builder(TimeSpan length) {
            this.translationTimelines = Maps.newHashMap();
            this.rotationTimelines = Maps.newHashMap();
            this.scaleTimelines = Maps.newHashMap();
            this.visibilityTimelines = Maps.newHashMap();
            this.timeMarkers = Maps.newHashMap();
            this.length = length;
        }

        public void putJointTranslationTimeline(String jointName, Timeline<Vector3f> timeline) {
            this.translationTimelines.put(jointName, timeline);
        }

        public void putJointRotationTimeline(String jointName, Timeline<Quaternionf> timeline) {
            this.rotationTimelines.put(jointName, timeline);
        }

        public void putJointScaleTimeline(String jointName, Timeline<Vector3f> timeline) {
            this.scaleTimelines.put(jointName, timeline);
        }

        public void putJointVisibilityTimeline(String jointName, Timeline<Boolean> timeline) {
            this.visibilityTimelines.put(jointName, timeline);
        }

        public void putTimeMarker(String identifier, TimeSpan time) {
            if (!this.timeMarkers.containsKey(identifier)) {
                this.timeMarkers.put(identifier, new ArrayList<>());
            }
            this.timeMarkers.get(identifier).add(time);
        }

        public AnimationSequence build() {
            return new AnimationSequence(
                    this.translationTimelines,
                    this.rotationTimelines,
                    this.scaleTimelines,
                    this.visibilityTimelines,
                    this.timeMarkers,
                    this.length
            );
        }

    }
}

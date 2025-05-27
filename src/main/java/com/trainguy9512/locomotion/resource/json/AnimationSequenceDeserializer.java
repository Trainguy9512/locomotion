package com.trainguy9512.locomotion.resource.json;

import com.google.gson.*;
import com.trainguy9512.locomotion.animation.sequence.AnimationSequence;
import com.trainguy9512.locomotion.resource.FormatVersion;
import com.trainguy9512.locomotion.util.Interpolator;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Timeline;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class AnimationSequenceDeserializer implements JsonDeserializer<AnimationSequence> {

    private static final String LENGTH_KEY = "length";
    private static final String JOINTS_KEY = "joints";

    private static final List<String> REQUIRED_KEYS = List.of(
            LENGTH_KEY,
            JOINTS_KEY
    );

    @Override
    public AnimationSequence deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject sequenceJsonObject = jsonElement.getAsJsonObject();

        FormatVersion version = FormatVersion.ofAssetJsonObject(sequenceJsonObject);
        if (!version.isCompatible()) {
            throw new JsonParseException("Animation sequence version is out of date for deserializer.");
        }

        for (String key : REQUIRED_KEYS) {
            if (!sequenceJsonObject.has(key)) {
                throw new JsonParseException("Required key " + key + " not found in data.");
            }
        }

        float sequenceLength = sequenceJsonObject.get(LENGTH_KEY).getAsFloat();
        AnimationSequence.Builder sequenceBuilder = AnimationSequence.builder(TimeSpan.ofSeconds(sequenceLength));

        Map<String, JsonElement> jointsJsonMap = sequenceJsonObject.getAsJsonObject("joints").asMap();
        jointsJsonMap.forEach((joint, jointElement) -> {
            JsonObject jointJsonObject = jointElement.getAsJsonObject();
            Timeline<Vector3f> translationTimeline = deserializeTimeline(
                    context,
                    jointJsonObject,
                    "translation",
                    Vector3f.class,
                    Interpolator.VECTOR_FLOAT,
                    sequenceLength
            );
            Timeline<Quaternionf> rotationTimeline = deserializeTimeline(
                    context,
                    jointJsonObject,
                    "rotation",
                    Quaternionf.class,
                    Interpolator.QUATERNION,
                    sequenceLength
            );
            Timeline<Vector3f> scaleTimeline = deserializeTimeline(
                    context,
                    jointJsonObject,
                    "scale",
                    Vector3f.class,
                    Interpolator.VECTOR_FLOAT,
                    sequenceLength
            );
            Timeline<Boolean> visibilityTimeline = deserializeTimeline(
                    context,
                    jointJsonObject,
                    "visibility",
                    Boolean.class,
                    Interpolator.BOOLEAN_KEYFRAME,
                    sequenceLength
            );

            sequenceBuilder.putJointTranslationTimeline(joint, translationTimeline);
            sequenceBuilder.putJointRotationTimeline(joint, rotationTimeline);
            sequenceBuilder.putJointScaleTimeline(joint, scaleTimeline);
            sequenceBuilder.putJointVisibilityTimeline(joint, visibilityTimeline);
        });

        // Load time markers from the JSON file, if it has any.
        if (sequenceJsonObject.has("time_markers")) {
            Map<String, JsonElement> timeMarkers = sequenceJsonObject.getAsJsonObject("time_markers").asMap();
            timeMarkers.forEach((timeMarkerIdentifier, timeMarkerElement) -> {
                JsonArray times = timeMarkerElement.getAsJsonArray();
                times.forEach(time -> sequenceBuilder.putTimeMarker(timeMarkerIdentifier, TimeSpan.ofSeconds(time.getAsFloat())));
            });
        }
        return sequenceBuilder.build();
    }

    private static <X> Timeline<X> deserializeTimeline(JsonDeserializationContext context, JsonObject jsonObject, String identifier, Class<X> type, Interpolator<X> interpolator, float sequenceLength){
        Timeline<X> timeline = Timeline.of(interpolator, sequenceLength);
        jsonObject.getAsJsonObject(identifier).asMap().forEach((keyframeString, keyframeValueElement) -> {
            float keyframe = Float.parseFloat(keyframeString);
            timeline.addKeyframe(keyframe, context.deserialize(keyframeValueElement, type));
        });
        return timeline;
    }
}


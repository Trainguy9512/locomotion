package com.trainguy9512.locomotion.resource.json;

import com.google.gson.*;
import com.trainguy9512.locomotion.animation.sequence.AnimationSequence;
import com.trainguy9512.locomotion.resource.FormatVersion;
import com.trainguy9512.locomotion.util.Interpolator;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Timeline;
import net.minecraft.util.GsonHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.Map;

public class AnimationSequenceDeserializer implements JsonDeserializer<AnimationSequence> {

    @Override
    public AnimationSequence deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject sequenceJsonObject = jsonElement.getAsJsonObject();

        FormatVersion version = FormatVersion.of(GsonHelper.getAsInt(sequenceJsonObject, "format_version", FormatVersion.ofDefault().version()));

        if (version.postScaleFormatUpdate()) {
            float sequenceLength = sequenceJsonObject.get("length").getAsFloat();
            AnimationSequence.Builder sequenceBuilder = AnimationSequence.builder(TimeSpan.ofSeconds(sequenceLength));

            Map<String, JsonElement> joints = sequenceJsonObject.getAsJsonObject("joints").asMap();
            joints.forEach((joint, jointElement) -> {
                JsonObject jointJSON = jointElement.getAsJsonObject();

                Timeline<Vector3f> translationTimeline = deserializeTimeline(
                        context,
                        jointJSON,
                        "translation",
                        Vector3f.class,
                        Interpolator.VECTOR_FLOAT,
                        sequenceLength
                );
                Timeline<Quaternionf> rotationTimeline = deserializeTimeline(
                        context,
                        jointJSON,
                        "rotation",
                        Quaternionf.class,
                        Interpolator.QUATERNION,
                        sequenceLength
                );
                Timeline<Vector3f> scaleTimeline = deserializeTimeline(
                        context,
                        jointJSON,
                        "scale",
                        Vector3f.class,
                        Interpolator.VECTOR_FLOAT,
                        sequenceLength
                );
                Timeline<Boolean> visibilityTimeline = deserializeTimeline(
                        context,
                        jointJSON,
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
            if(sequenceJsonObject.has("time_markers")){
                Map<String, JsonElement> timeMarkers = sequenceJsonObject.getAsJsonObject("time_markers").asMap();
                timeMarkers.forEach((timeMarkerIdentifier, timeMarkerElement) -> {
                    JsonArray times = timeMarkerElement.getAsJsonArray();
                    times.forEach(time -> sequenceBuilder.putTimeMarker(timeMarkerIdentifier, TimeSpan.ofSeconds(time.getAsFloat())));
                });
            }


            return sequenceBuilder.build();
        } else {
            return null;
        }
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


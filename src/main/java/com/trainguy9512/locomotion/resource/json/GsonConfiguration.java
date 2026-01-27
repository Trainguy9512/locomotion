package com.trainguy9512.locomotion.resource.json;

import com.google.gson.*;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.sequence.AnimationSequence;
import com.trainguy9512.locomotion.resource.FormatVersion;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GsonConfiguration {

    private static Gson GSON = createInternal();

    private static Gson createInternal() {
        return new GsonBuilder()
                .setStrictness(Strictness.STRICT)
                .setPrettyPrinting()
                .registerTypeAdapter(Vector3f.class, vector3fDeserializer())
                .registerTypeAdapter(Quaternionf.class, quaternionDeserializer())
                .registerTypeAdapter(AnimationSequence.class, new AnimationSequenceDeserializer())
                .registerTypeAdapter(JointSkeleton.class, new JointSkeletonDeserializer())
                .registerTypeAdapter(FormatVersion.class, FormatVersion.getDeserializer())
                .registerTypeAdapter(JointChannel.class, new JointChannelDeserializer())
                .registerTypeAdapter(PartPose.class, new PartPoseDeserializer())
                .registerTypeAdapter(ModelPart.class, new ModelPartSerializer())
                .create();
    }

    public static Gson getInstance() {
        return GSON;
    }

    public static <D> D deserializeWithFallback(JsonDeserializationContext context, JsonObject json, String key, Class<D> type, D fallback) {
        if (!json.has(key)) {
            return fallback;
        }
        if (json.get(key).isJsonNull()) {
            return null;
        }
        return context.deserialize(json.get(key), type);
    }

    private static JsonDeserializer<Vector3f> vector3fDeserializer() {
        return (jsonElement, type, context) -> {
            JsonArray components = jsonElement.getAsJsonArray();
            return new Vector3f(
                    components.get(0).getAsFloat(),
                    components.get(1).getAsFloat(),
                    components.get(2).getAsFloat()
            );
        };
    }

    private static JsonDeserializer<Quaternionf> quaternionDeserializer() {
        return (jsonElement, type, context) -> {
            JsonArray components = jsonElement.getAsJsonArray();
            return new Quaternionf().rotationZYX(
                    components.get(2).getAsFloat() * Mth.DEG_TO_RAD,
                    components.get(1).getAsFloat() * Mth.DEG_TO_RAD,
                    components.get(0).getAsFloat() * Mth.DEG_TO_RAD
            );
        };
    }
}

package com.trainguy9512.locomotion.resource.json;

import com.google.gson.*;
import com.trainguy9512.locomotion.animation.sequence.AnimationSequence;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class GsonConfiguration {

    private static Gson GSON = createInternal();

    private static Gson createInternal() {
        return new GsonBuilder()
                .setStrictness(Strictness.STRICT)
                .registerTypeAdapter(Vector3f.class, vector3fDeserializer())
                .registerTypeAdapter(Quaternionf.class, quaternionDeserializer())
                .registerTypeAdapter(AnimationSequence.class, new AnimationSequenceDeserializer())
                .create();
    }

    public static Gson getInstance() {
        return GSON;
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

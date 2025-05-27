package com.trainguy9512.locomotion.resource.json;

import com.google.gson.*;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.PartPose;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class PartPoseDeserializer implements JsonDeserializer<PartPose> {

    private static final String TRANSLATION_KEY = "translation";
    private static final String ROTATION_KEY = "rotation";
    private static final String SCALE_KEY = "scale";

    private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0, 0, 0);
    private static final Vector3f DEFAULT_ROTATION = new Vector3f(0, 0, 0);
    private static final Vector3f DEFAULT_SCALE = new Vector3f(1, 1, 1);

    @Override
    public PartPose deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject partPoseJson = json.getAsJsonObject();
        Vector3f translation = GsonConfiguration.deserializeWithFallback(
                context,
                partPoseJson,
                TRANSLATION_KEY,
                Vector3f.class,
                DEFAULT_TRANSLATION
        );
        Vector3f rotation = GsonConfiguration.deserializeWithFallback(
                context,
                partPoseJson,
                ROTATION_KEY,
                Vector3f.class,
                DEFAULT_ROTATION
        );
        Vector3f scale = GsonConfiguration.deserializeWithFallback(
                context,
                partPoseJson,
                SCALE_KEY,
                Vector3f.class,
                DEFAULT_SCALE
        );
        return new PartPose(
                translation.x(),
                translation.y(),
                translation.z(),
                rotation.x(),
                rotation.y(),
                rotation.z(),
                scale.x(),
                scale.y(),
                scale.z()
        );
    }
}

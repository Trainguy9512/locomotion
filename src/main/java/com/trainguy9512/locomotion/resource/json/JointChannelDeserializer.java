package com.trainguy9512.locomotion.resource.json;

import com.google.gson.*;
import com.mojang.math.Axis;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import net.minecraft.util.GsonHelper;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.lang.reflect.Type;

public class JointChannelDeserializer implements JsonDeserializer<JointChannel> {

    private static final String TRANSLATION_KEY = "translation";
    private static final String ROTATION_KEY = "rotation";
    private static final String SCALE_KEY = "scale";
    private static final String VISIBILITY_KEY = "visibility";

    private static final Vector3f DEFAULT_TRANSLATION = new Vector3f(0, 0, 0);
    private static final Quaternionf DEFAULT_ROTATION = Axis.XP.rotation(0);
    private static final Vector3f DEFAULT_SCALE = new Vector3f(1, 1, 1);
    private static final boolean DEFAULT_VISIBILITY = true;

    @Override
    public JointChannel deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jointChannelJson = json.getAsJsonObject();
        return JointChannel.ofTranslationRotationScaleQuaternion(
                GsonConfiguration.deserializeWithFallback(
                        context,
                        jointChannelJson,
                        TRANSLATION_KEY,
                        DEFAULT_TRANSLATION
                ),
                GsonConfiguration.deserializeWithFallback(
                        context,
                        jointChannelJson,
                        ROTATION_KEY,
                        DEFAULT_ROTATION
                ),
                GsonConfiguration.deserializeWithFallback(
                        context,
                        jointChannelJson,
                        SCALE_KEY,
                        DEFAULT_SCALE
                ),
                GsonConfiguration.deserializeWithFallback(
                        context,
                        jointChannelJson,
                        VISIBILITY_KEY,
                        DEFAULT_VISIBILITY
                )
        );
    }
}

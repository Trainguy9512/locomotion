package com.trainguy9512.locomotion.resource.json;

import com.google.gson.*;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.resource.FormatVersion;
import net.minecraft.client.model.geom.PartPose;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class JointSkeletonDeserializer implements JsonDeserializer<JointSkeleton> {

    private static final String ROOT_KEY = "root_joint";
    private static final String JOINTS_KEY = "joints";
    private static final String CUSTOM_ATTRIBUTE_KEY = "custom_attributes";
    private static final String CUSTOM_ATTRIBUTE_TYPE_KEY = "type";
    private static final String CUSTOM_ATTRIBUTE_DEFAULT_VALUE_KEY = "default_value";

    private static final List<String> REQUIRED_SKELETON_KEYS = List.of(
            ROOT_KEY,
            JOINTS_KEY
    );

    private static final String CHILDREN_KEY = "children";
    private static final String MIRROR_JOINT_KEY = "mirror_joint";
    private static final String MODEL_PART_IDENTIFIER_KEY = "model_part_identifier";
    private static final String REFERENCE_POSE_KEY = "reference_pose";
    private static final String MODEL_PART_OFFSET_KEY = "model_part_offset";

    private static final List<String> REQUIRED_JOINT_KEYS = List.of(
            CHILDREN_KEY,
            REFERENCE_POSE_KEY
    );

    @Override
    public JointSkeleton deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject skeletonJsonObject = jsonElement.getAsJsonObject();

        FormatVersion version = FormatVersion.ofAssetJsonObject(skeletonJsonObject);
        if (version.isIncompatible()) {
            throw new JsonParseException("Animation sequence version is out of date for deserializer.");
        }
        for (String key : REQUIRED_SKELETON_KEYS) {
            if (!skeletonJsonObject.has(key)) {
                throw new JsonParseException("Required key " + key + " not found in skeleton data.");
            }
        }
        JointSkeleton.Builder skeletonBuilder = JointSkeleton.of(skeletonJsonObject.get(ROOT_KEY).getAsString());
        Map<String, JsonElement> jointsJsonMap = skeletonJsonObject.get(JOINTS_KEY).getAsJsonObject().asMap();
        this.deserializeJointAndChildren(
                skeletonJsonObject.get(ROOT_KEY).getAsString(),
                null,
                jointsJsonMap,
                context,
                skeletonBuilder
        );
        if (skeletonJsonObject.has(CUSTOM_ATTRIBUTE_KEY)) {
            skeletonJsonObject.get(CUSTOM_ATTRIBUTE_KEY).getAsJsonObject().asMap().forEach((customAttributeName, customAttributeJson) -> {
                if (Objects.equals(customAttributeJson.getAsJsonObject().get(CUSTOM_ATTRIBUTE_TYPE_KEY).getAsString(), "float")) {
                    float customAttributeDefaultValue = customAttributeJson.getAsJsonObject().get(CUSTOM_ATTRIBUTE_DEFAULT_VALUE_KEY).getAsFloat();
                    skeletonBuilder.defineCustomAttribute(customAttributeName, customAttributeDefaultValue);
                }
            });
        }

        return skeletonBuilder.build();
    }

    private void deserializeJointAndChildren(
            String joint,
            @Nullable String parent,
            Map<String, JsonElement> jointsJsonMap,
            JsonDeserializationContext context,
            JointSkeleton.Builder skeletonBuilder
    ) {
        if (!jointsJsonMap.containsKey(joint)) {
            throw new JsonParseException("Joint \"" + joint + "\" being defined is not present in the skeleton.");
        }
        JointSkeleton.JointConfiguration.Builder jointConfigurationBuilder = JointSkeleton.JointConfiguration.builder();
        JsonObject jointJsonObject = jointsJsonMap.get(joint).getAsJsonObject();
        for (String key : REQUIRED_JOINT_KEYS) {
            if (!jointJsonObject.has(key)) {
                throw new JsonParseException("Required key " + key + " not found in joint data.");
            }
        }
        if (!jointsJsonMap.containsKey(parent) && parent != null) {
            throw new JsonParseException("Joint \"" + joint + "\" being defined has parent \"" + parent + "\" that is not present in the skeleton.");
        }
        for (JsonElement childJson : jointJsonObject.get(CHILDREN_KEY).getAsJsonArray()) {
            String child = childJson.getAsString();
            if (!jointsJsonMap.containsKey(child)) {
                throw new JsonParseException("Joint \"" + joint + "\" in skeleton has child \"" + joint + "\" that is not present in the skeleton.");
            }
            jointConfigurationBuilder.addChild(child);
            this.deserializeJointAndChildren(
                    child,
                    joint,
                    jointsJsonMap,
                    context,
                    skeletonBuilder
            );
        }
        jointConfigurationBuilder.setParent(parent);
        jointConfigurationBuilder.setReferencePose(GsonConfiguration.deserializeWithFallback(
                context,
                jointJsonObject,
                REFERENCE_POSE_KEY,
                JointChannel.class,
                JointChannel.ZERO
        ));
        jointConfigurationBuilder.setMirrorJoint(GsonConfiguration.deserializeWithFallback(
                context,
                jointJsonObject,
                MIRROR_JOINT_KEY,
                String.class,
                null
        ));
        jointConfigurationBuilder.setModelPartIdentifier(GsonConfiguration.deserializeWithFallback(
                context,
                jointJsonObject,
                MODEL_PART_IDENTIFIER_KEY,
                String.class,
                null
        ));
        jointConfigurationBuilder.setModelPartOffset(GsonConfiguration.deserializeWithFallback(
                context,
                jointJsonObject,
                MODEL_PART_OFFSET_KEY,
                PartPose.class,
                PartPose.ZERO
        ));
        skeletonBuilder.defineJoint(joint, jointConfigurationBuilder.build());
    }
}

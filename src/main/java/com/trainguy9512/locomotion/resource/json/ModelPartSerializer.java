package com.trainguy9512.locomotion.resource.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import net.minecraft.client.model.geom.ModelPart;

import java.lang.reflect.Type;

public class ModelPartSerializer implements JsonSerializer<ModelPart> {
    @Override
    @SuppressWarnings("unchecked")
    public JsonElement serialize(ModelPart modelPart, Type type, JsonSerializationContext jsonSerializationContext) {
        return ((JsonSerializer<ModelPart>)(Object)modelPart).serialize(modelPart, type, jsonSerializationContext);
    }

    public interface IndividualPartFunction {
        public JsonElement serializePartWithChildren(String name);
    }
}

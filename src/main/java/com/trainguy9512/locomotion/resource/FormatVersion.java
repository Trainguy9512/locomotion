package com.trainguy9512.locomotion.resource;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record FormatVersion(int version) {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/Resources/FormatVersion");
    private static final String FORMAT_VERSION_KEY = "format_version";

    public static FormatVersion of(int version) {
        return new FormatVersion(version);
    }

    public static FormatVersion ofDefault() {
        return FormatVersion.of(1);
    }

    public boolean isCompatible() {
        return this.version >= 4;
    }

    public static JsonDeserializer<FormatVersion> getDeserializer() {
        return (jsonElement, type, context) -> FormatVersion.of(jsonElement.getAsInt());
    }

    public static FormatVersion ofAssetJsonObject(JsonObject assetJson) {
        if (assetJson.has(FORMAT_VERSION_KEY)) {
            return FormatVersion.of(assetJson.get(FORMAT_VERSION_KEY).getAsInt());
        } else {
            throw new JsonParseException("Asset does not contain valid format version field in JSON data.");
        }
    }
}

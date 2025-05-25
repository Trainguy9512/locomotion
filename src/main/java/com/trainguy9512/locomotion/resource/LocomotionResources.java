package com.trainguy9512.locomotion.resource;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.sequence.AnimationSequence;
import com.trainguy9512.locomotion.resource.json.GsonConfiguration;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

public class LocomotionResources {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/Resources");

    public static final ResourceLocation RELOADER_IDENTIFIER = ResourceLocation.fromNamespaceAndPath(LocomotionMain.MOD_ID, "locomotion_asset_loader");
    private static final String ANIMATION_SEQUENCE_PATH = "sequences";
    private static final String SKELETON_PATH = "skeletons";
    private static final Map<ResourceLocation, AnimationSequence> ANIMATION_SEQUENCES;
    private static final Map<ResourceLocation, JointSkeleton> SKELETONS;

    static {
        ANIMATION_SEQUENCES = Maps.newHashMap();
        SKELETONS = Maps.newHashMap();
    }

    public static Map<ResourceLocation, AnimationSequence> getAnimationSequences() {
        return ANIMATION_SEQUENCES;
    }

    public static Map<ResourceLocation, JointSkeleton> getSkeletons() {
        return SKELETONS;
    }

    public static AnimationSequence getOrThrowAnimationSequence(ResourceLocation sequenceLocation) {
        if(ANIMATION_SEQUENCES.containsKey(sequenceLocation)){
            return ANIMATION_SEQUENCES.get(sequenceLocation);
        } else {
            throw new IllegalArgumentException("Tried to access animation sequence from resource location " + sequenceLocation + ", but it was not found in the loaded data.");
        }
    }

    public static CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {
        CompletableFuture<Map<ResourceLocation, AnimationSequence>> loadedAnimationSequences = loadAnimationSequences(manager, backgroundExecutor);

        return CompletableFuture.allOf(loadedAnimationSequences)
                .thenCompose(barrier::wait)
                .thenCompose(voided -> CompletableFuture.runAsync(() -> {
                    ANIMATION_SEQUENCES.clear();
                    ANIMATION_SEQUENCES.putAll(loadedAnimationSequences.join());
                }));
    }

    private static CompletableFuture<Map<ResourceLocation, AnimationSequence>> loadAnimationSequences(ResourceManager manager, Executor backgroundExecutor) {
        return CompletableFuture.supplyAsync(() -> {
            Predicate<ResourceLocation> isAssetJson = resourceLocation -> resourceLocation.getPath().endsWith(".json");
            Map<ResourceLocation, Resource> foundResources = manager.listResources(ANIMATION_SEQUENCE_PATH, isAssetJson);

            Map<ResourceLocation, AnimationSequence> deserializedSequences = Maps.newHashMap();
            foundResources.forEach((resourceLocation, resource) -> {
                try {
                    BufferedReader reader = resource.openAsReader();
                    JsonElement jsonElement = GsonHelper.fromJson(GsonConfiguration.getInstance(), reader, JsonElement.class);
                    AnimationSequence animationSequence = GsonConfiguration.getInstance().fromJson(jsonElement, AnimationSequence.class);
                    if (animationSequence != null) {
                        deserializedSequences.put(resourceLocation, animationSequence);
                        LOGGER.info("Successfully loaded animation sequence {}", resourceLocation);
                    } else {
                        LOGGER.warn("Failed to load sequence {}", resourceLocation);
                    }
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return deserializedSequences;
        }, backgroundExecutor);
    }

    private static CompletableFuture<Map<ResourceLocation, JsonElement>> loadJson(ResourceManager resourceManager, Executor executor, String path) {
        Gson gson = new Gson();

        Map<ResourceLocation, Resource> passedFiles = resourceManager.listResources("sequences", (string) -> string.toString().endsWith(".json"));

        return CompletableFuture.supplyAsync(() -> {
            Map<ResourceLocation, JsonElement> jsonData = Maps.newHashMap();
            passedFiles.forEach((resourceLocation, resource) -> {
                try {
                    BufferedReader reader = resource.openAsReader();
                    JsonElement jsonElement = GsonHelper.fromJson(gson, reader, JsonElement.class);
                    jsonData.put(resourceLocation, jsonElement);
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            return jsonData;
        }, executor);
    }
}

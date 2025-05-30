package com.trainguy9512.locomotion.resource;

import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
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
import java.lang.reflect.Type;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class LocomotionResources {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/Resources");

    public static final ResourceLocation RELOADER_IDENTIFIER = ResourceLocation.fromNamespaceAndPath(LocomotionMain.MOD_ID, "locomotion_asset_loader");
    private static final String JOINT_SKELETON_PATH = "skeletons";
    private static final String ANIMATION_SEQUENCE_PATH = "sequences";
    private static final Map<ResourceLocation, AnimationSequence> ANIMATION_SEQUENCES;
    private static final Map<ResourceLocation, JointSkeleton> JOINT_SKELETONS;

    static {
        JOINT_SKELETONS = Maps.newHashMap();
        ANIMATION_SEQUENCES = Maps.newHashMap();
    }

    public static Map<ResourceLocation, JointSkeleton> getJointSkeletons() {
        return JOINT_SKELETONS;
    }

    public static Map<ResourceLocation, AnimationSequence> getAnimationSequences() {
        return ANIMATION_SEQUENCES;
    }

    public static JointSkeleton getOrThrowJointSkeleton(ResourceLocation jointSkeletonLocation) {
        if (JOINT_SKELETONS.containsKey(jointSkeletonLocation)) {
            return JOINT_SKELETONS.get(jointSkeletonLocation);
        } else {
            throw new IllegalArgumentException("Tried to access joint skeleton from resource location " + jointSkeletonLocation + ", but it was not found in the loaded data: " + JOINT_SKELETONS.keySet());
        }
    }

    public static AnimationSequence getOrThrowAnimationSequence(ResourceLocation sequenceLocation) {
        if (ANIMATION_SEQUENCES.containsKey(sequenceLocation)) {
            return ANIMATION_SEQUENCES.get(sequenceLocation);
        } else {
            throw new IllegalArgumentException("Tried to access animation sequence from resource location " + sequenceLocation + ", but it was not found in the loaded data.");
        }
    }

    public static CompletableFuture<Void> reload(PreparableReloadListener.PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {
        CompletableFuture<Map<ResourceLocation, JointSkeleton>> loadedJointSkeletons = loadJointSkeletons(manager, backgroundExecutor);
        CompletableFuture<Map<ResourceLocation, AnimationSequence>> loadedAnimationSequences = loadAnimationSequences(manager, backgroundExecutor);

        return CompletableFuture.allOf(loadedJointSkeletons, loadedAnimationSequences)
                .thenCompose(barrier::wait)
                .thenCompose(voided -> CompletableFuture.runAsync(() -> {
                    JOINT_SKELETONS.clear();
                    JOINT_SKELETONS.putAll(loadedJointSkeletons.join());
                    ANIMATION_SEQUENCES.clear();
                    ANIMATION_SEQUENCES.putAll(loadedAnimationSequences.join());
                    ANIMATION_SEQUENCES.replaceAll((resourceLocation, animationSequence) -> animationSequence.getBaked());
                    LOGGER.info("Cleared and replaced Locomotion resource data.");
                }));
    }

    private static CompletableFuture<Map<ResourceLocation, AnimationSequence>> loadAnimationSequences(ResourceManager manager, Executor backgroundExecutor) {
        return loadJsonResources(
                manager,
                backgroundExecutor,
                AnimationSequence.class,
                ANIMATION_SEQUENCE_PATH,
                resourceLocation -> LOGGER.info("Successfully loaded animation sequence {}", resourceLocation)
        );
    }

    private static CompletableFuture<Map<ResourceLocation, JointSkeleton>> loadJointSkeletons(ResourceManager manager, Executor backgroundExecutor) {
        return loadJsonResources(
                manager,
                backgroundExecutor,
                JointSkeleton.class,
                JOINT_SKELETON_PATH,
                resourceLocation -> LOGGER.info("Successfully loaded joint skeleton {}", resourceLocation)
        );
    }

    private static <D> CompletableFuture<Map<ResourceLocation, D>> loadJsonResources(ResourceManager manager, Executor backgroundExecutor, Class<D> type, String pathToListFrom, Consumer<ResourceLocation> onSuccessfullyLoaded) {
        return CompletableFuture.supplyAsync(() -> {
            Predicate<ResourceLocation> isAssetJson = resourceLocation -> resourceLocation.getPath().endsWith(".json");
            Map<ResourceLocation, Resource> foundResources = manager.listResources(pathToListFrom, isAssetJson);

            Map<ResourceLocation, D> deserializedResources = Maps.newHashMap();
            foundResources.forEach((resourceLocation, resource) -> {
                try {
                    try (BufferedReader reader = resource.openAsReader()) {
                        JsonElement jsonElement = GsonHelper.fromJson(GsonConfiguration.getInstance(), reader, JsonElement.class);
                        D deserializedAsset = GsonConfiguration.getInstance().fromJson(jsonElement, type);
                        deserializedResources.put(resourceLocation, deserializedAsset);
                        onSuccessfullyLoaded.accept(resourceLocation);
                    } catch (JsonParseException exception) {
                        LOGGER.warn("Skipping loading of JSON asset {} of type {} due to a JSON parsing error:", resourceLocation, type.getSimpleName());
                        LOGGER.warn("--- {}", exception.getMessage());
                    }
                } catch (IOException exception) {
                    LOGGER.error("Encountered error while reading asset {} of type {}:", resourceLocation, type.getSimpleName());
                    LOGGER.error("--- {}", exception.getMessage());
                    throw new RuntimeException(exception);
                }
            });
            return deserializedResources;
        }, backgroundExecutor);
    }
}

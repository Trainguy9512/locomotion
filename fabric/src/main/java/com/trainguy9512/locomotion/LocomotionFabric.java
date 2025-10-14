package com.trainguy9512.locomotion;

import com.trainguy9512.locomotion.resource.LocomotionResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LocomotionFabric implements ClientModInitializer {
    
    private void registerResourceReloader() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(LocomotionResources.RELOADER_IDENTIFIER, new PreparableReloadListener() {
            @Override
            public @NotNull CompletableFuture<Void> reload(@NotNull SharedState sharedState, @NotNull Executor exectutor, @NotNull PreparationBarrier barrier, @NotNull Executor applyExectutor) {
                return LocomotionResources.reload(barrier, sharedState.resourceManager(), exectutor, applyExectutor);
            }
        });
    }

    @Override
    public void onInitializeClient() {
        LocomotionMain.initialize();
        this.registerResourceReloader();
    }
}
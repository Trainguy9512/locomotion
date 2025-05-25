package com.trainguy9512.locomotion;

import com.trainguy9512.locomotion.resource.LocomotionResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LocomotionFabric implements ClientModInitializer {
    
    private void registerResourceReloader() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            @Override
            public @NotNull CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, Executor backgroundExecutor, Executor gameExecutor) {
                return LocomotionResources.reload(barrier, manager, backgroundExecutor, gameExecutor);
            }

            @Override
            public ResourceLocation getFabricId() {
                return LocomotionResources.RELOADER_IDENTIFIER;
            }
        });
    }

    @Override
    public void onInitializeClient() {
        LocomotionMain.initialize();
        this.registerResourceReloader();
    }
}
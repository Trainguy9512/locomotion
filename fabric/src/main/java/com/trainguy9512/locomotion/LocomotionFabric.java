package com.trainguy9512.locomotion;

import com.trainguy9512.locomotion.resource.LocomotionResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.server.packs.PackType;

public class LocomotionFabric implements ClientModInitializer {
    
    private void registerResourceReloader() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(LocomotionResources.RELOADER_IDENTIFIER, new LocomotionResources());
    }

    @Override
    public void onInitializeClient() {
        LocomotionMain.initialize();
        this.registerResourceReloader();
    }
}
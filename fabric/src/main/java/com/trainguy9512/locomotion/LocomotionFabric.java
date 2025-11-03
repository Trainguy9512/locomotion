package com.trainguy9512.locomotion;

import com.trainguy9512.locomotion.debug.DebugEntryFirstPersonDrivers;
import com.trainguy9512.locomotion.debug.LocomotionDebugScreenEntries;
import com.trainguy9512.locomotion.resource.LocomotionResources;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;

public class LocomotionFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LocomotionMain.initialize();
        registerResourceReloader();
        registerDebugEntries();
    }

    private static void registerResourceReloader() {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(LocomotionResources.RELOADER_IDENTIFIER, new LocomotionResources());
    }

    private static void registerDebugEntries() {
        LocomotionDebugScreenEntries.register(DebugScreenEntries::register);
    }
}
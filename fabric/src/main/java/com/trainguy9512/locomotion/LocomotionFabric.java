package com.trainguy9512.locomotion;

import com.trainguy9512.locomotion.debug.DebugEntryFirstPersonDrivers;
import com.trainguy9512.locomotion.debug.LocomotionDebugScreenEntries;
import com.trainguy9512.locomotion.resource.LocomotionResources;
import net.fabricmc.api.ClientModInitializer;
//? if >= 1.21.0 {
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
//?} else {
/*import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;*/
//?}
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class LocomotionFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        LocomotionMain.initialize();
        registerResourceReloader();
        registerDebugEntries();
    }

    private static void registerResourceReloader() {
        //? if >= 1.21.0 {
        ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloader(LocomotionResources.RELOADER_IDENTIFIER, new LocomotionResources());
        //?} else {
        /*ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifiableResourceReloadListener() {
            private final LocomotionResources delegate = new LocomotionResources();

            @Override
            public Identifier getFabricId() {
                return LocomotionResources.RELOADER_IDENTIFIER;
            }

            @Override
            public CompletableFuture<Void> reload(PreparationBarrier barrier, ResourceManager manager, ProfilerFiller prepProfiler, ProfilerFiller applyProfiler, Executor backgroundExecutor, Executor gameExecutor) {
                return delegate.reload(barrier, manager, prepProfiler, applyProfiler, backgroundExecutor, gameExecutor);
            }
        });*/
        //?}
    }

    private static void registerDebugEntries() {
        //? if >= 1.21.0 {
        LocomotionDebugScreenEntries.register(DebugScreenEntries::register);
        //?} else {
        /*// Debug screen entries are not supported in this version.*/
        //?}
    }
}

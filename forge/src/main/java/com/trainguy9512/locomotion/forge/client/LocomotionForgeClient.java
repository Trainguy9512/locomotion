package com.trainguy9512.locomotion.forge.client;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.resource.LocomotionResources;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = LocomotionMain.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class LocomotionForgeClient {

    @SubscribeEvent
    public static void registerReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new LocomotionResources());
    }
}

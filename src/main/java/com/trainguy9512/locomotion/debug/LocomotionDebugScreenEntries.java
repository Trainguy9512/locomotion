package com.trainguy9512.locomotion.debug;

import com.trainguy9512.locomotion.LocomotionMain;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.ResourceLocation;

import java.util.function.BiConsumer;

public class LocomotionDebugScreenEntries {

    public static void register(BiConsumer<ResourceLocation, DebugScreenEntry> registrar){
        registrar.accept(ResourceLocation.fromNamespaceAndPath(LocomotionMain.MOD_ID, "first_person_drivers"), new DebugEntryFirstPersonDrivers());
    }

}

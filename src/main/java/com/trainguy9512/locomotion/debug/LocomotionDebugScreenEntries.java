package com.trainguy9512.locomotion.debug;

import com.trainguy9512.locomotion.LocomotionMain;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;

import java.util.function.BiConsumer;

public class LocomotionDebugScreenEntries {

    public static void register(BiConsumer<Identifier, DebugScreenEntry> registrar){
        registrar.accept(Identifier.fromNamespaceAndPath(LocomotionMain.MOD_ID, "first_person_drivers"), new DebugEntryFirstPersonDrivers());
        registrar.accept(Identifier.fromNamespaceAndPath(LocomotionMain.MOD_ID, "currently_evaluating_block_entity_animators"), new DebugEntryBlockEntityAnimators());
    }

}

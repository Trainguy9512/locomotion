package com.trainguy9512.locomotion.debug;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DebugEntryFirstPersonDrivers implements DebugScreenEntry {
    private static final ResourceLocation GROUP = ResourceLocation.fromNamespaceAndPath(LocomotionMain.MOD_ID, "locomotion");

    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        displayer.addToGroup(GROUP, "First Person Drivers");
        JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(dataContainer -> {
            List<Map.Entry<DriverKey<?>, Driver<?>>> sortedDrivers = dataContainer.getAllDrivers().entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().getIdentifier())).toList();

            for (Map.Entry<DriverKey<?>, Driver<?>> entry : sortedDrivers) {
                String driverName = entry.getKey().getIdentifier();
                String driverValue = entry.getValue().getChatFormattedString();

                String lineString = driverName + ":    " + driverValue;
                displayer.addToGroup(GROUP, lineString);
            }
        });
    }
}

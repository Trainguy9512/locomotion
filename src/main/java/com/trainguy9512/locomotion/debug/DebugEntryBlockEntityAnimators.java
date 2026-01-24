package com.trainguy9512.locomotion.debug;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import net.minecraft.client.gui.components.debug.DebugEntryCategory;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class DebugEntryBlockEntityAnimators implements DebugScreenEntry {
    private static final Identifier GROUP = Identifier.fromNamespaceAndPath(LocomotionMain.MOD_ID, "locomotion");

    @Override
    public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
        displayer.addToGroup(GROUP, "Block Entity Joint Animators currently evaluating:");
        JointAnimatorDispatcher.getInstance().getCurrentlyEvaluatingBlockEntityJointAnimators().forEach((blockPos, identifier) -> {
            displayer.addToGroup(GROUP, blockPos.toShortString() + ": " + identifier.toString());
        });
    }
}

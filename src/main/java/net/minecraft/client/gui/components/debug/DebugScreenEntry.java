//? if < 1.21.0 {
/*package net.minecraft.client.gui.components.debug;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.Nullable;

// Shim for <1.21.0 debug screen entry API.
public interface DebugScreenEntry {

    default void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk clientChunk, @Nullable LevelChunk serverChunk) {
    }
}
*///?}

package com.trainguy9512.locomotion.forge;

import com.trainguy9512.locomotion.LocomotionMain;
import net.minecraftforge.fml.common.Mod;

@Mod(LocomotionMain.MOD_ID)
public class LocomotionForge {
    public LocomotionForge() {
        LocomotionMain.initialize();
    }
}

package com.trainguy9512.locomotion.util;

import net.minecraft.world.item.ItemUseAnimation;

public class MultiVersionUtils {

    public static ItemUseAnimation getTridentUseAnimation() {
        //? if >= 1.21.11 {
        return ItemUseAnimation.TRIDENT;
        //?} else {
        /*return ItemUseAnimation.SPEAR;
        *///?}
    }

    public static ItemUseAnimation getSpearUseAnimation() {
        //? if >= 1.21.11 {
        return ItemUseAnimation.SPEAR;
        //?} else {
        /*throw new RuntimeException("1.21.11 feature attempted to be used in older version");
         *///?}
    }

}

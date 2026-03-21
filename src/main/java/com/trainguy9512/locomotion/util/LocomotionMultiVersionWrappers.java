package com.trainguy9512.locomotion.util;

//? if >= 1.21.0 {
import net.minecraft.world.item.ItemUseAnimation;
//?} else {
import net.minecraft.world.item.UseAnim;
//?}

public class LocomotionMultiVersionWrappers {

    //? if >= 1.21.0 {
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
    //?} else {
    /*public static UseAnim getTridentUseAnimation() {
        return UseAnim.SPEAR;
    }

    public static UseAnim getSpearUseAnimation() {
        throw new RuntimeException("1.21.11 feature attempted to be used in older version");
    }
    *///?}

}

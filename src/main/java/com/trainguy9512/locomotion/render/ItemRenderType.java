package com.trainguy9512.locomotion.render;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;

public enum ItemRenderType {
    THIRD_PERSON_ITEM,
    MIRRORED_THIRD_PERSON_ITEM,
    BLOCK_STATE,
    MAP,
    FIXED;

    public boolean isMirrored() {
        return this == MIRRORED_THIRD_PERSON_ITEM;
    }

    public ItemDisplayContext getItemDisplayContext(HumanoidArm side) {
        if (this == FIXED) {
            return ItemDisplayContext.ON_SHELF;
        }
        return side == HumanoidArm.RIGHT ? ItemDisplayContext.THIRD_PERSON_RIGHT_HAND : ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
    }
}

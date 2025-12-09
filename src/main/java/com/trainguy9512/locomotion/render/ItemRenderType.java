package com.trainguy9512.locomotion.render;

public enum ItemRenderType {
    THIRD_PERSON_ITEM,
    MIRRORED_THIRD_PERSON_ITEM,
    BLOCK_STATE,
    MAP;

    public boolean isMirrored() {
        return this == MIRRORED_THIRD_PERSON_ITEM;
    }
}

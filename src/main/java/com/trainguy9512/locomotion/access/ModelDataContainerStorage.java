package com.trainguy9512.locomotion.access;

import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;

public interface ModelDataContainerStorage {

    void locomotion$setDataContainer(AnimationDataContainer container);

    AnimationDataContainer locomotion$getDataContainer();
}

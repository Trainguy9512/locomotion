package com.trainguy9512.locomotion.animation.animator;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.MatrixModelPart;
import com.trainguy9512.locomotion.animation.animator.entity.EntityJointAnimator;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.pose.Pose;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.pose.ComponentSpacePose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;

import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Function;

public class JointAnimatorDispatcher {
    private static final JointAnimatorDispatcher INSTANCE = new JointAnimatorDispatcher();

    private final WeakHashMap<UUID, AnimationDataContainer> entityAnimationDataContainerStorage;

    private AnimationDataContainer firstPersonPlayerDataContainer;
    private ComponentSpacePose interpolatedFirstPersonPlayerPose;

    public JointAnimatorDispatcher() {
        this.entityAnimationDataContainerStorage = new WeakHashMap<>();
    }

    public static JointAnimatorDispatcher getInstance() {
        return INSTANCE;
    }

    /**
     * Re-initializes all created data containers
     */
    public void reInitializeData() {
        this.firstPersonPlayerDataContainer = null;
        this.entityAnimationDataContainerStorage.clear();
    }

    public <T extends Entity> void tickEntityJointAnimators(Iterable<T> entitiesForRendering) {
        entitiesForRendering.forEach(entity ->
                JointAnimatorRegistry.getThirdPersonJointAnimator(entity).ifPresent(
                        jointAnimator -> this.getEntityAnimationDataContainer(entity).ifPresent(
                                dataContainer -> this.tickJointAnimator(jointAnimator, entity, dataContainer)
                        )
                )
        );
    }

    public void tickFirstPersonPlayerJointAnimator(){
        if (LocomotionMain.CONFIG.data().firstPersonPlayer.enableRenderer) {
            JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(
                    jointAnimator -> this.getFirstPersonPlayerDataContainer().ifPresent(
                            dataContainer -> this.tickJointAnimator(jointAnimator, Minecraft.getInstance().player, dataContainer)
                    )
            );
        }
    }

    /**
     * Updates the data container every tick and if the joint animator is set to calculate once per tick, samples the animation pose and loads it into the data container.
     * @param jointAnimator         Joint animator
     * @param dataReference         Animation data reference
     * @param dataContainer         Animation data container
     */
    private <T> void tickJointAnimator(JointAnimator<T> jointAnimator, T dataReference, AnimationDataContainer dataContainer){
        dataContainer.preTick();
        jointAnimator.extractAnimationData(dataReference, dataContainer, dataContainer.getMontageManager());
        dataContainer.tick();
        if(jointAnimator.getPoseCalulationFrequency() == JointAnimator.PoseCalculationFrequency.CALCULATE_ONCE_PER_TICK){
            dataContainer.getDriver(dataContainer.getPerTickCalculatedPoseDriverKey()).setValue(dataContainer.computePose(1));
        }
        dataContainer.postTick();
    }

    public <T extends Entity> Optional<AnimationDataContainer> getEntityAnimationDataContainer(T entity){
        UUID uuid = entity.getUUID();
        if(!this.entityAnimationDataContainerStorage.containsKey(uuid)){
            JointAnimatorRegistry.getThirdPersonJointAnimator(entity).ifPresent(jointAnimator ->
                    this.entityAnimationDataContainerStorage.put(uuid, this.createDataContainer(jointAnimator))
            );
        }
        return Optional.ofNullable(this.entityAnimationDataContainerStorage.get(uuid));
    }

    public Optional<AnimationDataContainer> getFirstPersonPlayerDataContainer(){
        if(this.firstPersonPlayerDataContainer == null){
            JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(jointAnimator ->
                this.firstPersonPlayerDataContainer = this.createDataContainer(jointAnimator)
            );
        }
        return Optional.ofNullable(this.firstPersonPlayerDataContainer);
    }

    public Optional<ComponentSpacePose> getInterpolatedFirstPersonPlayerPose(){
        return Optional.ofNullable(this.interpolatedFirstPersonPlayerPose);
    }

    public void calculateInterpolatedFirstPersonPlayerPose(JointAnimator<?> jointAnimator, AnimationDataContainer dataContainer, float partialTicks){
        this.interpolatedFirstPersonPlayerPose = this.getInterpolatedAnimationPose(jointAnimator, dataContainer, partialTicks);
    }

    private AnimationDataContainer createDataContainer(JointAnimator<?> jointAnimator){
        return AnimationDataContainer.of(jointAnimator);
    }

    public ComponentSpacePose getInterpolatedAnimationPose(JointAnimator<?> jointAnimator, AnimationDataContainer dataContainer, float partialTicks){
        return switch (jointAnimator.getPoseCalulationFrequency()) {
            case CALCULATE_EVERY_FRAME -> dataContainer.computePose(partialTicks).convertedToComponentSpace();
            case CALCULATE_ONCE_PER_TICK -> dataContainer.getInterpolatedDriverValue(dataContainer.getPerTickCalculatedPoseDriverKey(), partialTicks).convertedToComponentSpace();
        };
    }

    public <S extends EntityRenderState> void setupAnimWithAnimationPose(EntityModel<S> entityModel, S entityRenderState, Pose pose, EntityJointAnimator<?, S> entityJointAnimator){
        entityModel.resetPose();
        JointSkeleton jointSkeleton = pose.getJointSkeleton();
        //? if <= 1.21.5 {
        /*jointSkeleton.getJoints()
                .forEach(joint -> {
                    if (jointSkeleton.getJointConfiguration(joint).modelPartIdentifier() != null) {
                        entityModel.getAnyDescendantWithName(jointSkeleton.getJointConfiguration(joint).modelPartIdentifier()).ifPresent(
                                modelPart -> ((MatrixModelPart)(Object) modelPart).locomotion$setMatrix(pose.getJointChannel(joint).getTransform())
                        );
                    }
                });
        *///?} else {
        Function<String, ModelPart> partLookup = entityModel.root().createPartLookup();
        jointSkeleton.getJoints().forEach(joint -> {
            String modelPartIdentifier = jointSkeleton.getJointConfiguration(joint).modelPartIdentifier();
            if (modelPartIdentifier != null) {
                ModelPart modelPart = partLookup.apply(modelPartIdentifier);
                if (modelPart != null) {
                    ((MatrixModelPart)(Object) modelPart).locomotion$setMatrix(pose.getJointChannel(joint).getTransform());
                }
            }
        });
        entityJointAnimator.postProcessModelParts(entityModel, entityRenderState);
        //?}
    }
}

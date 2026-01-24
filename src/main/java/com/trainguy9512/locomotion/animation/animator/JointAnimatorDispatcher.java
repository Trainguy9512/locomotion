package com.trainguy9512.locomotion.animation.animator;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.MatrixModelPart;
import com.trainguy9512.locomotion.animation.animator.block_entity.BlockEntityJointAnimator;
import com.trainguy9512.locomotion.animation.animator.entity.EntityJointAnimator;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.pose.Pose;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.pose.ComponentSpacePose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.BlockPos;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.swing.text.html.Option;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.WeakHashMap;
import java.util.function.Function;

public class JointAnimatorDispatcher {
    private static final JointAnimatorDispatcher INSTANCE = new JointAnimatorDispatcher();

    private final WeakHashMap<UUID, AnimationDataContainer> entityAnimationDataContainerStorage;
    private final HashMap<Long, AnimationDataContainer> blockEntityAnimationDataContainerStorage;
    private AnimationDataContainer firstPersonPlayerDataContainer;
    private ComponentSpacePose interpolatedFirstPersonPlayerPose;

    public JointAnimatorDispatcher() {
        this.entityAnimationDataContainerStorage = new WeakHashMap<>();
        this.blockEntityAnimationDataContainerStorage = Maps.newHashMap();
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
        this.blockEntityAnimationDataContainerStorage.clear();
    }

    public <T extends Entity, B extends BlockEntity> void tick(Iterable<T> entitiesForRendering) {

        this.tickEntityJointAnimators(entitiesForRendering);
        this.tickFirstPersonPlayerJointAnimator();
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

    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> void tickBlockEntityJointAnimator(Level level, BlockPos blockPos, BlockState blockState, T blockEntity) {
        Optional<BlockEntityJointAnimator<T>> potentialJointAnimator;
        potentialJointAnimator = JointAnimatorRegistry.getBlockEntityJointAnimator((BlockEntityType<T>) blockEntity.getType());
        if (potentialJointAnimator.isPresent()) {
            BlockEntityJointAnimator<T> jointAnimator = potentialJointAnimator.get();
            Optional<AnimationDataContainer> potentialDataContainer = this.getBlockEntityAnimationDataContainer(blockPos, blockEntity.getType());
            if (potentialDataContainer.isEmpty()) {
                return;
            }
            AnimationDataContainer dataContainer = potentialDataContainer.get();
            this.tickJointAnimator(jointAnimator, blockEntity, dataContainer);
        }
        // TODO: Flush data containers that are out of range.
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

    public <T extends BlockEntity> Optional<AnimationDataContainer> getBlockEntityAnimationDataContainer(BlockPos blockPos, BlockEntityType<T> type){
        long packedBlockPos = blockPos.asLong();
        if(!this.blockEntityAnimationDataContainerStorage.containsKey(packedBlockPos)){
            JointAnimatorRegistry.getBlockEntityJointAnimator(type).ifPresent(jointAnimator ->
                    this.blockEntityAnimationDataContainerStorage.put(packedBlockPos, this.createDataContainer(jointAnimator))
            );
        }
        return Optional.ofNullable(this.blockEntityAnimationDataContainerStorage.get(packedBlockPos));
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

    public <S> void setupAnimWithAnimationPose(Model<S> model, Pose pose){
        model.resetPose();
        JointSkeleton jointSkeleton = pose.getJointSkeleton();

        Function<String, ModelPart> partLookup = model.root().createPartLookup();
        jointSkeleton.getJoints().forEach(joint -> {
            String modelPartIdentifier = jointSkeleton.getJointConfiguration(joint).modelPartIdentifier();
            if (modelPartIdentifier != null) {
                ModelPart modelPart = partLookup.apply(modelPartIdentifier);
                if (modelPart != null) {
                    ((MatrixModelPart)(Object) modelPart).locomotion$setMatrix(pose.getJointChannel(joint).getTransform());
                }
            }
        });
    }
}

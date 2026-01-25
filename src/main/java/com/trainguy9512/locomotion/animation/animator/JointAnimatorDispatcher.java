package com.trainguy9512.locomotion.animation.animator;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.MatrixModelPart;
import com.trainguy9512.locomotion.animation.animator.block_entity.BlockEntityJointAnimator;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.pose.ModelPartSpacePose;
import com.trainguy9512.locomotion.animation.pose.Pose;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.pose.ComponentSpacePose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.function.Function;

public class JointAnimatorDispatcher {
    private static final JointAnimatorDispatcher INSTANCE = new JointAnimatorDispatcher();

    private final WeakHashMap<UUID, AnimationDataContainer> entityAnimationDataContainerStorage;
    private final HashMap<Long, AnimationDataContainer> blockEntityAnimationDataContainerStorage;
    private AnimationDataContainer firstPersonPlayerDataContainer;
    private ModelPartSpacePose interpolatedFirstPersonPlayerPose;

    private static DriverKey<VariableDriver<Identifier>> BLOCK_ENTITY_TYPE_DRIVER = DriverKey.of("block_entity_type", () -> VariableDriver.ofConstant(() -> Identifier.withDefaultNamespace("none")));
    private static DriverKey<VariableDriver<Identifier>> ENTITY_TYPE_DRIVER = DriverKey.of("entity_type", () -> VariableDriver.ofConstant(() -> Identifier.withDefaultNamespace("none")));

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

//        this.tickEntityJointAnimators(entitiesForRendering);
        this.tickFirstPersonPlayerJointAnimator();
    }

//    public <T extends Entity> void tickEntityJointAnimators(Iterable<T> entitiesForRendering) {
//        entitiesForRendering.forEach(entity ->
//                JointAnimatorRegistry.getThirdPersonJointAnimator(entity).ifPresent(
//                        jointAnimator -> this.getEntityAnimationDataContainer(entity).ifPresent(
//                                dataContainer -> this.tickJointAnimator(jointAnimator, entity, dataContainer)
//                        )
//                )
//        );
//    }

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

//    public <T extends Entity> Optional<AnimationDataContainer> getEntityAnimationDataContainer(T entity){
//        UUID uuid = entity.getUUID();
//        if(!this.entityAnimationDataContainerStorage.containsKey(uuid)){
//            JointAnimatorRegistry.getThirdPersonJointAnimator(entity).ifPresent(jointAnimator ->
//                    this.entityAnimationDataContainerStorage.put(uuid, this.createDataContainer(jointAnimator))
//            );
//        }
//        return Optional.ofNullable(this.entityAnimationDataContainerStorage.get(uuid));
//    }





    private static <T extends BlockEntity> Optional<AnimationDataContainer> tryConstructBlockEntityDataContainer(BlockEntityType<T> type) {
        Optional<BlockEntityJointAnimator<T>> potentialJointAnimator = JointAnimatorRegistry.getBlockEntityJointAnimator(type);
        if (potentialJointAnimator.isPresent()) {
            BlockEntityJointAnimator<T> jointAnimator = potentialJointAnimator.get();
            AnimationDataContainer dataContainer = AnimationDataContainer.of(jointAnimator);
            dataContainer.getDriver(BLOCK_ENTITY_TYPE_DRIVER).setValue(BlockEntityType.getKey(type));
            return Optional.of(dataContainer);
        }
        return Optional.empty();
    }

    public Map<BlockPos, Identifier> getCurrentlyEvaluatingBlockEntityJointAnimators() {
        Map<BlockPos, Identifier> map = Maps.newHashMap();
        this.blockEntityAnimationDataContainerStorage.forEach((packedBlockPos, dataContainer) -> {
            map.put(BlockPos.of(packedBlockPos), dataContainer.getDriverValue(BLOCK_ENTITY_TYPE_DRIVER));
        });
        return map;
    }

    private static boolean positionIsWithinCameraRadius(BlockPos blockPos, float radius) {
        BlockPos cameraBlockPos = Objects.requireNonNull(Minecraft.getInstance().getCameraEntity()).blockPosition();
        return cameraBlockPos.distChessboard(blockPos) < radius;
    }

    public <T extends BlockEntity> Optional<AnimationDataContainer> getBlockEntityAnimationDataContainer(BlockPos blockPos, BlockEntityType<T> type){
        long packedBlockPos = blockPos.asLong();

        if (positionIsWithinCameraRadius(blockPos, 16)) {
            // If this block position is within range, try and find a data container for it.

            if (this.blockEntityAnimationDataContainerStorage.containsKey(packedBlockPos)) {

                AnimationDataContainer dataContainer = this.blockEntityAnimationDataContainerStorage.get(packedBlockPos);
                if (dataContainer.getDriverValue(BLOCK_ENTITY_TYPE_DRIVER) == BlockEntityType.getKey(type)) {
                    // If the block is within range and its type matches the requested type, return it.
                    return Optional.of(dataContainer);
                } else {
                    // Flush the current data container for the block if its type does not match anymore. Will continue to create a new data container
                    this.blockEntityAnimationDataContainerStorage.remove(packedBlockPos);
                }

            }
            if (!this.blockEntityAnimationDataContainerStorage.containsKey(packedBlockPos)) {

                Optional<AnimationDataContainer> potentialDataContainer = tryConstructBlockEntityDataContainer(type);
                if (potentialDataContainer.isPresent()) {
                    // If no data container is found for this block, one is successfully created from the registry, and it is within range, put it in storage.
                    AnimationDataContainer dataContainer = potentialDataContainer.get();
                    this.blockEntityAnimationDataContainerStorage.put(packedBlockPos, dataContainer);
                }
            }
            // If a data container is present in storage and it is within range, return it.
            return Optional.ofNullable(this.blockEntityAnimationDataContainerStorage.getOrDefault(packedBlockPos, null));

        } else {
            // If this block position is not in range, flush it from storage and return nothing.
            this.blockEntityAnimationDataContainerStorage.remove(packedBlockPos);
            return Optional.empty();
        }
    }





    public Optional<AnimationDataContainer> getFirstPersonPlayerDataContainer(){
        if(this.firstPersonPlayerDataContainer == null){
            JointAnimatorRegistry.getFirstPersonPlayerJointAnimator().ifPresent(jointAnimator ->
                this.firstPersonPlayerDataContainer = AnimationDataContainer.of(jointAnimator)
            );
        }
        return Optional.ofNullable(this.firstPersonPlayerDataContainer);
    }

    public Optional<ModelPartSpacePose> getInterpolatedFirstPersonPlayerPose(){
        return Optional.ofNullable(this.interpolatedFirstPersonPlayerPose);
    }

    public void calculateInterpolatedFirstPersonPlayerPose(AnimationDataContainer dataContainer, float partialTicks){
        this.interpolatedFirstPersonPlayerPose = dataContainer.getInterpolatedAnimationPose(partialTicks);
    }
}

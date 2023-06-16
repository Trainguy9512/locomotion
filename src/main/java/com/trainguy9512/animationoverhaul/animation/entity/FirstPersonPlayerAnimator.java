package com.trainguy9512.animationoverhaul.animation.entity;

import com.trainguy9512.animationoverhaul.animation.pose.AnimationPose;
import com.trainguy9512.animationoverhaul.animation.pose.BakedAnimationPose;
import com.trainguy9512.animationoverhaul.animation.pose.sample.*;
import com.trainguy9512.animationoverhaul.util.animation.LocatorSkeleton;
import com.trainguy9512.animationoverhaul.util.data.AnimationDataContainer;
import com.trainguy9512.animationoverhaul.util.data.TimelineGroupData;
import com.trainguy9512.animationoverhaul.util.time.TickTimeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class FirstPersonPlayerAnimator extends LivingEntityAnimator<LocalPlayer, PlayerModel<LocalPlayer>, FirstPersonPlayerAnimator.FPPlayerLocators>{

    public static FirstPersonPlayerAnimator INSTANCE = new FirstPersonPlayerAnimator();

    public AnimationDataContainer localAnimationDataContainer = new AnimationDataContainer();
    public BakedAnimationPose<FPPlayerLocators> localBakedPose;


    public enum FPPlayerLocators {
        root,
        camera,
        rightArm,
        leftArm,
        rightHand,
        leftHand;

        public static final FPPlayerLocators[] arms = new FPPlayerLocators[] {
                rightArm,
                leftArm,
                rightHand,
                leftHand
        };
    }


    public static final ResourceLocation ANIMATION_FP_RIGHT_EMPTY_RAISE = TimelineGroupData.getNativeResourceLocation("player/fp_right_empty_raise");
    public static final ResourceLocation ANIMATION_FP_RIGHT_EMPTY_LOWER = TimelineGroupData.getNativeResourceLocation("player/fp_right_empty_lower");
    public static final ResourceLocation ANIMATION_FP_RIGHT_EMPTY_IDLE = TimelineGroupData.getNativeResourceLocation("player/fp_right_empty_idle");
    public static final ResourceLocation ANIMATION_FP_RIGHT_EMPTY_PUNCH = TimelineGroupData.getNativeResourceLocation("player/fp_right_empty_punch");
    public static final ResourceLocation ANIMATION_FP_RIGHT_EMPTY_USE_ITEM = TimelineGroupData.getNativeResourceLocation("player/fp_right_empty_use");
    public static final ResourceLocation ANIMATION_FP_RIGHT_EMPTY_WALK = TimelineGroupData.getNativeResourceLocation("player/fp_right_empty_walk");
    public static final ResourceLocation ANIMATION_FP_RIGHT_EMPTY_JUMP = TimelineGroupData.getNativeResourceLocation("player/fp_right_empty_jump");
    public static final ResourceLocation ANIMATION_FP_RIGHT_EMPTY_MINE = TimelineGroupData.getNativeResourceLocation("player/fp_right_empty_mine");
    public static final ResourceLocation ANIMATION_FP_RIGHT_EMPTY_FALLING = TimelineGroupData.getNativeResourceLocation("player/fp_right_empty_falling");
    public static final ResourceLocation ANIMATION_FP_RIGHT_BASICITEM_POSE = TimelineGroupData.getNativeResourceLocation("player/fp_right_basicitem_pose");


    public static final AnimationDataContainer.DataKey<ItemStack> MAIN_HAND_ITEM = new AnimationDataContainer.DataKey<>("main_hand_item_stack", ItemStack.EMPTY);
    public static final AnimationDataContainer.DataKey<Boolean> IS_ATTACKING = new AnimationDataContainer.DataKey<>("is_attacking", false);
    public static final AnimationDataContainer.DataKey<Boolean> IS_USING_ITEM = new AnimationDataContainer.DataKey<>("is_using_item", false);
    public static final AnimationDataContainer.DataKey<Boolean> IS_MINING = new AnimationDataContainer.DataKey<>("is_mining", false);
    public static final AnimationDataContainer.DataKey<Float> WALK_WEIGHT = new AnimationDataContainer.DataKey<>("walk_weight", 0F);
    public static final AnimationDataContainer.DataKey<Boolean> IS_FALLING = new AnimationDataContainer.DataKey<>("is_falling", false);
    public static final AnimationDataContainer.DataKey<Boolean> IS_JUMPING = new AnimationDataContainer.DataKey<>("is_jumping", false);


    private static final String ITEM_SWITCH_NOTIFY = "item_switch_notify";
    private static final AnimationSequencePlayer MAIN_EMPTY_RAISE_SEQUENCE_PLAYER = AnimationSequencePlayer.of("main_empty_raise_sequence_player", ANIMATION_FP_RIGHT_EMPTY_RAISE)
            .setLooping(false)
            .setDefaultPlayRate(1.5F);
    private static final AnimationSequencePlayer MAIN_EMPTY_LOWER_SEQUENCE_PLAYER = AnimationSequencePlayer.of("main_empty_lower_sequence_player", ANIMATION_FP_RIGHT_EMPTY_LOWER)
            .addAnimNotify(ITEM_SWITCH_NOTIFY, 7)
            .setLooping(false)
            .setDefaultPlayRate(1.6F);
    private static final AnimationSequencePlayer MAIN_EMPTY_IDLE_SEQUENCE_PLAYER = AnimationSequencePlayer.of("main_empty_idle_sequence_player", ANIMATION_FP_RIGHT_EMPTY_IDLE)
            .setDefaultPlayRate(0.8F);
    private static final AnimationSequencePlayer MAIN_EMPTY_JUMP_SEQUENCE_PLAYER = AnimationSequencePlayer.of("main_empty_jump_sequence_player", ANIMATION_FP_RIGHT_EMPTY_JUMP)
            .setDefaultPlayRate(1F)
            .setLooping(false)
            .setStartTime(TickTimeUtils.ticksFromMayaFrames(1))
            .setEndTime(TickTimeUtils.ticksFromMayaFrames(15));
    private static final AnimationSequencePlayer MAIN_EMPTY_LAND_SEQUENCE_PLAYER = AnimationSequencePlayer.of("main_empty_land_sequence_player", ANIMATION_FP_RIGHT_EMPTY_JUMP)
            .setDefaultPlayRate(1F)
            .setLooping(false)
            .setStartTime(TickTimeUtils.ticksFromMayaFrames(20))
            .setEndTime(TickTimeUtils.ticksFromMayaFrames(30));
    private static final AnimationSequencePlayer MAIN_EMPTY_FALLING_SEQUENCE_PLAYER = AnimationSequencePlayer.of("main_empty_falling_sequence_player", ANIMATION_FP_RIGHT_EMPTY_FALLING)
            .setDefaultPlayRate(1F);
    private static final AnimationSequencePlayer MAIN_EMPTY_MINING_BEGIN_SEQUENCE_PLAYER = AnimationSequencePlayer.of("main_empty_mining_begin_sequence_player", ANIMATION_FP_RIGHT_EMPTY_MINE)
            .setDefaultPlayRate(1F)
            .setLooping(false)
            .setStartTime(0)
            .setEndTime(TickTimeUtils.ticksFromMayaFrames(6));
    private static final AnimationSequencePlayer MAIN_EMPTY_MINING_LOOP_SEQUENCE_PLAYER = AnimationSequencePlayer.of("main_empty_mining_loop_sequence_player", ANIMATION_FP_RIGHT_EMPTY_MINE)
            .setDefaultPlayRate(1F)
            .setLooping(true)
            .setStartTime(TickTimeUtils.ticksFromMayaFrames(7))
            .setEndTime(TickTimeUtils.ticksFromMayaFrames(12));


    enum ItemSwitchStates {
        EMPTY,
        EMPTY_RAISING,
        BASIC_ITEM,
        BASIC_ITEM_RAISING,
        LOWERING
    }
    private static final AnimationStateMachine<ItemSwitchStates> MAINHAND_ITEMSWITCH_STATE_MACHINE = AnimationStateMachine.of("main_hand_state_machine", ItemSwitchStates.values())
            .addStateTransition(ItemSwitchStates.EMPTY, ItemSwitchStates.LOWERING, TickTimeUtils.ticksFromMayaFrames(1))
            .addStateTransition(ItemSwitchStates.LOWERING, ItemSwitchStates.EMPTY_RAISING, TickTimeUtils.ticksFromMayaFrames(1), 2)
            .addStateTransition(ItemSwitchStates.EMPTY_RAISING, ItemSwitchStates.EMPTY, TickTimeUtils.ticksFromMayaFrames(3))
            .addStateTransition(ItemSwitchStates.BASIC_ITEM, ItemSwitchStates.LOWERING, TickTimeUtils.ticksFromMayaFrames(3))
            .addStateTransition(ItemSwitchStates.LOWERING, ItemSwitchStates.BASIC_ITEM_RAISING, TickTimeUtils.ticksFromMayaFrames(1), 1)
            .addStateTransition(ItemSwitchStates.BASIC_ITEM_RAISING, ItemSwitchStates.BASIC_ITEM, TickTimeUtils.ticksFromMayaFrames(3));



    enum JumpingStates {
        JUMPING,
        FALLING,
        LANDING,
        ON_GROUND
    }
    private static final AnimationStateMachine<JumpingStates> JUMP_STATE_MACHINE = AnimationStateMachine.of("jump_state_machine", JumpingStates.values())
            .addStateTransition(JumpingStates.ON_GROUND, JumpingStates.JUMPING, TickTimeUtils.ticksFromMayaFrames(1), 1)
            .addStateTransition(JumpingStates.ON_GROUND, JumpingStates.FALLING, TickTimeUtils.ticksFromMayaFrames(3), 2)
            .addStateTransition(JumpingStates.JUMPING, JumpingStates.FALLING, TickTimeUtils.ticksFromMayaFrames(4), 1)
            .addStateTransition(JumpingStates.JUMPING, JumpingStates.LANDING, TickTimeUtils.ticksFromMayaFrames(1), 2)
            .addStateTransition(JumpingStates.FALLING, JumpingStates.LANDING, TickTimeUtils.ticksFromMayaFrames(1), 1)
            .addStateTransition(JumpingStates.LANDING, JumpingStates.JUMPING, TickTimeUtils.ticksFromMayaFrames(2), 2)
            .addStateTransition(JumpingStates.LANDING, JumpingStates.FALLING, TickTimeUtils.ticksFromMayaFrames(3), 3)
            .addStateTransition(JumpingStates.LANDING, JumpingStates.ON_GROUND, TickTimeUtils.ticksFromMayaFrames(4), 1);


    enum MiningStates {
        IDLE,
        BEGIN,
        LOOPING
    }
    private static final AnimationStateMachine<MiningStates> MINING_STATE_MACHINE = AnimationStateMachine.of("mining_state_machine", MiningStates.values())
            .addStateTransition(MiningStates.IDLE, MiningStates.BEGIN, TickTimeUtils.ticksFromMayaFrames(3))
            .addStateTransition(MiningStates.BEGIN, MiningStates.LOOPING, TickTimeUtils.ticksFromMayaFrames(1))
            .addStateTransition(MiningStates.BEGIN, MiningStates.IDLE, TickTimeUtils.ticksFromMayaFrames(3))
            .addStateTransition(MiningStates.LOOPING, MiningStates.IDLE, TickTimeUtils.ticksFromMayaFrames(3));



    private static final AnimationMontageTrack MAIN_HAND_EMPTY_PUNCH_MONTAGE_TRACK = AnimationMontageTrack.of("main_hand_empty_punch_montage_track");
    private static final AnimationMontage MAIN_HAND_EMPTY_PUNCH_MONTAGE = AnimationMontage.of(ANIMATION_FP_RIGHT_EMPTY_PUNCH)
            .setLength(TickTimeUtils.ticksFromMayaFrames(10F))
            .setBlendInDuration(1)
            .setBlendOutDuration(TickTimeUtils.ticksFromMayaFrames(8F));
    private static final AnimationMontage MAIN_HAND_EMPTY_USE_ITEM_MONTAGE = AnimationMontage.of(ANIMATION_FP_RIGHT_EMPTY_USE_ITEM)
            .setLength(TickTimeUtils.ticksFromMayaFrames(9F))
            .setBlendInDuration(1)
            .setBlendOutDuration(TickTimeUtils.ticksFromMayaFrames(5F));


    private static final AnimationBlendSpacePlayer MAIN_HAND_EMPTY_WALK_BLENDSPACE_PLAYER = AnimationBlendSpacePlayer.of("main_hand_empty_walk_blendspace_player")
            .addEntry(0, ANIMATION_FP_RIGHT_EMPTY_WALK, 0F)
            .addEntry(2, ANIMATION_FP_RIGHT_EMPTY_WALK, 2.9F);


    public FirstPersonPlayerAnimator(){
        super();
    }

    @Override
    protected LocatorSkeleton<FPPlayerLocators> buildRig() {
        return LocatorSkeleton.of(FPPlayerLocators.values())
                .setLocatorMirror(FPPlayerLocators.rightArm, FPPlayerLocators.leftArm)
                .setLocatorMirror(FPPlayerLocators.leftArm, FPPlayerLocators.rightArm)
                .setLocatorMirror(FPPlayerLocators.rightHand, FPPlayerLocators.leftHand)
                .setLocatorMirror(FPPlayerLocators.leftHand, FPPlayerLocators.rightHand);
    }

    @Override
    protected AnimationPose<FPPlayerLocators> calculatePose() {
        // Update main hand item based on the anim notify
        if(getAnimationState(MAIN_EMPTY_LOWER_SEQUENCE_PLAYER).isAnimNotityActive(ITEM_SWITCH_NOTIFY)){
            setEntityAnimationVariable(MAIN_HAND_ITEM, this.livingEntity.getMainHandItem().copy());
        }

        // Set the poses for the main hand item switch state machine
        getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE)
                .setPose(ItemSwitchStates.EMPTY, getMainEmptyLocomotionPose(getStaticMainEmptyPose(), true, false))
                .setPose(ItemSwitchStates.EMPTY_RAISING, getMainHandRaisePose(getStaticMainEmptyPose()))
                .setPose(ItemSwitchStates.BASIC_ITEM, getMainEmptyLocomotionPose(getStaticMainBasicItemPose(), true, true))
                .setPose(ItemSwitchStates.BASIC_ITEM_RAISING, getMainHandRaisePose(getStaticMainBasicItemPose()))
                .setPose(ItemSwitchStates.LOWERING, getMainHandLowerPose(getStaticMainEmptyPose()));

        AnimationPose<FPPlayerLocators> pose = sampleAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE);
        pose = pose.getSelectedByLocators(getCameraPose(), List.of(FPPlayerLocators.camera));

        return pose;
    }

    // Gets the camera pose
    private AnimationPose<FPPlayerLocators> getCameraPose(){
        return getMainEmptyLocomotionPose(this.getStaticMainEmptyPose(), true, false);
    }

    private AnimationPose<FPPlayerLocators> getMainEmptyLocomotionPose(AnimationPose<FPPlayerLocators> basePose, boolean applyPunch, boolean applyAdditive){
        AnimationPose<FPPlayerLocators> pose = sampleAnimationState(MAIN_EMPTY_IDLE_SEQUENCE_PLAYER);
        pose.blendLinear(sampleAnimationState(MAIN_HAND_EMPTY_WALK_BLENDSPACE_PLAYER), getEntityAnimationVariable(WALK_WEIGHT));

        getAnimationState(JUMP_STATE_MACHINE)
                .setPose(JumpingStates.ON_GROUND, pose)
                .setPose(JumpingStates.JUMPING, sampleAnimationState(MAIN_EMPTY_JUMP_SEQUENCE_PLAYER))
                .setPose(JumpingStates.FALLING,
                        AnimationPose.fromChannelTimeline(this.locatorSkeleton, ANIMATION_FP_RIGHT_EMPTY_JUMP, 0.5F)
                                .getBlendedLinear(
                                        sampleAnimationState(MAIN_EMPTY_FALLING_SEQUENCE_PLAYER),
                                        Mth.clampedMap(
                                                this.livingEntity.fallDistance,
                                                0,
                                                10,
                                                0.5F,
                                                1.2F
                                        )
                                ))
                .setPose(JumpingStates.LANDING, sampleAnimationState(MAIN_EMPTY_LAND_SEQUENCE_PLAYER));
        pose = sampleAnimationState(JUMP_STATE_MACHINE);


        if(applyPunch){
            AnimationPose<FPPlayerLocators> punchPose = sampleAnimationStateFromInputPose(MAIN_HAND_EMPTY_PUNCH_MONTAGE_TRACK, pose);

            AnimationPose<FPPlayerLocators> cameraAdditivePose = sampleAnimationStateFromInputPose(MAIN_HAND_EMPTY_PUNCH_MONTAGE_TRACK, AnimationPose.of(this.locatorSkeleton));
            pose = punchPose.getSelectedByLocators(pose.getAdded(cameraAdditivePose), List.of(FPPlayerLocators.camera));

            getAnimationState(MINING_STATE_MACHINE)
                    .setPose(MiningStates.IDLE, pose)
                    .setPose(MiningStates.BEGIN, sampleAnimationState(MAIN_EMPTY_MINING_BEGIN_SEQUENCE_PLAYER))
                    .setPose(MiningStates.LOOPING, sampleAnimationState(MAIN_EMPTY_MINING_LOOP_SEQUENCE_PLAYER));
            pose = sampleAnimationState(MINING_STATE_MACHINE);
        }

        //AnimationPose additivePose = basePose.getSubtracted(getStaticMainEmptyPose());
        //return pose.getAdded(additivePose);
        if(applyAdditive) {
            AnimationPose<FPPlayerLocators> additivePose = getStaticMainEmptyPose().getSubtracted(basePose);
            pose = pose.getAdded(additivePose);

            //AnimationPose<FPPlayerLocators> unAddedPose = pose.getSubtracted(getStaticMainEmptyPose());
            //pose = unAddedPose.getAdded(basePose);
        }
        return pose;

    }

    private AnimationPose<FPPlayerLocators> getMainHandRaisePose(AnimationPose<FPPlayerLocators> basePose){
        AnimationPose<FPPlayerLocators> pose = sampleAnimationState(MAIN_EMPTY_RAISE_SEQUENCE_PLAYER);
        return pose.getSubtracted(getStaticMainEmptyPose()).getAdded(basePose);
    }

    private AnimationPose<FPPlayerLocators> getMainHandLowerPose(AnimationPose<FPPlayerLocators> basePose){
        AnimationPose<FPPlayerLocators> pose = sampleAnimationState(MAIN_EMPTY_LOWER_SEQUENCE_PLAYER);
        return pose.getSubtracted(getStaticMainEmptyPose()).getAdded(basePose);
    }


    private AnimationPose<FPPlayerLocators> getStaticMainEmptyPose(){
        return AnimationPose.fromChannelTimeline(this.locatorSkeleton, ANIMATION_FP_RIGHT_EMPTY_PUNCH, 0);
    }

    private AnimationPose<FPPlayerLocators> getStaticMainBasicItemPose(){
        return AnimationPose.fromChannelTimeline(this.locatorSkeleton, ANIMATION_FP_RIGHT_BASICITEM_POSE, 0);
    }

    public static final AnimationDataContainer.DataKey<Float> TEST_VALUE = new AnimationDataContainer.DataKey<>("test_value", 0F);

    public void tick(LivingEntity livingEntity, AnimationDataContainer entityAnimationData){

        // Tick the main hand lower/empty sequence players based on active states
        getAnimationState(MAIN_EMPTY_LOWER_SEQUENCE_PLAYER).playFromStartOnStateActive(getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE),
                ItemSwitchStates.LOWERING);
        getAnimationState(MAIN_EMPTY_RAISE_SEQUENCE_PLAYER).playFromStartOnStateActive(getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE), List.of(
                ItemSwitchStates.EMPTY_RAISING,
                ItemSwitchStates.BASIC_ITEM_RAISING
        ));

        // Update the conditions for the maind hand empty item switching
        getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).setTransitionCondition(ItemSwitchStates.EMPTY, ItemSwitchStates.LOWERING, this.compareVariableItemStackWithEntityItemStack(MAIN_HAND_ITEM, this.livingEntity.getMainHandItem()));
        getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).setTransitionCondition(ItemSwitchStates.EMPTY_RAISING, ItemSwitchStates.EMPTY, getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).getTimeElapsed() > TickTimeUtils.ticksFromMayaFrames(4));
        getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).setTransitionCondition(ItemSwitchStates.LOWERING, ItemSwitchStates.EMPTY_RAISING, getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).getTimeElapsed() > TickTimeUtils.ticksFromMayaFrames(4) && getEntityAnimationVariable(MAIN_HAND_ITEM).isEmpty());
        getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).setTransitionCondition(ItemSwitchStates.BASIC_ITEM, ItemSwitchStates.LOWERING, this.compareVariableItemStackWithEntityItemStack(MAIN_HAND_ITEM, this.livingEntity.getMainHandItem()));
        getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).setTransitionCondition(ItemSwitchStates.BASIC_ITEM_RAISING, ItemSwitchStates.BASIC_ITEM, getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).getTimeElapsed() > TickTimeUtils.ticksFromMayaFrames(4));
        getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).setTransitionCondition(ItemSwitchStates.LOWERING, ItemSwitchStates.BASIC_ITEM_RAISING, getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).getTimeElapsed() > TickTimeUtils.ticksFromMayaFrames(4) && !getEntityAnimationVariable(MAIN_HAND_ITEM).isEmpty());
        // Debug
        //AnimationOverhaulMain.LOGGER.info("Current: {} New: {} Condition: {}", getEntityAnimationVariable(MAIN_HAND_ITEM).getItem(), this.livingEntity.getMainHandItem().getItem(), this.compareVariableItemStackWithEntityItemStack(MAIN_HAND_ITEM, this.livingEntity.getMainHandItem()));


        // Set variables for jumping and falling
        setEntityAnimationVariable(IS_JUMPING,
                this.livingEntity.input.jumping
                && !this.livingEntity.getAbilities().flying
                && !this.livingEntity.isPassenger()
        );
        setEntityAnimationVariable(IS_FALLING,
                !this.livingEntity.onGround()
                && !this.livingEntity.onClimbable()
        );
        getAnimationState(JUMP_STATE_MACHINE)
                .setTransitionCondition(JumpingStates.ON_GROUND, JumpingStates.JUMPING, getEntityAnimationVariable(IS_FALLING) && getEntityAnimationVariable(IS_JUMPING))
                .setTransitionCondition(JumpingStates.ON_GROUND, JumpingStates.FALLING, getEntityAnimationVariable(IS_FALLING))
                .setTransitionCondition(JumpingStates.JUMPING, JumpingStates.FALLING, getAnimationState(JUMP_STATE_MACHINE).getTimeElapsed() > TickTimeUtils.ticksFromMayaFrames(6) && getEntityAnimationVariable(IS_FALLING))
                .setTransitionCondition(JumpingStates.JUMPING, JumpingStates.LANDING, !getEntityAnimationVariable(IS_FALLING))
                .setTransitionCondition(JumpingStates.FALLING, JumpingStates.LANDING, !getEntityAnimationVariable(IS_FALLING))
                .setTransitionCondition(JumpingStates.LANDING, JumpingStates.JUMPING, getEntityAnimationVariable(IS_FALLING) && getEntityAnimationVariable(IS_JUMPING))
                .setTransitionCondition(JumpingStates.LANDING, JumpingStates.FALLING, getEntityAnimationVariable(IS_FALLING))
                .setTransitionCondition(JumpingStates.LANDING, JumpingStates.ON_GROUND, getAnimationState(JUMP_STATE_MACHINE).getTimeElapsed() > TickTimeUtils.ticksFromMayaFrames(3) && !getEntityAnimationVariable(IS_FALLING));
        getAnimationState(MAIN_EMPTY_JUMP_SEQUENCE_PLAYER).playFromStartOnStateActive(JUMP_STATE_MACHINE, JumpingStates.JUMPING);
        getAnimationState(MAIN_EMPTY_LAND_SEQUENCE_PLAYER).playFromStartOnStateActive(JUMP_STATE_MACHINE, JumpingStates.LANDING);
        getAnimationState(MAIN_EMPTY_FALLING_SEQUENCE_PLAYER).setPlayRate(Mth.clampedMap(this.livingEntity.fallDistance, 0, 20, 1, 2));



        // Set the conditions for the mining state machine, and reset the two animation sequences based on the state status
        getAnimationState(MINING_STATE_MACHINE)
                .setTransitionCondition(MiningStates.IDLE, MiningStates.BEGIN, getEntityAnimationVariable(IS_MINING) && !getAnimationState(MAIN_HAND_EMPTY_PUNCH_MONTAGE_TRACK).isActive())
                .setTransitionCondition(MiningStates.BEGIN, MiningStates.LOOPING, getAnimationState(MINING_STATE_MACHINE).getTimeElapsed() > TickTimeUtils.ticksFromMayaFrames(4))
                .setTransitionCondition(MiningStates.BEGIN, MiningStates.IDLE, !getEntityAnimationVariable(IS_MINING))
                .setTransitionCondition(MiningStates.LOOPING, MiningStates.IDLE, !getEntityAnimationVariable(IS_MINING));
        getAnimationState(MAIN_EMPTY_MINING_BEGIN_SEQUENCE_PLAYER).playFromStartOnStateActive(MINING_STATE_MACHINE, MiningStates.BEGIN);
        getAnimationState(MAIN_EMPTY_MINING_LOOP_SEQUENCE_PLAYER).playFromStartOnStateActive(MINING_STATE_MACHINE, MiningStates.LOOPING);
        getAnimationState(MAIN_EMPTY_MINING_LOOP_SEQUENCE_PLAYER).progressTimeIfStateActive(MINING_STATE_MACHINE, MiningStates.LOOPING);


        if(getEntityAnimationVariable(IS_ATTACKING) && !getEntityAnimationVariable(IS_MINING)){
            setEntityAnimationVariable(IS_ATTACKING, false);

            // Only play the attack animation if not mining
            if(getAnimationState(MINING_STATE_MACHINE).getActiveState() == MiningStates.IDLE){

                // Play the attack montage based on the main hand item switch state. Does not play for raising/lowering states.
                ItemSwitchStates state = getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).getActiveState();
                switch (state){
                    case EMPTY, BASIC_ITEM:
                        getAnimationState(MAIN_HAND_EMPTY_PUNCH_MONTAGE_TRACK).playMontage(MAIN_HAND_EMPTY_PUNCH_MONTAGE);
                }
            }
        }
        if(getEntityAnimationVariable(IS_USING_ITEM)){
            setEntityAnimationVariable(IS_USING_ITEM, false);
            ItemSwitchStates state = getAnimationState(MAINHAND_ITEMSWITCH_STATE_MACHINE).getActiveState();
            switch (state){
                case EMPTY, BASIC_ITEM:
                    getAnimationState(MAIN_HAND_EMPTY_PUNCH_MONTAGE_TRACK).playMontage(MAIN_HAND_EMPTY_USE_ITEM_MONTAGE);
            }
        }

        boolean isInputtingMovement =
                this.livingEntity.input.forwardImpulse != 0 ||
                this.livingEntity.input.leftImpulse != 0;
        //AnimationOverhaulMain.LOGGER.info("{}, {}", this.livingEntity.input.forwardImpulse, this.livingEntity.animationSpeed);
        getEntityAnimationData().incrementInFramesFromCondition(WALK_WEIGHT, isInputtingMovement, 2, 4);
        getAnimationState(MAIN_HAND_EMPTY_WALK_BLENDSPACE_PLAYER).setValue(this.getWalkAnimationSpeed());

    }

    public void tickExternal(){
        LocalPlayer player = Minecraft.getInstance().player;
        this.setEntity(player);

        this.tick(player, getEntityAnimationData());
        getEntityAnimationData().tickAnimationStates();

        if(this.localBakedPose == null){
            this.localBakedPose = new BakedAnimationPose();
            this.localBakedPose.setPose(AnimationPose.of(this.locatorSkeleton));
        }
        if(!this.localBakedPose.hasPose){
            this.localBakedPose.setPose(AnimationPose.of(this.locatorSkeleton));
            this.localBakedPose.hasPose = true;
        }
        this.localBakedPose.pushToOld();

        AnimationPose animationPose = this.calculatePose();
        if (animationPose == null){
            animationPose = AnimationPose.of(this.locatorSkeleton);
        }
        animationPose.applyDefaultPoseOffset();

        this.localBakedPose.setPose(animationPose.getCopy());
    }

    private boolean compareVariableItemStackWithEntityItemStack(AnimationDataContainer.DataKey<ItemStack> itemStackDataKey, ItemStack entityItemStack){
        ItemStack currentItemStack = getEntityAnimationVariable(itemStackDataKey);
        if(currentItemStack.getItem() != null && entityItemStack.getItem() == null || currentItemStack.getItem() == null && entityItemStack.getItem() != null) {
            return true;
        }
        return currentItemStack.getItem() != entityItemStack.getItem();
    }

    @Override
    protected AnimationDataContainer getEntityAnimationData() {
        return this.localAnimationDataContainer;
    }
}

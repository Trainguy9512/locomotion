package com.trainguy9512.locomotion.animation.animator.entity;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.*;
import com.trainguy9512.locomotion.animation.driver.*;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendMask;
import com.trainguy9512.locomotion.animation.joint.skeleton.BlendProfile;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageSlotFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.State;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class FirstPersonPlayerJointAnimator implements LivingEntityJointAnimator<LocalPlayer, PlayerRenderState> {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/FPJointAnimator");

    public static final String ROOT_JOINT = "root_jnt";
    public static final String CAMERA_JOINT = "camera_jnt";
    public static final String ARM_BUFFER_JOINT = "arm_buffer_jnt";
    public static final String RIGHT_ARM_BUFFER_JOINT = "arm_R_buffer_jnt";
    public static final String RIGHT_ARM_JOINT = "arm_R_jnt";
    public static final String RIGHT_HAND_JOINT = "hand_R_jnt";
    public static final String RIGHT_ITEM_JOINT = "item_R_jnt";
    public static final String LEFT_ARM_BUFFER_JOINT = "arm_L_buffer_jnt";
    public static final String LEFT_ARM_JOINT = "arm_L_jnt";
    public static final String LEFT_HAND_JOINT = "hand_L_jnt";
    public static final String LEFT_ITEM_JOINT = "item_L_jnt";


    public static final Set<String> RIGHT_SIDE_JOINTS = Set.of(
            RIGHT_ARM_BUFFER_JOINT,
            RIGHT_ARM_JOINT,
            RIGHT_HAND_JOINT,
            RIGHT_ITEM_JOINT
    );

    public static final Set<String> LEFT_SIDE_JOINTS = Set.of(
            LEFT_ARM_BUFFER_JOINT,
            LEFT_ARM_JOINT,
            LEFT_HAND_JOINT,
            LEFT_ITEM_JOINT
    );

    public static final BlendMask LEFT_SIDE_MASK = BlendMask.builder()
            .defineForMultipleJoints(LEFT_SIDE_JOINTS, 1)
            .build();

    @Override
    public void postProcessModelParts(EntityModel<PlayerRenderState> entityModel, PlayerRenderState entityRenderState) {
    }

    public JointSkeleton buildSkeleton() {
        return JointSkeleton.of(ROOT_JOINT)
                .addJointUnderRoot(CAMERA_JOINT)
                .addJointUnderParent(ARM_BUFFER_JOINT, CAMERA_JOINT)
                .addJointUnderParent(LEFT_ARM_BUFFER_JOINT, ARM_BUFFER_JOINT)
                .addJointUnderParent(RIGHT_ARM_BUFFER_JOINT, ARM_BUFFER_JOINT)
                .addJointUnderParent(LEFT_ARM_JOINT, LEFT_ARM_BUFFER_JOINT)
                .addJointUnderParent(RIGHT_ARM_JOINT, RIGHT_ARM_BUFFER_JOINT)
                .addJointUnderParent(LEFT_HAND_JOINT, LEFT_ARM_JOINT)
                .addJointUnderParent(RIGHT_HAND_JOINT, RIGHT_ARM_JOINT)
                .addJointUnderParent(LEFT_ITEM_JOINT, LEFT_HAND_JOINT)
                .addJointUnderParent(RIGHT_ITEM_JOINT, RIGHT_HAND_JOINT)
                .setMirrorJoint(RIGHT_ARM_BUFFER_JOINT, LEFT_ARM_BUFFER_JOINT)
                .setMirrorJoint(RIGHT_ARM_JOINT, LEFT_ARM_JOINT)
                .setMirrorJoint(RIGHT_HAND_JOINT, LEFT_HAND_JOINT)
                .setMirrorJoint(RIGHT_ITEM_JOINT, LEFT_ITEM_JOINT)
                .build();
    }

    @Override
    public PoseCalculationFrequency getPoseCalulationFrequency() {
        return PoseCalculationFrequency.CALCULATE_EVERY_FRAME;
    }

    public static final String ADDITIVE_GROUND_MOVEMENT_CACHE = "additive_ground_movement";

    @Override
    public PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer) {
        cachedPoseContainer.register(ADDITIVE_GROUND_MOVEMENT_CACHE, constructAdditiveGroundMovementPoseFunction(cachedPoseContainer), false);

        PoseFunction<LocalSpacePose> mainHandPose = this.constructHandPoseFunction(cachedPoseContainer, InteractionHand.MAIN_HAND);
        PoseFunction<LocalSpacePose> offHandPose = this.constructHandPoseFunction(cachedPoseContainer, InteractionHand.OFF_HAND);

        PoseFunction<LocalSpacePose> combinedHandPose = BlendPosesFunction.builder(mainHandPose)
                .addBlendInput(MirrorFunction.of(offHandPose), evaluationState -> 1f, LEFT_SIDE_MASK)
                .build();

        combinedHandPose = twoHandedOverridePoseFunction(combinedHandPose, cachedPoseContainer);

        PoseFunction<LocalSpacePose> handPoseWithAdditive = ApplyAdditiveFunction.of(combinedHandPose, cachedPoseContainer.getOrThrow(ADDITIVE_GROUND_MOVEMENT_CACHE));

        PoseFunction<LocalSpacePose> mirroredBasedOnHandednessPose = MirrorFunction.of(handPoseWithAdditive, context -> Minecraft.getInstance().options.mainHand().get() == HumanoidArm.LEFT);

        PoseFunction<LocalSpacePose> movementDirectionOffsetTransformer =
                JointTransformerFunction.localOrParentSpaceBuilder(mirroredBasedOnHandednessPose, ARM_BUFFER_JOINT)
                        .setTranslation(
                                context -> context.driverContainer().getDriverValue(MOVEMENT_DIRECTION_OFFSET, context.partialTicks()).mul(1.5f, new Vector3f()),
                                JointChannel.TransformType.ADD,
                                JointChannel.TransformSpace.COMPONENT
                        )
                        .setRotationEuler(
                                context -> context.driverContainer().getDriverValue(CAMERA_ROTATION_DAMPING, context.partialTicks()).mul(-0.15f, -0.15f, 0, new Vector3f()),
                                JointChannel.TransformType.ADD,
                                JointChannel.TransformSpace.COMPONENT
                        )
                        .setWeight(interpolationContext -> LocomotionMain.CONFIG.data().firstPersonPlayer.enableCameraRotationDamping ? 1f : 0f)
                        .build();

        return movementDirectionOffsetTransformer;
    }

    public enum TwoHandedOverrideStates {
        NORMAL,
        BOW_PULL_MAIN_HAND,
        BOW_PULL_OFF_HAND,
        BOW_RELEASE_MAIN_HAND,
        BOW_RELEASE_OFF_HAND
    }

    public static PoseFunction<LocalSpacePose> twoHandedOverridePoseFunction(PoseFunction<LocalSpacePose> normalPoseFunction, CachedPoseContainer cachedPoseContainer) {
        StateMachineFunction.Builder<TwoHandedOverrideStates> builder = StateMachineFunction.builder(evaluationState -> TwoHandedOverrideStates.NORMAL)
                .bindDriverToCurrentActiveState(CURRENT_TWO_HANDED_OVERRIDE_STATE)
                .defineState(State.builder(TwoHandedOverrideStates.NORMAL, normalPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build());
        defineBowStatesForHand(builder, InteractionHand.MAIN_HAND);
        defineBowStatesForHand(builder, InteractionHand.OFF_HAND);
        return builder.build();
    }

    public static void defineBowStatesForHand(StateMachineFunction.Builder<TwoHandedOverrideStates> stateMachineBuilder, InteractionHand interactionHand) {
        InteractionHand oppositeHand = interactionHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        TwoHandedOverrideStates bowPullState = switch (interactionHand) {
            case MAIN_HAND -> TwoHandedOverrideStates.BOW_PULL_MAIN_HAND;
            case OFF_HAND -> TwoHandedOverrideStates.BOW_PULL_OFF_HAND;
        };
        TwoHandedOverrideStates bowReleaseState = switch (interactionHand) {
            case MAIN_HAND -> TwoHandedOverrideStates.BOW_RELEASE_MAIN_HAND;
            case OFF_HAND -> TwoHandedOverrideStates.BOW_RELEASE_OFF_HAND;
        };
        PoseFunction<LocalSpacePose> pullPoseFunction = SequencePlayerFunction.builder(HAND_BOW_PULL)
                .bindToTimeMarker("arrow_placed_in_bow", evaluationState -> {
                    evaluationState.driverContainer().getDriver(getRenderedItemDriver(oppositeHand)).setValue(ItemStack.EMPTY);
                    evaluationState.driverContainer().getDriver(getHandPoseDriver(oppositeHand)).setValue(HandPose.EMPTY);
                    evaluationState.driverContainer().getDriver(getRenderItemAsStaticDriver(interactionHand)).setValue(false);
                    evaluationState.driverContainer().getDriver(getGenericItemPoseDriver(oppositeHand)).setValue(GenericItemPose.DEFAULT_2D_ITEM);
                })
                .bindToTimeMarker("get_new_arrow", evaluationState -> {
                })
                .build();
        PoseFunction<LocalSpacePose> releasePoseFunction = SequencePlayerFunction.builder(HAND_BOW_RELEASE)
                .build();

        if (interactionHand == InteractionHand.OFF_HAND) {
            pullPoseFunction = MirrorFunction.of(pullPoseFunction);
            releasePoseFunction = MirrorFunction.of(releasePoseFunction);
        }

        BlendProfile blendOffhandArrowMoreQuickly = BlendProfile.builder().defineForJoint(LEFT_HAND_JOINT, 0.2f).build();
        if (interactionHand == InteractionHand.OFF_HAND) {
            blendOffhandArrowMoreQuickly = blendOffhandArrowMoreQuickly.getMirrored();
        }

        stateMachineBuilder.defineState(State.builder(bowPullState, pullPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(bowReleaseState)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(getUsingItemDriver(interactionHand)).negate().and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .bindToOnTransitionTaken(evaluationState -> {
                                    evaluationState.driverContainer().getDriver(getRenderedItemDriver(oppositeHand)).setValue(ItemStack.EMPTY);
                                    evaluationState.driverContainer().getDriver(getHandPoseDriver(oppositeHand)).setValue(HandPose.EMPTY);
                                    evaluationState.driverContainer().getDriver(getRenderItemAsStaticDriver(interactionHand)).setValue(false);
                                    evaluationState.driverContainer().getDriver(getGenericItemPoseDriver(oppositeHand)).setValue(GenericItemPose.DEFAULT_2D_ITEM);
                                })
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(bowReleaseState, releasePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(TwoHandedOverrideStates.NORMAL)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        TwoHandedOverrideStates.NORMAL,
                                        bowReleaseState
                                ))
                        .addOutboundTransition(StateTransition.builder(bowPullState)
                                .isTakenIfTrue(
                                        StateTransition.booleanDriverPredicate(getUsingItemDriver(interactionHand))
                                                .and(transitionContext -> transitionContext.driverContainer().getDriverValue(getHandPoseDriver(interactionHand)) == HandPose.BOW)
                                                .and(transitionContext -> transitionContext.driverContainer().getDriverValue(getItemDriver(interactionHand)).is(Items.BOW))
                                )
                                .bindToOnTransitionTaken(evaluationState -> {
                                    ItemStack projectileStack = evaluationState.driverContainer().getDriverValue(PROJECTILE_ITEM);
                                    evaluationState.driverContainer().getDriver(getRenderedItemDriver(oppositeHand)).setValue(projectileStack);
                                    evaluationState.driverContainer().getDriver(getGenericItemPoseDriver(oppositeHand)).setValue(GenericItemPose.fromItemStack(projectileStack));
                                    evaluationState.driverContainer().getDriver(getHandPoseDriver(oppositeHand)).setValue(HandPose.GENERIC_ITEM);
                                    evaluationState.driverContainer().getDriver(getRenderItemAsStaticDriver(interactionHand)).setValue(true);
                                })
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(12))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .setBlendProfile(blendOffhandArrowMoreQuickly)
                                        .build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                        Set.of(
                                bowPullState,
                                bowReleaseState
                        ))
                        .addOutboundTransition(StateTransition.builder(TwoHandedOverrideStates.NORMAL)
                                .isTakenIfTrue(transitionContext -> !transitionContext.driverContainer().getDriverValue(getItemDriver(interactionHand)).is(Items.BOW))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build()
                );
    }

    public enum GenericItemPose {
        DEFAULT_2D_ITEM (HAND_GENERIC_ITEM_2D_ITEM_POSE, false),
        BLOCK (HAND_GENERIC_ITEM_BLOCK_POSE, true),
        SMALL_BLOCK (HAND_GENERIC_ITEM_SMALL_BLOCK_POSE, true),
        ROD (HAND_GENERIC_ITEM_ROD_POSE, false),
        DOOR_BLOCK (HAND_GENERIC_ITEM_DOOR_BLOCK_POSE, true),
        BANNER (HAND_GENERIC_ITEM_BANNER_POSE, false),
        ARROW (HAND_GENERIC_ITEM_ARROW_POSE, false);

        public final ResourceLocation basePoseLocation;
        public final boolean rendersBlockState;

        GenericItemPose(ResourceLocation basePoseLocation, boolean rendersBlockState) {
            this.basePoseLocation = basePoseLocation;
            this.rendersBlockState = rendersBlockState;
        }

        public static final List<Item> ROD_ITEMS = List.of(
                Items.BONE,
                Items.STICK,
                Items.BLAZE_ROD,
                Items.BREEZE_ROD,
                Items.POINTED_DRIPSTONE,
                Items.BAMBOO,
                Items.DEBUG_STICK,
                Items.END_ROD
        );

        public static final List<Item> SMALL_BLOCK_ITEMS = List.of(
                Items.HEAVY_CORE,
                Items.FLOWER_POT,
                Items.LANTERN,
                Items.SOUL_LANTERN,
                Items.LEVER
        );

        public static final List<TagKey<Item>> SMALL_BLOCK_ITEM_TAGS = List.of(
                ItemTags.SKULLS,
                ItemTags.BUTTONS
        );

        public static final List<Item> BLOCK_2D_OVERRIDE_ITEMS = List.of(
                Items.IRON_BARS,
                Items.CHAIN,
                Items.GLASS_PANE,
                Items.WHITE_STAINED_GLASS_PANE,
                Items.ORANGE_STAINED_GLASS_PANE,
                Items.MAGENTA_STAINED_GLASS_PANE,
                Items.LIGHT_BLUE_STAINED_GLASS_PANE,
                Items.YELLOW_STAINED_GLASS_PANE,
                Items.LIME_STAINED_GLASS_PANE,
                Items.PINK_STAINED_GLASS_PANE,
                Items.GRAY_STAINED_GLASS_PANE,
                Items.LIGHT_GRAY_STAINED_GLASS_PANE,
                Items.CYAN_STAINED_GLASS_PANE,
                Items.PURPLE_STAINED_GLASS_PANE,
                Items.BLUE_STAINED_GLASS_PANE,
                Items.BROWN_STAINED_GLASS_PANE,
                Items.GREEN_STAINED_GLASS_PANE,
                Items.RED_STAINED_GLASS_PANE,
                Items.BLACK_STAINED_GLASS_PANE,
                Items.POINTED_DRIPSTONE,
                Items.SMALL_AMETHYST_BUD,
                Items.MEDIUM_AMETHYST_BUD,
                Items.LARGE_AMETHYST_BUD,
                Items.AMETHYST_CLUSTER,
                Items.RED_MUSHROOM,
                Items.BROWN_MUSHROOM,
                Items.DEAD_BUSH,
                Items.SHORT_GRASS,
                Items.TALL_GRASS,
                Items.FERN,
                Items.LARGE_FERN,
                Items.CRIMSON_FUNGUS,
                Items.WARPED_FUNGUS,
                Items.BAMBOO,
                Items.SUGAR_CANE,
                Items.SMALL_DRIPLEAF,
                Items.BIG_DRIPLEAF,
                Items.CRIMSON_ROOTS,
                Items.WARPED_ROOTS,
                Items.NETHER_SPROUTS,
                Items.WEEPING_VINES,
                Items.TWISTING_VINES,
                Items.VINE,
                Items.GLOW_BERRIES,
                Items.COCOA_BEANS,
                Items.LILY_PAD,
                Items.BRAIN_CORAL,
                Items.BUBBLE_CORAL,
                Items.HORN_CORAL,
                Items.FIRE_CORAL,
                Items.TUBE_CORAL,
                Items.DEAD_BRAIN_CORAL,
                Items.DEAD_BUBBLE_CORAL,
                Items.DEAD_HORN_CORAL,
                Items.DEAD_FIRE_CORAL,
                Items.DEAD_TUBE_CORAL,
                Items.BRAIN_CORAL_FAN,
                Items.BUBBLE_CORAL_FAN,
                Items.HORN_CORAL_FAN,
                Items.FIRE_CORAL_FAN,
                Items.TUBE_CORAL_FAN,
                Items.DEAD_BRAIN_CORAL_FAN,
                Items.DEAD_BUBBLE_CORAL_FAN,
                Items.DEAD_HORN_CORAL_FAN,
                Items.DEAD_FIRE_CORAL_FAN,
                Items.DEAD_TUBE_CORAL_FAN,
                Items.COBWEB,
                Items.LILAC,
                Items.PEONY,
                Items.ROSE_BUSH,
                Items.SUNFLOWER,
                Items.MANGROVE_PROPAGULE,
                Items.PINK_PETALS,
                Items.PITCHER_PLANT,
                Items.MELON_SEEDS,
                Items.PUMPKIN_SEEDS,
                Items.GLOW_LICHEN,
                Items.SCULK_VEIN,
                Items.NETHER_WART,
                Items.SWEET_BERRIES,
                Items.SEAGRASS,
                Items.KELP,
                Items.TORCH,
                Items.SOUL_TORCH,
                Items.REDSTONE_TORCH,
                Items.BELL,
                Items.LADDER,
                Items.LIGHTNING_ROD,
                Items.DECORATED_POT,
                Items.REDSTONE,
                Items.STRING,
                Items.TRIPWIRE_HOOK,
                Items.RAIL,
                Items.ACTIVATOR_RAIL,
                Items.DETECTOR_RAIL,
                Items.POWERED_RAIL,
                Items.FROGSPAWN,
                //? >= 1.21.4 {
                Items.PALE_HANGING_MOSS,
                Items.RESIN_CLUMP,
                //}
                //? >= 1.21.5 {
                Items.DRY_SHORT_GRASS,
                Items.DRY_TALL_GRASS,
                Items.BUSH,
                Items.FIREFLY_BUSH,
                Items.LEAF_LITTER,
                Items.CACTUS_FLOWER,
                Items.WILDFLOWERS
                //}
        );

        public static final List<TagKey<Item>> BLOCK_2D_OVERRIDE_ITEM_TAGS = List.of(
                ItemTags.CANDLES,
                ItemTags.BANNERS,
                ItemTags.SMALL_FLOWERS,
                ItemTags.VILLAGER_PLANTABLE_SEEDS,
                ItemTags.SAPLINGS,
                ItemTags.SIGNS,
                ItemTags.HANGING_SIGNS
        );

        public static GenericItemPose fromItemStack(ItemStack itemStack) {
            if (itemStack.is(ItemTags.ARROWS)) {
                return ARROW;
            }
            if (itemStack.is(ItemTags.BANNERS)) {
                return BANNER;
            }
            if (itemStack.is(ItemTags.DOORS)) {
                return DOOR_BLOCK;
            }
            for (Item item : ROD_ITEMS) {
                if (itemStack.is(item)) {
                    return ROD;
                }
            }
            if (itemStack.getItem() instanceof BlockItem) {
                for (Item item : SMALL_BLOCK_ITEMS) {
                    if (itemStack.is(item)) {
                        return SMALL_BLOCK;
                    }
                }
                for (TagKey<Item> tag : SMALL_BLOCK_ITEM_TAGS) {
                    if (itemStack.is(tag)) {
                        return SMALL_BLOCK;
                    }
                }
                for (Item item : BLOCK_2D_OVERRIDE_ITEMS) {
                    if (itemStack.is(item)) {
                        return DEFAULT_2D_ITEM;
                    }
                }
                for (TagKey<Item> tag : BLOCK_2D_OVERRIDE_ITEM_TAGS) {
                    if (itemStack.is(tag)) {
                        return DEFAULT_2D_ITEM;
                    }
                }
                return BLOCK;
            }
            return DEFAULT_2D_ITEM;
        }
    }

    public enum HandPose {
        EMPTY (HandPoseStates.EMPTY_RAISE, HandPoseStates.EMPTY_LOWER, HandPoseStates.EMPTY, HAND_EMPTY_POSE, HAND_TOOL_ATTACK_PICKAXE_MONTAGE),
        GENERIC_ITEM (HandPoseStates.GENERIC_ITEM_RAISE, HandPoseStates.GENERIC_ITEM_LOWER, HandPoseStates.GENERIC_ITEM, HAND_GENERIC_ITEM_2D_ITEM_POSE, HAND_TOOL_ATTACK_PICKAXE_MONTAGE),
        TOOL (HandPoseStates.TOOL_RAISE, HandPoseStates.TOOL_LOWER, HandPoseStates.TOOL, HAND_TOOL_POSE, HAND_TOOL_ATTACK_PICKAXE_MONTAGE),
        SWORD (HandPoseStates.SWORD_RAISE, HandPoseStates.SWORD_LOWER, HandPoseStates.SWORD, HAND_TOOL_POSE, null),
        SHIELD (HandPoseStates.SHIELD_RAISE, HandPoseStates.SHIELD_LOWER, HandPoseStates.SHIELD, HAND_SHIELD_POSE, HAND_TOOL_ATTACK_PICKAXE_MONTAGE),
        BOW (HandPoseStates.BOW_RAISE, HandPoseStates.BOW_LOWER, HandPoseStates.BOW, HAND_BOW_POSE, HAND_TOOL_ATTACK_PICKAXE_MONTAGE);

        public final HandPoseStates raisingState;
        public final HandPoseStates loweringState;
        public final HandPoseStates poseState;
        public final ResourceLocation basePoseLocation;
        public final MontageConfiguration attackMontage;

        HandPose(HandPoseStates raisingState, HandPoseStates loweringState, HandPoseStates poseState, ResourceLocation basePoseLocation, MontageConfiguration attackMontage) {
            this.raisingState = raisingState;
            this.loweringState = loweringState;
            this.poseState = poseState;
            this.basePoseLocation = basePoseLocation;
            this.attackMontage = attackMontage;
        }

        private static final List<TagKey<Item>> TOOL_ITEM_TAGS = List.of(
                ItemTags.PICKAXES,
                ItemTags.AXES,
                ItemTags.SHOVELS,
                ItemTags.HOES
        );

        private static HandPose fromItemStack(ItemStack itemStack) {
            if (itemStack.isEmpty()) {
                return EMPTY;
            }
            if (itemStack.is(Items.BOW)) {
                return BOW;
            }
            if (itemStack.is(Items.SHIELD)) {
                return SHIELD;
            }
            if (itemStack.is(ItemTags.SWORDS)) {
                return SWORD;
            }
            for (TagKey<Item> tag : TOOL_ITEM_TAGS) {
                if (itemStack.is(tag)) {
                    return TOOL;
                }
            }
            return GENERIC_ITEM;
        }

        private PoseFunction<LocalSpacePose> getMiningStateMachine(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
            return switch (interactionHand) {
                case MAIN_HAND -> switch (this) {
                    case TOOL -> makeMiningLoopStateMachine(
                            cachedPoseContainer,
                            SequenceEvaluatorFunction.builder(HAND_TOOL_POSE).build(),
                            SequencePlayerFunction.builder(HAND_TOOL_PICKAXE_MINE_SWING)
                                    .looping(true)
                                    .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(16))
                                    .setPlayRate(evaluationState -> 1.15f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                                    .build(),
                            SequencePlayerFunction.builder(HAND_TOOL_PICKAXE_MINE_FINISH).build(),
                            Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build());
                    default -> ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(this.basePoseLocation).build(), MakeDynamicAdditiveFunction.of(
                            makeMiningLoopStateMachine(
                                    cachedPoseContainer,
                                    SequenceEvaluatorFunction.builder(HAND_EMPTY_POSE).build(),
                                    SequencePlayerFunction.builder(HAND_EMPTY_MINE_SWING)
                                            .looping(true)
                                            .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(20))
                                            .setPlayRate(evaluationState -> 1.35f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                                            .build(),
                                    SequencePlayerFunction.builder(HAND_EMPTY_MINE_FINISH).build(),
                                    Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()),
                            SequenceEvaluatorFunction.builder(HAND_EMPTY_POSE).build()));
                };
                case OFF_HAND -> SequenceEvaluatorFunction.builder(this.basePoseLocation).build();
            };
        }
    }

    public enum HandPoseStates {
        DROPPING_LAST_ITEM,
        USING_LAST_ITEM,
        EMPTY,
        EMPTY_RAISE,
        EMPTY_LOWER,
        GENERIC_ITEM,
        GENERIC_ITEM_RAISE,
        GENERIC_ITEM_LOWER,
        TOOL,
        TOOL_RAISE,
        TOOL_LOWER,
        SWORD,
        SWORD_RAISE,
        SWORD_LOWER,
        SHIELD,
        SHIELD_RAISE,
        SHIELD_LOWER,
        BOW,
        BOW_RAISE,
        BOW_LOWER
    }

    public static final ResourceLocation HAND_EMPTY_LOWERED = makeAnimationSequenceResourceLocation("hand/empty/lowered");
    public static final ResourceLocation HAND_EMPTY_POSE = makeAnimationSequenceResourceLocation("hand/empty/pose");
    public static final ResourceLocation HAND_EMPTY_LOWER = makeAnimationSequenceResourceLocation("hand/empty/lower");
    public static final ResourceLocation HAND_EMPTY_RAISE = makeAnimationSequenceResourceLocation("hand/empty/raise");
    public static final ResourceLocation HAND_EMPTY_MINE_SWING = makeAnimationSequenceResourceLocation("hand/empty/mine_swing");
    public static final ResourceLocation HAND_EMPTY_MINE_FINISH = makeAnimationSequenceResourceLocation("hand/empty/mine_finish");

    public static final ResourceLocation HAND_TOOL_POSE = makeAnimationSequenceResourceLocation("hand/tool/pose");
    public static final ResourceLocation HAND_TOOL_LOWER = makeAnimationSequenceResourceLocation("hand/tool/lower");
    public static final ResourceLocation HAND_TOOL_RAISE = makeAnimationSequenceResourceLocation("hand/tool/raise");
    public static final ResourceLocation HAND_TOOL_PICKAXE_MINE_SWING = makeAnimationSequenceResourceLocation("hand/tool/pickaxe/mine_swing");
    public static final ResourceLocation HAND_TOOL_PICKAXE_MINE_FINISH = makeAnimationSequenceResourceLocation("hand/tool/pickaxe/mine_finish");
    public static final ResourceLocation HAND_TOOL_ATTACK = makeAnimationSequenceResourceLocation("hand/tool/attack");
    public static final ResourceLocation HAND_TOOL_USE = makeAnimationSequenceResourceLocation("hand/tool/use");

    public static final ResourceLocation HAND_GENERIC_ITEM_2D_ITEM_POSE = makeAnimationSequenceResourceLocation("hand/generic_item/2d_item_pose");
    public static final ResourceLocation HAND_GENERIC_ITEM_BLOCK_POSE = makeAnimationSequenceResourceLocation("hand/generic_item/block_pose");
    public static final ResourceLocation HAND_GENERIC_ITEM_SMALL_BLOCK_POSE = makeAnimationSequenceResourceLocation("hand/generic_item/small_block_pose");
    public static final ResourceLocation HAND_GENERIC_ITEM_ROD_POSE = makeAnimationSequenceResourceLocation("hand/generic_item/rod_pose");
    public static final ResourceLocation HAND_GENERIC_ITEM_DOOR_BLOCK_POSE = makeAnimationSequenceResourceLocation("hand/generic_item/door_block_pose");
    public static final ResourceLocation HAND_GENERIC_ITEM_BANNER_POSE = makeAnimationSequenceResourceLocation("hand/generic_item/banner_pose");
    public static final ResourceLocation HAND_GENERIC_ITEM_ARROW_POSE = makeAnimationSequenceResourceLocation("hand/generic_item/arrow_pose");
    public static final ResourceLocation HAND_GENERIC_ITEM_RAISE = makeAnimationSequenceResourceLocation("hand/generic_item/raise");
    public static final ResourceLocation HAND_GENERIC_ITEM_LOWER = makeAnimationSequenceResourceLocation("hand/generic_item/lower");

    public static final ResourceLocation HAND_BOW_POSE = makeAnimationSequenceResourceLocation("hand/bow/pose");
    public static final ResourceLocation HAND_BOW_PULL = makeAnimationSequenceResourceLocation("hand/bow/pull");
    public static final ResourceLocation HAND_BOW_RELEASE = makeAnimationSequenceResourceLocation("hand/bow/release");


    public PoseFunction<LocalSpacePose> constructHandPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {

        StateMachineFunction.Builder<HandPoseStates> handPoseStateMachineBuilder = switch (interactionHand) {
            case MAIN_HAND -> StateMachineFunction.builder(evaluationState -> evaluationState.driverContainer().getDriverValue(MAIN_HAND_POSE).poseState);
            case OFF_HAND -> StateMachineFunction.builder(evaluationState -> evaluationState.driverContainer().getDriverValue(OFF_HAND_POSE).poseState);
        };

        handPoseStateMachineBuilder.resetsUponRelevant(true);
        StateAlias.Builder<HandPoseStates> fromLoweringAliasBuilder = StateAlias.builder(Set.of(HandPoseStates.EMPTY_LOWER));

        this.addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                HandPose.GENERIC_ITEM,
                genericItemPoseFunction(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(context -> context.driverContainer().getDriverValue(getGenericItemPoseDriver(interactionHand), 1).basePoseLocation).build(),
                        SequencePlayerFunction.builder(HAND_GENERIC_ITEM_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(context -> context.driverContainer().getDriverValue(getGenericItemPoseDriver(interactionHand), 1).basePoseLocation).build(),
                        SequencePlayerFunction.builder(HAND_GENERIC_ITEM_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        this.addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                HandPose.TOOL,
                HandPose.TOOL.getMiningStateMachine(cachedPoseContainer, interactionHand),
                SequencePlayerFunction.builder(HAND_TOOL_LOWER).build(),
                SequencePlayerFunction.builder(HAND_TOOL_RAISE).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        this.addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                HandPose.SWORD,
                handSwordPoseFunction(cachedPoseContainer, interactionHand),
                SequencePlayerFunction.builder(HAND_TOOL_LOWER).build(),
                SequencePlayerFunction.builder(HAND_TOOL_RAISE).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        this.addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                HandPose.SHIELD,
                handShieldPoseFunction(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(HAND_SHIELD_POSE).build(),
                        SequencePlayerFunction.builder(HAND_TOOL_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(HAND_SHIELD_POSE).build(),
                        SequencePlayerFunction.builder(HAND_TOOL_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        this.addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                HandPose.BOW,
                HandPose.BOW.getMiningStateMachine(cachedPoseContainer, interactionHand),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(HAND_BOW_POSE).build(),
                        SequencePlayerFunction.builder(HAND_TOOL_LOWER).isAdditive(true, SequenceReferencePoint.BEGINNING).build()
                ),
                ApplyAdditiveFunction.of(
                        SequenceEvaluatorFunction.builder(HAND_BOW_POSE).build(),
                        SequencePlayerFunction.builder(HAND_TOOL_RAISE).isAdditive(true, SequenceReferencePoint.END).build()
                ),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build(),
                Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build()
        );
        this.addStatesForHandPose(
                handPoseStateMachineBuilder,
                fromLoweringAliasBuilder,
                interactionHand,
                HandPose.EMPTY,
                switch (interactionHand) {
                    case MAIN_HAND -> HandPose.EMPTY.getMiningStateMachine(cachedPoseContainer, interactionHand);
                    case OFF_HAND -> SequenceEvaluatorFunction.builder(HAND_EMPTY_LOWERED).build();
                },
                switch (interactionHand) {
                    case MAIN_HAND -> SequencePlayerFunction.builder(HAND_EMPTY_LOWER).build();
                    case OFF_HAND -> SequencePlayerFunction.builder(HAND_EMPTY_LOWERED).build();
                },
                switch (interactionHand) {
                    case MAIN_HAND -> SequencePlayerFunction.builder(HAND_EMPTY_RAISE).build();
                    case OFF_HAND -> SequencePlayerFunction.builder(HAND_EMPTY_LOWERED).build();
                },
                switch (interactionHand) {
                    case MAIN_HAND -> Transition.builder(TimeSpan.of60FramesPerSecond(7)).setEasement(Easing.SINE_IN_OUT).build();
                    case OFF_HAND -> Transition.INSTANT;
                },
                switch (interactionHand) {
                    case MAIN_HAND -> Transition.builder(TimeSpan.of60FramesPerSecond(18)).setEasement(Easing.SINE_IN_OUT).build();
                    case OFF_HAND -> Transition.INSTANT;
                }
        );
        handPoseStateMachineBuilder.addStateAlias(fromLoweringAliasBuilder.build())
                .defineState(State.builder(HandPoseStates.DROPPING_LAST_ITEM,
                                ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(HAND_EMPTY_POSE).build(), MakeDynamicAdditiveFunction.of(SequencePlayerFunction.builder(HAND_TOOL_USE).build(), SequenceEvaluatorFunction.builder(HAND_TOOL_POSE).build()))
                        )
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.EMPTY)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(HandPoseStates.USING_LAST_ITEM,
                                ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(HAND_EMPTY_POSE).build(), MakeDynamicAdditiveFunction.of(SequencePlayerFunction.builder(HAND_TOOL_USE).build(), SequenceEvaluatorFunction.builder(HAND_TOOL_POSE).build()))
                        )
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.EMPTY)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build());


        return handPoseStateMachineBuilder.build();
    }

    public void addStatesForHandPose(
            StateMachineFunction.Builder<HandPoseStates> stateMachineBuilder,
            StateAlias.Builder<HandPoseStates> fromLoweringAliasBuilder,
            InteractionHand interactionHand,
            HandPose handPose,
            PoseFunction<LocalSpacePose> posePoseFunction,
            PoseFunction<LocalSpacePose> loweringPoseFunction,
            PoseFunction<LocalSpacePose> raisingPoseFunction,
            Transition poseToLoweringTiming,
            Transition raisingToPoseTiming
    ) {
        DriverKey<TriggerDriver> hasUsedItemDriver = switch (interactionHand) {
            case MAIN_HAND -> HAS_USED_MAIN_HAND_ITEM;
            case OFF_HAND -> HAS_USED_OFF_HAND_ITEM;
        };

        Predicate<StateTransition.TransitionContext> itemHasChanged = context -> {
            ItemStack itemPreviousTick = context.driverContainer().getDriverValue(getRenderedItemDriver(interactionHand));
            ItemStack itemCurrentTick = context.driverContainer().getDriverValue(getItemDriver(interactionHand));
            if (!itemPreviousTick.is(itemCurrentTick.getItem())) {
                return true;
            }
            for (TypedDataComponent<?> dataComponent : itemCurrentTick.getComponents()) {
                if (dataComponent.type() == DataComponents.DAMAGE) {
                    continue;
                }
                if (!itemPreviousTick.getComponents().has(dataComponent.type())) {
                    return true;
                }
                if (!Objects.equals(itemPreviousTick.get(dataComponent.type()), dataComponent.value())) {
                    LOGGER.info(dataComponent.type().toString());
                    LOGGER.info(dataComponent.value());
                    LOGGER.info(itemPreviousTick.get(dataComponent.type()));
                    return true;
                }
            }
            return false;
        };
        Predicate<StateTransition.TransitionContext> hotbarHasChanged = context -> interactionHand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(HOTBAR_SLOT).hasValueChanged();
        Predicate<StateTransition.TransitionContext> newItemIsEmpty = context -> context.driverContainer().getDriverValue(getItemDriver(interactionHand)).isEmpty();
        Predicate<StateTransition.TransitionContext> oldItemIsEmpty = context -> context.driverContainer().getDriverValue(getRenderedItemDriver(interactionHand)).isEmpty();
        Predicate<StateTransition.TransitionContext> noTwoHandedOverrides = context -> context.driverContainer().getDriverValue(CURRENT_TWO_HANDED_OVERRIDE_STATE) == TwoHandedOverrideStates.NORMAL;

        Predicate<StateTransition.TransitionContext> hardSwitchCondition = (hotbarHasChanged.and(newItemIsEmpty.and(oldItemIsEmpty).negate()).or(itemHasChanged)).and(noTwoHandedOverrides);
        Predicate<StateTransition.TransitionContext> dropLastItemCondition = newItemIsEmpty.and(context -> interactionHand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(HAS_DROPPED_ITEM).hasBeenTriggered());
        Predicate<StateTransition.TransitionContext> useLastItemCondition = itemHasChanged.and(newItemIsEmpty).and(context -> context.driverContainer().getDriver(hasUsedItemDriver).hasBeenTriggered() || (interactionHand == InteractionHand.MAIN_HAND && context.driverContainer().getDriver(HAS_ATTACKED).hasBeenTriggered()));

        Predicate<StateTransition.TransitionContext> skipRaiseAnimationCondition = StateTransition.booleanDriverPredicate(getUsingItemDriver(interactionHand));
        if (interactionHand == InteractionHand.MAIN_HAND) {
            skipRaiseAnimationCondition = skipRaiseAnimationCondition.or(StateTransition.booleanDriverPredicate(IS_MINING)).or(StateTransition.booleanDriverPredicate(HAS_ATTACKED)).or(StateTransition.booleanDriverPredicate(HAS_USED_MAIN_HAND_ITEM));
        }
        Consumer<PoseFunction.FunctionEvaluationState> updateRenderedItem = evaluationState -> {
            if (evaluationState.driverContainer().getDriverValue(CURRENT_TWO_HANDED_OVERRIDE_STATE) == TwoHandedOverrideStates.NORMAL) {
                updateRenderedItem(evaluationState.driverContainer(), interactionHand);
            }
        };
        Consumer<PoseFunction.FunctionEvaluationState> clearAttackMontages = evaluationState -> {
            LOGGER.info("interrupted, {}, {}", evaluationState.driverContainer().getDriverValue(HAS_USED_MAIN_HAND_ITEM), evaluationState.currentTick());
            evaluationState.montageManager().interruptMontagesInSlot(MAIN_HAND_ATTACK_SLOT, Transition.INSTANT);
        };

        stateMachineBuilder
                .defineState(State.builder(handPose.poseState, MontageSlotFunction.of(posePoseFunction, interactionHand == InteractionHand.MAIN_HAND ? MAIN_HAND_ATTACK_SLOT : OFF_HAND_ATTACK_SLOT))
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(handPose.loweringState, loweringPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(handPose.raisingState, MontageSlotFunction.of(raisingPoseFunction, interactionHand == InteractionHand.MAIN_HAND ? MAIN_HAND_ATTACK_SLOT : OFF_HAND_ATTACK_SLOT))
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(handPose.poseState)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1f)
                                .setTiming(raisingToPoseTiming)
                                .build())
                        .addOutboundTransition(StateTransition.builder(handPose.poseState)
                                .isTakenIfTrue(skipRaiseAnimationCondition)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(3)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                        Set.of(
                                handPose.poseState,
                                handPose.raisingState
                        ))
                        .addOutboundTransition(StateTransition.builder(handPose.loweringState)
                                .isTakenIfTrue(hardSwitchCondition)
                                .setTiming(poseToLoweringTiming)
                                .setPriority(50)
                                .build())
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.DROPPING_LAST_ITEM)
                                .isTakenIfTrue(dropLastItemCondition)
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                .bindToOnTransitionTaken(updateRenderedItem)
                                .bindToOnTransitionTaken(clearAttackMontages)
                                .build())
                        .addOutboundTransition(StateTransition.builder(HandPoseStates.USING_LAST_ITEM)
                                .isTakenIfTrue(useLastItemCondition)
                                .setPriority(60)
                                .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                .bindToOnTransitionTaken(updateRenderedItem)
                                .bindToOnTransitionTaken(clearAttackMontages)
                                .build())
                        .build());
        fromLoweringAliasBuilder
                .addOriginatingState(handPose.loweringState)
                .addOutboundTransition(StateTransition.builder(handPose.raisingState)
                        .setTiming(Transition.INSTANT)
                        .isTakenIfTrue(StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING
                                .and(context -> HandPose.fromItemStack(context.driverContainer().getDriverValue(getItemDriver(interactionHand))) == handPose)
                        )
                        .bindToOnTransitionTaken(updateRenderedItem)
                        .bindToOnTransitionTaken(clearAttackMontages)
                        .build());
    }

    public static PoseFunction<LocalSpacePose> genericItemPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        PoseFunction<LocalSpacePose> miningStateMachine = switch (interactionHand) {
            case MAIN_HAND -> ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(context -> context.driverContainer().getDriverValue(getGenericItemPoseDriver(interactionHand), 1).basePoseLocation).build(), MakeDynamicAdditiveFunction.of(
                    makeMiningLoopStateMachine(
                            cachedPoseContainer,
                            SequenceEvaluatorFunction.builder(HAND_EMPTY_POSE).build(),
                            SequencePlayerFunction.builder(HAND_EMPTY_MINE_SWING)
                                    .looping(true)
                                    .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(20))
                                    .setPlayRate(evaluationState -> 1.35f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
                                    .build(),
                            SequencePlayerFunction.builder(HAND_EMPTY_MINE_FINISH).build(),
                            Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_OUT).build()),
                    SequenceEvaluatorFunction.builder(HAND_EMPTY_POSE).build()));
            case OFF_HAND -> SequenceEvaluatorFunction.builder(context -> context.driverContainer().getDriverValue(getGenericItemPoseDriver(interactionHand), 1).basePoseLocation).build();
        };

        return miningStateMachine;
    }

    public static final ResourceLocation HAND_TOOL_SWORD_SWING_LEFT = makeAnimationSequenceResourceLocation("hand/tool/sword/swing_left");
    public static final ResourceLocation HAND_TOOL_SWORD_SWING_RIGHT = makeAnimationSequenceResourceLocation("hand/tool/sword/swing_right");

    public enum SwordSwingStates {
        IDLE,
        SWING_LEFT,
        SWING_RIGHT
    }

    public static PoseFunction<LocalSpacePose> handSwordPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> StateMachineFunction.builder(evaluationState -> SwordSwingStates.IDLE)
                    .resetsUponRelevant(true)
                    .defineState(State.builder(SwordSwingStates.IDLE, HandPose.SWORD.getMiningStateMachine(cachedPoseContainer, interactionHand))
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_LEFT)
                                    .isTakenIfTrue(StateTransition.booleanDriverPredicate(HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .build())
                            .build())
                    .defineState(State.builder(SwordSwingStates.SWING_LEFT, SequencePlayerFunction.builder(HAND_TOOL_SWORD_SWING_LEFT).build())
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_RIGHT)
                                    .isTakenIfTrue(StateTransition.booleanDriverPredicate(HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .build())
                            .build())
                    .defineState(State.builder(SwordSwingStates.SWING_RIGHT, SequencePlayerFunction.builder(HAND_TOOL_SWORD_SWING_RIGHT).build())
                            .resetsPoseFunctionUponEntry(true)
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.SWING_LEFT)
                                    .isTakenIfTrue(StateTransition.booleanDriverPredicate(HAS_ATTACKED))
                                    .setTiming(Transition.builder(TimeSpan.ofTicks(2)).build())
                                    .build())
                            .build())
                    .addStateAlias(StateAlias.builder(
                                    Set.of(
                                            SwordSwingStates.SWING_LEFT,
                                            SwordSwingStates.SWING_RIGHT
                                    ))
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.IDLE)
                                    .isTakenIfMostRelevantAnimationPlayerFinishing(0)
                                    .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(10)).setEasement(Easing.SINE_IN_OUT).build())
                                    .build())
                            .addOutboundTransition(StateTransition.builder(SwordSwingStates.IDLE)
                                    .isTakenIfTrue(StateTransition.booleanDriverPredicate(IS_MINING))
                                    .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build())
                                    .build())
                            .build())
                    .build();
            case OFF_HAND -> HandPose.SWORD.getMiningStateMachine(cachedPoseContainer, interactionHand);
        };
    }

    public static final ResourceLocation HAND_SHIELD_POSE = makeAnimationSequenceResourceLocation("hand/shield/pose");
    public static final ResourceLocation HAND_SHIELD_BLOCK_IN = makeAnimationSequenceResourceLocation("hand/shield/block_in");
    public static final ResourceLocation HAND_SHIELD_BLOCK_OUT = makeAnimationSequenceResourceLocation("hand/shield/block_out");
    public static final ResourceLocation HAND_SHIELD_DISABLE_IN = makeAnimationSequenceResourceLocation("hand/shield/disable_in");
    public static final ResourceLocation HAND_SHIELD_DISABLE_OUT = makeAnimationSequenceResourceLocation("hand/shield/disable_out");
    public static final ResourceLocation HAND_SHIELD_IMPACT = makeAnimationSequenceResourceLocation("hand/shield/impact");

    public enum ShieldStates {
        LOWERED,
        BLOCKING_IN,
        BLOCKING,
        BLOCKING_OUT,
        DISABLED_IN,
        DISABLED,
        DISABLED_OUT
    }

    public static PoseFunction<LocalSpacePose> handShieldPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        DriverKey<VariableDriver<Boolean>> usingItemDriverKey = switch (interactionHand) {
            case MAIN_HAND -> IS_USING_MAIN_HAND_ITEM;
            case OFF_HAND -> IS_USING_OFF_HAND_ITEM;
        };
        DriverKey<VariableDriver<Boolean>> isHandOnCooldownKey = switch (interactionHand) {
            case MAIN_HAND -> IS_MAIN_HAND_ON_COOLDOWN;
            case OFF_HAND -> IS_OFF_HAND_ON_COOLDOWN;
        };
        PoseFunction<LocalSpacePose> shieldBlockingStateMachine = StateMachineFunction.builder(evaluationState -> ShieldStates.LOWERED)
                .resetsUponRelevant(true)
                .defineState(State.builder(ShieldStates.LOWERED, HandPose.SHIELD.getMiningStateMachine(cachedPoseContainer, interactionHand))
                        .build())
                .defineState(State.builder(ShieldStates.BLOCKING_IN, SequencePlayerFunction.builder(HAND_SHIELD_BLOCK_IN).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(5)).build())
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.BLOCKING, MontageSlotFunction.of(SequenceEvaluatorFunction.builder(HAND_SHIELD_BLOCK_OUT).build(), SHIELD_BLOCK_SLOT))
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(ShieldStates.BLOCKING_OUT, SequencePlayerFunction.builder(HAND_SHIELD_BLOCK_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.LOWERED)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(15)).build())
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.DISABLED_IN, SequencePlayerFunction.builder(HAND_SHIELD_DISABLE_IN).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.DISABLED)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(0)
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.DISABLED, SequenceEvaluatorFunction.builder(HAND_SHIELD_DISABLE_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.DISABLED_OUT)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(isHandOnCooldownKey).negate())
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(ShieldStates.DISABLED_OUT, SequencePlayerFunction.builder(HAND_SHIELD_DISABLE_OUT).build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ShieldStates.LOWERED)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(20)).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                        Set.of(
                                ShieldStates.BLOCKING_IN,
                                ShieldStates.BLOCKING,
                                ShieldStates.BLOCKING_OUT,
                                ShieldStates.LOWERED,
                                ShieldStates.DISABLED_OUT
                        ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.DISABLED_IN)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(isHandOnCooldownKey).and(transitionContext -> transitionContext.driverContainer().getDriver(usingItemDriverKey).getPreviousValue()))
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                        Set.of(
                                ShieldStates.BLOCKING_IN,
                                ShieldStates.BLOCKING
                        ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING_OUT)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey).negate()
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED)
                                )
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).build())
                                .setPriority(50)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                        Set.of(
                                ShieldStates.BLOCKING_IN,
                                ShieldStates.BLOCKING,
                                ShieldStates.BLOCKING_OUT
                        ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.LOWERED)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey).negate()
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED)
                                        .and(StateTransition.booleanDriverPredicate(IS_MINING))
                                )
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(6)).build())
                                .setPriority(60)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                        Set.of(
                                ShieldStates.BLOCKING_OUT,
                                ShieldStates.DISABLED_OUT
                        ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING_IN)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey)
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(13)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                        Set.of(
                                ShieldStates.LOWERED
                        ))
                        .addOutboundTransition(StateTransition.builder(ShieldStates.BLOCKING_IN)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(usingItemDriverKey))
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(13)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .build();
        return shieldBlockingStateMachine;
    }

    public enum MiningStates {
        IDLE,
        SWING,
        FINISH
    }

    public static PoseFunction<LocalSpacePose> makeMiningLoopStateMachine(
            CachedPoseContainer cachedPoseContainer,
            PoseFunction<LocalSpacePose> idlePoseFunction,
            PoseFunction<LocalSpacePose> swingPoseFunction,
            PoseFunction<LocalSpacePose> finishPoseFunction,
            Transition idleToMiningTiming
    ) {
        return StateMachineFunction.builder(evaluationState -> MiningStates.IDLE)
                .resetsUponRelevant(true)
                .defineState(State.builder(MiningStates.IDLE, idlePoseFunction)
                        .addOutboundTransition(StateTransition.builder(MiningStates.SWING)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(IS_MINING))
                                .setTiming(idleToMiningTiming)
                                .build())
                        .build())
                .defineState(State.builder(MiningStates.SWING, swingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MiningStates.FINISH)
                                .isTakenIfTrue(
                                        StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_IS_FINISHING.and(StateTransition.booleanDriverPredicate(IS_MINING).negate())
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(50)
                                .build())
                        .build())
                .defineState(State.builder(MiningStates.FINISH, finishPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(MiningStates.IDLE)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setPriority(50)
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .addOutboundTransition(StateTransition.builder(MiningStates.SWING)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(IS_MINING).and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setPriority(60)
                                .setTiming(idleToMiningTiming)
                                .build())
                        .build())
                .build();
    }

    public enum GroundMovementStates {
        IDLE,
        WALKING,
        STOPPING,
        JUMP,
        FALLING,
        LAND,
        SOFT_LAND
    }

    public static final ResourceLocation GROUND_MOVEMENT_POSE = makeAnimationSequenceResourceLocation("ground_movement/pose");
    public static final ResourceLocation GROUND_MOVEMENT_IDLE = makeAnimationSequenceResourceLocation("ground_movement/idle");
    public static final ResourceLocation GROUND_MOVEMENT_WALKING = makeAnimationSequenceResourceLocation("ground_movement/walking");
    public static final ResourceLocation GROUND_MOVEMENT_WALK_TO_STOP = makeAnimationSequenceResourceLocation("ground_movement/walk_to_stop");
    public static final ResourceLocation GROUND_MOVEMENT_JUMP = makeAnimationSequenceResourceLocation("ground_movement/jump");
    public static final ResourceLocation GROUND_MOVEMENT_FALLING = makeAnimationSequenceResourceLocation("ground_movement/falling");
    public static final ResourceLocation GROUND_MOVEMENT_FALLING_DOWN = makeAnimationSequenceResourceLocation("ground_movement/falling_down");
    public static final ResourceLocation GROUND_MOVEMENT_FALLING_IN_PLACE = makeAnimationSequenceResourceLocation("ground_movement/falling_in_place");
    public static final ResourceLocation GROUND_MOVEMENT_FALLING_UP = makeAnimationSequenceResourceLocation("ground_movement/falling_up");
    public static final ResourceLocation GROUND_MOVEMENT_LAND = makeAnimationSequenceResourceLocation("ground_movement/land");

    public PoseFunction<LocalSpacePose> constructAdditiveGroundMovementPoseFunction(CachedPoseContainer cachedPoseContainer) {

        PoseFunction<LocalSpacePose> idleAnimationPlayer = BlendPosesFunction.builder(SequenceEvaluatorFunction.builder(GROUND_MOVEMENT_IDLE).build())
                .addBlendInput(SequencePlayerFunction.builder(GROUND_MOVEMENT_IDLE).looping(true).build(), evaluationState -> 0.6f)
                .build();
        PoseFunction<LocalSpacePose> walkToStopPoseFunction = SequencePlayerFunction.builder(GROUND_MOVEMENT_WALK_TO_STOP).setPlayRate(0.6f).build();
        PoseFunction<LocalSpacePose> jumpPoseFunction = SequencePlayerFunction.builder(GROUND_MOVEMENT_JUMP).build();
        PoseFunction<LocalSpacePose> fallingPoseFunction = BlendedSequencePlayerFunction.builder(VERTICAL_MOVEMENT_SPEED)
                .addEntry(0.5f, GROUND_MOVEMENT_FALLING_UP)
                .addEntry(-0f, GROUND_MOVEMENT_FALLING_IN_PLACE)
                .addEntry(-1f, GROUND_MOVEMENT_FALLING_DOWN)
                .build();
        PoseFunction<LocalSpacePose> walkingPoseFunction = BlendedSequencePlayerFunction.builder(MODIFIED_WALK_SPEED)
                .setResetStartTimeOffset(TimeSpan.of30FramesPerSecond(5))
                .addEntry(0f, GROUND_MOVEMENT_POSE, 0.5f)
                .addEntry(0.5f, GROUND_MOVEMENT_WALKING, 2f)
                .addEntry(0.86f, GROUND_MOVEMENT_WALKING, 2.25f)
                .addEntry(1f, GROUND_MOVEMENT_WALKING, 3.5f)
                .build();

        PoseFunction<LocalSpacePose> landPoseFunction = SequencePlayerFunction.builder(GROUND_MOVEMENT_LAND).build();
        PoseFunction<LocalSpacePose> softLandPoseFunction = BlendPosesFunction.builder(SequenceEvaluatorFunction.builder(GROUND_MOVEMENT_POSE).build())
                .addBlendInput(SequencePlayerFunction.builder(GROUND_MOVEMENT_LAND).setPlayRate(1f).build(), evaluationState -> 0.5f)
                .build();

        Predicate<StateTransition.TransitionContext> walkingCondition = transitionContext -> transitionContext.driverContainer().getDriverValue(IS_MOVING);


        PoseFunction<LocalSpacePose> movementStateMachine = StateMachineFunction.builder(evaluationState -> GroundMovementStates.IDLE)
                .defineState(State.builder(GroundMovementStates.IDLE, idleAnimationPlayer)
                        // Begin walking if the player is moving horizontally
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.WALKING)
                                .isTakenIfTrue(walkingCondition)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.WALKING, walkingPoseFunction)
                        // Stop walking with the walk-to-stop animation if the player's already been walking for a bit.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.STOPPING)
                                .isTakenIfTrue(walkingCondition.negate()
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        // Stop walking directly into the idle animation if the player only just began walking.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.IDLE)
                                .isTakenIfTrue(walkingCondition.negate()
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED.negate()))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.STOPPING, walkToStopPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.IDLE)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(0f)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.WALKING)
                                .isTakenIfTrue(walkingCondition.and(StateTransition.CURRENT_TRANSITION_FINISHED))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.3f)).setEasement(Easing.SINE_IN_OUT).build())
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.JUMP, jumpPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Automatically move into the falling animation player
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.FALLING)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1f)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(19)).setEasement(Easing.CUBIC_OUT).build())
                                .build())
                        // If the player lands before it can move into the falling animation, go straight to the landing animation as long as the jump state is fully transitioned.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.LAND)
                                .isTakenIfTrue(StateTransition.CURRENT_TRANSITION_FINISHED
                                        .and(StateTransition.booleanDriverPredicate(IS_GROUNDED))
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.FALLING, fallingPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        // Move into the landing animation if the player is no longer falling
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.LAND)
                                .isTakenIfTrue(
                                        StateTransition.booleanDriverPredicate(IS_GROUNDED)
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(50)
                                .build())
                        // Move into the landing animation if the player is no longer falling, but only just began falling.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.SOFT_LAND)
                                .isTakenIfTrue(
                                        StateTransition.booleanDriverPredicate(IS_GROUNDED)
                                        .and(StateTransition.CURRENT_TRANSITION_FINISHED.negate())
                                )
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(60)
                                .build())
                        // Transition to the jumping animation if the player is jumping.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.JUMP)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(IS_JUMPING).and(StateTransition.booleanDriverPredicate(IS_GROUNDED)))
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(70)
                                .build())
                        .build())
                .defineState(State.builder(GroundMovementStates.SOFT_LAND, softLandPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(GroundMovementStates.LAND, landPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .addStateAlias(StateAlias.builder(
                        Set.of(
                                GroundMovementStates.IDLE,
                                GroundMovementStates.WALKING,
                                GroundMovementStates.STOPPING,
                                GroundMovementStates.LAND,
                                GroundMovementStates.SOFT_LAND
                        ))
                        // Transition to the jumping animation if the player is jumping.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.JUMP)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(IS_JUMPING)
                                        .and(StateTransition.booleanDriverPredicate(IS_GROUNDED).negate()))
                                .setTiming(Transition.SINGLE_TICK)
                                .setPriority(60)
                                .build())
                        // Transition to the jumping animation if the player is falling.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.FALLING)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(IS_GROUNDED).negate())
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.SINE_OUT).build())
                                .setPriority(50)
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        GroundMovementStates.LAND,
                                        GroundMovementStates.SOFT_LAND
                                ))
                        // If the falling animation is finishing and the player is not walking, play the idle animation.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.IDLE)
                                .isTakenIfTrue(walkingCondition.negate().and(StateTransition.MOST_RELEVANT_ANIMATION_PLAYER_HAS_FINISHED))
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(1)).setEasement(Easing.SINE_IN_OUT).build())
                                .setPriority(50)
                                .build())
                        // If the falling animation is finishing and the player is walking, play the walking animation.
                        .addOutboundTransition(StateTransition.builder(GroundMovementStates.WALKING)
                                .isTakenIfTrue(walkingCondition)
                                .setTiming(Transition.builder(TimeSpan.of60FramesPerSecond(40)).setEasement(Easing.CUBIC_IN_OUT).build())
                                .setPriority(60)
                                .build())
                        .build())
                .build();

//        return movementStateMachine;

        return MakeDynamicAdditiveFunction.of(
                movementStateMachine,
                SequenceEvaluatorFunction.builder(GROUND_MOVEMENT_POSE).build()
        );
    }

    public static void updateRenderedItem(OnTickDriverContainer driverContainer, InteractionHand interactionHand) {
        driverContainer.getDriver(getRenderedItemDriver(interactionHand)).setValue(driverContainer.getDriverValue(getItemDriver(interactionHand)).copy());
        HandPose handPose = HandPose.fromItemStack(driverContainer.getDriver(getRenderedItemDriver(interactionHand)).getCurrentValue());
        driverContainer.getDriver(getHandPoseDriver(interactionHand)).setValue(handPose);
        if (handPose == HandPose.GENERIC_ITEM) {
            driverContainer.getDriver(getGenericItemPoseDriver(interactionHand)).setValue(GenericItemPose.fromItemStack(driverContainer.getDriver(getRenderedItemDriver(interactionHand)).getCurrentValue()));
        } else {
            driverContainer.getDriver(getGenericItemPoseDriver(interactionHand)).setValue(GenericItemPose.DEFAULT_2D_ITEM);
        }
    }

    public static DriverKey<VariableDriver<HandPose>> getHandPoseDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> MAIN_HAND_POSE;
            case OFF_HAND -> OFF_HAND_POSE;
        };
    }

    public static DriverKey<VariableDriver<GenericItemPose>> getGenericItemPoseDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> MAIN_HAND_GENERIC_ITEM_POSE;
            case OFF_HAND -> OFF_HAND_GENERIC_ITEM_POSE;
        };
    }

    public static DriverKey<VariableDriver<ItemStack>> getItemDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> MAIN_HAND_ITEM;
            case OFF_HAND -> OFF_HAND_ITEM;
        };
    }

    public static DriverKey<VariableDriver<ItemStack>> getRenderedItemDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> RENDERED_MAIN_HAND_ITEM;
            case OFF_HAND -> RENDERED_OFF_HAND_ITEM;
        };
    }

    public static DriverKey<VariableDriver<Boolean>> getUsingItemDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> IS_USING_MAIN_HAND_ITEM;
            case OFF_HAND -> IS_USING_OFF_HAND_ITEM;
        };
    }

    public static DriverKey<VariableDriver<Boolean>> getRenderItemAsStaticDriver(InteractionHand interactionHand) {
        return switch (interactionHand) {
            case MAIN_HAND -> RENDER_MAIN_HAND_ITEM_AS_STATIC;
            case OFF_HAND -> RENDER_OFF_HAND_ITEM_AS_STATIC;
        };
    }

    public static final DriverKey<SpringDriver<Vector3f>> DAMPED_VELOCITY = DriverKey.of("damped_velocity", () -> SpringDriver.ofVector3f(0.8f, 0.6f, 1f, Vector3f::new, false));
    public static final DriverKey<VariableDriver<Vector3f>> MOVEMENT_DIRECTION_OFFSET = DriverKey.of("movement_direction_offset", () -> VariableDriver.ofVector(Vector3f::new));
    public static final DriverKey<SpringDriver<Vector3f>> CAMERA_ROTATION_DAMPING = DriverKey.of("camera_rotation_damping", () -> SpringDriver.ofVector3f(LocomotionMain.CONFIG.data().firstPersonPlayer.cameraRotationStiffnessFactor, LocomotionMain.CONFIG.data().firstPersonPlayer.cameraRotationDampingFactor, 1f, Vector3f::new, true));

    public static final DriverKey<VariableDriver<Integer>> HOTBAR_SLOT = DriverKey.of("hotbar_slot", () -> VariableDriver.ofConstant(() -> 0));
    public static final DriverKey<VariableDriver<ItemStack>> MAIN_HAND_ITEM = DriverKey.of("main_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> OFF_HAND_ITEM = DriverKey.of("off_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> RENDERED_MAIN_HAND_ITEM = DriverKey.of("rendered_main_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<ItemStack>> RENDERED_OFF_HAND_ITEM = DriverKey.of("rendered_off_hand_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));
    public static final DriverKey<VariableDriver<Boolean>> RENDER_MAIN_HAND_ITEM_AS_STATIC = DriverKey.of("render_main_hand_item_as_static", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> RENDER_OFF_HAND_ITEM_AS_STATIC = DriverKey.of("render_off_hand_item_as_static", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<HandPose>> MAIN_HAND_POSE = DriverKey.of("main_hand_pose", () -> VariableDriver.ofConstant(() -> HandPose.EMPTY));
    public static final DriverKey<VariableDriver<HandPose>> OFF_HAND_POSE = DriverKey.of("off_hand_pose", () -> VariableDriver.ofConstant(() -> HandPose.EMPTY));
    public static final DriverKey<VariableDriver<GenericItemPose>> MAIN_HAND_GENERIC_ITEM_POSE = DriverKey.of("main_hand_generic_item_pose", () -> VariableDriver.ofConstant(() -> GenericItemPose.DEFAULT_2D_ITEM));
    public static final DriverKey<VariableDriver<GenericItemPose>> OFF_HAND_GENERIC_ITEM_POSE = DriverKey.of("off_hand_generic_item_pose", () -> VariableDriver.ofConstant(() -> GenericItemPose.DEFAULT_2D_ITEM));
    public static final DriverKey<VariableDriver<ItemStack>> PROJECTILE_ITEM = DriverKey.of("projectile_item", () -> VariableDriver.ofConstant(() -> ItemStack.EMPTY));

    public static final DriverKey<VariableDriver<TwoHandedOverrideStates>> CURRENT_TWO_HANDED_OVERRIDE_STATE = DriverKey.of("current_two_handed_override_state", () -> VariableDriver.ofConstant(() -> TwoHandedOverrideStates.NORMAL));

    public static final DriverKey<VariableDriver<Float>> HORIZONTAL_MOVEMENT_SPEED = DriverKey.of("horizontal_movement_speed", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Float>> VERTICAL_MOVEMENT_SPEED = DriverKey.of("vertical_movement_speed", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Float>> MODIFIED_WALK_SPEED = DriverKey.of("modified_walk_speed", () -> VariableDriver.ofFloat(() -> 0f));
    public static final DriverKey<VariableDriver<Boolean>> IS_MOVING = DriverKey.of("is_moving", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_GROUNDED = DriverKey.of("is_grounded", () -> VariableDriver.ofBoolean(() -> true));
    public static final DriverKey<VariableDriver<Boolean>> IS_JUMPING = DriverKey.of("is_jumping", () -> VariableDriver.ofBoolean(() -> false));

    public static final DriverKey<VariableDriver<Boolean>> IS_MINING = DriverKey.of("is_mining", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<TriggerDriver> HAS_ATTACKED = DriverKey.of("has_attacked", TriggerDriver::of);
    public static final DriverKey<TriggerDriver> HAS_USED_MAIN_HAND_ITEM = DriverKey.of("has_used_main_hand_item", () -> TriggerDriver.of(2));
    public static final DriverKey<TriggerDriver> HAS_USED_OFF_HAND_ITEM = DriverKey.of("has_used_off_hand_item", () -> TriggerDriver.of(2));
    public static final DriverKey<TriggerDriver> HAS_BLOCKED_ATTACK = DriverKey.of("has_blocked_attack", TriggerDriver::of);
    public static final DriverKey<TriggerDriver> HAS_DROPPED_ITEM = DriverKey.of("has_dropped_item", TriggerDriver::of);
    public static final DriverKey<VariableDriver<Boolean>> IS_USING_MAIN_HAND_ITEM = DriverKey.of("is_using_main_hand_item", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_USING_OFF_HAND_ITEM = DriverKey.of("is_using_off_hand_item", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_MAIN_HAND_ON_COOLDOWN = DriverKey.of("is_main_hand_on_cooldown", () -> VariableDriver.ofBoolean(() -> false));
    public static final DriverKey<VariableDriver<Boolean>> IS_OFF_HAND_ON_COOLDOWN = DriverKey.of("is_off_hand_on_cooldown", () -> VariableDriver.ofBoolean(() -> false));

    public static final String MAIN_HAND_ATTACK_SLOT = "main_hand_attack";
    public static final String OFF_HAND_ATTACK_SLOT = "off_hand_attack";
    public static final String SHIELD_BLOCK_SLOT = "shield_block";

    public static final MontageConfiguration HAND_TOOL_ATTACK_PICKAXE_MONTAGE = MontageConfiguration.builder("hand_tool_attack_pickaxe", HAND_TOOL_ATTACK)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(8))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(1)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(12)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(driverContainer -> {
                HandPose handPose = driverContainer.getDriverValue(MAIN_HAND_POSE);
                if (handPose == HandPose.GENERIC_ITEM) {
                    return driverContainer.getDriverValue(MAIN_HAND_GENERIC_ITEM_POSE).basePoseLocation;
                }
                return handPose.basePoseLocation;
            })
            .build();
    public static final MontageConfiguration USE_MAIN_HAND_MONTAGE = MontageConfiguration.builder("hand_use_main_hand", HAND_TOOL_USE)
            .playsInSlot(MAIN_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(5))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(16)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(driverContainer -> {
                HandPose handPose = driverContainer.getDriverValue(MAIN_HAND_POSE);
                if (handPose == HandPose.GENERIC_ITEM) {
                    return driverContainer.getDriverValue(MAIN_HAND_GENERIC_ITEM_POSE).basePoseLocation;
                }
                return handPose.basePoseLocation;
            })
            .build();
    public static final MontageConfiguration USE_OFF_HAND_MONTAGE = MontageConfiguration.builder("hand_use_off_hand", HAND_TOOL_USE)
            .playsInSlot(OFF_HAND_ATTACK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(5))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(3)).setEasement(Easing.SINE_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(16)).setEasement(Easing.SINE_IN_OUT).build())
            .makeAdditive(driverContainer -> {
                HandPose handPose = driverContainer.getDriverValue(OFF_HAND_POSE);
                if (handPose == HandPose.GENERIC_ITEM) {
                    return driverContainer.getDriverValue(OFF_HAND_GENERIC_ITEM_POSE).basePoseLocation;
                }
                return handPose.basePoseLocation;
            })
            .build();
    public static final MontageConfiguration SHIELD_BLOCK_IMPACT_MONTAGE = MontageConfiguration.builder("shield_block_impact", HAND_SHIELD_IMPACT)
            .playsInSlot(SHIELD_BLOCK_SLOT)
            .setCooldownDuration(TimeSpan.of60FramesPerSecond(5))
            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_IN_OUT).build())
            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(8)).setEasement(Easing.SINE_IN_OUT).build())
            .build();

    @Override
    public void extractAnimationData(LocalPlayer dataReference, OnTickDriverContainer driverContainer, MontageManager montageManager){

        driverContainer.getDriver(MODIFIED_WALK_SPEED).setValue(dataReference.walkAnimation.speed());
        driverContainer.getDriver(HORIZONTAL_MOVEMENT_SPEED).setValue(new Vector3f((float) (dataReference.getX() - dataReference.xo), 0.0f, (float) (dataReference.getZ() - dataReference.zo)).length());
        driverContainer.getDriver(VERTICAL_MOVEMENT_SPEED).setValue((float) (dataReference.getY() - dataReference.yo));

        // DEBUG ITEMS
//        updateRenderedItem(driverContainer, InteractionHand.MAIN_HAND);
//        updateRenderedItem(driverContainer, InteractionHand.OFF_HAND);
//        LocomotionMain.LOGGER.info("-------------------");
//        LocomotionMain.LOGGER.info(driverContainer.getDriverValue(HAS_USED_MAIN_HAND_ITEM));
//        LocomotionMain.LOGGER.info(dataReference.getMainHandItem());
//        LocomotionMain.LOGGER.info(dataReference.getMainHandItem().getCount());

        driverContainer.getDriver(MAIN_HAND_ITEM).setValue(dataReference.getMainHandItem());
        driverContainer.getDriver(OFF_HAND_ITEM).setValue(dataReference.getOffhandItem());

        //? if >= 1.21.5 {
        driverContainer.getDriver(HOTBAR_SLOT).setValue(dataReference.getInventory().getSelectedSlot());
        //?} else
        /*driverContainer.getDriver(HOTBAR_SLOT).setValue(dataReference.getInventory().selected);*/


        driverContainer.getDriver(HAS_USED_MAIN_HAND_ITEM).runIfTriggered(() -> {
            montageManager.playMontage(USE_MAIN_HAND_MONTAGE, driverContainer);
            if (HandPose.fromItemStack(driverContainer.getDriverValue(RENDERED_MAIN_HAND_ITEM)) == HandPose.fromItemStack(driverContainer.getDriverValue(MAIN_HAND_ITEM))) {
                updateRenderedItem(driverContainer, InteractionHand.MAIN_HAND);
            }
        });
        driverContainer.getDriver(HAS_USED_OFF_HAND_ITEM).runIfTriggered(() -> {
            montageManager.playMontage(USE_OFF_HAND_MONTAGE, driverContainer);
            if (HandPose.fromItemStack(driverContainer.getDriverValue(RENDERED_OFF_HAND_ITEM)) == HandPose.fromItemStack(driverContainer.getDriverValue(OFF_HAND_ITEM))) {
                updateRenderedItem(driverContainer, InteractionHand.OFF_HAND);
            }
        });
        driverContainer.getDriver(HAS_DROPPED_ITEM).runIfTriggered(() -> montageManager.playMontage(USE_MAIN_HAND_MONTAGE, driverContainer));
        driverContainer.getDriver(HAS_ATTACKED).runIfTriggered(() -> {
            MontageConfiguration montageConfiguration = driverContainer.getDriverValue(MAIN_HAND_POSE).attackMontage;
            if (montageConfiguration != null) {
                montageManager.playMontage(montageConfiguration, driverContainer);
            }
        });
        driverContainer.getDriver(HAS_BLOCKED_ATTACK).runIfTriggered(() -> montageManager.playMontage(SHIELD_BLOCK_IMPACT_MONTAGE, driverContainer));

        driverContainer.getDriver(IS_USING_MAIN_HAND_ITEM).setValue(false);
        driverContainer.getDriver(IS_USING_OFF_HAND_ITEM).setValue(false);
        if (dataReference.isUsingItem() && dataReference.getUsedItemHand() == InteractionHand.MAIN_HAND) {
            driverContainer.getDriver(IS_USING_MAIN_HAND_ITEM).setValue(true);
            driverContainer.getDriver(PROJECTILE_ITEM).setValue(dataReference.getProjectile(dataReference.getMainHandItem()));
        }
        if (dataReference.isUsingItem() && dataReference.getUsedItemHand() == InteractionHand.OFF_HAND) {
            driverContainer.getDriver(IS_USING_OFF_HAND_ITEM).setValue(true);
            driverContainer.getDriver(PROJECTILE_ITEM).setValue(dataReference.getProjectile(dataReference.getOffhandItem()));
        }


        driverContainer.getDriver(IS_MAIN_HAND_ON_COOLDOWN).setValue(dataReference.getCooldowns().isOnCooldown(driverContainer.getDriverValue(RENDERED_MAIN_HAND_ITEM)));
        driverContainer.getDriver(IS_OFF_HAND_ON_COOLDOWN).setValue(dataReference.getCooldowns().isOnCooldown(driverContainer.getDriverValue(RENDERED_OFF_HAND_ITEM)));

        if (driverContainer.getDriver(IS_MINING).getCurrentValue()) {
            montageManager.interruptMontagesInSlot(MAIN_HAND_ATTACK_SLOT, Transition.builder(TimeSpan.ofTicks(2)).build());
        }

        driverContainer.getDriver(IS_MOVING).setValue(dataReference.input.keyPresses.forward() || dataReference.input.keyPresses.backward() || dataReference.input.keyPresses.left() || dataReference.input.keyPresses.right());
        driverContainer.getDriver(IS_GROUNDED).setValue(dataReference.onGround());
        driverContainer.getDriver(IS_JUMPING).setValue(dataReference.input.keyPresses.jump());

        Vector3f velocity = new Vector3f((float) (dataReference.getX() - dataReference.xo), (float) (dataReference.getY() - dataReference.yo), (float) (dataReference.getZ() - dataReference.zo));
        // We don't want vertical velocity to be factored into the movement direction offset as much as the horizontal velocity.
        velocity.mul(1, 0f, 1).mul(dataReference.isSprinting() ? 4f : 3f).min(new Vector3f(1)).max(new Vector3f(-1));
        driverContainer.getDriver(DAMPED_VELOCITY).setValue(velocity);

        Vector3f dampedVelocity = new Vector3f(driverContainer.getDriverValue(DAMPED_VELOCITY));
        Quaternionf rotation = new Quaternionf().rotationYXZ(Mth.PI - dataReference.getYRot() * Mth.DEG_TO_RAD, -dataReference.getXRot() * Mth.DEG_TO_RAD, 0.0F);
        Vector3f movementDirection = new Vector3f(
                dampedVelocity.dot(new Vector3f(1, 0, 0).rotate(rotation)),
                dampedVelocity.dot(new Vector3f(0, 1, 0).rotate(rotation)),
                dampedVelocity.dot(new Vector3f(0, 0, -1).rotate(rotation))
        );
        driverContainer.getDriver(MOVEMENT_DIRECTION_OFFSET).setValue(movementDirection);
        driverContainer.getDriver(CAMERA_ROTATION_DAMPING).setValue(new Vector3f(dataReference.getXRot(), dataReference.getYRot(), dataReference.getYRot()).mul(Mth.DEG_TO_RAD));

    }

    private static ResourceLocation makeAnimationSequenceResourceLocation(String path) {
        return ResourceLocation.fromNamespaceAndPath(LocomotionMain.MOD_ID, "sequences/entity/player/first_person/".concat(path).concat(".json"));
    }
}

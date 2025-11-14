package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.State;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateAlias;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateMachineFunction;
import com.trainguy9512.locomotion.animation.pose.function.statemachine.StateTransition;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.*;

import java.util.List;
import java.util.Set;

public enum FirstPersonGenericItemPose {
    DEFAULT_2D_ITEM(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE
    ),
    BLOCK(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_BLOCK_POSE
    ),
    SMALL_BLOCK(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_SMALL_BLOCK_POSE
    ),
    ROD(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_ROD_POSE
    ),
    DOOR_BLOCK(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DOOR_BLOCK_POSE
    ),
    BANNER(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_BANNER_POSE
    ),
    ARROW(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_ARROW_POSE
    ),
    FISHING_ROD(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_FISHING_ROD_POSE
    ),
    SHEARS(
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_SHEARS_POSE
    );

    public final ResourceLocation basePoseLocation;

    FirstPersonGenericItemPose(ResourceLocation basePoseLocation) {
        this.basePoseLocation = basePoseLocation;
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
            ItemTags.HANGING_SIGNS,
            ItemTags.CHAINS
    );

    public static FirstPersonGenericItemPose fromItemStack(ItemStack itemStack) {
        if (itemStack.is(ItemTags.FISHING_ENCHANTABLE)) {
            return FISHING_ROD;
        }
        if (itemStack.is(Items.SHEARS)) {
            return SHEARS;
        }
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

    public boolean shouldRenderBlockstate() {
        return List.of(
                BLOCK,
                SMALL_BLOCK,
                DOOR_BLOCK
        ).contains(this);
    }

    public boolean shouldMirrorItemModel(FirstPersonHandPose handPose, HumanoidArm side) {
        if (side == HumanoidArm.RIGHT) {
            return false;
        }
        if (handPose != FirstPersonHandPose.GENERIC_ITEM) {
            return false;
        }
        return this == ARROW || this == SHEARS;
    }

    public static PoseFunction<LocalSpacePose> constructPoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        PoseFunction<LocalSpacePose> miningStateMachine = switch (interactionHand) {
            case MAIN_HAND ->
                    ApplyAdditiveFunction.of(SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE).build(), MakeDynamicAdditiveFunction.of(
                            FirstPersonMining.makePickaxeMiningPoseFunction(cachedPoseContainer),
                            SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_POSE).build()));
//                            FirstPersonMining.constructPoseFunction(
//                                    cachedPoseContainer,
//                                    SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build(),
//                                    SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_SWING)
//                                            .looping(true)
//                                            .setResetStartTimeOffset(TimeSpan.of60FramesPerSecond(20))
//                                            .setPlayRate(evaluationState -> 1.35f * LocomotionMain.CONFIG.data().firstPersonPlayer.miningAnimationSpeedMultiplier)
//                                            .build(),
//                                    SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_MINE_FINISH).build(),
//                                    Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_OUT).build()),
//                            SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_EMPTY_POSE).build()));
            case OFF_HAND -> SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE).build();
        };

        PoseFunction<LocalSpacePose> consumableStateMachine = constructConsumableStateMachine(cachedPoseContainer, interactionHand, miningStateMachine);
        PoseFunction<LocalSpacePose> consumableStateMachineWithBasePose = ApplyAdditiveFunction.of(
                constructBasePoseFunction(cachedPoseContainer, interactionHand),
                MakeDynamicAdditiveFunction.of(
                        consumableStateMachine,
                        SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE).build()
                )
        );

        return consumableStateMachineWithBasePose;
    }

    private static PoseFunction<LocalSpacePose> constructBasePoseFunction(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand) {
        return SequenceEvaluatorFunction.builder(context ->
                        context.driverContainer()
                        .getInterpolatedDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(interactionHand), 1)
                        .basePoseLocation)
                .build();
    }

    public enum ConsumableStates {
        IDLE,
        DRINKING_BEGIN,
        DRINKING_LOOP,
        DRINKING_FINISHED,
        EATING_BEGIN,
        EATING_LOOP
    }

    private static PoseFunction<LocalSpacePose> constructConsumableStateMachine(CachedPoseContainer cachedPoseContainer, InteractionHand interactionHand, PoseFunction<LocalSpacePose> idlePoseFunction) {
        PoseFunction<LocalSpacePose> drinkingLoopPoseFunction = ApplyAdditiveFunction.of(
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_PROGRESS)
                        .setPlayRate(evaluationState -> evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.ITEM_CONSUMPTION_SPEED))
                        .build(),
                SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_LOOP)
                        .looping(true)
                        .setPlayRate(1f)
                        .isAdditive(true, SequenceReferencePoint.BEGINNING)
                        .build()
        );

        // Eating pose functions
        PoseFunction<LocalSpacePose> eatingLoopPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_EAT_LOOP)
                .setPlayRate(1.5f)
                .looping(true)
                .build();
        PoseFunction<LocalSpacePose> eatingBeginPoseFunction = SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_EAT_BEGIN).build();

        return StateMachineFunction.builder(evaluationState -> ConsumableStates.IDLE)
                .resetsUponRelevant(true)
                .defineState(State.builder(ConsumableStates.IDLE, idlePoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ConsumableStates.EATING_BEGIN)
                                .isTakenIfTrue(context -> isEating(context, interactionHand))
                                .build())
                        .build())
                // Drinking
                .defineState(State.builder(ConsumableStates.DRINKING_BEGIN, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_BEGIN)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ConsumableStates.DRINKING_LOOP)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .defineState(State.builder(ConsumableStates.DRINKING_LOOP, drinkingLoopPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(ConsumableStates.DRINKING_FINISHED, SequencePlayerFunction.builder(FirstPersonAnimationSequences.HAND_GENERIC_ITEM_DRINK_FINISH)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .addOutboundTransition(StateTransition.builder(ConsumableStates.IDLE)
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ConsumableStates.DRINKING_BEGIN,
                                        ConsumableStates.DRINKING_LOOP
                                ))
                        .addOutboundTransition(StateTransition.builder(ConsumableStates.DRINKING_FINISHED)
                                .isTakenIfTrue(context -> !isDrinking(context, interactionHand))
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.2f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(evaluationState -> FirstPersonDrivers.updateRenderedItem(evaluationState.driverContainer(), interactionHand))
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ConsumableStates.DRINKING_FINISHED,
                                        ConsumableStates.IDLE
                                ))
                        .addOutboundTransition(StateTransition.builder(ConsumableStates.DRINKING_BEGIN)
                                .isTakenIfTrue(context -> isDrinking(context, interactionHand))
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(evaluationState -> updateConsumptionSpeed(evaluationState, interactionHand))
                                .build())
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ConsumableStates.DRINKING_BEGIN,
                                        ConsumableStates.DRINKING_LOOP,
                                        ConsumableStates.DRINKING_FINISHED
                                ))
                        .addOutboundTransition(StateTransition.builder(ConsumableStates.IDLE)
                                .isTakenIfTrue(StateTransition.booleanDriverPredicate(FirstPersonDrivers.IS_MINING))
                                .setCanInterruptOtherTransitions(true)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f))
                                        .setEasement(Easing.SINE_IN_OUT)
                                        .build())
                                .bindToOnTransitionTaken(evaluationState -> updateConsumptionSpeed(evaluationState, interactionHand))
                                .build())
                        .build())
                // Eating
                .defineState(State.builder(ConsumableStates.EATING_BEGIN, eatingBeginPoseFunction)
                        .addOutboundTransition(StateTransition.builder(ConsumableStates.EATING_LOOP)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.1f)).setEasement(Easing.SINE_IN_OUT).build())
                                .isTakenIfMostRelevantAnimationPlayerFinishing(1)
                                .build())
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .defineState(State.builder(ConsumableStates.EATING_LOOP, eatingLoopPoseFunction)
                        .resetsPoseFunctionUponEntry(true)
                        .build())
                .addStateAlias(StateAlias.builder(
                                Set.of(
                                        ConsumableStates.EATING_BEGIN,
                                        ConsumableStates.EATING_LOOP
                                ))
                        .addOutboundTransition(StateTransition.builder(ConsumableStates.IDLE)
                                .isTakenIfTrue(context -> !isEating(context, interactionHand))
                                .setCanInterruptOtherTransitions(false)
                                .setTiming(Transition.builder(TimeSpan.ofSeconds(0.8f))
                                        .setEasement(Easing.Elastic.of(4, true))
                                        .build())
                                .build())
                        .build())
                .build();
    }

    public static void updateConsumptionSpeed(PoseFunction.FunctionEvaluationState evaluationState, InteractionHand interactionHand) {
        ItemStack item = evaluationState.driverContainer().getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand));
        if (!item.has(DataComponents.CONSUMABLE)) {
            return;
        }
        float speed = item.get(DataComponents.CONSUMABLE).consumeSeconds();
        speed = 1f / Math.max(speed, 0.1f);
        evaluationState.driverContainer().getDriver(FirstPersonDrivers.ITEM_CONSUMPTION_SPEED).setValue(speed);
    }

    private static boolean isDrinking(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        OnTickDriverContainer driverContainer = context.driverContainer();
        if (!driverContainer.getDriverValue(FirstPersonDrivers.getUsingItemDriver(interactionHand))) {
            return false;
        }
        return driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand)).getUseAnimation() == ItemUseAnimation.DRINK;
    }

    private static boolean isEating(StateTransition.TransitionContext context, InteractionHand interactionHand) {
        OnTickDriverContainer driverContainer = context.driverContainer();
        if (!driverContainer.getDriverValue(FirstPersonDrivers.getUsingItemDriver(interactionHand))) {
            return false;
        }
        return driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand)).getUseAnimation() == ItemUseAnimation.EAT;
    }
}
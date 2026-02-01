package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonHandPoses;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.driver.TriggerDriver;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FirstPersonUseAnimations {

    private static final Map<Identifier, UseAnimationRule> USE_ANIMATION_RULES_BY_LOCATION = Maps.newHashMap();

    public static final Identifier DEFAULT = register(LocomotionMain.makeIdentifier("default"), UseAnimationRule.of(
            FirstPersonMontages::getUseAnimationMontage,
            UseAnimationConditionContext::swingFromClient,
            0
    ));
    public static final Identifier AXE_SCRAPE = register(LocomotionMain.makeIdentifier("axe_scrape"), UseAnimationRule.of(
            FirstPersonMontages::getAxeScrapeMontage,
            FirstPersonUseAnimations::shouldPlayAxeScrape,
            10
    ));
    public static final Identifier HOE_TILL = register(LocomotionMain.makeIdentifier("hoe_till"), UseAnimationRule.of(
            FirstPersonMontages::getHoeTillMontage,
            FirstPersonUseAnimations::shouldPlayHoeTill,
            20
    ));
    public static final Identifier SHOVEL_FLATTEN = register(LocomotionMain.makeIdentifier("shovel_flatten"), UseAnimationRule.of(
            FirstPersonMontages::getShovelFlattenMontage,
            FirstPersonUseAnimations::shouldPlayShovelFlatten,
            30
    ));
    public static final Identifier SHEARS_USE = register(LocomotionMain.makeIdentifier("shears_use"), UseAnimationRule.of(
            FirstPersonMontages::getShearsUseMontage,
            FirstPersonUseAnimations::shouldPlayShearsUse,
            40
    ));

    private static boolean shouldPlayAxeScrape(UseAnimationConditionContext context) {
        if (!context.currentItem.is(ItemTags.AXES) || !context.isTargetingBlock()) {
            return false;
        }
        if (WeatheringCopper.getPrevious(context.lastTargetedBlock()).isPresent()) {
            return true;
        }
        if (HoneycombItem.WAX_OFF_BY_BLOCK.get().get(context.lastTargetedBlock().getBlock()) != null) {
            return true;
        }
        if (context.lastTargetedBlock().getBlock().asItem().getDefaultInstance().is(ItemTags.LOGS)) {
            return true;
        }
        if (context.lastTargetedBlock().getBlock() == Blocks.BAMBOO_BLOCK) {
            return true;
        }
        return false;
    }

    private static final List<Block> TILLABLES = List.of(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT_PATH,
            Blocks.DIRT,
            Blocks.COARSE_DIRT,
            Blocks.ROOTED_DIRT
    );

    private static boolean shouldPlayHoeTill(UseAnimationConditionContext context) {
        if (!context.currentItem.is(ItemTags.HOES) || !context.isTargetingBlock()) {
            return false;
        }
        if (TILLABLES.contains(context.lastTargetedBlock().getBlock())) {
            return true;
        }
        return false;
    }

    private static final List<Block> FLATTENABLES = List.of(
            Blocks.GRASS_BLOCK,
            Blocks.DIRT,
            Blocks.PODZOL,
            Blocks.COARSE_DIRT,
            Blocks.MYCELIUM,
            Blocks.ROOTED_DIRT
    );

    private static boolean shouldPlayShovelFlatten(UseAnimationConditionContext context) {
        if (!context.currentItem.is(ItemTags.SHOVELS) || !context.isTargetingBlock()) {
            return false;
        }
        if (FLATTENABLES.contains(context.lastTargetedBlock().getBlock())) {
            return true;
        }
        return false;
    }

    private static boolean shouldPlayShearsUse(UseAnimationConditionContext context) {
        return context.currentItem.is(Items.SHEARS) && context.useAnimationType() == UseAnimationType.INTERACT_ENTITY;
    }

    /**
     * Registers a use animation rule that plays a specified montage upon a use animation being triggered if it meets a condition.
     * @param identifier            Name to give the use animation
     * @param useAnimationRule      Use animation rule
     */
    public static Identifier register(Identifier identifier, UseAnimationRule useAnimationRule) {
        USE_ANIMATION_RULES_BY_LOCATION.put(identifier, useAnimationRule);
        return identifier;
    }

    public record UseAnimationRule(
            Function<InteractionHand, MontageConfiguration> montageProvider,
            Predicate<UseAnimationConditionContext> shouldChooseUseAnimation,
            int evaluationPriority
    ) {
        /**
         * Creates a use animation rule.
         * @param montageProvider               Function that provides the montage given the interaction hand.
         * @param shouldChooseUseAnimation      Function that determines whether this animation should play given the current context upon being triggered.
         * @param evaluationPriority            Order in which to evaluate this rule. If a rule with a higher priority passes, all below will be skipped.
         */
        public static UseAnimationRule of(
                Function<InteractionHand, MontageConfiguration> montageProvider,
                Predicate<UseAnimationConditionContext> shouldChooseUseAnimation,
                int evaluationPriority
        ) {
            return new UseAnimationRule(montageProvider, shouldChooseUseAnimation, evaluationPriority);
        }
    }

    public record UseAnimationConditionContext(
            UseAnimationType useAnimationType,
            ItemStack currentItem,
            boolean isTargetingBlock,
            boolean isTargetingEntity,
            BlockState lastTargetedBlock,
            EntityType<?> lastTargetedEntity,
            boolean swingFromClient
    ) {
        public boolean isTargetingBlockOrEntity() {
            return this.isTargetingBlock || this.isTargetingEntity;
        }
    }

    public enum UseAnimationType {
        INTERACT_ENTITY,
        INTERACT_AT_ENTITY,
        USE_ITEM,
        USE_ITEM_ON_BLOCK;

        UseAnimationType() {

        }
    }

    public static void playUseAnimationIfTriggered(OnTickDriverContainer driverContainer, MontageManager montageManager, InteractionHand hand) {
        // Scheduling the next use animation if triggered by the multiplayer game mode.
        TriggerDriver hasUsedItemDriver = driverContainer.getDriver(FirstPersonDrivers.getHasUsedItemDriver(hand));
        TriggerDriver hasAttackedDriver = driverContainer.getDriver(FirstPersonDrivers.HAS_ATTACKED);

        int swingTime = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_SWING_TIME);
        InteractionHand lastUsedHand = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_HAND);
        boolean swingTimeIsOne = swingTime == 1;

        if (lastUsedHand != hand) {
            return;
        }
        boolean shouldPlayUseAnimationThisTick = hasUsedItemDriver.hasBeenTriggeredAndNotConsumed();
        if (!shouldPlayUseAnimationThisTick) {
            return;
        }
        hasUsedItemDriver.consume();

        ItemStack renderedItem = driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
        ItemStack actualItem = driverContainer.getDriverValue(FirstPersonDrivers.getItemDriver(hand));
        UseAnimationType useAnimationType = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USE_TYPE);
        BlockState lastTargetedBlock = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_TARGET_BLOCK_STATE);
        EntityType<?> lastTargetedEntity = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_TARGET_ENTITY);
        boolean lastSwingFromClient = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_SWING_FROM_CLIENT);

        UseAnimationConditionContext context = new UseAnimationConditionContext(
                useAnimationType,
                actualItem,
                useAnimationType == UseAnimationType.USE_ITEM_ON_BLOCK,
                useAnimationType == UseAnimationType.INTERACT_ENTITY || useAnimationType == UseAnimationType.INTERACT_AT_ENTITY,
                lastTargetedBlock,
                lastTargetedEntity,
                lastSwingFromClient
        );

        Map<Identifier, UseAnimationRule> sortedUseAnimationRules = USE_ANIMATION_RULES_BY_LOCATION.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().evaluationPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        for (Identifier ruleIdentifier : sortedUseAnimationRules.keySet()) {
            UseAnimationRule rule = sortedUseAnimationRules.get(ruleIdentifier);
            if (rule.shouldChooseUseAnimation.test(context)) {
                LocomotionMain.DEBUG_LOGGER.info("Playing use animation \"{}\"", ruleIdentifier);
                montageManager.playMontage(rule.montageProvider.apply(hand));
                Identifier renderedItemHandPose = FirstPersonHandPoses.testForNextHandPose(renderedItem, hand);
                Identifier currentItemHandPose = FirstPersonHandPoses.testForNextHandPose(actualItem, hand);
                if (currentItemHandPose == renderedItemHandPose) {
                    FirstPersonDrivers.updateRenderedItem(driverContainer, hand);
                }
                return;
            }
        }
    }

    public static void triggerUseAnimation(
            InteractionHand hand,
            UseAnimationType useAnimationType,
            InteractionResult.SwingSource swingSource
    ) {
        var optional = JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer();
        if (optional.isEmpty()) {
            return;
        }

        AnimationDataContainer dataContainer = optional.get();
        dataContainer.getDriver(FirstPersonDrivers.getHasUsedItemDriver(hand)).trigger();
        dataContainer.getDriver(FirstPersonDrivers.LAST_USE_TYPE).setValue(useAnimationType);
        dataContainer.getDriver(FirstPersonDrivers.LAST_USED_HAND).setValue(hand);
        dataContainer.getDriver(FirstPersonDrivers.LAST_USED_SWING_FROM_CLIENT).setValue(swingSource == InteractionResult.SwingSource.CLIENT);
    }

    public static void updateUseAnimationHitResults(AnimationDataContainer dataContainer) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        assert hitResult != null;

        if (hitResult instanceof BlockHitResult blockHitResult && Minecraft.getInstance().level != null) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos());
            dataContainer.getDriver(FirstPersonDrivers.LAST_USED_TARGET_BLOCK_STATE).setValue(blockState);
        }

        if (hitResult instanceof EntityHitResult entityHitResult) {
            EntityType<?> entityType = entityHitResult.getEntity().getType();
            dataContainer.getDriver(FirstPersonDrivers.LAST_USED_TARGET_ENTITY).setValue(entityType);
        }
    }
}

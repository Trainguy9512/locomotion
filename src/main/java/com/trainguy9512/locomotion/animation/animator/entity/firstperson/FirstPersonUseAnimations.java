package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.driver.TriggerDriver;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class FirstPersonUseAnimations {

    private static final ArrayList<UseAnimationRule> USE_ANIMATION_RULES = new ArrayList<>();

    static {
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("default"),
                FirstPersonMontages::getUseAnimationMontage,
                UseAnimationConditionContext::isTargetingBlockOrEntity
        );
//        registerUseAnimationRule(
//                LocomotionMain.makeResourceLocation("crossbow_fire"),
//                FirstPersonMontages::getCrossbowFireMontage,
//                FirstPersonUseAnimations::shouldPlayCrossbowFire
//        );
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("axe_scrape"),
                FirstPersonMontages::getAxeScrapeMontage,
                FirstPersonUseAnimations::shouldPlayAxeScrape
        );
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("hoe_till"),
                FirstPersonMontages::getHoeTillMontage,
                FirstPersonUseAnimations::shouldPlayHoeTill
        );
        registerUseAnimationRule(
                LocomotionMain.makeResourceLocation("shovel_flatten"),
                FirstPersonMontages::getShovelFlattenMontage,
                FirstPersonUseAnimations::shouldPlayShovelFlatten
        );
    }

//    private static boolean shouldPlayCrossbowFire(UseAnimationConditionContext context) {
//        boolean bothItemsAreCrossbows = context.bothItemsMeetCondition(itemStack -> itemStack.has(DataComponents.CHARGED_PROJECTILES));
//        if (!bothItemsAreCrossbows) {
//            return false;
//        }
//        boolean currentCrossbowHasCharge = !context.currentItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
//        boolean previousCrossbowHasCharge = !context.previousItem.get(DataComponents.CHARGED_PROJECTILES).isEmpty();
//        return !currentCrossbowHasCharge && previousCrossbowHasCharge;
//    }

    private static boolean shouldPlayAxeScrape(UseAnimationConditionContext context) {
        if (!context.currentItem.is(ItemTags.AXES) || !context.isTargetingBlock()) {
            return false;
        }
        if (WeatheringCopper.getPrevious(context.lastTargetedBlock()).isPresent()) {
            return true;
        }
        if (HoneycombItem.WAX_OFF_BY_BLOCK.get().get(context.lastTargetedBlock()) != null) {
            return true;
        }
        if (context.lastTargetedBlock().asItem().getDefaultInstance().is(ItemTags.LOGS)) {
            return true;
        }
        if (context.lastTargetedBlock() == Blocks.BAMBOO_BLOCK) {
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
        if (TILLABLES.contains(context.lastTargetedBlock())) {
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
        if (FLATTENABLES.contains(context.lastTargetedBlock())) {
            return true;
        }
        return false;
    }

    public static void registerUseAnimationRule(
            ResourceLocation identifier,
            Function<InteractionHand, MontageConfiguration> montageProvider,
            Predicate<UseAnimationConditionContext> shouldChooseUseAnimation
    ) {
        USE_ANIMATION_RULES.addFirst(new UseAnimationRule(identifier, montageProvider, shouldChooseUseAnimation));
    }

    public record UseAnimationRule(
            ResourceLocation identifier,
            Function<InteractionHand, MontageConfiguration> montageProvider,
            Predicate<UseAnimationConditionContext> shouldChooseUseAnimation
    ) {

    }

    public record UseAnimationConditionContext(
            UseAnimationType useAnimationType,
            ItemStack currentItem,
            boolean isTargetingBlock,
            boolean isTargetingEntity,
            Block lastTargetedBlock,
            EntityType<?> lastTargetedEntity
    ) {

        public boolean isTargetingBlockOrEntity() {
            return this.isTargetingBlock || this.isTargetingEntity;
        }
    }

    public enum UseAnimationType {
        INTERACT_WITH_ENTITY,
        USE_ITEM_ON_ENTITY,
        USE_ITEM,
        USE_ITEM_ON_BLOCK;

        UseAnimationType() {

        }
    }

    public static void playUseAnimationIfTriggered(OnTickDriverContainer driverContainer, MontageManager montageManager, InteractionHand interactionHand) {
        // Scheduling the next use animation if triggered by the multiplayer game mode.
        TriggerDriver hasUsedItemDriver = driverContainer.getDriver(FirstPersonDrivers.getHasUsedItemDriver(interactionHand));
        TriggerDriver hasAttackedDriver = driverContainer.getDriver(FirstPersonDrivers.HAS_ATTACKED);

        int swingTime = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_SWING_TIME);
        InteractionHand lastUsedHand = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_HAND);
        boolean swingTimeIsOne = swingTime == 1;

        if (lastUsedHand != interactionHand) {
            return;
        }
        boolean shouldPlayUseAnimationThisTick = hasUsedItemDriver.hasBeenTriggeredAndNotConsumed() || (swingTimeIsOne && hasAttackedDriver.hasBeenTriggered());
        if (!shouldPlayUseAnimationThisTick) {
            return;
        }
        hasUsedItemDriver.consume();

        ItemStack renderedItem = driverContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand));
        ItemStack actualItem = driverContainer.getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand));
        UseAnimationType useAnimationType = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USE_TYPE);
        Block lastTargetedBlock = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_TARGET_BLOCK);
        EntityType<?> lastTargetedEntity = driverContainer.getDriverValue(FirstPersonDrivers.LAST_USED_TARGET_ENTITY);

        LocomotionMain.DEBUG_LOGGER.info(lastTargetedBlock);

        UseAnimationConditionContext context = new UseAnimationConditionContext(
                useAnimationType,
                actualItem,
                useAnimationType == UseAnimationType.USE_ITEM_ON_BLOCK,
                useAnimationType == UseAnimationType.INTERACT_WITH_ENTITY || useAnimationType == UseAnimationType.USE_ITEM_ON_ENTITY,
                lastTargetedBlock,
                lastTargetedEntity
        );

        for (UseAnimationRule rule : USE_ANIMATION_RULES) {
            if (rule.shouldChooseUseAnimation.test(context)) {
                LocomotionMain.DEBUG_LOGGER.info("Playing use animation \"{}\"", rule.identifier);
                montageManager.playMontage(rule.montageProvider.apply(interactionHand));
                if (FirstPersonHandPose.fromItemStack(renderedItem) == FirstPersonHandPose.fromItemStack(actualItem)) {
                    FirstPersonDrivers.updateRenderedItem(driverContainer, interactionHand);
                }
                return;
            }
        }
    }

    public static void triggerUseAnimation(
            InteractionHand hand,
            UseAnimationType useAnimationType
    ) {
        var optional = JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer();
        if (optional.isEmpty()) {
            return;
        }

        AnimationDataContainer dataContainer = optional.get();
        dataContainer.getDriver(FirstPersonDrivers.getHasUsedItemDriver(hand)).trigger();
        dataContainer.getDriver(FirstPersonDrivers.LAST_USE_TYPE).setValue(useAnimationType);
        dataContainer.getDriver(FirstPersonDrivers.LAST_USED_HAND).setValue(hand);
    }

    public static void updateUseAnimationHitResults(AnimationDataContainer dataContainer) {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        assert hitResult != null;

        if (hitResult instanceof BlockHitResult blockHitResult && Minecraft.getInstance().level != null) {
            BlockState blockState = Minecraft.getInstance().level.getBlockState(blockHitResult.getBlockPos());
            Block block = blockState.getBlock();
            dataContainer.getDriver(FirstPersonDrivers.LAST_USED_TARGET_BLOCK).setValue(block);
        }

        if (hitResult instanceof EntityHitResult entityHitResult) {
            EntityType<?> entityType = entityHitResult.getEntity().getType();
            dataContainer.getDriver(FirstPersonDrivers.LAST_USED_TARGET_ENTITY).setValue(entityType);
        }
    }
}

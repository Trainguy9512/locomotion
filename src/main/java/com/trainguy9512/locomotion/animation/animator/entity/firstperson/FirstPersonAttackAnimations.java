package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.LocomotionMultiVersionWrappers;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FirstPersonAttackAnimations {

    private static final Map<Identifier, AttackAnimationRule> ATTACK_ANIMATION_RULES_BY_IDENTIFIER = Maps.newHashMap();

    public static Identifier register(Identifier identifier, AttackAnimationRule rule) {
        ATTACK_ANIMATION_RULES_BY_IDENTIFIER.put(identifier, rule);
        return identifier;
    }

    public static final Identifier DEFAULT = register(LocomotionMain.makeIdentifier("default"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE,
            context -> true,
            0
    ));
    public static final Identifier EMPTY_HAND_PUNCH = register(LocomotionMain.makeIdentifier("empty_hand_punch"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_EMPTY_ATTACK_MONTAGE,
            context -> context.item().isEmpty(),
            20
    ));
    public static final Identifier TRIDENT = register(LocomotionMain.makeIdentifier("trident_jab"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_TRIDENT_JAB_MONTAGE,
            context -> context.item().getUseAnimation() == LocomotionMultiVersionWrappers.getTridentUseAnimation(),
            30
    ));
    public static final Identifier AXE_ACROSS = register(LocomotionMain.makeIdentifier("axe_across"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_TOOL_ATTACK_AXE_MONTAGE,
            context -> context.item().is(ItemTags.AXES),
            30
    ));
    public static final Identifier MACE_SLAM = register(LocomotionMain.makeIdentifier("mace_slam"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_MACE_ATTACK_MONTAGE,
            context -> context.item().is(ItemTags.MACE_ENCHANTABLE),
            30
    ));
    public static final Identifier SWORD_MAIN = register(LocomotionMain.makeIdentifier("sword_main"), AttackAnimationRule.of(
            MontageConfiguration.builder("hand_tool_sword_attack", FirstPersonAnimationSequences.HAND_TOOL_SWORD_ATTACK)
                    .playsInSlot(FirstPersonMontages.MAIN_HAND_ATTACK_SLOT)
                    .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
                    .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
                    .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
                    .build(),
            context -> context.item().is(ItemTags.SWORDS),
            60
    ));
    //? if >= 1.21.11 {
    public static final Identifier SPEAR_JAB = register(LocomotionMain.makeIdentifier("spear_jab"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_SPEAR_JAB_MONTAGE,
            context -> context.item().getUseAnimation() == LocomotionMultiVersionWrappers.getSpearUseAnimation(),
            30
    ));
    //? }

    public record AttackAnimationRule(
            MontageConfiguration montageToPlay,
            Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation,
            int evaluationPriority
    ) {
        public static AttackAnimationRule of(
                MontageConfiguration montageToPlay,
                Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation,
                int evaluationPriority
        ) {
            return new AttackAnimationRule(montageToPlay, shouldChooseAttackAnimation, evaluationPriority);
        }
    }

    public record AttackAnimationConditionContext(
            ItemStack item
    ) {

    }

    public static void playAttackAnimation(OnTickDriverContainer driverContainer, MontageManager montageManager) {

        AttackAnimationConditionContext context = new AttackAnimationConditionContext(
                driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM)
        );

        Map<Identifier, AttackAnimationRule> sortedAttackAnimationRules = ATTACK_ANIMATION_RULES_BY_IDENTIFIER.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().evaluationPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        for (Identifier ruleIdentifier : sortedAttackAnimationRules.keySet()) {
            AttackAnimationRule rule = sortedAttackAnimationRules.get(ruleIdentifier);
            if (rule.shouldChooseAttackAnimation().test(context)) {
                LocomotionMain.DEBUG_LOGGER.info("Playing use animation \"{}\"", ruleIdentifier);

                MontageConfiguration montage = rule.montageToPlay();
                if (montage == null) {
                    return;
                }

                montageManager.playMontage(montage);
                return;
            }
        }
    }
}

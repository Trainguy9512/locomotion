package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import net.minecraft.resources.Identifier;
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
    public static final Identifier TRIDENT = register(LocomotionMain.makeIdentifier("trident_jab"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_TRIDENT_JAB_MONTAGE,
            context -> context.currentMainHandPose() == FirstPersonHandPoses.TRIDENT,
            30
    ));
    public static final Identifier SWORD_NULL = register(LocomotionMain.makeIdentifier("sword_null"), AttackAnimationRule.of(
            null,
            context -> context.currentMainHandPose() == FirstPersonHandPoses.SWORD,
            30
    ));
    public static final Identifier AXE_ACROSS = register(LocomotionMain.makeIdentifier("axe_across"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_TOOL_ATTACK_AXE_MONTAGE,
            context -> context.currentMainHandPose() == FirstPersonHandPoses.AXE,
            30
    ));
    public static final Identifier MACE_SLAM = register(LocomotionMain.makeIdentifier("mace_slam"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_MACE_ATTACK_MONTAGE,
            context -> context.currentMainHandPose() == FirstPersonHandPoses.MACE,
            30
    ));
    //? if >= 1.21.11 {
    public static final Identifier SPEAR_JAB = register(LocomotionMain.makeIdentifier("spear_jab"), AttackAnimationRule.of(
            FirstPersonMontages.HAND_SPEAR_JAB_MONTAGE,
            context -> context.currentMainHandPose() == FirstPersonHandPoses.SPEAR,
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
            ItemStack item,
            Identifier currentMainHandPose
    ) {

    }

    public static void playAttackAnimation(OnTickDriverContainer driverContainer, MontageManager montageManager) {

        AttackAnimationConditionContext context = new AttackAnimationConditionContext(
                driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM),
                driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_POSE)
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

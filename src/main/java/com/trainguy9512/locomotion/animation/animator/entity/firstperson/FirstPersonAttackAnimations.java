package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.google.common.collect.Maps;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.*;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageConfiguration;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageManager;
import com.trainguy9512.locomotion.animation.pose.function.montage.MontageSlotFunction;
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

    public static final Identifier DEFAULT = register(LocomotionMain.makeIdentifier("default"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_TOOL_ATTACK_PICKAXE_MONTAGE,
            context -> true,
            0
    )
            .build());
    public static final Identifier EMPTY_HAND_PUNCH = register(LocomotionMain.makeIdentifier("empty_hand_punch"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_EMPTY_ATTACK_MONTAGE,
            context -> context.item().isEmpty(),
            20
    )
            .build());
    public static final Identifier TRIDENT = register(LocomotionMain.makeIdentifier("trident_jab"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_TRIDENT_JAB_MONTAGE,
            context -> context.item().getUseAnimation() == LocomotionMultiVersionWrappers.getTridentUseAnimation(),
            30
    )
            .build());
    public static final Identifier AXE_ACROSS = register(LocomotionMain.makeIdentifier("axe_across"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_TOOL_ATTACK_AXE_MONTAGE,
            context -> context.item().is(ItemTags.AXES),
            30
    )
            .setDoesAnimationOffsetOffHand(true)
            .build());
    public static final Identifier MACE_SLAM = register(LocomotionMain.makeIdentifier("mace_slam"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_MACE_ATTACK_MONTAGE,
            context -> context.item().is(ItemTags.MACE_ENCHANTABLE),
            30
    )
            .build());
    public static final Identifier SWORD_MAIN = register(LocomotionMain.makeIdentifier("sword_main"), AttackAnimationRule.builder(
            MontageConfiguration.builder("hand_tool_sword_attack", FirstPersonAnimationSequences.HAND_TOOL_SWORD_ATTACK)
                    .playsInSlot(FirstPersonMontages.MAIN_HAND_ATTACK_SLOT)
                    .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
                    .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
                    .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
                    .build(),
            context -> context.item().is(ItemTags.SWORDS),
            60
    )
            .setDoesAnimationOffsetOffHand(true)
            .build());
    public static final Identifier SWORD_CRITICAL = register(LocomotionMain.makeIdentifier("sword_critical"), AttackAnimationRule.builder(
                    MontageConfiguration.builder("hand_tool_sword_attack_critical", FirstPersonAnimationSequences.HAND_TOOL_SWORD_ATTACK_CRITICAL)
                            .playsInSlot(FirstPersonMontages.MAIN_HAND_ATTACK_SLOT)
                            .setCooldownDuration(TimeSpan.of60FramesPerSecond(3))
                            .setTransitionIn(Transition.builder(TimeSpan.of60FramesPerSecond(2)).setEasement(Easing.SINE_OUT).build())
                            .setTransitionOut(Transition.builder(TimeSpan.of60FramesPerSecond(30)).setEasement(Easing.SINE_IN_OUT).build())
                            .build(),
                    context -> context.item().is(ItemTags.SWORDS) && context.isCriticalAttack(),
                    70
            )
            .setDoesAnimationOffsetOffHand(true)
            .build());
    //? if >= 1.21.11 {
    public static final Identifier SPEAR_JAB = register(LocomotionMain.makeIdentifier("spear_jab"), AttackAnimationRule.builder(
            FirstPersonMontages.HAND_SPEAR_JAB_MONTAGE,
            context -> context.item().getUseAnimation() == LocomotionMultiVersionWrappers.getSpearUseAnimation(),
            30
    )
            .build());
    //? }

    public record AttackAnimationRule(
            MontageConfiguration montageToPlay,
            Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation,
            int evaluationPriority,
            boolean doesAnimationOffsetOffHand
    ) {
        public static Builder builder(
                MontageConfiguration montageToPlay,
                Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation,
                int evaluationPriority
        ) {
            return new Builder(montageToPlay, shouldChooseAttackAnimation, evaluationPriority);
        }

        public static class Builder {

            private final MontageConfiguration montageToPlay;
            private final Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation;
            private final int evaluationPriority;
            private boolean doesAnimationOffsetOffHand;

            private Builder(
                MontageConfiguration montageToPlay,
                Predicate<AttackAnimationConditionContext> shouldChooseAttackAnimation,
                int evaluationPriority
            ) {
                this.montageToPlay = montageToPlay;
                this.shouldChooseAttackAnimation = shouldChooseAttackAnimation;
                this.evaluationPriority = evaluationPriority;
                this.doesAnimationOffsetOffHand = false;
            }

            public Builder setDoesAnimationOffsetOffHand(boolean doesAnimationOffsetOffHand) {
                this.doesAnimationOffsetOffHand = doesAnimationOffsetOffHand;
                return this;
            }

            public AttackAnimationRule build() {
                return new AttackAnimationRule(
                        this.montageToPlay,
                        this.shouldChooseAttackAnimation,
                        this.evaluationPriority,
                        this.doesAnimationOffsetOffHand
                );
            }
        }
    }

    public record AttackAnimationConditionContext(
            ItemStack item,
            boolean isCriticalAttack
    ) {

    }

    public static void tryPlayingAttackAnimation(OnTickDriverContainer driverContainer, MontageManager montageManager) {

        AttackAnimationConditionContext context = new AttackAnimationConditionContext(
                driverContainer.getDriverValue(FirstPersonDrivers.MAIN_HAND_ITEM),
                driverContainer.getDriverValue(FirstPersonDrivers.MEETS_CRITICAL_ATTACK_CONDITIONS)
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

                playAttackAnimation(montageManager, rule);
                return;
            }
        }
    }

    public static String ATTACK_OFF_HAND_OFFSET_SLOT = "attack_off_hand_offset_slot";

    private static void playAttackAnimation(MontageManager montageManager, AttackAnimationRule rule) {
        MontageConfiguration attackMontage = rule.montageToPlay();
        montageManager.playMontage(attackMontage);

        if (rule.doesAnimationOffsetOffHand()) {
            MontageConfiguration offHandOffsetMontage;
            offHandOffsetMontage = attackMontage.makeBuilderCopy(
                    attackMontage.identifier() + "_offhand_offset",
                            attackMontage.animationSequence())
                    .playsInSlot(ATTACK_OFF_HAND_OFFSET_SLOT)
                    .build();
            montageManager.playMontage(offHandOffsetMontage);
        }
    }

    public static void cancelAttackOffHandOffset(PoseFunction.FunctionEvaluationState evaluationState) {
        Transition outTransition = Transition.builder(TimeSpan.ofSeconds(0.2f)).setEasement(Easing.CUBIC_OUT).build();
        evaluationState.montageManager().interruptMontagesInSlot(ATTACK_OFF_HAND_OFFSET_SLOT, outTransition);
    }

    public static PoseFunction<LocalSpacePose> constructWithOffsetOffHandAttack(PoseFunction<LocalSpacePose> inputOffHandPose) {
        PoseFunction<LocalSpacePose> offsetBasePose;
        offsetBasePose = SequenceEvaluatorFunction.builder(FirstPersonAnimationSequences.HAND_TOOL_SWORD_ATTACK).build();

        PoseFunction<LocalSpacePose> offsetAdditivePose;
        offsetAdditivePose = MontageSlotFunction.of(offsetBasePose, ATTACK_OFF_HAND_OFFSET_SLOT);
        offsetAdditivePose = MakeDynamicAdditiveFunction.of(offsetAdditivePose, offsetBasePose);
        offsetAdditivePose = BlendPosesFunction.builder(EmptyPoseFunction.of(false))
                .addBlendInput(offsetAdditivePose, evaluationState -> 1f, FirstPersonJointAnimator.LEFT_SIDE_MASK)
                .build();

        PoseFunction<LocalSpacePose> pose;
        pose = ApplyAdditiveFunction.of(inputOffHandPose, offsetAdditivePose);
        return pose;
    }
}

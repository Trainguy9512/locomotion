package com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonAnimationSequences;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonMining;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.render.ItemRenderType;
import com.trainguy9512.locomotion.animation.util.Easing;
import com.trainguy9512.locomotion.util.LocomotionMultiVersionWrappers;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ShieldItem;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class FirstPersonHandPoses {

    private static final Map<Identifier, HandPoseDefinition> HAND_POSES_BY_IDENTIFIER = new HashMap<>();

    public static Identifier register(Identifier identifier, HandPoseDefinition configuration) {
        HAND_POSES_BY_IDENTIFIER.put(identifier, configuration);
        return identifier;
    }

    public static final Identifier EMPTY_MAIN_HAND = register(LocomotionMain.makeIdentifier("empty_main_hand"), HandPoseDefinition.builder(
            "empty_main_hand",
            FirstPersonMining::constructMainHandEmptyHandMiningPoseFunction,
            FirstPersonAnimationSequences.HAND_EMPTY_POSE,
            ItemStack::isEmpty,
            10)
            .setHandsToUsePoseIn(InteractionHand.MAIN_HAND)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_EMPTY_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_EMPTY_LOWER)
            .build());

    public static final Identifier EMPTY_OFF_HAND = register(LocomotionMain.makeIdentifier("empty_off_hand"), HandPoseDefinition.builder(
            "empty_off_hand",
            FirstPersonMining::constructMainHandPickaxeMiningPoseFunction,
            FirstPersonAnimationSequences.HAND_EMPTY_LOWERED,
            ItemStack::isEmpty,
            10)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_EMPTY_LOWERED)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_EMPTY_LOWERED)
            .setHandsToUsePoseIn(InteractionHand.OFF_HAND)
            .build());
    public static final Identifier GENERIC_ITEM = register(LocomotionMain.makeIdentifier("generic_item"), HandPoseDefinition.builder(
            "generic_item",
            FirstPersonGenericItems::constructPoseFunction,
            FirstPersonAnimationSequences.HAND_GENERIC_ITEM_2D_ITEM_POSE,
            itemStack -> true,
            0)
            .build());
    public static final Identifier PICKAXE = register(LocomotionMain.makeIdentifier("pickaxe"), HandPoseDefinition.builder(
            "pickaxe",
            FirstPersonMining::constructMainHandPickaxeMiningPoseFunction,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.PICKAXES),
            60)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier AXE = register(LocomotionMain.makeIdentifier("axe"), HandPoseDefinition.builder(
            "axe",
            FirstPersonMining::constructMainHandAxeMiningPoseFunction,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.AXES),
            50)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier SHOVEL = register(LocomotionMain.makeIdentifier("shovel"), HandPoseDefinition.builder(
            "shovel",
            FirstPersonMining::constructMainHandShovelMiningPoseFunction,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.SHOVELS),
            40)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier HOE = register(LocomotionMain.makeIdentifier("hoe"), HandPoseDefinition.builder(
            "hoe",
            FirstPersonMining::constructMainHandPickaxeMiningPoseFunction,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.HOES),
            40)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier SWORD = register(LocomotionMain.makeIdentifier("sword"), HandPoseDefinition.builder(
            "sword",
            FirstPersonSword::handSwordPoseFunction,
            FirstPersonAnimationSequences.HAND_TOOL_POSE,
            itemStack -> itemStack.is(ItemTags.SWORDS),
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_SWORD_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier SHIELD = register(LocomotionMain.makeIdentifier("shield"), HandPoseDefinition.builder(
            "shield",
            FirstPersonShield::constructShieldPoseFunction,
            FirstPersonAnimationSequences.HAND_SHIELD_POSE,
            itemStack -> itemStack.getItem() instanceof ShieldItem,
            90)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier BOW = register(LocomotionMain.makeIdentifier("bow"), HandPoseDefinition.builder(
            "bow",
            FirstPersonMining::constructMainHandPickaxeMiningPoseFunction,
            FirstPersonAnimationSequences.HAND_BOW_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.BOW,
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier CROSSBOW = register(LocomotionMain.makeIdentifier("crossbow"), HandPoseDefinition.builder(
            "crossbow",
            FirstPersonMining::constructMainHandPickaxeMiningPoseFunction,
            FirstPersonAnimationSequences.HAND_CROSSBOW_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.CROSSBOW,
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_CROSSBOW_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier TRIDENT = register(LocomotionMain.makeIdentifier("trident"), HandPoseDefinition.builder(
            "trident",
            FirstPersonTrident::handTridentPoseFunction,
            FirstPersonAnimationSequences.HAND_TRIDENT_POSE,
            itemStack -> itemStack.getUseAnimation() == LocomotionMultiVersionWrappers.getTridentUseAnimation(),
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_SPEAR_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_SPEAR_LOWER)
            .build());
    public static final Identifier BRUSH = register(LocomotionMain.makeIdentifier("brush"), HandPoseDefinition.builder(
            "brush",
            FirstPersonBrush::handBrushPoseFunction,
            FirstPersonAnimationSequences.HAND_BRUSH_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.BRUSH,
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier MACE = register(LocomotionMain.makeIdentifier("mace"), HandPoseDefinition.builder(
            "mace",
            FirstPersonMace::handMacePoseFunction,
            FirstPersonAnimationSequences.HAND_MACE_POSE,
            itemStack -> itemStack.is(ItemTags.MACE_ENCHANTABLE),
            110)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier SPYGLASS = register(LocomotionMain.makeIdentifier("spyglass"), HandPoseDefinition.builder(
            "spyglass",
            FirstPersonSpyglass::handSpyglassPoseFunction,
            FirstPersonAnimationSequences.HAND_SPYGLASS_POSE,
            itemStack -> itemStack.getUseAnimation() == ItemUseAnimation.SPYGLASS,
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .build());
    public static final Identifier MAP = register(LocomotionMain.makeIdentifier("map"), HandPoseDefinition.builder(
            "map",
            FirstPersonMining::constructMainHandPickaxeMiningPoseFunction,
            FirstPersonAnimationSequences.HAND_MAP_SINGLE_HAND_POSE,
            itemStack -> itemStack.has(DataComponents.MAP_ID),
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_TOOL_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_TOOL_LOWER)
            .setItemRenderType(ItemRenderType.MAP)
            .build());
    //? if >= 1.21.11 {
    public static final Identifier SPEAR = register(LocomotionMain.makeIdentifier("spear"), HandPoseDefinition.builder(
            "spear",
            FirstPersonSpear::constructSpearPoseFunction,
            FirstPersonAnimationSequences.HAND_SPEAR_POSE,
            itemStack -> itemStack.getUseAnimation() == LocomotionMultiVersionWrappers.getSpearUseAnimation(),
            100)
            .setRaiseSequence(FirstPersonAnimationSequences.HAND_SPEAR_RAISE)
            .setLowerSequence(FirstPersonAnimationSequences.HAND_SPEAR_LOWER)
            .build());
    //? }

    public static Identifier getFallback() {
        return GENERIC_ITEM;
    }

    public static Identifier getEmptyMainHand() {
        return EMPTY_MAIN_HAND;
    }

    public static Identifier getEmptyOffHand() {
        return EMPTY_OFF_HAND;
    }

    public static Identifier getEmptyHandPose(InteractionHand hand) {
        return switch (hand) {
            case MAIN_HAND -> getEmptyMainHand();
            case OFF_HAND -> getEmptyOffHand();
        };
    }

    public record HandPoseDefinition(
            String stateIdentifier,
            Predicate<ItemStack> choosePoseIfTrue,
            int evaluationPriority,
            BiFunction<CachedPoseContainer, InteractionHand, PoseFunction<LocalSpacePose>> poseFunctionProvider,
            Identifier basePoseSequence,
            Identifier raiseSequence,
            Identifier lowerSequence,
            Transition raiseToPoseTransition,
            Transition poseToLowerTransition,
            ItemRenderType itemRenderType,
            InteractionHand[] handsToUsePoseIn
    ) {

        public String getRaiseStateIdentifier() {
            return this.stateIdentifier + "_raise";
        }

        public String getLowerStateIdentifier() {
            return this.stateIdentifier + "_lower";
        }

        public static Builder builder(
                String stateIdentifier,
                BiFunction<CachedPoseContainer, InteractionHand, PoseFunction<LocalSpacePose>> poseFunctionProvider,
                Identifier basePoseSequence,
                Predicate<ItemStack> choosePoseIfTrue,
                int chooseEvaluationPriority
        ) {
            return new Builder(stateIdentifier, poseFunctionProvider, basePoseSequence, choosePoseIfTrue, chooseEvaluationPriority);
        }
        public static class Builder {
            private final String stateIdentifier;
            private final Predicate<ItemStack> choosePoseIfTrue;
            private final int evaluationPriority;
            private final BiFunction<CachedPoseContainer, InteractionHand, PoseFunction<LocalSpacePose>> poseFunctionProvider;
            private final Identifier basePoseSequence;

            private Identifier raiseSequence;
            private Identifier lowerSequence;
            private Transition raiseToPoseTransition;
            private Transition poseToLowerTransition;
            private ItemRenderType itemRenderType;
            private InteractionHand[] handsToUsePoseIn;

            private Builder(
                    String stateIdentifier,
                    BiFunction<CachedPoseContainer, InteractionHand, PoseFunction<LocalSpacePose>> poseFunctionProvider,
                    Identifier basePoseSequence,
                    Predicate<ItemStack> choosePoseIfTrue,
                    int evaluationPriority
            ) {
                this.stateIdentifier = stateIdentifier;
                this.poseFunctionProvider = poseFunctionProvider;
                this.choosePoseIfTrue = choosePoseIfTrue;
                this.evaluationPriority = evaluationPriority;
                this.basePoseSequence = basePoseSequence;

                this.raiseSequence = FirstPersonAnimationSequences.HAND_GENERIC_ITEM_RAISE;
                this.lowerSequence = FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER;
                this.raiseToPoseTransition = Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build();
                this.poseToLowerTransition = Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build();
                this.itemRenderType = ItemRenderType.THIRD_PERSON_ITEM;
                this.handsToUsePoseIn = InteractionHand.values();
            }

            public Builder setRaiseSequence(Identifier sequence) {
                this.raiseSequence = sequence;
                return this;
            }

            public Builder setLowerSequence(Identifier sequence) {
                this.lowerSequence = sequence;
                return this;
            }

            public Builder setRaiseToPoseTransition(Transition transition) {
                this.raiseToPoseTransition = transition;
                return this;
            }

            public Builder setPoseToLowerTransition(Transition transition) {
                this.poseToLowerTransition = transition;
                return this;
            }

            public Builder setItemRenderType(ItemRenderType renderType) {
                this.itemRenderType = renderType;
                return this;
            }

            public Builder setHandsToUsePoseIn(InteractionHand... hands) {
                this.handsToUsePoseIn = hands;
                return this;
            }

            public HandPoseDefinition build() {
                return new HandPoseDefinition(
                        this.stateIdentifier,
                        this.choosePoseIfTrue,
                        this.evaluationPriority,
                        this.poseFunctionProvider,
                        this.basePoseSequence,
                        this.raiseSequence,
                        this.lowerSequence,
                        this.raiseToPoseTransition,
                        this.poseToLowerTransition,
                        this.itemRenderType,
                        this.handsToUsePoseIn
                );
            }
        }
    }

    public static HandPoseDefinition getOrThrowFromIdentifier(Identifier identifier) {
        HandPoseDefinition definition = HAND_POSES_BY_IDENTIFIER.get(identifier);
        if (definition == null) {
            throw new RuntimeException("Identifier " + identifier + " is not a registered hand pose.");
        }
        return definition;
    }

    public static Set<Identifier> getRegisteredHandPoseDefinitions() {
        return HAND_POSES_BY_IDENTIFIER.keySet();
    }

    public static Identifier testForNextHandPose(ItemStack itemStack, InteractionHand hand) {

        Map<Identifier, HandPoseDefinition> handPosesSortedByPriority = HAND_POSES_BY_IDENTIFIER.entrySet()
                .stream()
                .sorted(Comparator.comparingInt(entry -> -entry.getValue().evaluationPriority()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));

        for (Identifier key : handPosesSortedByPriority.keySet()) {
            HandPoseDefinition definition = HAND_POSES_BY_IDENTIFIER.get(key);
            boolean poseHasBeenChosen = definition.choosePoseIfTrue().test(itemStack);
            boolean poseCanPlayInCurrentHand = Arrays.asList(definition.handsToUsePoseIn()).contains(hand);
            if (poseHasBeenChosen && poseCanPlayInCurrentHand) {
                return key;
            }
        }
        return getFallback();
    }
}

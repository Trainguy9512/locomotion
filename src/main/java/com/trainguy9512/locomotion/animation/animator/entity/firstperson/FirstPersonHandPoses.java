package com.trainguy9512.locomotion.animation.animator.entity.firstperson;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.pose.function.PoseFunction;
import com.trainguy9512.locomotion.animation.pose.function.cache.CachedPoseContainer;
import com.trainguy9512.locomotion.render.ItemRenderType;
import com.trainguy9512.locomotion.util.Easing;
import com.trainguy9512.locomotion.util.TimeSpan;
import com.trainguy9512.locomotion.util.Transition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;

public class FirstPersonHandPoses {

    private static final Map<Identifier, HandPoseConfiguration> HAND_POSES_BY_IDENTIFIER = new HashMap<>();

    public static Identifier register(Identifier identifier, HandPoseConfiguration configuration) {
        HAND_POSES_BY_IDENTIFIER.put(identifier, configuration);
        return identifier;
    }

    public static final Identifier EMPTY = register(LocomotionMain.makeIdentifier("empty"), HandPoseConfiguration.builder(
            "empty",

    ))

    public record HandPoseConfiguration(
            String stateIdentifier,
            Predicate<ItemStack> choosePoseIfTrue,
            int chooseEvaluationPriority,
            BiFunction<CachedPoseContainer, InteractionHand, PoseFunction<LocalSpacePose>> poseFunctionProvider,
            Identifier basePoseSequence,
            Identifier raiseSequence,
            Identifier lowerSequence,
            Transition raiseToPoseTransition,
            Transition poseToLowerTransition,
            ItemRenderType itemRenderType
    ) {

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
            private final int chooseEvaluationPriority;
            private final BiFunction<CachedPoseContainer, InteractionHand, PoseFunction<LocalSpacePose>> poseFunctionProvider;
            private final Identifier basePoseSequence;

            private Identifier raiseSequence;
            private Identifier lowerSequence;
            private Transition raiseToPoseTransition;
            private Transition poseToLowerTransition;
            private ItemRenderType itemRenderType;

            private Builder(
                    String stateIdentifier,
                    BiFunction<CachedPoseContainer, InteractionHand, PoseFunction<LocalSpacePose>> poseFunctionProvider,
                    Identifier basePoseSequence,
                    Predicate<ItemStack> choosePoseIfTrue,
                    int chooseEvaluationPriority
            ) {
                this.stateIdentifier = stateIdentifier;
                this.poseFunctionProvider = poseFunctionProvider;
                this.choosePoseIfTrue = choosePoseIfTrue;
                this.chooseEvaluationPriority = chooseEvaluationPriority;
                this.basePoseSequence = basePoseSequence;

                this.raiseSequence = FirstPersonAnimationSequences.HAND_GENERIC_ITEM_RAISE;
                this.lowerSequence = FirstPersonAnimationSequences.HAND_GENERIC_ITEM_LOWER;
                this.raiseToPoseTransition = Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build();
                this.poseToLowerTransition = Transition.builder(TimeSpan.of60FramesPerSecond(6)).setEasement(Easing.SINE_IN_OUT).build();
                this.itemRenderType = ItemRenderType.THIRD_PERSON_ITEM;
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

            public HandPoseConfiguration build() {
                return new HandPoseConfiguration(
                        stateIdentifier,
                        choosePoseIfTrue,
                        chooseEvaluationPriority,
                        poseFunctionProvider,
                        basePoseSequence,
                        raiseSequence,
                        lowerSequence,
                        raiseToPoseTransition,
                        poseToLowerTransition,
                        itemRenderType
                );
            }
        }
    }
}

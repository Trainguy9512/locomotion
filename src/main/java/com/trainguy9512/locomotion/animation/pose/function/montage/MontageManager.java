package com.trainguy9512.locomotion.animation.pose.function.montage;

import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.data.OnTickDriverContainer;
import com.trainguy9512.locomotion.animation.driver.VariableDriver;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.joint.skeleton.JointSkeleton;
import com.trainguy9512.locomotion.animation.pose.LocalSpacePose;
import com.trainguy9512.locomotion.animation.sequence.AnimationSequence;
import com.trainguy9512.locomotion.resource.LocomotionResources;
import com.trainguy9512.locomotion.animation.util.TimeSpan;
import com.trainguy9512.locomotion.animation.util.Transition;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MontageManager {

    private final AnimationDataContainer animationDataContainer;
    private final List<MontageInstance> montageStack;

    public MontageManager(AnimationDataContainer animationDataContainer) {
        this.animationDataContainer = animationDataContainer;
        this.montageStack = new ArrayList<>();
    }

    public void tick() {
        this.montageStack.forEach(MontageInstance::tick);
        this.montageStack.removeIf(montageInstance -> montageInstance.ticksElapsed.getPreviousValue() > montageInstance.tickLength + (1 - montageInstance.configuration.transitionOutCrossfadeWeight()) * montageInstance.configuration.transitionOut().duration().inTicks());
        this.montageStack.removeIf(montageInstance -> {
            if (montageInstance.hasBeenInterrupted) {
                return montageInstance.ticksElapsed.getPreviousValue() - montageInstance.interruptTick > montageInstance.interruptTransition.duration().inTicks();
            }
            return false;
        });
    }

    public static MontageManager of(AnimationDataContainer animationDataContainer) {
        return new MontageManager(animationDataContainer);
    }

    /**
     * Plays a montage of the given configuration.
     * @param configuration         Montage configuration to use as the template for the montage.
     */
    public void playMontage(MontageConfiguration configuration) {
        for (MontageInstance instance : this.montageStack) {
            if (Objects.equals(instance.configuration.identifier(), configuration.identifier())) {
                if (instance.ticksElapsed.getCurrentValue() < configuration.cooldownDuration().inTicks()) {
                    return;
                }
            }
        }
        this.montageStack.addLast(MontageInstance.of(configuration, this.animationDataContainer));
    }

    /**
     * Immediately stops and removes any montages currently playing within the provided slot.
     * @param slot                  Slot identifier
     */
    public void interruptMontagesInSlot(String slot, Transition transition) {
        for (MontageInstance montageInstance : this.montageStack) {
            if (montageInstance.configuration.slots().contains(slot)) {
                montageInstance.interrupt(transition);
            }
        }
    }

    /**
     * Returns whether a montage of the provided identifier is playing or not.
     * @param identifier            Montage configuration identifier
     */
    public boolean isMontagePlaying(String identifier) {
        for (MontageInstance montageInstance : this.montageStack) {
            if (montageInstance.configuration.identifier().equals(identifier)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether any montage is playing in the provided slot
     * @param slot                  Slot identifier
     */
    public boolean isAnythingPlayingInSlot(String slot) {
        for (MontageInstance montageInstance : this.montageStack) {
            if (montageInstance.configuration.slots().contains(slot)) {
                return true;
            }
        }
        return false;
    }

    public LocalSpacePose getLayeredSlotPose(LocalSpacePose basePose, String slot, JointSkeleton jointSkeleton, float partialTicks) {
        LocalSpacePose slotPose = LocalSpacePose.of(basePose);
        LocalSpacePose previousPose = LocalSpacePose.of(basePose);
        for (MontageInstance montageInstance : this.montageStack) {
            if (montageInstance.configuration.slots().contains(slot)) {

                float weight = montageInstance.getWeight(partialTicks);
                Transition transition = montageInstance.isInEntranceTransition(partialTicks) ? montageInstance.configuration.transitionIn() : montageInstance.configuration.transitionOut().withInverseEasing();
                slotPose.interpolatedByTransition(
                        montageInstance.getPose(jointSkeleton, partialTicks),
                        weight,
                        transition,
                        montageInstance.configuration.blendMask()
                );
                if (montageInstance.hasBeenInterrupted) {
                    slotPose.interpolatedByTransition(
                            previousPose,
                            montageInstance.getInterruptWeight(partialTicks),
                            montageInstance.interruptTransition,
                            null
                    );
                }
                previousPose = LocalSpacePose.of(slotPose);
//                slotPose = slotPose.interpolated(montageInstance.getPose(jointSkeleton, partialTicks), montageInstance.getWeight(partialTicks), null, null);
            }
        }
        return slotPose;
    }

    public boolean areAnyMontagesInSlotFullyOverriding(String slot) {
        for (MontageInstance montageInstance : this.montageStack) {
            if (!montageInstance.configuration.slots().contains(slot)) {
                break;
            }
            if (montageInstance.getWeightIsFull(1) && montageInstance.getWeightIsFull(0)) {
                return true;
            }
        }
        return false;
    }

    private static class MontageInstance {
        private final VariableDriver<Float> ticksElapsed;
        private final MontageConfiguration configuration;

        private final float playRate;
        private final float tickLength;

        private boolean hasBeenInterrupted;
        private float interruptTick;
        private Transition interruptTransition;

        private final Identifier additiveBasePoseLocation;
        private LocalSpacePose additiveBasePose;
        private LocalSpacePose additiveSubtractionPose;

        private MontageInstance(MontageConfiguration configuration, OnTickDriverContainer driverContainer) {
            this.ticksElapsed = VariableDriver.ofFloat(() -> configuration.startTimeOffset().inTicks());
            this.configuration = configuration;

            this.playRate = configuration.playRateFunction().apply(driverContainer);
            this.tickLength = LocomotionResources.getOrThrowAnimationSequence(configuration.animationSequence()).length().inTicks();

            this.hasBeenInterrupted = false;
            this.interruptTick = 0;
            this.interruptTransition = Transition.INSTANT;

            if (configuration.isAdditive()) {
                this.additiveBasePoseLocation = configuration.additiveBasePoseProvider().apply(driverContainer);
            } else {
                this.additiveBasePoseLocation = null;
            }
            this.additiveBasePose = null;
            this.additiveSubtractionPose = null;
        }

        private static MontageInstance of(MontageConfiguration configuration, OnTickDriverContainer driverContainer) {
            return new MontageInstance(configuration, driverContainer);
        }

        private void tick() {
            this.ticksElapsed.pushCurrentToPrevious();
            this.ticksElapsed.modifyValue(currentValue -> currentValue + this.playRate);
        }

        private void interrupt(Transition transition) {
            if (!this.hasBeenInterrupted) {
                this.hasBeenInterrupted = true;
                this.interruptTransition = transition;
                this.interruptTick = this.ticksElapsed.getCurrentValue();
            }
        }

        private boolean getWeightIsFull(float partialTicks) {
            float interpolatedTimeElapsed = this.ticksElapsed.getValueInterpolated(partialTicks);
            if (interpolatedTimeElapsed > this.configuration.startTimeOffset().inTicks() + this.configuration.transitionIn().duration().inTicks()) {
                if (interpolatedTimeElapsed < this.tickLength - (this.configuration.transitionOut().duration().inTicks() * this.configuration.transitionOutCrossfadeWeight())) {
                    return !this.hasBeenInterrupted;
                }
            }
            return false;
        }

        private LocalSpacePose getPose(JointSkeleton jointSkeleton, float partialTicks) {
            LocalSpacePose pose = AnimationSequence.samplePose(
                    jointSkeleton,
                    this.configuration.animationSequence(),
                    TimeSpan.ofTicks(this.ticksElapsed.getValueInterpolated(partialTicks)),
                    false
            );
            if (this.configuration.isAdditive()) {
                // If the additive base pose and the additive subtraction poses are null, initialize them (only initialized when needed.
                if (this.additiveBasePose == null) {
                    this.additiveBasePose = AnimationSequence.samplePose(
                            jointSkeleton,
                            this.additiveBasePoseLocation,
                            TimeSpan.ofTicks(0),
                            false
                    );
                }
                if (this.additiveSubtractionPose == null) {
                    this.additiveSubtractionPose = AnimationSequence.samplePose(
                            jointSkeleton,
                            this.configuration.animationSequence(),
                            this.configuration.startTimeOffset(),
                            false
                    );
                    this.additiveSubtractionPose.invert();
                }

                pose.multiply(this.additiveSubtractionPose, JointChannel.TransformSpace.COMPONENT);
                pose.multiply(this.additiveBasePose, JointChannel.TransformSpace.COMPONENT);
            }
            return pose;
        }

        private boolean isInEntranceTransition(float partialTicks) {
            float elapsedTicksInterpolated = this.ticksElapsed.getValueInterpolated(partialTicks);
            float entranceTransitionEndTime = this.configuration.startTimeOffset().inTicks() + this.configuration.transitionIn().duration().inTicks();
            return elapsedTicksInterpolated < entranceTransitionEndTime;
        }

        private boolean isInExitTransition(float partialTicks) {
            float elapsedTicksInterpolated = this.ticksElapsed.getValueInterpolated(partialTicks);
            float exitTransitionStartTime = this.tickLength - this.configuration.transitionOut().duration().inTicks() * this.configuration.transitionOutCrossfadeWeight();
            return elapsedTicksInterpolated > exitTransitionStartTime;
        }

        private float getWeight(float partialTicks) {
            if (this.getWeightIsFull(partialTicks)) {
                return 1;
            }
            float elapsedTicksInterpolated = this.ticksElapsed.getValueInterpolated(partialTicks);

            float entranceTransitionEndTime = this.configuration.startTimeOffset().inTicks() + this.configuration.transitionIn().duration().inTicks();
            float exitTransitionStartTime = this.tickLength - this.configuration.transitionOut().duration().inTicks() * this.configuration.transitionOutCrossfadeWeight();

            boolean isInEntranceTransition = this.isInEntranceTransition(partialTicks);
            boolean isInExitTransition = this.isInExitTransition(partialTicks);

            float weight = 1f;

            if (isInEntranceTransition) {
                weight = (elapsedTicksInterpolated - this.configuration.startTimeOffset().inTicks()) / this.configuration.transitionIn().duration().inTicks();
            } else if (isInExitTransition) {
                weight = 1 - Math.min((elapsedTicksInterpolated - exitTransitionStartTime) / this.configuration.transitionOut().duration().inTicks(), 1f);
            }
//            if (this.hasBeenInterrupted) {
//                weight *= (1 - Math.min((elapsedTicksInterpolated - this.interruptTick) / this.interruptTransition.duration().inTicks(), 1));
//            }
            return weight;
        }

        private float getInterruptWeight(float partialTicks) {
            float elapsedTicksInterpolated = this.ticksElapsed.getValueInterpolated(partialTicks);
            return 1 - (1 - Math.min((elapsedTicksInterpolated - this.interruptTick) / this.interruptTransition.duration().inTicks(), 1));
        }
    }
}

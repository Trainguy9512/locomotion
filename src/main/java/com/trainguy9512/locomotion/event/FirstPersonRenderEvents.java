package com.trainguy9512.locomotion.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.trainguy9512.locomotion.render.ItemRenderType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class FirstPersonRenderEvents {
    private FirstPersonRenderEvents() {
        // []
    }

    /**
     * Event triggered before rendering the player's arm
     */
    public static class BeforeArmRender {
        private final AbstractClientPlayer player;
        private final HumanoidArm arm;
        private final InteractionHand hand;
        private final PoseStack poseStack;
        private final SubmitNodeCollector nodeCollector;
        private final int combinedLight;
        private final float partialTick;
        private boolean cancelled = false;

        public BeforeArmRender(AbstractClientPlayer player, HumanoidArm arm, InteractionHand hand,
                               PoseStack poseStack, SubmitNodeCollector nodeCollector,
                               int combinedLight, float partialTick) {
            this.player = player;
            this.arm = arm;
            this.hand = hand;
            this.poseStack = poseStack;
            this.nodeCollector = nodeCollector;
            this.combinedLight = combinedLight;
            this.partialTick = partialTick;
        }

        public AbstractClientPlayer getPlayer() {
            return player;
        }

        public HumanoidArm getArm() {
            return arm;
        }

        public InteractionHand getHand() {
            return hand;
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        public SubmitNodeCollector getNodeCollector() {
            return nodeCollector;
        }

        public int getCombinedLight() {
            return combinedLight;
        }

        public float getPartialTick() {
            return partialTick;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    /**
     * Event triggered after rendering the player's arm
     */
    public static class AfterArmRender {
        private final AbstractClientPlayer player;
        private final HumanoidArm arm;
        private final InteractionHand hand;
        private final PoseStack poseStack;
        private final SubmitNodeCollector nodeCollector;
        private final int combinedLight;
        private final float partialTick;

        public AfterArmRender(AbstractClientPlayer player, HumanoidArm arm, InteractionHand hand,
                              PoseStack poseStack, SubmitNodeCollector nodeCollector,
                              int combinedLight, float partialTick) {
            this.player = player;
            this.arm = arm;
            this.hand = hand;
            this.poseStack = poseStack;
            this.nodeCollector = nodeCollector;
            this.combinedLight = combinedLight;
            this.partialTick = partialTick;
        }

        public AbstractClientPlayer getPlayer() {
            return player;
        }

        public HumanoidArm getArm() {
            return arm;
        }

        public InteractionHand getHand() {
            return hand;
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        public SubmitNodeCollector getNodeCollector() {
            return nodeCollector;
        }

        public int getCombinedLight() {
            return combinedLight;
        }

        public float getPartialTick() {
            return partialTick;
        }
    }

    /**
     * Event triggered before rendering an item in first person
     */
    public static class BeforeItemRender {
        private final AbstractClientPlayer player;
        private final ItemStack itemStack;
        private final PoseStack poseStack;
        private final SubmitNodeCollector nodeCollector;
        private final int combinedLight;
        private final HumanoidArm arm;
        private final InteractionHand hand;
        private final ItemRenderType renderType;
        private boolean cancelled = false;

        public BeforeItemRender(AbstractClientPlayer player, ItemStack itemStack,
                                PoseStack poseStack, SubmitNodeCollector nodeCollector,
                                int combinedLight, HumanoidArm arm, InteractionHand hand,
                                ItemRenderType renderType) {
            this.player = player;
            this.itemStack = itemStack;
            this.poseStack = poseStack;
            this.nodeCollector = nodeCollector;
            this.combinedLight = combinedLight;
            this.arm = arm;
            this.hand = hand;
            this.renderType = renderType;
        }

        public AbstractClientPlayer getPlayer() {
            return player;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        public SubmitNodeCollector getNodeCollector() {
            return nodeCollector;
        }

        public int getCombinedLight() {
            return combinedLight;
        }

        public HumanoidArm getArm() {
            return arm;
        }

        public InteractionHand getHand() {
            return hand;
        }

        public ItemRenderType getRenderType() {
            return renderType;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    /**
     * Event triggered after rendering an item in first person
     */
    public static class AfterItemRender {
        private final AbstractClientPlayer player;
        private final ItemStack itemStack;
        private final PoseStack poseStack;
        private final SubmitNodeCollector nodeCollector;
        private final int combinedLight;
        private final HumanoidArm arm;
        private final InteractionHand hand;
        private final ItemRenderType renderType;

        public AfterItemRender(AbstractClientPlayer player, ItemStack itemStack,
                               PoseStack poseStack, SubmitNodeCollector nodeCollector,
                               int combinedLight, HumanoidArm arm, InteractionHand hand,
                               ItemRenderType renderType) {
            this.player = player;
            this.itemStack = itemStack;
            this.poseStack = poseStack;
            this.nodeCollector = nodeCollector;
            this.combinedLight = combinedLight;
            this.arm = arm;
            this.hand = hand;
            this.renderType = renderType;
        }

        public AbstractClientPlayer getPlayer() {
            return player;
        }

        public ItemStack getItemStack() {
            return itemStack;
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        public SubmitNodeCollector getNodeCollector() {
            return nodeCollector;
        }

        public int getCombinedLight() {
            return combinedLight;
        }

        public HumanoidArm getArm() {
            return arm;
        }

        public InteractionHand getHand() {
            return hand;
        }

        public ItemRenderType getRenderType() {
            return renderType;
        }
    }

    /**
     * Event triggered before rendering the first full person
     */
    public static class BeforeFirstPersonRender {
        private final net.minecraft.client.player.LocalPlayer player;
        private final PoseStack poseStack;
        private final SubmitNodeCollector nodeCollector;
        private final int combinedLight;
        private final float partialTicks;
        private boolean cancelled = false;

        public BeforeFirstPersonRender(net.minecraft.client.player.LocalPlayer player, PoseStack poseStack,
                                       SubmitNodeCollector nodeCollector, int combinedLight, float partialTicks) {
            this.player = player;
            this.poseStack = poseStack;
            this.nodeCollector = nodeCollector;
            this.combinedLight = combinedLight;
            this.partialTicks = partialTicks;
        }

        public net.minecraft.client.player.LocalPlayer getPlayer() {
            return player;
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        public SubmitNodeCollector getNodeCollector() {
            return nodeCollector;
        }

        public int getCombinedLight() {
            return combinedLight;
        }

        public float getPartialTicks() {
            return partialTicks;
        }

        public boolean isCancelled() {
            return cancelled;
        }

        public void setCancelled(boolean cancelled) {
            this.cancelled = cancelled;
        }
    }

    /**
     * Event triggered after rendering the first full person
     */
    public static class AfterFirstPersonRender {
        private final net.minecraft.client.player.LocalPlayer player;
        private final PoseStack poseStack;
        private final SubmitNodeCollector nodeCollector;
        private final int combinedLight;
        private final float partialTicks;

        public AfterFirstPersonRender(net.minecraft.client.player.LocalPlayer player, PoseStack poseStack,
                                      SubmitNodeCollector nodeCollector, int combinedLight, float partialTicks) {
            this.player = player;
            this.poseStack = poseStack;
            this.nodeCollector = nodeCollector;
            this.combinedLight = combinedLight;
            this.partialTicks = partialTicks;
        }

        public net.minecraft.client.player.LocalPlayer getPlayer() {
            return player;
        }

        public PoseStack getPoseStack() {
            return poseStack;
        }

        public SubmitNodeCollector getNodeCollector() {
            return nodeCollector;
        }

        public int getCombinedLight() {
            return combinedLight;
        }

        public float getPartialTicks() {
            return partialTicks;
        }
    }
}
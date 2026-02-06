package com.trainguy9512.locomotion.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.trainguy9512.locomotion.access.MatrixModelPart;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.*;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonGenericItems;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonHandPoses;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.animation.pose.ModelPartSpacePose;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.PlayerSkin;


import java.util.Objects;

public class FirstPersonPlayerRenderer implements RenderLayerParent<AvatarRenderState, PlayerModel> {

    private final Minecraft minecraft;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    private final BlockRenderDispatcher blockRenderer;
    private final ItemModelResolver itemModelResolver;
    private final JointAnimatorDispatcher jointAnimatorDispatcher;

    public static boolean IS_RENDERING_LOCOMOTION_FIRST_PERSON = false;
    public static boolean SHOULD_FLIP_ITEM_TRANSFORM = false;
    public static InteractionHand CURRENT_ITEM_INTERACTION_HAND = InteractionHand.MAIN_HAND;
    public static float CURRENT_PARTIAL_TICKS = 0;

    public FirstPersonPlayerRenderer(EntityRendererProvider.Context context) {
        this.minecraft = Minecraft.getInstance();
        this.entityRenderDispatcher = context.getEntityRenderDispatcher();
        this.itemRenderer = minecraft.getItemRenderer();
        this.blockRenderer = context.getBlockRenderDispatcher();
        this.itemModelResolver = context.getItemModelResolver();
        this.jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
    }

    public void renderLocomotionArmWithItem(float partialTick, PoseStack poseStack, SubmitNodeCollector nodeCollector, AbstractClientPlayer player, int combinedLight, InteractionHand hand) {

        CURRENT_PARTIAL_TICKS = partialTick;
        JointAnimatorDispatcher jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();

        if (jointAnimatorDispatcher.getFirstPersonPlayerDataContainer().isEmpty()) {
            return;
        }
        if (jointAnimatorDispatcher.getInterpolatedFirstPersonPlayerPose().isEmpty()) {
            return;
        }
        // Getting the arm side
        boolean leftHanded = this.minecraft.options.mainHand().get() == HumanoidArm.LEFT;
        HumanoidArm side = hand == InteractionHand.MAIN_HAND ? HumanoidArm.RIGHT : HumanoidArm.LEFT;
        if (leftHanded) {
            side = side.getOpposite();
        }

        AnimationDataContainer dataContainer = jointAnimatorDispatcher.getFirstPersonPlayerDataContainer().get();
        ModelPartSpacePose pose = jointAnimatorDispatcher.getInterpolatedFirstPersonPlayerPose().get();

        JointChannel armPose = pose.getJointChannel(FirstPersonJointAnimator.getArmJoint(side));
        JointChannel itemPose = pose.getJointChannel(FirstPersonJointAnimator.getItemJoint(side));


        //? if >= 1.21.9 {
        AvatarRenderer<@NotNull AbstractClientPlayer> playerRenderer = this.entityRenderDispatcher.getPlayerRenderer(player);
        //?} else {
        /*PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(abstractClientPlayer);
         *///?}

        // Posing the player model
        PlayerModel playerModel = playerRenderer.getModel();
//        playerModel.resetPose();
        ModelPart armModelPart = playerModel.getArm(side);
        ((MatrixModelPart)(Object)armModelPart).locomotion$setMatrix(armPose.getTransform());

        // Rendering the arm
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(180));
        this.renderArm(player, playerModel, side, poseStack, nodeCollector, combinedLight);

        Identifier genericItemPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(hand));
        FirstPersonGenericItems.GenericItemPoseDefinition genericItemPoseDefinition = FirstPersonGenericItems.getOrThrowFromIdentifier(genericItemPoseIdentifier);
        Identifier handPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getHandPoseDriver(hand));
        FirstPersonHandPoses.HandPoseDefinition handPose = FirstPersonHandPoses.getOrThrowFromIdentifier(handPoseIdentifier);
        ItemRenderType itemRenderType = handPoseIdentifier == FirstPersonHandPoses.GENERIC_ITEM ? genericItemPoseDefinition.itemRenderType() : handPose.itemRenderType();

        ItemStack item = getItemStackInHandToRender(dataContainer, player, hand);

        this.renderItem(
                player,
                item,
                poseStack,
                itemPose,
                nodeCollector,
                combinedLight,
                side,
                hand,
                itemRenderType
        );


        poseStack.popPose();
    }

    public void render(float partialTicks, PoseStack poseStack, SubmitNodeCollector nodeCollector, LocalPlayer player, int combinedLight) {

        CURRENT_PARTIAL_TICKS = partialTicks;
        JointAnimatorDispatcher jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();

        JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(
                dataContainer -> jointAnimatorDispatcher.getInterpolatedFirstPersonPlayerPose().ifPresent(
                        animationPose -> {

                            JointChannel rightArmPose = animationPose.getJointChannel(FirstPersonJointAnimator.RIGHT_ARM_JOINT);
                            JointChannel leftArmPose = animationPose.getJointChannel(FirstPersonJointAnimator.LEFT_ARM_JOINT);
                            JointChannel rightItemPose = animationPose.getJointChannel(FirstPersonJointAnimator.RIGHT_ITEM_JOINT);
                            JointChannel leftItemPose = animationPose.getJointChannel(FirstPersonJointAnimator.LEFT_ITEM_JOINT);

                            poseStack.pushPose();
                            poseStack.mulPose(Axis.ZP.rotationDegrees(180));

//                            poseStack.mulPose(Axis.YP.rotationDegrees(-player.getViewYRot(partialTicks)));
//                            poseStack.mulPose(Axis.XP.rotationDegrees(-player.getViewXRot(partialTicks)));

                            //? if >= 1.21.9 {
                            AvatarRenderer<AbstractClientPlayer> playerRenderer = this.entityRenderDispatcher.getPlayerRenderer(player);
                            //?} else {
                            /*PlayerRenderer playerRenderer = (PlayerRenderer)this.entityRenderDispatcher.getRenderer(abstractClientPlayer);
                            *///?}

                            PlayerModel playerModel = playerRenderer.getModel();
                            playerModel.resetPose();

                            ((MatrixModelPart)(Object) playerModel.rightArm).locomotion$setMatrix(rightArmPose.getTransform());
                            ((MatrixModelPart)(Object) playerModel.leftArm).locomotion$setMatrix(leftArmPose.getTransform());

                            playerModel.body.visible = false;

                            this.renderArm(player, playerModel, HumanoidArm.LEFT, poseStack, nodeCollector, combinedLight);
                            this.renderArm(player, playerModel, HumanoidArm.RIGHT, poseStack, nodeCollector, combinedLight);

                            //this.entityRenderDispatcher.render(abstractClientPlayer, 0, 0, 0, partialTicks, poseStack, buffer, combinedLight);

                            boolean leftHanded = this.minecraft.options.mainHand().get() == HumanoidArm.LEFT;

//                            ItemStack mainHandRenderedItem = dataContainer.getDriverValue(leftHanded ? FirstPersonDrivers.RENDERED_MAIN_HAND_ITEM : FirstPersonDrivers.RENDERED_OFF_HAND_ITEM);
//                            ItemStack offHandRenderedItem = dataContainer.getDriverValue(leftHanded ? FirstPersonDrivers.RENDERED_OFF_HAND_ITEM : FirstPersonDrivers.RENDERED_MAIN_HAND_ITEM);
//                            ItemStack mainHandItem = dataContainer.getDriverValue(leftHanded ? FirstPersonDrivers.MAIN_HAND_ITEM : FirstPersonDrivers.OFF_HAND_ITEM);
//                            ItemStack offHandItem = dataContainer.getDriverValue(leftHanded ? FirstPersonDrivers.OFF_HAND_ITEM : FirstPersonDrivers.MAIN_HAND_ITEM);

                            Identifier leftHandGenericItemPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            Identifier rightHandGenericItemPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(!leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            FirstPersonGenericItems.GenericItemPoseDefinition leftHandGenericItemPoseDefinition = FirstPersonGenericItems.getOrThrowFromIdentifier(leftHandGenericItemPoseIdentifier);
                            FirstPersonGenericItems.GenericItemPoseDefinition rightHandGenericItemPoseDefinition = FirstPersonGenericItems.getOrThrowFromIdentifier(rightHandGenericItemPoseIdentifier);
                            Identifier leftHandPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getHandPoseDriver(leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            Identifier rightHandPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getHandPoseDriver(!leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            FirstPersonHandPoses.HandPoseDefinition leftHandPose = FirstPersonHandPoses.getOrThrowFromIdentifier(leftHandPoseIdentifier);
                            FirstPersonHandPoses.HandPoseDefinition rightHandPose = FirstPersonHandPoses.getOrThrowFromIdentifier(rightHandPoseIdentifier);

                            ItemRenderType leftHandItemRenderType = leftHandPoseIdentifier == FirstPersonHandPoses.GENERIC_ITEM ? leftHandGenericItemPoseDefinition.itemRenderType() : leftHandPose.itemRenderType();
                            ItemRenderType rightHandItemRenderType = rightHandPoseIdentifier == FirstPersonHandPoses.GENERIC_ITEM ? rightHandGenericItemPoseDefinition.itemRenderType() : rightHandPose.itemRenderType();

                            ItemStack mainHandItem = getItemStackInHandToRender(dataContainer, player, InteractionHand.MAIN_HAND);
                            ItemStack offHandItem = getItemStackInHandToRender(dataContainer, player, InteractionHand.OFF_HAND);

                            ItemStack rightHandItem = leftHanded ? offHandItem : mainHandItem;
                            ItemStack leftHandItem = leftHanded ? mainHandItem : offHandItem;

                            this.renderItem(
                                    player,
                                    rightHandItem,
                                    poseStack,
                                    rightItemPose,
                                    nodeCollector,
                                    combinedLight,
                                    HumanoidArm.RIGHT,
                                    !leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                                    rightHandItemRenderType
                            );
                            this.renderItem(
                                    player,
                                    leftHandItem,
                                    poseStack,
                                    leftItemPose,
                                    nodeCollector,
                                    combinedLight,
                                    HumanoidArm.LEFT,
                                    leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                                    leftHandItemRenderType
                            );


//                            if (!this.minecraft.isPaused()) {
//                                LocomotionMain.LOGGER.info(rightItemPose.getTransform().getScale(new Vector3f()));
//                            }

                            //this.renderItemInHand(abstractClientPlayer, ItemStack.EMPTY, poseStack, HumanoidArm.LEFT, animationPose, bufferSource, i);


                            //playerRenderer.renderRightHand(poseStack, bufferSource, i, abstractClientPlayer);
                            //poseStack.popPose();
                            poseStack.popPose();
                        }
                )
        );

        this.minecraft.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
        this.minecraft.renderBuffers().bufferSource().endBatch();
    }

    private static ItemStack getItemStackInHandToRender(AnimationDataContainer dataContainer, AbstractClientPlayer localPlayer, InteractionHand hand) {
        ItemStack driverRenderedItem = dataContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
        ItemStack playerItem = localPlayer.getItemInHand(hand);

        if (!ItemStack.isSameItem(playerItem, driverRenderedItem)) {
            return driverRenderedItem;
        }
        if (ItemStack.isSameItemSameComponents(playerItem, driverRenderedItem)) {
            return playerItem;
        }
        for (TypedDataComponent<?> dataComponent : playerItem.getComponents()) {
            if (dataComponent.type() == DataComponents.DAMAGE) {
                continue;
            }
            if (!driverRenderedItem.getComponents().has(dataComponent.type())) {
                return driverRenderedItem;
            }
            if (!Objects.equals(driverRenderedItem.get(dataComponent.type()), dataComponent.value())) {
                return driverRenderedItem;
            }
        }
        return playerItem;
    }

    private void renderArm(
            AbstractClientPlayer abstractClientPlayer,
            PlayerModel playerModel,
            HumanoidArm arm,
            PoseStack poseStack,
            SubmitNodeCollector nodeCollector,
            int combinedLight
    ) {

        // Skin data changed in 1.21.9
        PlayerSkin skin = abstractClientPlayer.getSkin();
        boolean isUsingSlimArms = skin.model() == PlayerModelType.SLIM;
        boolean leftArm = arm == HumanoidArm.LEFT;
        Identifier skinTextureLocation = skin.body().texturePath();

        // Getting the model parts
        ModelPart armModelPart = playerModel.getArm(arm);
        ModelPart sleeveModelPart = leftArm ? playerModel.leftSleeve : playerModel.rightSleeve;
        PlayerModelPart sleevePlayerModelPart = leftArm ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE;

        // Translating the slim arm model part over by half a pixel to be in the middle
        poseStack.pushPose();
        if (isUsingSlimArms) {
            poseStack.translate(0.5f / 16f * (leftArm ? 1 : -1), 0, 0);
        }

        // Hiding the sleeve model part based on the player's settings and then rendering both the arm and its sleeve
        sleeveModelPart.visible = abstractClientPlayer.isModelPartShown(sleevePlayerModelPart);
        nodeCollector.submitModelPart(
                armModelPart,
                poseStack,
                RenderTypes.entityTranslucent(skinTextureLocation),
                combinedLight,
                OverlayTexture.NO_OVERLAY,
                null
        );

        poseStack.popPose();
    }

    public void renderItem(
            LivingEntity entity,
            ItemStack itemStack,
            PoseStack poseStack,
            JointChannel jointChannel,
            SubmitNodeCollector nodeCollector,
            int combinedLight,
            HumanoidArm side,
            InteractionHand hand,
            ItemRenderType renderType
    ) {
        if (!itemStack.isEmpty()) {
            IS_RENDERING_LOCOMOTION_FIRST_PERSON = true;
            CURRENT_ITEM_INTERACTION_HAND = hand;

            poseStack.pushPose();
            jointChannel.transformPoseStack(poseStack, 16f);

            if (renderType.isMirrored() && side == HumanoidArm.LEFT) {
                SHOULD_FLIP_ITEM_TRANSFORM = true;
            }
            switch (renderType) {
                case MAP -> this.renderMap(nodeCollector, poseStack, itemStack, combinedLight);
                case THIRD_PERSON_ITEM, MIRRORED_THIRD_PERSON_ITEM, ON_SHELF -> {
                    //? if >= 1.21.9 {

                    ItemDisplayContext displayContext = renderType.getItemDisplayContext(side);
                    ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                    this.itemModelResolver.updateForTopItem(itemStackRenderState, itemStack, displayContext, entity.level(), entity, entity.getId() + displayContext.ordinal());
                    itemStackRenderState.submit(poseStack, nodeCollector, combinedLight, OverlayTexture.NO_OVERLAY, 0);

                    //?} else if >= 1.21.5 {
                    /*this.itemRenderer.renderStatic(entity, itemStack, displayContext, poseStack, bufferSource, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayContext.ordinal());
                     *///?} else
                    /*this.itemRenderer.renderStatic(entity, itemStackToRender, displayContext, side == HumanoidArm.LEFT, poseStack, buffer, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayContext.ordinal());*/
                }
            }
            SHOULD_FLIP_ITEM_TRANSFORM = false;
            IS_RENDERING_LOCOMOTION_FIRST_PERSON = false;
            poseStack.popPose();
        }
    }


    private static final RenderType MAP_BACKGROUND = RenderTypes.text(Identifier.withDefaultNamespace("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderTypes.text(
            Identifier.withDefaultNamespace("textures/map/map_background_checkerboard.png")
    );
    private final MapRenderState mapRenderState = new MapRenderState();

    private void renderMap(SubmitNodeCollector nodeCollector, PoseStack poseStack, ItemStack itemStack, int combinedLight) {
        MapId mapId = itemStack.get(DataComponents.MAP_ID);
        assert this.minecraft.level != null;
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(mapId, this.minecraft.level);
        RenderType renderType = mapItemSavedData == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD;

        poseStack.scale(-1, 1, -1);

        poseStack.scale(1f/16f, 1f/16f, 1f/16f);
//        poseStack.translate(2, -4, 1);
        poseStack.translate(-2, -4, -1);
        poseStack.scale(1f/8f, 1f/8f, 1f/8f);
        poseStack.scale(1/4f, 1/4f, 1/4f);
        nodeCollector.submitCustomGeometry(poseStack, renderType, (pose, vertexConsumer) -> {
            vertexConsumer.addVertex(pose, -7.0F, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(combinedLight);
            vertexConsumer.addVertex(pose, 135.0F, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(combinedLight);
            vertexConsumer.addVertex(pose, 135.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(combinedLight);
            vertexConsumer.addVertex(pose, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(combinedLight);
        });
        if (mapItemSavedData != null) {
            MapRenderer mapRenderer = this.minecraft.getMapRenderer();
            mapRenderer.extractRenderState(mapId, mapItemSavedData, this.mapRenderState);
            mapRenderer.render(this.mapRenderState, poseStack, nodeCollector, false, combinedLight);
        }
    }


    public void transformCamera(PoseStack poseStack){
        if(this.minecraft.options.getCameraType().isFirstPerson()){
            this.jointAnimatorDispatcher.getInterpolatedFirstPersonPlayerPose().ifPresent(animationPose -> {
                JointChannel cameraPose = animationPose.getJointChannel(FirstPersonJointAnimator.CAMERA_JOINT);

                Vector3f cameraRot = cameraPose.getEulerRotationZYX();
                cameraRot.z *= -1;
                cameraPose.rotate(cameraRot, JointChannel.TransformSpace.LOCAL, JointChannel.TransformType.REPLACE);
                cameraPose.translate(cameraPose.getTranslation().mul(1, 1, -1), JointChannel.TransformSpace.COMPONENT, JointChannel.TransformType.REPLACE);

                cameraPose.transformPoseStack(poseStack, 16f);
            });
        }
    }

    @Override
    public @NotNull PlayerModel getModel() {
        assert minecraft.player != null;
        return entityRenderDispatcher.getPlayerRenderer(minecraft.player).getModel();
    }
}

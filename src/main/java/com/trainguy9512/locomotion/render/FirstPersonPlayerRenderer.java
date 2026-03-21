package com.trainguy9512.locomotion.render;

import com.mojang.blaze3d.vertex.PoseStack;
//? if < 1.21.9 {
/*import com.mojang.blaze3d.vertex.VertexConsumer;*/
//?}
import com.mojang.math.Axis;
import com.trainguy9512.locomotion.access.FirstPersonSingleBlockRenderer;
import com.trainguy9512.locomotion.access.MatrixModelPart;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.*;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonGenericItems;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.handpose.FirstPersonHandPoses;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import net.minecraft.client.Minecraft;
//? if >= 1.21.9 {
import net.minecraft.client.model.player.PlayerModel;
//?} else {
import net.minecraft.client.model.PlayerModel;
//?}
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
//? if >= 1.21.9 {
import net.minecraft.client.renderer.rendertype.RenderType;
//?} else {
import net.minecraft.client.renderer.RenderType;
//?}
//? if >= 1.21.9 {
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.PlayerSkin;
//?} else {
/*import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.gui.MapRenderer;*/
//?}
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
//? if >= 1.20.5 {
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
//?}
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
//? if >= 1.21.9 {
import net.minecraft.world.entity.player.PlayerModelType;
//?}
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
//? if >= 1.20.5 {
import net.minecraft.world.level.saveddata.maps.MapId;
//?}
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;


import java.util.Objects;

//? if >= 1.21.9 {
public class FirstPersonPlayerRenderer implements RenderLayerParent<AvatarRenderState, PlayerModel> {
//?} else {
/*public class FirstPersonPlayerRenderer implements RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {*/
//?}

    private final Minecraft minecraft;
    private final EntityRenderDispatcher entityRenderDispatcher;
    private final ItemRenderer itemRenderer;
    private final BlockRenderDispatcher blockRenderer;
    //? if >= 1.21.9 {
    private final ItemModelResolver itemModelResolver;
    //?}
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
        //? if >= 1.21.9 {
        this.itemModelResolver = context.getItemModelResolver();
        //?}
        this.jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
    }

    //? if < 1.21.9 {
    /*public FirstPersonPlayerRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.entityRenderDispatcher = minecraft.getEntityRenderDispatcher();
        this.itemRenderer = minecraft.getItemRenderer();
        this.blockRenderer = minecraft.getBlockRenderer();
        this.jointAnimatorDispatcher = JointAnimatorDispatcher.getInstance();
    }*/
    //?}

    //? if >= 1.21.9 {
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
                            resetPlayerModelPose(playerModel);

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
                            playerModel.body.visible = true;
                            clearArmMatrices(playerModel);
                        }
                )
        );

        this.minecraft.gameRenderer.getFeatureRenderDispatcher().renderAllFeatures();
        this.minecraft.renderBuffers().bufferSource().endBatch();
    }
    //?} else {
    /*public void render(float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, LocalPlayer player, int combinedLight) {

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

                            PlayerRenderer playerRenderer = (PlayerRenderer) this.entityRenderDispatcher.getRenderer(player);

                            PlayerModel<AbstractClientPlayer> playerModel = playerRenderer.getModel();
                            resetPlayerModelPose(playerModel);

                            ((MatrixModelPart)(Object) playerModel.rightArm).locomotion$setMatrix(rightArmPose.getTransform());
                            ((MatrixModelPart)(Object) playerModel.leftArm).locomotion$setMatrix(leftArmPose.getTransform());

                            playerModel.body.visible = false;

                            this.renderArm(player, playerModel, HumanoidArm.LEFT, poseStack, bufferSource, combinedLight);
                            this.renderArm(player, playerModel, HumanoidArm.RIGHT, poseStack, bufferSource, combinedLight);

                            boolean leftHanded = this.minecraft.options.mainHand().get() == HumanoidArm.LEFT;

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
                                    bufferSource,
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
                                    bufferSource,
                                    combinedLight,
                                    HumanoidArm.LEFT,
                                    leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
                                    leftHandItemRenderType
                            );

                            poseStack.popPose();
                            playerModel.body.visible = true;
                            clearArmMatrices(playerModel);
                        }
                )
        );
    }*/
    //?}

    private static ItemStack getItemStackInHandToRender(AnimationDataContainer dataContainer, LocalPlayer localPlayer, InteractionHand hand) {
        ItemStack driverItem = dataContainer.getDriverValue(FirstPersonDrivers.getItemDriver(hand));
        ItemStack driverRenderedItem = dataContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(hand));
        ItemStack playerItem = localPlayer.getItemInHand(hand);

        if (!ItemStack.isSameItem(playerItem, driverRenderedItem)) {
            return driverRenderedItem;
        }
        //? if >= 1.20.5 {
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
        //?} else {
        /*return ItemStack.isSameItemSameTags(playerItem, driverRenderedItem) ? playerItem : driverRenderedItem;*/
        //?}
    }

    //? if >= 1.21.9 {
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
        ModelPart armModelPart = leftArm ? playerModel.leftArm : playerModel.rightArm;
        ModelPart sleeveModelPart = leftArm ? playerModel.leftSleeve : playerModel.rightSleeve;
        PlayerModelPart sleevePlayerModelPart = leftArm ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE;

        // Translating the slim arm model part over by half a pixel to be in the middle
        poseStack.pushPose();
        if (isUsingSlimArms) {
            poseStack.translate(0.5f / 16f * (leftArm ? 1 : -1), 0, 0);
        }

        // Hiding the sleeve model part based on the player's settings and then rendering both the arm and its sleeve
        sleeveModelPart.visible = abstractClientPlayer.isModelPartShown(sleevePlayerModelPart);
        Matrix4f armMatrix = ((MatrixModelPart)(Object) armModelPart).locomotion$getMatrix();
        if (armMatrix != null) {
            ((MatrixModelPart)(Object) sleeveModelPart).locomotion$setMatrix(new Matrix4f(armMatrix));
        }
        nodeCollector.submitModelPart(
                armModelPart,
                poseStack,
                RenderTypes.entityTranslucent(skinTextureLocation),
                combinedLight,
                OverlayTexture.NO_OVERLAY,
                null
        );
        if (sleeveModelPart.visible) {
            nodeCollector.submitModelPart(
                    sleeveModelPart,
                    poseStack,
                    RenderTypes.entityTranslucent(skinTextureLocation),
                    combinedLight,
                    OverlayTexture.NO_OVERLAY,
                    null
            );
        }

        poseStack.popPose();
    }
    //?} else {
    /*private void renderArm(
            AbstractClientPlayer abstractClientPlayer,
            PlayerModel<AbstractClientPlayer> playerModel,
            HumanoidArm arm,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int combinedLight
    ) {

        boolean isUsingSlimArms = "slim".equals(abstractClientPlayer.getModelName());
        boolean leftArm = arm == HumanoidArm.LEFT;
        Identifier skinTextureLocation;
        //? if >= 1.21.0 {
        skinTextureLocation = abstractClientPlayer.getSkinTextureLocation();
        //?} else {
        skinTextureLocation = new Identifier(abstractClientPlayer.getSkinTextureLocation().getNamespace(), abstractClientPlayer.getSkinTextureLocation().getPath());
        //?}

        // Getting the model parts
        ModelPart armModelPart = leftArm ? playerModel.leftArm : playerModel.rightArm;
        ModelPart sleeveModelPart = leftArm ? playerModel.leftSleeve : playerModel.rightSleeve;
        PlayerModelPart sleevePlayerModelPart = leftArm ? PlayerModelPart.LEFT_SLEEVE : PlayerModelPart.RIGHT_SLEEVE;

        // Translating the slim arm model part over by half a pixel to be in the middle
        poseStack.pushPose();
        if (isUsingSlimArms) {
            poseStack.translate(0.5f / 16f * (leftArm ? 1 : -1), 0, 0);
        }

        // Hiding the sleeve model part based on the player's settings and then rendering both the arm and its sleeve
        sleeveModelPart.visible = abstractClientPlayer.isModelPartShown(sleevePlayerModelPart);
        Matrix4f armMatrix = ((MatrixModelPart)(Object) armModelPart).locomotion$getMatrix();
        if (armMatrix != null) {
            ((MatrixModelPart)(Object) sleeveModelPart).locomotion$setMatrix(new Matrix4f(armMatrix));
        }
        RenderType renderType = RenderType.entityTranslucent(skinTextureLocation);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(renderType);
        armModelPart.render(poseStack, vertexConsumer, combinedLight, OverlayTexture.NO_OVERLAY);
        if (sleeveModelPart.visible) {
            sleeveModelPart.render(poseStack, vertexConsumer, combinedLight, OverlayTexture.NO_OVERLAY);
        }

        poseStack.popPose();
    }*/
    //?}

    //? if >= 1.21.9 {
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
                case THIRD_PERSON_ITEM, MIRRORED_THIRD_PERSON_ITEM, FIXED -> {
                    ItemDisplayContext displayContext = renderType.getItemDisplayContext(side);
                    ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                    this.itemModelResolver.updateForTopItem(itemStackRenderState, itemStack, displayContext, entity.level(), entity, entity.getId() + displayContext.ordinal());
                    itemStackRenderState.submit(poseStack, nodeCollector, combinedLight, OverlayTexture.NO_OVERLAY, 0);
                }
                case BLOCK_STATE -> {
                    FirstPersonBlockItemRenderer.submit(
                            itemStack,
                            poseStack,
                            nodeCollector,
                            combinedLight,
                            side
                    );
                }
            }
            SHOULD_FLIP_ITEM_TRANSFORM = false;
            IS_RENDERING_LOCOMOTION_FIRST_PERSON = false;
            poseStack.popPose();
        }
    }
    //?} else {
    /*public void renderItem(
            LivingEntity entity,
            ItemStack itemStack,
            PoseStack poseStack,
            JointChannel jointChannel,
            MultiBufferSource bufferSource,
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
                case MAP -> this.renderMap(bufferSource, poseStack, itemStack, combinedLight);
                case THIRD_PERSON_ITEM, MIRRORED_THIRD_PERSON_ITEM, FIXED -> {
                    if (renderType == ItemRenderType.FIXED && side == HumanoidArm.LEFT) {
                        poseStack.mulPose(Axis.ZP.rotationDegrees(180f));
                        poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
                    }
                    ItemDisplayContext displayContext = renderType.getItemDisplayContext(side);
                    //? if >= 1.21.5 {
                    this.itemRenderer.renderStatic(entity, itemStack, displayContext, poseStack, bufferSource, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayContext.ordinal());
                    //?} else {
                    boolean leftHanded = displayContext == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                            || displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
                    this.itemRenderer.renderStatic(entity, itemStack, displayContext, leftHanded, poseStack, bufferSource, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayContext.ordinal());
                    //?}
                }
                case BLOCK_STATE -> {
                    FirstPersonBlockItemRenderer.submit(
                            itemStack,
                            poseStack,
                            bufferSource,
                            combinedLight,
                            side
                    );
                }
            }
            SHOULD_FLIP_ITEM_TRANSFORM = false;
            IS_RENDERING_LOCOMOTION_FIRST_PERSON = false;
            poseStack.popPose();
        }
    }*/
    //?}

    //? if >= 1.21.9 {
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
    //?} else {
    /*private static final RenderType MAP_BACKGROUND = RenderType.text(Identifier.withDefaultNamespace("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(
            Identifier.withDefaultNamespace("textures/map/map_background_checkerboard.png")
    );

    private void renderMap(MultiBufferSource bufferSource, PoseStack poseStack, ItemStack itemStack, int combinedLight) {
        assert this.minecraft.level != null;
        MapItemSavedData mapItemSavedData = MapItem.getSavedData(itemStack, this.minecraft.level);
        if (mapItemSavedData == null) {
            return;
        }

        Integer mapId = MapItem.getMapId(itemStack);
        if (mapId == null) {
            return;
        }

        poseStack.scale(-1, 1, -1);
        poseStack.scale(1f/16f, 1f/16f, 1f/16f);
        poseStack.translate(-2, -4, -1);
        poseStack.scale(1f/8f, 1f/8f, 1f/8f);
        poseStack.scale(1/4f, 1/4f, 1/4f);

        MapRenderer mapRenderer = this.minecraft.gameRenderer.getMapRenderer();
        mapRenderer.render(poseStack, bufferSource, mapId, mapItemSavedData, false, combinedLight);
    }*/
    //?}

    //? if >= 1.21.9 {
    private void renderBlock(SubmitNodeCollector nodeCollector, PoseStack poseStack, BlockState blockState, int combinedLight) {

        // Render the block through the special block renderer if it has one (skulls, beds, banners)

        ((FirstPersonSingleBlockRenderer) Minecraft.getInstance().getBlockRenderer()).locomotion$submitSingleBlockWithEmission(blockState, poseStack, nodeCollector, combinedLight);
//        nodeCollector.submitBlockModel(
//                poseStack,
//                ItemBlockRenderTypes.getRenderType(blockState),
//                this.blockRenderer.getBlockModel(blockState),
//                1.0F,
//                1.0F,
//                1.0F,
//                combinedLight,
//                OverlayTexture.NO_OVERLAY,
//                0
//        );
//        nodeCollector.submitBlockModel(poseStack, RenderType.entitySolidZOffsetForward(TextureAtlas.LOCATION_BLOCKS), this.blockRenderer.getBlockModel(blockState), );
//        nodeCollector.submitBlock(poseStack, blockState, combinedLight, OverlayTexture.NO_OVERLAY, 0);
    }
    //?} else {
    /*private void renderBlock(MultiBufferSource bufferSource, PoseStack poseStack, BlockState blockState, int combinedLight) {
        this.blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, combinedLight, OverlayTexture.NO_OVERLAY);
    }*/
    //?}

    private BlockState getDefaultBlockState(Block block) {
        BlockState blockState = block.defaultBlockState();
        blockState = blockState.trySetValue(BlockStateProperties.ROTATION_16, 8);
        blockState = blockState.trySetValue(BlockStateProperties.ATTACH_FACE, AttachFace.FLOOR);
        blockState = blockState.trySetValue(BlockStateProperties.DOWN, true);
        if (block instanceof StairBlock) {
            blockState = blockState.trySetValue(BlockStateProperties.FACING, Direction.NORTH);
            blockState = blockState.trySetValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        } else {
            blockState = blockState.trySetValue(BlockStateProperties.FACING, Direction.SOUTH);
            blockState = blockState.trySetValue(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH);
        }
        return blockState;
    }

    private void renderBedBlock(
            BlockState blockState,
            PoseStack poseStack,
            MultiBufferSource multiVersionRenderData,
            int combinedLight
    ) {
        poseStack.translate(1f, -0.25f, 0.5f);
        poseStack.mulPose(Axis.YP.rotation(Mth.PI));
//        this.blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, combinedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderDoorBlock(
            BlockState blockState,
            PoseStack poseStack,
            MultiBufferSource multiVersionRenderData,
            int combinedLight
    ) {
//        this.blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, combinedLight, OverlayTexture.NO_OVERLAY);
//        this.renderUpperHalfBlock(blockState, poseStack, bufferSource, combinedLight);
    }

    private void renderFenceBlock(
            BlockState blockState,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int combinedLight
    ) {
        blockState = blockState.setValue(BlockStateProperties.EAST, true);
        blockState = blockState.setValue(BlockStateProperties.WEST, true);
        this.blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, combinedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderWallBlock(
            BlockState blockState,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int combinedLight
    ) {
         blockState = blockState.setValue(BlockStateProperties.EAST_WALL, WallSide.LOW);
         blockState = blockState.setValue(BlockStateProperties.WEST_WALL, WallSide.LOW);
         this.blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, combinedLight, OverlayTexture.NO_OVERLAY);
    }

    private void renderUpperHalfBlock(
            BlockState blockState,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int combinedLight
    ) {
        poseStack.translate(0, 1, 0);
        blockState = blockState.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
        this.blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, combinedLight, OverlayTexture.NO_OVERLAY);
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
        //? if >= 1.21.9 {
        return entityRenderDispatcher.getPlayerRenderer(minecraft.player).getModel();
        //?} else {
        /*return ((PlayerRenderer) entityRenderDispatcher.getRenderer(minecraft.player)).getModel();*/
        //?}
    }

    private static void clearArmMatrices(PlayerModel playerModel) {
        ((MatrixModelPart)(Object) playerModel.rightArm).locomotion$setMatrix(null);
        ((MatrixModelPart)(Object) playerModel.leftArm).locomotion$setMatrix(null);
        ((MatrixModelPart)(Object) playerModel.rightSleeve).locomotion$setMatrix(null);
        ((MatrixModelPart)(Object) playerModel.leftSleeve).locomotion$setMatrix(null);
    }

    //? if < 1.21.9 {
    /*@Override
    public Identifier getTextureLocation(AbstractClientPlayer player) {
        //? if >= 1.21.0 {
        return player.getSkinTextureLocation();
        //?} else {
        return new Identifier(player.getSkinTextureLocation().getNamespace(), player.getSkinTextureLocation().getPath());
        //?}
    }
    *///?}

    private static void resetPlayerModelPose(PlayerModel playerModel) {
        //? if >= 1.21.0 {
        playerModel.resetPose();
        //?} else {
        /*playerModel.head.resetPose();
        playerModel.hat.resetPose();
        playerModel.body.resetPose();
        playerModel.rightArm.resetPose();
        playerModel.leftArm.resetPose();
        playerModel.rightLeg.resetPose();
        playerModel.leftLeg.resetPose();
        playerModel.leftSleeve.resetPose();
        playerModel.rightSleeve.resetPose();
        playerModel.leftPants.resetPose();
        playerModel.rightPants.resetPose();
        playerModel.jacket.resetPose();*/
        //?}
    }

//    private enum ItemRenderType {
//        THIRD_PERSON_ITEM,
//        DEFAULT_BLOCK_STATE,
//        MAP;
//
//        public static ItemRenderType fromItemStack(ItemStack itemStack, FirstPersonHandPose handPose, FirstPersonGenericItemPose genericItemPose) {
//            Item item = itemStack.getItem();
//            if (handPose == FirstPersonHandPose.MAP) {
//                return MAP;
//            }
//            if (genericItemPose.shouldRenderBlockstate() && item instanceof BlockItem && handPose == FirstPersonHandPose.GENERIC_ITEM) {
//                return DEFAULT_BLOCK_STATE;
//            }
//            return THIRD_PERSON_ITEM;
//        }
//    }
}

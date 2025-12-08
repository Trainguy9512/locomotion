package com.trainguy9512.locomotion.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.access.FirstPersonSingleBlockRenderer;
import com.trainguy9512.locomotion.access.MatrixModelPart;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.*;
import com.trainguy9512.locomotion.animation.data.AnimationDataContainer;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.TypedDataComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.*;
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

                            ResourceLocation leftHandGenericItemPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            ResourceLocation rightHandGenericItemPoseIdentifier = dataContainer.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(!leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            FirstPersonGenericItems.GenericItemPoseDefinition leftHandGenericItemPoseDefinition = FirstPersonGenericItems.getOrThrowFromIdentifier(leftHandGenericItemPoseIdentifier);
                            FirstPersonGenericItems.GenericItemPoseDefinition rightHandGenericItemPoseDefinition = FirstPersonGenericItems.getOrThrowFromIdentifier(rightHandGenericItemPoseIdentifier);
                            FirstPersonHandPose leftHandPose = dataContainer.getDriverValue(FirstPersonDrivers.getHandPoseDriver(leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            FirstPersonHandPose rightHandPose = dataContainer.getDriverValue(FirstPersonDrivers.getHandPoseDriver(!leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));

                            ItemRenderType leftHandItemRenderType = leftHandPose == FirstPersonHandPose.GENERIC_ITEM ? leftHandGenericItemPoseDefinition.itemRenderType() : leftHandPose.itemRenderType;
                            ItemRenderType rightHandItemRenderType = rightHandPose == FirstPersonHandPose.GENERIC_ITEM ? rightHandGenericItemPoseDefinition.itemRenderType() : rightHandPose.itemRenderType;

                            ItemStack mainHandItem = getItemStackInHandToRender(dataContainer, player, InteractionHand.MAIN_HAND);
                            ItemStack offHandItem = getItemStackInHandToRender(dataContainer, player, InteractionHand.OFF_HAND);

                            ItemStack rightHandItem = leftHanded ? offHandItem : mainHandItem;
                            ItemStack leftHandItem = leftHanded ? mainHandItem : offHandItem;

                            this.renderItem(
                                    player,
                                    rightHandItem,
                                    ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
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
                                    ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
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

    private static ItemStack getItemStackInHandToRender(AnimationDataContainer dataContainer, LocalPlayer localPlayer, InteractionHand interactionHand) {
        ItemStack driverItem = dataContainer.getDriverValue(FirstPersonDrivers.getItemDriver(interactionHand));
        ItemStack driverRenderedItem = dataContainer.getDriverValue(FirstPersonDrivers.getRenderedItemDriver(interactionHand));
        ItemStack playerItem = localPlayer.getItemInHand(interactionHand);

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
        ResourceLocation skinTextureLocation = skin.body().texturePath();

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
        nodeCollector.submitModelPart(
                armModelPart,
                poseStack,
                RenderType.entityTranslucent(skinTextureLocation),
                combinedLight,
                OverlayTexture.NO_OVERLAY,
                null
        );

        poseStack.popPose();
    }

    public void renderItem(
            LivingEntity entity,
            ItemStack itemStack,
            ItemDisplayContext displayContext,
            PoseStack poseStack,
            JointChannel jointChannel,
            SubmitNodeCollector nodeCollector,
            int combinedLight,
            HumanoidArm side,
            InteractionHand interactionHand,
            ItemRenderType renderType
    ) {
        if (!itemStack.isEmpty()) {
            IS_RENDERING_LOCOMOTION_FIRST_PERSON = true;
            CURRENT_ITEM_INTERACTION_HAND = interactionHand;

            poseStack.pushPose();
            jointChannel.transformPoseStack(poseStack, 16f);

            if (renderType.isMirrored() && side == HumanoidArm.LEFT) {
                SHOULD_FLIP_ITEM_TRANSFORM = true;
            }
            switch (renderType) {
                case MAP -> this.renderMap(nodeCollector, poseStack, itemStack, combinedLight);
                case THIRD_PERSON_ITEM, MIRRORED_THIRD_PERSON_ITEM -> {
                    //? if >= 1.21.9 {

                    ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                    this.itemModelResolver.updateForTopItem(itemStackRenderState, itemStack, displayContext, entity.level(), entity, entity.getId() + displayContext.ordinal());
                    itemStackRenderState.submit(poseStack, nodeCollector, combinedLight, OverlayTexture.NO_OVERLAY, 0);

                    //?} else if >= 1.21.5 {
                    /*this.itemRenderer.renderStatic(entity, itemStack, displayContext, poseStack, bufferSource, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayContext.ordinal());
                    *///?} else
                    /*this.itemRenderer.renderStatic(entity, itemStackToRender, displayContext, side == HumanoidArm.LEFT, poseStack, buffer, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayContext.ordinal());*/
                }
                case SINGLE_BLOCK_STATE -> {
                    Block block = ((BlockItem)itemStack.getItem()).getBlock();
                    BlockState blockState = this.getDefaultBlockState(block);

                    if (side == HumanoidArm.LEFT) {
                        poseStack.translate(-1, 0, 0);
                    }
                    if (block instanceof FenceGateBlock || block instanceof ConduitBlock) {
                        poseStack.translate(0, -0.4f, 0);
                    }
                    if (block instanceof SporeBlossomBlock) {
                        poseStack.translate(0, 1, 1);
                        poseStack.mulPose(Axis.XP.rotation(Mth.PI));
                    }

                    this.renderBlock(nodeCollector, poseStack, blockState, combinedLight);
//                    if (block instanceof WallBlock) {
//                        this.renderWallBlock(blockState, poseStack, nodeCollector, combinedLight);
//                    } else if (block instanceof FenceBlock) {
//                        this.renderFenceBlock(blockState, poseStack, nodeCollector, combinedLight);
//                    } else if (block instanceof BedBlock) {
//                        this.renderBedBlock(blockState, poseStack, nodeCollector, combinedLight);
//                    } else if (block instanceof DoorBlock) {
//                        this.renderDoorBlock(blockState, poseStack, nodeCollector, combinedLight);
//                    } else {

//                        ((AlternateSingleBlockRenderer)(this.blockRenderer)).locomotion$renderSingleBlockWithEmission(blockState, poseStack, nodeCollector, combinedLight);
//                        this.blockRenderer.renderSingleBlock(blockState, poseStack, bufferSource, combinedLight, OverlayTexture.NO_OVERLAY);
//                    }
                }
            }
            SHOULD_FLIP_ITEM_TRANSFORM = false;
            IS_RENDERING_LOCOMOTION_FIRST_PERSON = false;
            poseStack.popPose();
        }
    }

    private static final RenderType MAP_BACKGROUND = RenderType.text(ResourceLocation.withDefaultNamespace("textures/map/map_background.png"));
    private static final RenderType MAP_BACKGROUND_CHECKERBOARD = RenderType.text(
            ResourceLocation.withDefaultNamespace("textures/map/map_background_checkerboard.png")
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

    private void renderBlock(SubmitNodeCollector nodeCollector, PoseStack poseStack, BlockState blockState, int combinedLight) {

        // Render the block through the special block renderer if it has one (skulls, beds, banners)

        ((FirstPersonSingleBlockRenderer) Minecraft.getInstance().getBlockRenderer()).locomotion$renderSingleBlockWithEmission(blockState, poseStack, nodeCollector, combinedLight);
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
        return entityRenderDispatcher.getPlayerRenderer(minecraft.player).getModel();
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

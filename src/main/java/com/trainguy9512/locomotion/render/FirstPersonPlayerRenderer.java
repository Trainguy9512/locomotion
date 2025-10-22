package com.trainguy9512.locomotion.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.trainguy9512.locomotion.access.MatrixModelPart;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonDrivers;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonGenericItemPose;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonHandPose;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonJointAnimator;
import com.trainguy9512.locomotion.animation.joint.JointChannel;
import com.trainguy9512.locomotion.access.AlternateSingleBlockRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
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
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.client.renderer.SubmitNodeCollector;


import java.util.List;
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

                            ItemStack leftHandRenderedItem = dataContainer.getDriverValue(leftHanded ? FirstPersonDrivers.RENDERED_MAIN_HAND_ITEM : FirstPersonDrivers.RENDERED_OFF_HAND_ITEM);
                            ItemStack rightHandRenderedItem = dataContainer.getDriverValue(leftHanded ? FirstPersonDrivers.RENDERED_OFF_HAND_ITEM : FirstPersonDrivers.RENDERED_MAIN_HAND_ITEM);
                            ItemStack leftHandItem = dataContainer.getDriverValue(leftHanded ? FirstPersonDrivers.MAIN_HAND_ITEM : FirstPersonDrivers.OFF_HAND_ITEM);
                            ItemStack rightHandItem = dataContainer.getDriverValue(leftHanded ? FirstPersonDrivers.OFF_HAND_ITEM : FirstPersonDrivers.MAIN_HAND_ITEM);

                            FirstPersonGenericItemPose leftHandGenericItemPose = dataContainer.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            FirstPersonGenericItemPose rightHandGenericItemPose = dataContainer.getDriverValue(FirstPersonDrivers.getGenericItemPoseDriver(!leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            FirstPersonHandPose leftHandPose = dataContainer.getDriverValue(FirstPersonDrivers.getHandPoseDriver(leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
                            FirstPersonHandPose rightHandPose = dataContainer.getDriverValue(FirstPersonDrivers.getHandPoseDriver(!leftHanded ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));

                            leftHandItem = getItemStackToRender(leftHandItem, leftHandRenderedItem);
                            rightHandItem = getItemStackToRender(rightHandItem, rightHandRenderedItem);

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
                                    rightHandPose,
                                    rightHandGenericItemPose
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
                                    leftHandPose,
                                    leftHandGenericItemPose
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

    private static ItemStack getItemStackToRender(ItemStack currentItem, ItemStack renderedItem) {
        if (!ItemStack.isSameItem(currentItem, renderedItem)) {
            return renderedItem;
        }
        if (ItemStack.isSameItemSameComponents(currentItem, renderedItem)) {
            return currentItem;
        }
        for (TypedDataComponent<?> dataComponent : currentItem.getComponents()) {
            if (dataComponent.type() == DataComponents.DAMAGE) {
                continue;
            }
            if (!renderedItem.getComponents().has(dataComponent.type())) {
                return renderedItem;
            }
            if (!Objects.equals(renderedItem.get(dataComponent.type()), dataComponent.value())) {
                return renderedItem;
            }
        }
        return currentItem;
    }

    private void renderArm(AbstractClientPlayer abstractClientPlayer, PlayerModel playerModel, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector nodeCollector, int combinedLight) {

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
            FirstPersonHandPose handPose,
            FirstPersonGenericItemPose genericItemPose
    ) {
        if (!itemStack.isEmpty()) {
            IS_RENDERING_LOCOMOTION_FIRST_PERSON = true;
            CURRENT_ITEM_INTERACTION_HAND = interactionHand;
            ItemRenderType renderType = ItemRenderType.fromItemStack(itemStack, handPose, genericItemPose);

            poseStack.pushPose();
            jointChannel.transformPoseStack(poseStack, 16f);

            if (genericItemPose.shouldMirrorItemModel(handPose, side)) {
                SHOULD_FLIP_ITEM_TRANSFORM = true;
            }
            switch (renderType) {
                case THIRD_PERSON_ITEM -> {
                    //? if >= 1.21.9 {

                    ItemStackRenderState itemStackRenderState = new ItemStackRenderState();
                    this.itemModelResolver.updateForTopItem(itemStackRenderState, itemStack, displayContext, entity.level(), entity, entity.getId() + displayContext.ordinal());
                    itemStackRenderState.submit(poseStack, nodeCollector, combinedLight, OverlayTexture.NO_OVERLAY, 0);

                    //?} else if >= 1.21.5 {
                    /*this.itemRenderer.renderStatic(entity, itemStack, displayContext, poseStack, bufferSource, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayContext.ordinal());
                    *///?} else
                    /*this.itemRenderer.renderStatic(entity, itemStackToRender, displayContext, side == HumanoidArm.LEFT, poseStack, buffer, entity.level(), combinedLight, OverlayTexture.NO_OVERLAY, entity.getId() + displayContext.ordinal());*/
                }
                case DEFAULT_BLOCK_STATE -> {
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

    private void renderBlock(SubmitNodeCollector nodeCollector, PoseStack poseStack, BlockState blockState, int combinedLight) {

        // Render the block through the special block renderer if it has one (skulls, beds, banners)

        nodeCollector.submitBlockModel(
                poseStack,
                ItemBlockRenderTypes.getRenderType(blockState),
                this.blockRenderer.getBlockModel(blockState),
                1.0F,
                1.0F,
                1.0F,
                combinedLight,
                OverlayTexture.NO_OVERLAY,
                0
        );
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

                cameraPose.transformPoseStack(poseStack, 16f);
                //poseStack.mulPose(cameraPose.getTransform().setTranslation(cameraPose.getTransform().getTranslation(new Vector3f().div(16f))));
            });
        }
    }

    @Override
    public @NotNull PlayerModel getModel() {
        assert minecraft.player != null;
        return entityRenderDispatcher.getPlayerRenderer(minecraft.player).getModel();
    }

    private enum ItemRenderType {
        THIRD_PERSON_ITEM,
        DEFAULT_BLOCK_STATE;

        public static ItemRenderType fromItemStack(ItemStack itemStack, FirstPersonHandPose handPose, FirstPersonGenericItemPose genericItemPose) {
            Item item = itemStack.getItem();
            if (genericItemPose.rendersBlockState && itemStack.getItem() instanceof BlockItem && handPose == FirstPersonHandPose.GENERIC_ITEM) {
                return DEFAULT_BLOCK_STATE;
            }
            return THIRD_PERSON_ITEM;
        }
    }
}

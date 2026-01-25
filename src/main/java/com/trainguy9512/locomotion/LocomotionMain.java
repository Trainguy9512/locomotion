package com.trainguy9512.locomotion;

import com.trainguy9512.locomotion.animation.animator.JointAnimatorRegistry;
import com.trainguy9512.locomotion.animation.animator.block_entity.ChestJointAnimator;
import com.trainguy9512.locomotion.animation.animator.entity.firstperson.FirstPersonJointAnimator;
import com.trainguy9512.locomotion.config.LocomotionConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LocomotionMain {

	public static final Logger DEBUG_LOGGER = LogManager.getLogger("Locomotion/DEBUG");
	public static final String MOD_ID = "locomotion";
	public static final LocomotionConfig CONFIG = new LocomotionConfig();

	public static void initialize() {
		CONFIG.load();

		registerAnimators();

	}

	public static Identifier makeIdentifier(String location) {
		return Identifier.fromNamespaceAndPath(MOD_ID, location);
	}

	/*
	public static void onClientPostInit(Platform.ModSetupContext ctx) {
	}

	public static void onCommonInit() {
	}

	public static void onCommonPostInit(P.ModSetupContext ctx) {
	}

	 */


	private static void registerAnimators() {
		JointAnimatorRegistry.registerFirstPersonPlayerJointAnimator(new FirstPersonJointAnimator());
		JointAnimatorRegistry.registerBlockEntityJointAnimator(BlockEntityType.CHEST, new ChestJointAnimator());
		JointAnimatorRegistry.registerBlockEntityJointAnimator(BlockEntityType.ENDER_CHEST, new ChestJointAnimator());
	}

	/*
	private static void registerBlockRenderers(){
		registerBlockRenderer(new PressurePlateBlockRenderer(), PressurePlateBlockRenderer.PRESSURE_PLATES);
		registerBlockRenderer(new ButtonBlockRenderer(), ButtonBlockRenderer.BUTTONS);
		registerBlockRenderer(new TrapDoorBlockRenderer(), TrapDoorBlockRenderer.TRAPDOORS);
		registerBlockRenderer(new LeverBlockRenderer(), LeverBlockRenderer.LEVERS);
		registerBlockRenderer(new EndPortalFrameBlockRenderer(), EndPortalFrameBlockRenderer.END_PORTAL_BLOCKS);
		registerBlockRenderer(new ChainedBlockRenderer(), ChainedBlockRenderer.CHAINED_BLOCKS);
		//registerBlockRenderer(new FloatingPlantBlockRenderer(), FloatingPlantBlockRenderer.FLOATING_PLANTS);
	}
	 */


	/*
	private static void registerBlockRenderer(TickableBlockRenderer tickableBlockRenderer, Block[] blocks){
		for(Block block : blocks){
			BlockRendererRegistry.register(block, tickableBlockRenderer);
		}
	}
	 */

}
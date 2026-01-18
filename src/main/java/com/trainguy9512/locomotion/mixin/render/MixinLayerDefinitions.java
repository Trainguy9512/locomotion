package com.trainguy9512.locomotion.mixin.render;

import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.resource.json.GsonConfiguration;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.*;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Mixin(LayerDefinitions.class)
public class MixinLayerDefinitions {

    @Inject(
            method = "createRoots",
            at = @At(value = "RETURN")
    )
    private static void getCreatedModels(CallbackInfoReturnable<Map<ModelLayerLocation, LayerDefinition>> cir) {
        Logger logger = LocomotionMain.DEBUG_LOGGER;

        if (true) {
            return;
        }

        Map<ModelLayerLocation, LayerDefinition> models = cir.getReturnValue();
//        logger.info(modelJson);
        String filePath = new JFileChooser().getFileSystemView().getDefaultDirectory().toString();
        for (ModelLayerLocation location : models.keySet()) {
            LayerDefinition layerDefinition = models.get(location);

            try {
                GsonConfiguration.getInstance().toJson(layerDefinition, new FileWriter(filePath + "/models/" + location.model().toDebugFileName() + "_" + location.layer() + ".json"));
            } catch (IOException e) {
                logger.error(e.getMessage());
            }
        }
//        for (ModelLayerLocation location : models.keySet()) {
//            logger.info("--------");
//            logger.info(location.model());
//
//            LayerDefinition layerDefinition = models.get(location);
//            layerDefinition.apply(MeshTransformer.IDENTITY);
//            MeshDefinition meshDefinition = MeshGeneratorUtils.LAST_VISITED_MESH_DEFINITION;
//            PartDefinition root = meshDefinition.getRoot();
//
//            ModelPart bakedModelPart = root.bake(64, 64);
//            String modelPartJson = GsonConfiguration.getInstance().toJson(bakedModelPart);
//            logger.info(modelPartJson);
//            logger.info("--------");
//        }
    }
}

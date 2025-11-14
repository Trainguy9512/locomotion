package com.trainguy9512.locomotion.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.trainguy9512.locomotion.LocomotionMain;
import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AlertScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;

public class LocomotionConfig {

    private static final Logger LOGGER = LogManager.getLogger("Locomotion/Config");

    //private static final Path CONFIG_FILE_PATH = FabricLoader.getInstance().getConfigDir().resolve(LocomotionMain.MOD_ID + ".json");
    private static final Path CONFIG_FILE_PATH = Path.of("config").resolve( LocomotionMain.MOD_ID + ".json");

    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    private Data configData;

    public LocomotionConfig() {
        this.configData = new Data();
    }

    public Data data() {
        return configData;
    }

    public void load() {
        if (Files.exists(CONFIG_FILE_PATH)) {
            try (FileReader reader = new FileReader(CONFIG_FILE_PATH.toFile())) {
                this.configData = GSON.fromJson(reader, LocomotionConfig.Data.class);
            } catch (IOException e) {
                throw new RuntimeException("Failed to load locomotion config file", e);
            }
        } else {
            configData = new LocomotionConfig.Data();
        }
        save();

    }

    public void save() {
        try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE_PATH)) {
            writer.write(GSON.toJson(this.configData));
//            LocomotionMain.LOGGER.info("Saved config to path {}", CONFIG_FILE_PATH);
        } catch (Exception e) {
            LOGGER.error("Failed to write config to path {}", CONFIG_FILE_PATH.toAbsolutePath());
        }
        JointAnimatorDispatcher.getInstance().reInitializeData();
    }


    public static class Data {

        public final FirstPersonPlayer firstPersonPlayer = new FirstPersonPlayer();

        public static class FirstPersonPlayer {
            public boolean enableRenderer = true;

            public boolean enableCameraRotationDamping = true;
            public float cameraRotationStiffnessFactor = 0.3f;
            public float cameraRotationDampingFactor = 0.65f;

            public float cameraShakeMasterIntensity = 1f;
            public float cameraShakeMovementIntensity = 1f;
            public float cameraShakeItemInteractionIntensity = 0.5f;

            public float miningAnimationSpeedMultiplier = 1f;
            public float mapMovementAnimationIntensity = 0.2f;
        }
    }

    public Function<Screen, Screen> getConfigScreen(Predicate<String> ifModLoaded) {

        if (ifModLoaded.test("yet_another_config_lib_v3")) {
            return LocomotionConfigScreen::createConfigScreen;
        } else {
            return parent -> new AlertScreen(
                    () -> Minecraft.getInstance().setScreen(parent),
                    Component.translatable("locomotion.config.yacl_not_found.header"),
                    Component.translatable("locomotion.config.yacl_not_found.description"),
                    Component.translatable("locomotion.config.yacl_not_found.close"),
                    true
            );
        }
    }
}

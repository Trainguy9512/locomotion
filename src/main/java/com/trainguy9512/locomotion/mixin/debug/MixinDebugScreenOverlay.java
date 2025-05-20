package com.trainguy9512.locomotion.mixin.debug;

import com.trainguy9512.locomotion.animation.animator.JointAnimatorDispatcher;
import com.trainguy9512.locomotion.animation.driver.Driver;
import com.trainguy9512.locomotion.animation.driver.DriverKey;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.Map;

@Mixin(DebugScreenOverlay.class)
public class MixinDebugScreenOverlay {
    @Shadow @Final private Minecraft minecraft;

    @Shadow @Final private Font font;

    @Shadow @Final private static int MARGIN_RIGHT;

    @Shadow @Final private static int MARGIN_TOP;

    @Shadow @Final private static int COLOR_GREY;

    @Shadow private boolean renderProfilerChart;
    @Unique
    private static int LABEL_TOP_MARGIN = 12 + MARGIN_TOP;
    @Unique
    private static int LINE_MARGIN = 0;
    @Unique
    private static int BOX_MARGIN = 4;
    @Unique
    private static int NAME_BOX_WIDTH = 100 + BOX_MARGIN;

    @Inject(method = "drawSystemInformation", at = @At("HEAD"), cancellable = true)
    private void drawLocomotionDebugging(GuiGraphics guiGraphics, CallbackInfo ci){
        if (this.minecraft.options.fov().get() == 73) {
            JointAnimatorDispatcher.getInstance().getFirstPersonPlayerDataContainer().ifPresent(dataContainer -> {
                int lineHeight = 9;
                int currentLineTop = 0;


                int maxDriverNameWidth = 0;
                for (Map.Entry<DriverKey<? extends Driver<?>>, Driver<?>> entry : dataContainer.getAllDrivers().entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().getIdentifier())).toList()) {
                    Driver<?> driver = entry.getValue();

                    // Get the string value
                    String driverNameString = entry.getKey().getIdentifier() + ":";
                    String dataString = entry.getValue().toString();

                    // Format the string
                    DecimalFormat format = new DecimalFormat("0.00");
                    if (driver.getValueInterpolated(1) instanceof Float value) {
                        dataString = format.format(value);
                    } else if (driver.getValueInterpolated(1) instanceof Vector3f value) {
                        dataString = "("
                                + format.format(value.x) + " "
                                + format.format(value.y) + " "
                                + format.format(value.z) + ")";
                    }

                    // Get the color for the value
                    int valueTextColor = COLOR_GREY;
                    if (driver.getValueInterpolated(1) instanceof Boolean) {
                        valueTextColor = (Boolean) driver.getValueInterpolated(1) ? 65280 : 16711680;
                    }

                    int driverNameWidth = this.font.width(driverNameString);
                    int driverDataWidth = this.font.width(dataString);
                    maxDriverNameWidth = Math.max(driverNameWidth, maxDriverNameWidth);

                    int nameLineLeftPosition = guiGraphics.guiWidth() - driverNameWidth - MARGIN_RIGHT - BOX_MARGIN - NAME_BOX_WIDTH;
                    int dataLineLeftPosition = guiGraphics.guiWidth() - driverDataWidth - MARGIN_RIGHT - BOX_MARGIN;
                    int nameLineTopPosition = currentLineTop + LABEL_TOP_MARGIN + BOX_MARGIN;

                    guiGraphics.drawString(this.font, driverNameString, nameLineLeftPosition, nameLineTopPosition, COLOR_GREY, true);

                    if (driver.getValueInterpolated(1) instanceof ItemStack itemStack) {
                        itemStack = itemStack == ItemStack.EMPTY ? Items.BARRIER.getDefaultInstance() : itemStack;
                        guiGraphics.pose().pushPose();
                        guiGraphics.pose().scale(1F, 0.5F, 1F);

                        guiGraphics.renderItem(itemStack, dataLineLeftPosition + driverDataWidth - 14, (nameLineTopPosition * 2 - 2));

                        guiGraphics.pose().scale(1F, 2F, 1F);
                        guiGraphics.pose().popPose();
                    } else {
                        int randomColor = (dataString.hashCode() & 11184810) + 4473924;
                        guiGraphics.drawString(this.font, dataString, dataLineLeftPosition, nameLineTopPosition, driver.getValueInterpolated(1) instanceof Enum ? randomColor : valueTextColor, true);
                    }

                    currentLineTop += lineHeight + LINE_MARGIN * 2;
                }
                int nameBoxLeftBoundary = guiGraphics.guiWidth() - maxDriverNameWidth - MARGIN_RIGHT - BOX_MARGIN - NAME_BOX_WIDTH;
                int nameBoxTopBoundary = MARGIN_TOP + BOX_MARGIN;

                guiGraphics.fill(nameBoxLeftBoundary - BOX_MARGIN, nameBoxTopBoundary - BOX_MARGIN, nameBoxLeftBoundary + maxDriverNameWidth + BOX_MARGIN + NAME_BOX_WIDTH, nameBoxTopBoundary + currentLineTop + BOX_MARGIN + 12, -1873784752);
                guiGraphics.drawString(this.font, "First Person Player Animation Drivers", nameBoxLeftBoundary, MARGIN_TOP + BOX_MARGIN, COLOR_GREY, true);

                ci.cancel();
            });
        }
    }
}

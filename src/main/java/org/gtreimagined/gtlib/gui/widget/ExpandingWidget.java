package org.gtreimagined.gtlib.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.ButtonOverlay;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.ICanSyncData;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.gtreimagined.gtlib.gui.widget.ButtonWidget.*;

public class ExpandingWidget extends Widget {
    final Predicate<IGuiHandler> syncFunction;
    Consumer<ExpandingWidget> onExpand;
    final ButtonOverlay icon;
    boolean open = false;
    boolean pressed = false;
    int expansion = 50;
    int rgbEdgeTop, rgbEdgeBottom, rgbCorner, rgbCenter;

    protected ExpandingWidget(@NotNull GuiInstance gui, @Nullable IGuiElement parent, ButtonOverlay icon, Consumer<ExpandingWidget> onExpand, Predicate<IGuiHandler> syncFunction) {
        this(gui, parent, icon, onExpand, syncFunction, backgroundWhiteEdge, backgroundBlackEdge, backgroundCenter, backgroundCenter);
    }
    protected ExpandingWidget(@NotNull GuiInstance gui, @Nullable IGuiElement parent, ButtonOverlay icon, Consumer<ExpandingWidget> onExpand, Predicate<IGuiHandler> syncFunction, int rgbEdgeTop, int rgbEdgeBottom, int rgbCorner, int rgbCenter) {
        super(gui, parent);
        this.syncFunction = syncFunction;
        this.icon = icon;
        this.onExpand = onExpand;
        this.rgbEdgeTop = rgbEdgeTop;
        this.rgbEdgeBottom = rgbEdgeBottom;
        this.rgbCorner = rgbCorner;
        this.rgbCenter = rgbCenter;
    }

    @Override
    public void init() {
        super.init();
        this.gui.syncBoolean(() -> syncFunction.test(gui.handler), b -> {
            if (b != open){
                if (b){
                    setX(getX() - expansion);
                    setW(getW() + expansion);
                    setH(getH() + expansion);
                } else {
                    setX(getX() + expansion);
                    setW(getW() - expansion);
                    setH(getH() - expansion);
                }
                updateSize();
            }
            open = b;
        }, ICanSyncData.SyncDirection.SERVER_TO_CLIENT);
    }

    @Override
    public void render(GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
        int x = realX();
        int y = realY();
        int width = getW();
        int height = getH();
        fillGradient(graphics, x, y, width, height, rgbCenter, rgbCenter);
        fillGradient(graphics, x + width, y - 1, 1, 1, rgbCorner, rgbCorner);
        fillGradient(graphics, x - 1, y + height, 1, 1, rgbCorner, rgbCorner);
        fillGradient(graphics, x - 1, y - 1, width + 1, 1, rgbEdgeTop, rgbEdgeTop);
        fillGradient(graphics, x - 1, y, 1, height, rgbEdgeTop, rgbEdgeTop);
        fillGradient(graphics, x, y + height, width + 1, 1, rgbEdgeBottom, rgbEdgeBottom);
        fillGradient(graphics, x + width, y, 1, height, rgbEdgeBottom, rgbEdgeBottom);
        drawTexture(graphics, icon.getTexture(), realX() + 1, realY() + 1, 0, 0, icon.getH(), icon.getW(), icon.getH(), icon.getW());
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        this.pressed = false;
        super.onRelease(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (button != 0) return;
        this.pressed = true;
        super.onClick(mouseX, mouseY, button);
        if (this.onExpand != null) {
            if (this.gui.handler.isRemote()) {
                clientClick();
            }
            this.onExpand.accept(this);
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void clientClick() {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    public static WidgetSupplier build(ButtonOverlay closed, int id, Predicate<IGuiHandler> syncFunction) {
        return builder((a, b) -> new ExpandingWidget(a, b, closed, but -> but.gui.sendPacket(but.gui.handler.createGuiPacket(new GuiEvents.GuiEvent(GuiEvents.EXPANDING_WIDGET, Screen.hasShiftDown() ? 1 : 0, id))), syncFunction));
    }

    public static WidgetSupplier build(ButtonOverlay closed, int id, Predicate<IGuiHandler> syncFunction, int rgbEdgeTop, int rgbEdgeBottom, int rgbCorner, int rgbCenter) {
        return builder((a, b) -> new ExpandingWidget(a, b, closed, but -> but.gui.sendPacket(but.gui.handler.createGuiPacket(new GuiEvents.GuiEvent(GuiEvents.EXPANDING_WIDGET, Screen.hasShiftDown() ? 1 : 0, id))), syncFunction, rgbEdgeTop, rgbEdgeBottom, rgbCorner, rgbCenter));
    }
}

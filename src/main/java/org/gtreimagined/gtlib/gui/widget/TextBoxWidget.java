package org.gtreimagined.gtlib.gui.widget;

import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.capability.IGuiHandler;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.ICanSyncData;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class TextBoxWidget extends Widget {

    EditBox textBox;
    BiConsumer<IGuiHandler, String> consumer;
    Function<IGuiHandler, String> function;

    protected TextBoxWidget(@NotNull GuiInstance gui, @Nullable IGuiElement parent, BiConsumer<IGuiHandler, String> consumer, Function<IGuiHandler, String> function) {
        super(gui, parent);
        this.consumer = consumer;
        this.function = function;
    }

    public static WidgetSupplier build(BiConsumer<IGuiHandler, String> consumer, Function<IGuiHandler, String> function) {
        return builder((i, p) -> new TextBoxWidget(i, p, consumer, function));
    }

    @Override
    public void render(GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
        textBox.render(graphics, (int) mouseX, (int) mouseY, partialTicks);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        super.onClick(mouseX, mouseY, button);
        if (isInside(mouseX, mouseY)){
            if (button == 0 && !textBox.isFocused()){
                textBox.setFocused(true);
            } else if (button == 1 && textBox.isFocused()){
                textBox.setFocused(false);
            }
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers, double mouseX, double mouseY) {
        if (textBox.isFocused()){
            return textBox.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers, mouseX, mouseY);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers, double mouseX, double mouseY) {
        if (textBox.isFocused()){
            return textBox.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers, mouseX, mouseY);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers, double mouseX, double mouseY) {
        if (textBox.isFocused()) return textBox.charTyped(codePoint, modifiers);
        return super.charTyped(codePoint, modifiers, mouseX, mouseY);
    }

    @Override
    public void updateSize() {
        super.updateSize();
        if (gui.isRemote && textBox != null){
            textBox.setX(realX());
            textBox.setY(realY());
        }
    }

    @Override
    public void init() {
        super.init();

        this.gui.syncString(() -> function.apply(gui.handler), this::setValue, ICanSyncData.SyncDirection.SERVER_TO_CLIENT);
        this.gui.syncString(() -> textBox.getValue(), s -> consumer.accept(gui.handler, s), ICanSyncData.SyncDirection.CLIENT_TO_SERVER);
        if (gui.isRemote){
            initTextBox();
        } else {
            gui.update();
        }
    }

    boolean initialized = false;

    private void setValue(String value){
        if (!initialized){
            textBox.setValue(value);
            initialized = true;
        }
    }

    @OnlyIn(Dist.CLIENT)
    protected void initTextBox(){
        textBox = new EditBox(Minecraft.getInstance().font, this.realX(), this.realY(), this.getW(), this.getH(), Utils.literal(""));
        this.textBox.setMaxLength(32500);
    }
}

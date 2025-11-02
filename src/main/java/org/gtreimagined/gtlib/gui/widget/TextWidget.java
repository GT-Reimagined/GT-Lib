package org.gtreimagined.gtlib.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.gui.GuiInstance;
import org.gtreimagined.gtlib.gui.IGuiElement;
import org.gtreimagined.gtlib.gui.Widget;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.client.Minecraft;

import java.util.function.Function;

public class TextWidget extends Widget {

    private final Function<TextWidget, String> getter;
    private final int color;

    private final boolean centered;

    protected TextWidget(GuiInstance gui, IGuiElement parent, String text, int color, boolean centered) {
        this(gui, parent, a -> text, color, centered);
    }

    protected TextWidget(GuiInstance gui, IGuiElement parent, Function<TextWidget, String> getter, int color, boolean centered) {
        super(gui, parent);
        this.getter = getter;
        this.color = color;
        this.centered = centered;
    }

    public static WidgetSupplier build(String text, int color, boolean centered) {
        return builder((a, b) -> new TextWidget(a, b, text, color, centered)).clientSide();
    }

    @Override
    public void render(GuiGraphics graphics, double mouseX, double mouseY, float partialTicks) {
        String text = getter.apply(this);
        int textWidth = Minecraft.getInstance().font.width(text);
        int xScaled = textWidth / 2;
        int xCenter = (getW() / 2);
        int xPosition = xCenter - xScaled;
        this.drawText(graphics, Utils.literal(text), (centered ? realX() + xPosition : realX()), realY(), color);
    }
}

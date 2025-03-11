package muramasa.antimatter.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import lombok.Setter;
import lombok.experimental.Accessors;
import muramasa.antimatter.gui.GuiInstance;
import muramasa.antimatter.gui.IGuiElement;
import muramasa.antimatter.gui.Widget;
import muramasa.antimatter.util.int2;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TextureWidget extends Widget {
    final ResourceLocation texture;

    @Accessors(chain = true)
    @Setter
    int textureX = 256, textureY = 256;
    protected TextureWidget(@NotNull GuiInstance gui, @Nullable IGuiElement parent, ResourceLocation texture, int2 pos, int2 size) {
        super(gui, parent);
        this.texture = texture;
        this.setX(pos.x);
        this.setY(pos.y);
        this.setW(size.x);
        this.setH(size.y);
    }

    @Override
    public void render(PoseStack matrixStack, double mouseX, double mouseY, float partialTicks) {
        this.drawTexture(matrixStack, texture, realX(), realY(), 0, 0, getW(), getH(), textureX, textureY);
    }

    public static WidgetSupplier build(ResourceLocation texture, int2 pos, int2 size) {
        return builder((a, b) -> new TextureWidget(a, b, texture, pos, size));
    }

    public static WidgetSupplier build(ResourceLocation texture, int2 pos, int2 size, int textureX, int textureY) {
        return builder((a, b) -> new TextureWidget(a, b, texture, pos, size).setTextureX(textureX).setTextureY(textureY));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }
}

package org.gtreimagined.gtlib.mixin.client;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.world.item.ItemStack;
import org.gtreimagined.gtlib.tool.IGTTool;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class GuiGraphicsMixin {
    @Shadow
    public abstract void fill(RenderType renderType, int minX, int minY, int maxX, int maxY, int color);

    @Inject(method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V", at = @At("TAIL"))
    private void injectRenderGuiItemDecorations(Font fr, ItemStack stack, int xPosition, int yPosition, String text, CallbackInfo ci){
        if (stack.getItem() instanceof IGTTool tool && tool.getGTToolType().isPowered() && tool.isPoweredBarVisible(stack)) {
            int l = tool.getPoweredBarWidth(stack);
            int i = tool.getPoweredBarColor(stack);
            int j = xPosition + 2;
            int k = yPosition + 11;
            this.fill(RenderType.guiOverlay(), j, k, j + 13, k + 2, -16777216);
            this.fill(RenderType.guiOverlay(), j, k, j + l, k + 1, i | -16777216);
        }

    }
}

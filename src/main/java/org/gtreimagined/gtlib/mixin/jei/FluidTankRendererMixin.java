package org.gtreimagined.gtlib.mixin.jei;

import brachy.modularui.drawable.text.RichText;
import brachy.modularui.screen.RichTooltip;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.library.render.FluidTankRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.data.GTLibTags;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.RecipeWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FluidTankRenderer.class, remap = false)
public class FluidTankRendererMixin<T> {
    @Inject(method = "getTooltip(Lmezz/jei/api/gui/builder/ITooltipBuilder;Ljava/lang/Object;Lnet/minecraft/world/item/TooltipFlag;)V", at = @At("TAIL"))
    private void gtlib$injectGetTooltip(ITooltipBuilder tooltip, T fluidStack, TooltipFlag tooltipFlag, CallbackInfo ci){
        if (fluidStack instanceof FluidStack stack){
            if (!stack.getFluid().is(GTLibTags.GT_FLUID)) return;
            RichTooltip richTooltip = new RichTooltip();
            var tooltips = tooltip.toLegacyToComponents();
            for (Component t : tooltips){
                richTooltip.addLine(t);
            }
            tooltip.removeAll(tooltips);
            RecipeWidget.createFluidTooltip(richTooltip, stack);
            if (richTooltip.getRichText() instanceof RichText richText) {
                for (var line : richText.getAsText()) {
                    // scuffed conversion, but it mostly works
                    line.ifLeft(tooltip::add).ifRight(tooltip::add);
                }
            }

        }
    }
}

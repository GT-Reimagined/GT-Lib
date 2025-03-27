package org.gtreimagined.gtlib.gui.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import org.gtreimagined.gtlib.blockentity.multi.BlockEntityHatch;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.gtreimagined.gtlib.gui.container.ContainerMachine;

public class ScreenHatch<T extends BlockEntityHatch<T>, U extends ContainerMachine<T>> extends ScreenMachine<T, U> {

    public ScreenHatch(U container, Inventory inv, Component name) {
        super(container, inv, name);
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(stack, partialTicks, mouseX, mouseY);
    }
}

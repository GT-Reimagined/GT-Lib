package org.gtreimagined.gtlib.tool.behaviour;

import org.gtreimagined.gtlib.behaviour.IItemUse;
import org.gtreimagined.gtlib.tool.IBasicAntimatterTool;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import tesseract.api.forge.TesseractCaps;

public class BehaviourPoweredDebug implements IItemUse<IBasicAntimatterTool> {

    public static final BehaviourPoweredDebug INSTANCE = new BehaviourPoweredDebug();

    @Override
    public String getId() {
        return "powered_debug";
    }

    @Override
    public InteractionResult onItemUse(IBasicAntimatterTool instance, UseOnContext c) {
        if (instance.getAntimatterToolType().isPowered() && c.getLevel().getBlockState(c.getClickedPos()) == Blocks.REDSTONE_BLOCK.defaultBlockState() && c.getPlayer() != null && c.getPlayer().isCreative()) {
            ItemStack stack = c.getPlayer().getItemInHand(c.getHand());
            stack.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).ifPresent(i -> {
                if (i.getCapacity() - i.getEnergy() <= 50000)
                    i.setEnergy(i.getCapacity());
                else i.setEnergy(i.getEnergy() + 50000);
                c.getPlayer().setItemInHand(c.getHand(), i.getContainer().getItemStack());
            });
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}

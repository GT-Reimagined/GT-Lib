package org.gtreimagined.gtlib.registration;

import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.GTItemBlock;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public interface IItemBlockProvider {

    default BlockItem getItemBlock() {
        return new GTItemBlock((Block) this);
    }

    default CreativeModeTab getItemGroup() {
        return Ref.TAB_BLOCKS;
    }

    default Component getDisplayName(ItemStack stack) {
        return Utils.translatable(stack.getDescriptionId());
    }

    default boolean generateItemBlock() {
        return true;
    }
}

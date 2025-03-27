package org.gtreimagined.gtlib.block;

import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.registration.IItemBlockProvider;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class AntimatterItemBlock extends BlockItem {

    public AntimatterItemBlock(Block block) {
        super(block, new Properties().tab(block instanceof IItemBlockProvider ? ((IItemBlockProvider) block).getItemGroup() : Ref.TAB_BLOCKS));
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        return getBlock() instanceof IItemBlockProvider ? ((IItemBlockProvider) getBlock()).getDisplayName(stack) : Utils.translatable(stack.getDescriptionId());
    }
}

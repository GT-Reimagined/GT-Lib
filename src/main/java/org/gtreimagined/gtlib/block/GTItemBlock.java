package org.gtreimagined.gtlib.block;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import org.gtreimagined.gtlib.GTCreativeTabs;
import org.gtreimagined.gtlib.registration.ICreativeTabProvider;
import org.gtreimagined.gtlib.registration.IItemBlockProvider;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

public class GTItemBlock extends BlockItem implements ICreativeTabProvider {

    public GTItemBlock(Block block) {
        super(block, new Properties());
    }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        return getBlock() instanceof IItemBlockProvider ? ((IItemBlockProvider) getBlock()).getDisplayName(stack) : Utils.translatable(stack.getDescriptionId());
    }

    @Override
    public boolean allowedIn(ResourceKey<CreativeModeTab> tab) {
        if (getBlock() instanceof ICreativeTabProvider provider && !provider.allowedIn(tab)) return false;
        ResourceKey<CreativeModeTab> compareTab = getBlock() instanceof IItemBlockProvider provider ? provider.getItemGroup() : GTCreativeTabs.BLOCKS.getKey();
        return tab == compareTab;
    }
}

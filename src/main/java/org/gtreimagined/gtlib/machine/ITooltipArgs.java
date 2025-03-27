package org.gtreimagined.gtlib.machine;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ITooltipArgs {
    Object[] getTooltipArgs(BlockMachine machine, ItemStack stack, @Nullable BlockGetter world, TooltipFlag flag, int i);
}

package org.gtreimagined.gtlib.registration;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface ICreativeTabProvider {
    boolean allowedIn(CreativeModeTab tab);

    default Item asItem(){
        return (Item)  this;
    }

    default void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
        if (this.allowedIn(group)) {
            ItemStack stack = new ItemStack(this.asItem());
            items.add(stack.copy());
        }
    }
}

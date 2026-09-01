package org.gtreimagined.gtlib.behaviour;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface IInventoryTick<T> extends IBehaviour<T> {

    @Override
    default String getId(){
        return "inventory_tick";
    }

    void inventoryTick(T instance, ItemStack stack, Level level, Entity entity, int slotID, boolean isSelected);
}

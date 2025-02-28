package muramasa.antimatter.capability.item;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

public interface IItemHandlerExtended extends IItemHandlerModifiable {


    //Container methods

    default ItemStack removeItem(int index, int count){
        return extractItem(index, count, false);
    }

    default ItemStack removeItemNoUpdate(int index) {
        return removeItem(index, getStackInSlot(index).getCount());
    }

    default boolean isEmpty(){
        boolean hasStack = false;
        for (int i = 0; i < getSlots(); i++) {
            ItemStack stack = getStackInSlot(i);
            if (!stack.isEmpty()) hasStack = true;
        };
        return !hasStack;
    }

    default boolean stillValid(Player player){
        return true;
    }

    default void setChanged(){

    }

    default void clearContent(){
        for (int i = 0; i < getSlots(); i++) {
            this.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}

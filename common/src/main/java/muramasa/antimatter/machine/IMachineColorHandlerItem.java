package muramasa.antimatter.machine;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface IMachineColorHandlerItem {
    int getItemColor(ItemStack stack, @Nullable Block block, int i);
}

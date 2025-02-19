package muramasa.antimatter.capability.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.NotNull;

public interface ITrackedHandler extends ExtendedItemContainer, INBTSerializable<CompoundTag> {
    @NotNull
    ItemStack insertOutputItem(int slot, @NotNull ItemStack stack, boolean simulate);

    @NotNull
    ItemStack extractFromInput(int slot, int amount, boolean simulate);
}

package org.gtreimagined.gtlib.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.forge.ForgeEmiStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.integration.xei.GTLibXEIPlugin;

import java.util.ArrayList;
import java.util.List;

@EmiEntrypoint
public class GTLibEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry emiRegistry) {
        List<ItemLike> list = new ArrayList<>();
        GTLibXEIPlugin.getItemsToHide().forEach(c -> c.accept(list));
        if (!list.isEmpty()) {
            emiRegistry.removeEmiStacks(s -> s.getKey() instanceof Item i && list.contains(i));
        }
        List<Fluid> fluidList = new ArrayList<>();
        GTLibXEIPlugin.getFluidsToHide().forEach(c -> c.accept(fluidList));
        List<Item> buckets = fluidList.stream().map(Fluid::getBucket).toList();
        if (!fluidList.isEmpty()){
            emiRegistry.removeEmiStacks(s -> s.getKey() instanceof Fluid f && fluidList.contains(f));
            emiRegistry.removeEmiStacks(s -> s.getKey() instanceof Item i && buckets.contains(i));
        }


    }
}

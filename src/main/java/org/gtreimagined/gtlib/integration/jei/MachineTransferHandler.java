package org.gtreimagined.gtlib.integration.jei;

import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.gui.container.ContainerBasicMachine;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

import java.util.List;

@SuppressWarnings("removal")
public class MachineTransferHandler implements IRecipeTransferInfo<ContainerBasicMachine, IRecipe> {
    final ResourceLocation id;
    public MachineTransferHandler(ResourceLocation id){
        this.id = id;
    }

    @Override
    public Class<ContainerBasicMachine> getContainerClass() {
        return ContainerBasicMachine.class;
    }

    @Override
    public boolean canHandle(ContainerBasicMachine container, IRecipe recipe) {
        String[] split = recipe.getMapId().split(":");
        String name;
        if (split.length == 2) {
            name = split[1];
        } else if (split.length == 1) {
            name = split[0];
        } else {
            return false;
        }
        IRecipeMap rmap = AntimatterAPI.get(IRecipeMap.class, name);
        if (rmap == null) return false;
        Machine<?> machine = container.getTile().getMachineType();
        if (machine.getRecipeMap(container.getTile().getMachineTier()) == null || machine.getRecipeMap(container.getTile().getMachineTier()) != rmap) return false;
        if (!recipe.hasInputItems()) return false;
        if (!container.slotMap.containsKey(SlotType.IT_IN)) return false;
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(ContainerBasicMachine container, IRecipe recipe) {
        return (List<Slot>) container.slotMap.get(SlotType.IT_IN);
    }

    @Override
    public List<Slot> getInventorySlots(ContainerBasicMachine container, IRecipe recipe) {
        return container.playerSlots;
    }

    @Override
    public RecipeType<IRecipe> getRecipeType() {
        return new RecipeType<>(getRecipeCategoryUid(), getRecipeClass());
    }

    @Override
    public Class<IRecipe> getRecipeClass() {
        return IRecipe.class;
    }

    @Override
    public ResourceLocation getRecipeCategoryUid() {
        return id;
    }
}

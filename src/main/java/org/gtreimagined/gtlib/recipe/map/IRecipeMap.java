package org.gtreimagined.gtlib.recipe.map;

import org.gtreimagined.gtlib.blockentity.BlockEntityMachine;
import org.gtreimagined.gtlib.capability.FluidHandler;
import org.gtreimagined.gtlib.capability.Holder;
import org.gtreimagined.gtlib.capability.machine.MachineFluidHandler;
import org.gtreimagined.gtlib.capability.machine.MachineItemHandler;
import org.gtreimagined.gtlib.gui.GuiData;
import org.gtreimagined.gtlib.integration.xei.renderer.IRecipeInfoRenderer;
import org.gtreimagined.gtlib.integration.xei.renderer.InfoRenderers;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.registration.ISharedGTObject;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public interface IRecipeMap extends ISharedGTObject {
    ItemStack[] EMPTY_ITEM = new ItemStack[0];
    FluidStack[] EMPTY_FLUID = new FluidStack[0];

    IRecipe find(@NotNull ItemStack[] items, @NotNull FluidStack[] fluids, Tier tier, @NotNull Predicate<IRecipe> canHandle);

    default IRecipe findByID(ResourceLocation id){
        return getRecipes(false).stream().filter(r -> r.getId().equals(id)).findFirst().orElse(null);
    }
    void add(IRecipe recipe);
    void compileRecipe(IRecipe recipe);
    void compile(RecipeManager manager);
    void resetCompiled();
    Collection<IRecipe> getRecipes(boolean filterHidden);
    boolean acceptsItem(ItemStack item);
    boolean acceptsFluid(FluidStack fluid);

    RecipeType<? extends IRecipe> getRecipeType();

    Map<String, SubCategory> getSubCategories();

    @Nullable
    default Tier getGuiTier() {
        return null;
    }

    default Object getIcon() {
        return null;
    }

    default <T extends BlockEntityMachine<T>> IRecipe find(Holder<IItemHandler, MachineItemHandler<T>> itemHandler, Holder<IFluidHandler, MachineFluidHandler<T>> fluidHandler, Tier tier, Predicate<IRecipe> validateRecipe) {
        return find(itemHandler.map(MachineItemHandler::getInputs).orElse(EMPTY_ITEM),
                fluidHandler.map(FluidHandler::getInputs).orElse(EMPTY_FLUID), tier, validateRecipe);
    }

    default IRecipe find(@NotNull Optional<MachineItemHandler<?>> itemHandler,
                        @NotNull Optional<MachineFluidHandler<?>> fluidHandler, Tier tier, Predicate<IRecipe> validator) {
        return find(itemHandler.map(MachineItemHandler::getInputs).orElse(EMPTY_ITEM),
                fluidHandler.map(MachineFluidHandler::getInputs).orElse(EMPTY_FLUID), tier, validator);
    }
    @Nullable
    default GuiData getGui() {
        return null;
    }

    default Proxy getProxy(){
        return null;
    }

    @NotNull
    @OnlyIn(Dist.CLIENT)
    default IRecipeInfoRenderer getInfoRenderer() {
        return InfoRenderers.DEFAULT_RENDERER;
    }

    default Component getDisplayName() {
        return Utils.translatable("jei.category." + getLoc().getPath());
    }
}

package org.gtreimagined.gtlib.integration.ct;

import com.blamejared.crafttweaker.api.CraftTweakerAPI;
import com.blamejared.crafttweaker.api.CraftTweakerConstants;
import com.blamejared.crafttweaker.api.action.recipe.ActionAddRecipe;
import com.blamejared.crafttweaker.api.annotation.ZenRegister;
import com.blamejared.crafttweaker.api.fluid.IFluidStack;
import com.blamejared.crafttweaker.api.ingredient.IIngredient;
import com.blamejared.crafttweaker.api.item.IItemStack;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.recipe.Recipe;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.RecipeBuilder;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.openzen.zencode.java.ZenCodeType.Method;
import org.openzen.zencode.java.ZenCodeType.Name;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@ZenRegister
@Name("mods.gtlib.GTRecipeBuilder")
public class CTRecipeBuilder {
    RecipeBuilder recipeBuilder;
    RecipeManager manager;

    public CTRecipeBuilder(String mapId, RecipeManager manager) {
        IRecipeMap map = GTAPI.get(IRecipeMap.class, mapId);
        if(!(map instanceof RecipeMap<?> recipeMap)) {
            throw new IllegalArgumentException("Invalid recipe map: " + mapId);
        }
        if (!recipeMap.getRecipeType().equals(manager.getRecipeType())) {
            throw new IllegalArgumentException("Recipe map: " + mapId + " does not use this recipe type!");
        }
        recipeBuilder = recipeMap.RB();
        this.manager = manager;
    }

    @Method
    public CTRecipeBuilder ii(IIngredient... itemInput) {
        List<Ingredient> input = itemInput == null ? Collections.emptyList() : Arrays.stream(itemInput).map(IIngredient::asVanillaIngredient).toList();
        recipeBuilder.ii(input);
        return this;
    }

    @Method
    public CTRecipeBuilder io(IItemStack... itemOutput){
        ItemStack[] outputs = itemOutput == null || itemOutput.length == 0 ? IRecipeMap.EMPTY_ITEM : Arrays.stream(itemOutput).map(IItemStack::getInternal).toArray(ItemStack[]::new);
        recipeBuilder.io(outputs);
        return this;
    }

    @Method
    public CTRecipeBuilder fi(IFluidStack... fluidInput){
        List<FluidIngredient> fluidIn = fluidInput == null ? Collections.emptyList() : Arrays.stream(fluidInput).map(t -> FluidIngredient.of(t.getInternal())).toList();
        recipeBuilder.fi(fluidIn.toArray(new FluidIngredient[0]));
        return this;
    }

    @Method
    public CTRecipeBuilder fo(IFluidStack... fluidOutput){
        FluidStack[] fluidOut = fluidOutput == null ? IRecipeMap.EMPTY_FLUID : Arrays.stream(fluidOutput).map(t -> t.getInternal()).toArray(FluidStack[]::new);
        recipeBuilder.fo(fluidOut);
        return this;
    }

    @Method
    public CTRecipeBuilder outputChances(double... chances){
        recipeBuilder.outputChances(chances);
        return this;
    }

    @Method
    public CTRecipeBuilder inputChances(double... chances){
        recipeBuilder.inputChances(chances);
        return this;
    }

    @Method
    public CTRecipeBuilder hide() {
        recipeBuilder.hide();
        return this;
    }

    @Method
    public CTRecipeBuilder fake(){
        recipeBuilder.fake();
        return this;
    }

    @Method
    public void build(String id, long duration, long power, long special) {
        build(id, duration, power, special, 1);
    }

    @Method
    public void build(String domain, String id, long duration, long power, long special, int amps) {
        CraftTweakerAPI.apply(new ActionAddRecipe<>(manager, (Recipe) recipeBuilder.recipeMapOnly().add(domain, id, duration, power, special, amps)));
    }

    @Method
    public void build(String id, long duration, long power, long special, int amps) {
        build(CraftTweakerConstants.MOD_ID, id, duration, power, special, amps);
    }

    @Method
    public void build(String id, long duration, long power) {
        build(id, duration, power, 0);
    }

    @Method
    public void build(String id, long duration) {
        build(id, duration, 0, 0);
    }
}

package org.gtreimagined.gtlib.recipe;

import com.google.gson.JsonObject;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.recipe.serializer.MachineRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public interface IRecipe extends net.minecraft.world.item.crafting.Recipe<Container>{
    boolean isValid();
    void invalidate();

    int getAmps();

    void addOutputChances(int[] chances);

    void addInputChances(int[] chances);

    void setHidden(boolean hidden);

    void setFake(boolean fake);

    void addTags(Set<String> tags);

    boolean hasInputItems();

    boolean hasOutputItems();

    boolean hasInputFluids();

    boolean hasOutputFluids();

    boolean hasOutputChances();

    boolean hasInputChances();

    void setIds(ResourceLocation id, String map);

    void setId(ResourceLocation id);

    void setMapId(String map);

    void sortInputItems();

    List<Ingredient> getInputItems();

    ItemStack[] getOutputItems();

    ItemStack[] getOutputItems(boolean chance);

    /**
     * Returns a list of items not bound by chances.
     *
     * @return list of items.
     */
    ItemStack[] getFlatOutputItems();

    //Note: does call get().
    boolean hasSpecialIngredients();

    @NotNull
    List<FluidIngredient> getInputFluids();

    @Nullable
    FluidStack[] getOutputFluids();

    int getDuration();

    long getPower();

    @Nullable
    int[] getOutputChances();

    @Nullable
    int[] getInputChances();

    default long getTotalPower(){
        return getDuration() * getPower();
    }
    int getSpecialValue();

    boolean isHidden();

    boolean isFake();

    Set<String> getTags();

    String getMapId();

    default ResourceLocation getMapLoc(){
        return new ResourceLocation(getMapId());
    }

    default JsonObject toJson() {
        JsonObject json = new JsonObject();
        RecipeMap<?> recipeMap = GTAPI.get(RecipeMap.class, getMapLoc());
        if (recipeMap != null) {
            recipeMap.getRecipeSerializer().toJson(json, this);
        } else {
            MachineRecipeSerializer.INSTANCE.toJson(json, this);
        }
        return json;
    }

    List<IRecipeValidator> getValidators();
}

package muramasa.antimatter.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.recipe.ingredient.FluidIngredient;
import muramasa.antimatter.recipe.map.RecipeMap;
import muramasa.antimatter.recipe.serializer.AntimatterRecipeSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IRecipe extends net.minecraft.world.item.crafting.Recipe<Container>{
    boolean isValid();
    void invalidate();

    int getAmps();

    void addOutputChances(int[] chances);

    void addInputChances(int[] chances);

    void setHidden(boolean hidden);

    void addTags(Set<RecipeTag> tags);

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
    FluidHolder[] getOutputFluids();

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

    Set<RecipeTag> getTags();

    String getMapId();

    default JsonObject toJson() {
        JsonObject json = new JsonObject();
        RecipeMap<?> recipeMap = AntimatterAPI.get(RecipeMap.class, getMapId());
        if (recipeMap != null) {
            recipeMap.getRecipeSerializer().toJson(json, this);
        } else {
            AntimatterRecipeSerializer.INSTANCE.toJson(json, this);
        }
        return json;
    }

    List<IRecipeValidator> getValidators();
}

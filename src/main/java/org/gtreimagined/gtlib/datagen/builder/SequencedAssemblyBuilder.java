package org.gtreimagined.gtlib.datagen.builder;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.core.Registry;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class SequencedAssemblyBuilder {
    final Ingredient input;
    ItemLike transitionalItem;
    List<Sequence> sequences = new ArrayList<>();
    int loops = 1;
    List<Tuple<ItemStack, Float>> results = new ArrayList<>();

    public SequencedAssemblyBuilder(Ingredient input){
        this.input = input;
    }

    public SequencedAssemblyBuilder transitionalItem(ItemLike transitionalItem){
        this.transitionalItem = transitionalItem;
        return this;
    }

    public SequencedAssemblyBuilder loops(int loops){
        this.loops = loops;
        return this;
    }

    public SequencedAssemblyBuilder addDummySequence(){
        return addSequence(new ResourceLocation(Ref.MOD_CREATE, "pressing"), transitionalItem, Ingredient.of(transitionalItem));
    }

    public SequencedAssemblyBuilder addSequence(ResourceLocation type, ItemLike result, Ingredient... inputs){
        this.sequences.add(new Sequence(type.toString(), Arrays.stream(inputs).map(Ingredient::toJson).toArray(JsonElement[]::new), result));
        return this;
    }
    public SequencedAssemblyBuilder addSequence(ResourceLocation type, ItemLike result, JsonElement... inputs){
        this.sequences.add(new Sequence(type.toString(), inputs, result));
        return this;
    }

    public SequencedAssemblyBuilder addDeployingSequence(Object in){
        if (transitionalItem == null){
            throw new IllegalStateException("Recipe must add a transitional item first!");
        }
        Ingredient ingredient;
        if (in instanceof TagKey<?> key && key.registry() == Registries.ITEM){
            ingredient = Ingredient.of((TagKey<Item>) key);
        } else if (in instanceof ItemLike itemLike){
            ingredient = Ingredient.of(itemLike);
        } else if (in instanceof ItemStack stack){
            ingredient = Ingredient.of(stack);
        } else if (in instanceof Ingredient ing){
            ingredient = ing;
        } else {
            throw new IllegalArgumentException("Unknown object type added!");
        }
        return addSequence(new ResourceLocation(Ref.MOD_CREATE, "deploying"), transitionalItem, Ingredient.of(transitionalItem), ingredient);
    }

    public SequencedAssemblyBuilder addFillingSequence(Object in){
        if (transitionalItem == null){
            throw new IllegalStateException("Recipe must add a transitional item first!");
        }
        FluidIngredient ingredient;
        if (in instanceof FluidStack stack){
            ingredient = FluidIngredient.of(stack);
        } else if (in instanceof FluidIngredient ing){
            ingredient = ing;
        } else {
            throw new IllegalArgumentException("Unknown object type added!");
        }
        return addSequence(new ResourceLocation(Ref.MOD_CREATE, "deploying"), transitionalItem, Ingredient.of(transitionalItem).toJson(), ingredient.toJson());
    }

    public SequencedAssemblyBuilder addResult(ItemLike result, float weight){
        return addResult(new ItemStack(result), weight);
    }

    public SequencedAssemblyBuilder addResult(ItemStack result, float weight){
        results.add(new Tuple<>(result, weight));
        return this;
    }

    /**
     * Builds this recipe into an {@link FinishedRecipe}.
     */
    public void build(Consumer<FinishedRecipe> consumer, ResourceLocation id) {
        if (sequences.size() != 3){
            throw new IllegalStateException("Must have exactly 3 sequences, no more, no less");
        }
        consumer.accept(new Result(new ResourceLocation(id.getNamespace(), "sequenced_assembly/" + id.getPath())));
    }

    private record Sequence(String type, JsonElement[] ingredients, ItemLike result){}

    private class Result implements FinishedRecipe {
        ResourceLocation id;
        ResourceLocation advancementID = null;
        public Result(ResourceLocation id){
            this.id = id;
        }

        public Result(ResourceLocation id, ResourceLocation advancementID){
            this.id = id;
            this.advancementID = advancementID;
        }
        @Override
        public void serializeRecipeData(JsonObject json) {
            json.add("ingredient", input.toJson());
            JsonArray nestedRecipes = new JsonArray();
            sequences.forEach(s -> {
                JsonObject nestedRecipe = new JsonObject();
                nestedRecipe.addProperty("type", s.type());
                JsonArray ingredients = new JsonArray();
                for (JsonElement ingredient : s.ingredients) {
                    ingredients.add(ingredient);
                }
                nestedRecipe.add("ingredients", ingredients);
                JsonArray results = new JsonArray();
                JsonObject item = new JsonObject();
                item.addProperty("item", RegistryUtils.getIdFromItem(s.result.asItem()).toString());
                results.add(item);
                nestedRecipe.add("results", results);
                nestedRecipes.add(nestedRecipe);
            });
            JsonArray resultArray = new JsonArray();
            for (Tuple<ItemStack, Float> result : results) {
                JsonObject resultObject = new JsonObject();
                ItemStack stack = result.getA();
                resultObject.addProperty("item", RegistryUtils.getIdFromItem(stack.getItem()).toString());
                if (stack.getCount() != 1)
                    resultObject.addProperty("count", stack.getCount());
                if (stack.hasTag())
                    resultObject.add("nbt", JsonParser.parseString(stack.getTag()
                            .toString()));
                if (result.getB() != 1){
                    resultObject.addProperty("chance", result.getB());
                }
                resultArray.add(resultObject);
            }
            json.add("sequence", nestedRecipes);
            json.add("results", resultArray);
            JsonObject transitionalItemObject = new JsonObject();
            transitionalItemObject.addProperty("item", RegistryUtils.getIdFromItem(transitionalItem.asItem()).toString());
            json.add("transitionalItem", transitionalItemObject);
            json.addProperty("loops", loops);
        }

        @Override
        public ResourceLocation getId() {
            return id;
        }

        @Override
        public RecipeSerializer<?> getType() {
            return BuiltInRegistries.RECIPE_SERIALIZER.get(new ResourceLocation(Ref.MOD_CREATE, "sequenced_assembly"));
        }

        @Nullable
        @Override
        public JsonObject serializeAdvancement() {
            return null;
        }

        @Nullable
        @Override
        public ResourceLocation getAdvancementId() {
            return advancementID;
        }
    }
}

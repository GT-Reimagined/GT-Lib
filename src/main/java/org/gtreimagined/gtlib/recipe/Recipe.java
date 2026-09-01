package org.gtreimagined.gtlib.recipe;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.RegistryAccess;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.recipe.serializer.MachineRecipeSerializer;
import org.gtreimagined.gtlib.util.RegistryUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Recipe implements IRecipe {
    private final List<ItemStack> itemsOutput;
    @NotNull
    private final List<Ingredient> itemsInput;
    @NotNull
    private final List<FluidIngredient> fluidsInput;
    private final List<FluidStack> fluidsOutput;
    @Getter
    private final int duration;
    private final int special;
    @Getter
    private final long power;
    @Getter
    private final int amps;
    private int[] outputChances, inputChances;
    @Getter
    @Setter
    private boolean hidden, fake;
    @Getter
    private Set<String> tags = new ObjectOpenHashSet<>();
    public ResourceLocation id;
    public String mapId;
    //Used for recipe validators, e.g. cleanroom.
    public final List<IRecipeValidator> validators = Collections.emptyList();

    //After data reload this is false.
    @Getter
    private boolean valid;

    public static void init() {
        
    }

    public static final RecipeType<Recipe> RECIPE_TYPE = RecipeType.simple(new ResourceLocation(Ref.ID, "machine"));

    public Recipe(@NotNull List<Ingredient> stacksInput, List<ItemStack> stacksOutput, @NotNull List<FluidIngredient> fluidsInput, List<FluidStack> fluidsOutput, int duration, long power, int special, int amps) {
        this.itemsInput = ImmutableList.copyOf(stacksInput);
        this.itemsOutput = stacksOutput;
        this.duration = duration;
        this.power = power;
        this.special = special;
        this.fluidsInput = ImmutableList.copyOf(fluidsInput);
        this.amps = amps;
        this.fluidsOutput = fluidsOutput;
        this.valid = true;
    }

    public void invalidate() {
        if (this.id != null)
            this.valid = false;
    }

    public void addOutputChances(int[] chances) {
        this.outputChances = chances;
    }

    @Override
    public void addInputChances(int[] chances) {
        this.inputChances = chances;
    }

    public void addTags(Set<String> tags) {
        this.tags = tags;
    }

    public boolean hasInputItems() {
        return !itemsInput.isEmpty();
    }

    public boolean hasOutputItems() {
        return !itemsOutput.isEmpty();
    }

    public boolean hasInputFluids() {
        return !fluidsInput.isEmpty();
    }

    public boolean hasOutputFluids() {
        return !fluidsOutput.isEmpty();
    }

    public boolean hasOutputChances() {
        return outputChances != null && outputChances.length == itemsOutput.size();
    }

    @Override
    public boolean hasInputChances() {
        return inputChances != null && inputChances.length == itemsInput.size();
    }

    public void setIds(ResourceLocation id, String map) {
        this.id = id;
        this.mapId = map;
    }

    @Override
    public void setId(ResourceLocation id) {
        this.id = id;
    }

    @Override
    public void setMapId(String mapId) {
        this.mapId = mapId;
    }

    public void sortInputItems() {
        this.itemsInput.sort((a, b) -> {
            boolean a1 = RecipeMap.isIngredientSpecial(a);
            boolean a2 = RecipeMap.isIngredientSpecial(b);
            if (a1 == a2) return 0;
            if (a1) return 1;
            return -1;
        });
    }

    @NotNull
    public List<Ingredient> getInputItems() {
        return hasInputItems() ? itemsInput : Collections.emptyList();
    }

    @NotNull
    public List<RecipeIngredient> getCastedInputs() {
        return hasInputItems() ? itemsInput.stream().filter(t -> t instanceof RecipeIngredient).map(t -> (RecipeIngredient)t).collect(Collectors.toList()) : Collections.emptyList();
    }


    public List<ItemStack> getOutputItems() {
        return getOutputItems(true);
    }

    public List<ItemStack> getOutputItems(boolean chance) {
        if (hasOutputItems()) {
            List<ItemStack> outputs = new ArrayList<>(itemsOutput);
            if (outputChances != null) {
                List<ItemStack> evaluated = new ObjectArrayList<>();
                for (int i = 0; i < outputs.size(); i++) {
                    if (!chance || Ref.RNG.nextInt(10000) < outputChances[i]) {
                        evaluated.add(outputs.get(i).copy());
                    }
                }
                outputs = evaluated;
            }
            return outputs;
        }
        return List.of();
    }

    /**
     * Returns a list of items not bound by chances.
     *
     * @return list of items.
     */
    public List<ItemStack> getFlatOutputItems() {
        if (hasOutputItems()) {
            List<ItemStack> outputs = new ArrayList<>(itemsOutput);
            if (outputChances != null) {
                List<ItemStack> evaluated = new ObjectArrayList<>();
                for (int i = 0; i < outputs.size(); i++) {
                    if (outputChances[i] < 10000) continue;
                    evaluated.add(outputs.get(i));
                }
                outputs = evaluated;
            }
            return outputs;
        }
        return List.of();
    }

    //Note: does call get().
    public boolean hasSpecialIngredients() {
        for (Ingredient ingredient : itemsInput) {
            if (RecipeMap.isIngredientSpecial(ingredient)) {
                return true;
            }
        }
        return false;
    }

    @NotNull
    public List<FluidIngredient> getInputFluids() {
        return fluidsInput;
    }

    public List<FluidStack> getOutputFluids() {
        return fluidsOutput;
    }

    public int @Nullable [] getOutputChances() {
        return outputChances;
    }

    @Override
    public int @Nullable [] getInputChances() {
        return inputChances;
    }

    public int getSpecialValue() {
        return special;
    }

    @Override
    public boolean isFake() {
        return fake;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (!itemsInput.isEmpty()) {
            builder.append("\nInput Items: { ");
            for (int i = 0; i < itemsInput.size(); i++) {
                builder.append("\n Item ").append(i);
                //builder.append(itemsInput.get(i).get().getMatchingStacks()[0].getDisplayName()).append(" x").append(itemsInput.get(i).get().getMatchingStacks()[0].getCount());
                builder.append(itemsInput.get(i).toJson());
                if (i != itemsInput.size() - 1) builder.append(", ");
            }
            builder.append(" }\n");
        }
        if (itemsOutput != null) {
            builder.append("Output Items: { ");
            for (int i = 0; i < itemsOutput.size(); i++) {
                builder.append(itemsOutput.get(i).getHoverName()).append(" x").append(itemsOutput.get(i).getCount());
                if (i != itemsOutput.size() - 1) builder.append(", ");
            }
            builder.append(" }\n");
        }
        if (!fluidsInput.isEmpty()) {
            builder.append("Input Fluids: { ");
            //for (int i = 0; i < fluidsInput.size(); i++) {
            //    builder.append(fluidsInput.get(i).getFluid().getRegistryName()).append(": ").append(fluidsInput[i].getAmount()).append("mb");
            //    if (i != fluidsInput.length - 1) builder.append(", ");
            // }
            builder.append(" }\n");
        }
        if (fluidsOutput != null) {
            builder.append("Output Fluids: { ");
            for (int i = 0; i < fluidsOutput.size(); i++) {
                builder.append(RegistryUtils.getIdFromFluid(fluidsOutput.get(i).getFluid())).append(": ").append(fluidsOutput.get(i).getAmount()).append("mb");
                if (i != fluidsOutput.size() - 1) builder.append(", ");
            }
            builder.append(" }\n");
        }
        if (outputChances != null) {
            builder.append("Output Chances: { ");
            for (int i = 0; i < outputChances.length; i++) {
                builder.append((float) outputChances[i] / 100).append("%");
                if (i != outputChances.length - 1) builder.append(", ");
            }
            builder.append(" }\n");
        }

        if (inputChances != null) {
            builder.append("Input Chances: { ");
            for (int i = 0; i < inputChances.length; i++) {
                builder.append((float) inputChances[i] / 100).append("%");
                if (i != inputChances.length - 1) builder.append(", ");
            }
            builder.append(" }\n");
        }
        builder.append("Special: ").append(special).append("\n");
        return builder.toString();
    }

    @Override
    public boolean matches(Container inv, Level worldIn) {
        return false;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return ItemStack.EMPTY;
    }

    @Override
    public String getMapId() {
        return mapId;
    }

    @Override
    public ResourceLocation getId() {
        return id != null ? id : new ResourceLocation(Ref.ID, "default");
    }

    @Override
    public net.minecraft.world.item.crafting.RecipeSerializer<?> getSerializer() {
        return MachineRecipeSerializer.INSTANCE;
    }

    @NotNull
    @Override
    public RecipeType<?> getType() {
        return Recipe.RECIPE_TYPE;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public List<IRecipeValidator> getValidators() {
        return validators;
    }

}

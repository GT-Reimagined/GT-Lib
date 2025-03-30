package org.gtreimagined.gtlib.datagen.providers;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.datagen.builder.GTShapedRecipeBuilder;
import org.gtreimagined.gtlib.datagen.builder.GTShapelessRecipeBuilder;
import org.gtreimagined.gtlib.datagen.builder.SequencedAssemblyBuilder;
import org.gtreimagined.gtlib.recipe.condition.ConfigCondition;
import org.gtreimagined.gtlib.recipe.condition.TomlConfigCondition;
import org.gtreimagined.gtlib.recipe.ingredient.PropertyIngredient;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.Registry;
import net.minecraft.data.HashCache;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.crafting.ConditionalRecipe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.gtreimagined.gtlib.util.TagUtils.nc;

public class GTRecipeProvider extends RecipeProvider {

    protected final String providerDomain, providerName;
    @Getter
    private static final Set<ResourceLocation> RECIPES_TO_REMOVE = new HashSet<>();

    @SuppressWarnings("ConstantConditions")
    public GTRecipeProvider(String providerDomain, String providerName) {
        super(null);
        this.providerDomain = providerDomain;
        this.providerName = providerName;
    }

    @Override
    public void run(HashCache cache) {

    }

    public void removeRecipe(ResourceLocation recipeId){
        RECIPES_TO_REMOVE.add(recipeId);
    }

    public SequencedAssemblyBuilder getSequencedAssemblyRecipe(Ingredient input){
        return new SequencedAssemblyBuilder(input);
    }

    public void addConditionalRecipe(Consumer<FinishedRecipe> consumer, GTShapedRecipeBuilder builtRecipe, Class configClass, String configFieldName, String recipeDomain, String recipeName) {
        ConditionalRecipe.builder().addCondition(new ConfigCondition(configClass, configFieldName))
                .addRecipe(builtRecipe::build).build(consumer, recipeDomain, recipeName);
    }

    public void addConditionalRecipe(Consumer<FinishedRecipe> consumer, GTShapedRecipeBuilder builtRecipe, String config, String configField, String recipeDomain, String recipeName) {
        ConditionalRecipe.builder().addCondition(new TomlConfigCondition(config, configField))
                .addRecipe(builtRecipe::build).build(consumer, recipeDomain, recipeName);
    }

    public GTShapedRecipeBuilder getItemRecipe(String groupName, boolean customCriterion, ItemLike output, ImmutableMap<Character, Object> inputs, String... inputPattern) {
        return getStackRecipe(groupName, customCriterion, new ItemStack(output), inputs, inputPattern);
    }

    public GTShapedRecipeBuilder getStackRecipe(String groupName, boolean customCriterion, ItemStack output, ImmutableMap<Character, Object> inputs, String... inputPattern) {
        return getStackRecipe(groupName, customCriterion, Collections.singletonList(output), inputs, inputPattern);
    }

    public GTShapedRecipeBuilder getStackRecipe(String groupName, boolean customCriterion, List<ItemStack> output, ImmutableMap<Character, Object> inputs, String... inputPattern) {
        if (inputs.isEmpty()) Utils.onInvalidData("Inputs should not be empty!");
        if (inputPattern.length < 1 || inputPattern.length > 3)
            Utils.onInvalidData("Input pattern must have between 1 and 3 rows!");
        GTShapedRecipeBuilder recipeBuilder = GTShapedRecipeBuilder.shapedRecipe(output);
        recipeBuilder = resolveKeys(recipeBuilder, inputs);
        for (String s : inputPattern) {
            if (s.length() > 3) Utils.onInvalidData("Input pattern rows must have between 0 and 3 characters!");
            recipeBuilder = recipeBuilder.patternLine(s);
        }
        recipeBuilder = recipeBuilder.setGroup(groupName);
        if (!customCriterion){
            List<String> criteria = new ArrayList<>();
            for (Object o : inputs.values()) {
                if (o instanceof TagKey<?> tag && tag.registry() == Registry.ITEM_REGISTRY) {
                    String id = "has_" + tag.location().getPath();
                    if (criteria.contains(id)) continue;
                    recipeBuilder.addCriterion(id, hasSafeItem((TagKey<Item>) tag));
                    criteria.add(id);
                } else if (o instanceof ItemLike itemLike){
                    String id = "has_" + RegistryUtils.getIdFromItem(itemLike.asItem()).getPath();
                    if (criteria.contains(id)) continue;
                    recipeBuilder.addCriterion(id, hasSafeItem(itemLike));
                    criteria.add(id);
                }
            }
            if (criteria.isEmpty()){
                recipeBuilder.addCriterion("has_wrench", hasSafeItem(GTTools.WRENCH.getTag()));
            }
        }
        return recipeBuilder;
    }

    public void shapeless(Consumer<FinishedRecipe> consumer, String recipeID, String groupName, ItemStack output, Object... inputs) {
        shapeless(consumer, Ref.ID, recipeID, groupName, output, inputs);
    }

    public void shapeless(Consumer<FinishedRecipe> consumer, String domain, String recipeID, String groupName, ItemStack output, Object... inputs) {
        GTShapelessRecipeBuilder builder = GTShapelessRecipeBuilder.shapeless(output.getItem(), output.getCount())
                .group(groupName);
        List<String> criteria = new ArrayList<>();
        for (Object input : inputs) {
            try {
                if (input instanceof ItemLike l) {
                    builder.requires(l);
                    String id = "has_" + RegistryUtils.getIdFromItem(l.asItem()).getPath();
                    if (criteria.contains(id)) continue;
                    builder.unlockedBy(id, hasSafeItem(l));
                    criteria.add(id);
                } else if (input instanceof TagKey tagKey) {
                    builder.requires(nc(TagUtils.getItemTag(tagKey.location()).location()));
                    String id = "has_" + tagKey.location().getPath();
                    if (criteria.contains(id)) continue;
                    builder.unlockedBy(id, hasSafeItem((TagKey<Item>) tagKey));
                    criteria.add(id);
                } else if (input instanceof Ingredient i) {
                    builder.requires(i);
                }
            } catch (ClassCastException ex) {
                throw new RuntimeException("ERROR PARSING SHAPELESS RECIPE" + ex.getMessage());
            }
        }
        if (criteria.isEmpty()){
            builder.unlockedBy("has_wrench", hasSafeItem(GTTools.WRENCH.getTag()));
        }
        if (recipeID.isEmpty())builder.save(consumer);
        else {
            if (domain.isEmpty()) builder.save(consumer, recipeID);
            else builder.save(consumer, fixLoc(domain, recipeID));
        }
    }

    public void addItemRecipe(Consumer<FinishedRecipe> consumer, String groupName, ItemLike output, ImmutableMap<Character, Object> inputs, String... inputPattern) {
        addStackRecipe(consumer, "", "", groupName, new ItemStack(output), inputs, inputPattern);
    }

    public void addItemRecipe(Consumer<FinishedRecipe> consumer, String recipeDomain, String recipeName, String groupName, ItemLike output, ImmutableMap<Character, Object> inputs, String... inputPattern) {
        addStackRecipe(consumer, recipeDomain, recipeName, groupName, new ItemStack(output), inputs, inputPattern);
    }

    public void addStackRecipe(Consumer<FinishedRecipe> consumer, String recipeDomain, String recipeName, String groupName, ItemStack output, Function<ImmutableMap.Builder<Character, Object>, ImmutableMap.Builder<Character, Object>> inputs, String... inputPattern) {
        addStackRecipe(consumer, recipeDomain, recipeName, groupName, output, inputs.apply(new ImmutableMap.Builder<>()).build(), inputPattern);
    }

    public void addStackRecipe(Consumer<FinishedRecipe> consumer, String groupName, ItemStack output, ImmutableMap<Character, Object> inputs, String... inputPattern) {
        addStackRecipe(consumer, "", "", groupName, output, inputs, inputPattern);
    }

    public void addStackRecipe(Consumer<FinishedRecipe> consumer, String recipeDomain, String recipeName, String groupName, ItemStack output, ImmutableMap<Character, Object> inputs, String... inputPattern) {
        GTShapedRecipeBuilder recipeBuilder = getStackRecipe(groupName, false, output, inputs, inputPattern);
        if (recipeName.isEmpty()) recipeBuilder.build(consumer);
        else {
            if (recipeDomain.isEmpty()) recipeBuilder.build(consumer, recipeName);
            else recipeBuilder.build(consumer, fixLoc(recipeDomain, recipeName));
        }
    }

    public void addToolRecipe(String builder, Consumer<FinishedRecipe> consumer, String recipeDomain, String recipeName, String groupName, ItemStack output, ImmutableMap<Character, Object> inputs, String... inputPattern) {
        if (output.isEmpty()) {
            GTLib.LOGGER.warn("Material recipe " + recipeDomain + ":" + recipeName + "has an empty output.");
            return;
        }
        GTShapedRecipeBuilder recipeBuilder = getStackRecipe(groupName, false, output, inputs, inputPattern);
        if (recipeName.isEmpty()) recipeBuilder.buildTool(consumer, builder);
        else {
            if (recipeDomain.isEmpty()) recipeBuilder.buildTool(consumer, builder, recipeName);
            else recipeBuilder.buildTool(consumer, builder, fixLoc(recipeDomain, recipeName));
        }
    }

    @SuppressWarnings("unchecked")
    protected GTShapedRecipeBuilder resolveKeys(GTShapedRecipeBuilder incompleteBuilder, ImmutableMap<Character, Object> inputs) {
        for (Map.Entry<Character, Object> entry : inputs.entrySet()) {
            if (entry.getValue() instanceof ItemLike l) {
                incompleteBuilder = incompleteBuilder.key(entry.getKey(), l);
            } else if (entry.getValue() instanceof ItemStack stack){
                incompleteBuilder = incompleteBuilder.key(entry.getKey(), stack.getItem());
            } else if (entry.getValue() instanceof TagKey tagKey) {
                try {
                    //Wrap the tag using tag manager.
                    incompleteBuilder = incompleteBuilder.key(entry.getKey(), nc(tagKey.location()));
                } catch (ClassCastException e) {
                    Utils.onInvalidData("Tag inputs only allow Item Tags!");
                }
            } else if (entry.getValue() instanceof PropertyIngredient pi) {
                incompleteBuilder = incompleteBuilder.key(entry.getKey(), pi);
            } else if (entry.getValue() instanceof Ingredient i) {
                incompleteBuilder = incompleteBuilder.key(entry.getKey(), i);
            }
        }
        return incompleteBuilder;
    }

    public String fixLoc(String providerDomain, String attach) {
        return providerDomain.concat(":").concat(attach);
    }

    @Override
    public String getName() {
        return providerName;
    }

    public CriterionTriggerInstance hasSafeItem(TagKey<Item> tag) {
        return inventoryTrigger(ItemPredicate.Builder.item().of(tag).build());
    }

    public CriterionTriggerInstance hasSafeItem(ItemLike stack) {
        return RecipeProvider.has(stack);
    }
}

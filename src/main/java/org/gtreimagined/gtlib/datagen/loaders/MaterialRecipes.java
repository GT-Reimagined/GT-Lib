package org.gtreimagined.gtlib.datagen.loaders;

import com.google.common.collect.ImmutableMap;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import org.gtreimagined.gtlib.datagen.builder.GTCookingRecipeBuilder;
import org.gtreimagined.gtlib.datagen.providers.GTRecipeProvider;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.material.MaterialTypeItem;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.world.item.ItemStack;

import java.util.function.Consumer;

import static com.google.common.collect.ImmutableMap.of;
import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;
import static org.gtreimagined.gtlib.material.MaterialTags.*;

public class MaterialRecipes {
    public static void init(Consumer<FinishedRecipe> consumer, GTRecipeProvider provider) {
        GTMaterialTypes.DUST.all().forEach(m -> {
            provider.addStackRecipe(consumer, Ref.ID, m.getId() + "_dust_small", "gtlib_dusts", GTMaterialTypes.DUST.get(m, 1), of('D', GTMaterialTypes.DUST_SMALL.getMaterialTag(m)), "DD", "DD");
            provider.addStackRecipe(consumer, Ref.ID, m.getId() + "_dust_tiny", "gtlib_dusts", GTMaterialTypes.DUST.get(m, 1), of('D', GTMaterialTypes.DUST_TINY.getMaterialTag(m)), "DDD", "DDD", "DDD");
        });
        GTMaterialTypes.INGOT.all().forEach(m -> {
            if (m.has(GTMaterialTypes.NUGGET) && (!INGOT.hasReplacement(m) || !NUGGET.hasReplacement(m))){
                provider.addItemRecipe(consumer, Ref.ID, m.getId() + "_ingot", "ingots", GTMaterialTypes.INGOT.get(m), ImmutableMap.of('I', GTMaterialTypes.NUGGET.getMaterialTag(m)), "III", "III", "III");
                provider.shapeless(consumer,"nugget_" + m.getId() + "_from_ingot", "ingots", GTMaterialTypes.NUGGET.get(m, 9), GTMaterialTypes.INGOT.getMaterialTag(m));
            }
            if (m.has(GTMaterialTypes.CHUNK)){
                provider.addItemRecipe(consumer, Ref.ID, m.getId() + "_ingot_from_chunk", "ingots", INGOT.get(m), ImmutableMap.of('I', CHUNK.getMaterialTag(m)), "II", "II");
            }
        });
        GTMaterialTypes.RAW_ORE.all().stream().filter(m -> !m.has(HAS_CUSTOM_SMELTING) && SMELT_INTO.getMapping(m).has(GTMaterialTypes.INGOT)).forEach(m -> {
            if (!INGOT.hasReplacement(m) && !RAW_ORE.hasReplacement(m)) {
                addSmeltingRecipe(consumer, provider, GTMaterialTypes.RAW_ORE, GTMaterialTypes.INGOT, 1, m, SMELT_INTO.getMapping(m));
            }
        });
        GTMaterialTypes.ORE.all().stream().filter(m -> !m.has(HAS_CUSTOM_SMELTING) && SMELT_INTO.getMapping(m).has(GTMaterialTypes.INGOT)).forEach(m -> {
            if (!ORE.hasReplacement(m) && !INGOT.hasReplacement(m)) {
                addSmeltingRecipe(consumer, provider, GTMaterialTypes.ORE, GTMaterialTypes.INGOT, 1, m, SMELT_INTO.getMapping(m));
            }
        });
        GTMaterialTypes.CRUSHED.all().stream().filter(m -> !m.has(HAS_CUSTOM_SMELTING) && SMELT_INTO.getMapping(m).has(GTMaterialTypes.INGOT)).forEach(m -> {
            if (m != SMELT_INTO.getMapping(m) || !m.has(GTMaterialTypes.NUGGET)) return;
            addSmeltingRecipe(consumer, provider, GTMaterialTypes.CRUSHED, GTMaterialTypes.NUGGET, 12, m);
            addSmeltingRecipe(consumer, provider, GTMaterialTypes.DUST_IMPURE, INGOT, 1, m);
            addSmeltingRecipe(consumer, provider, GTMaterialTypes.CRUSHED_PURIFIED, GTMaterialTypes.NUGGET, 11, m);
            addSmeltingRecipe(consumer, provider, GTMaterialTypes.DUST_PURE, INGOT, 1, m);
            addSmeltingRecipe(consumer, provider, GTMaterialTypes.CRUSHED_REFINED, GTMaterialTypes.NUGGET, 10, m);
        });
        GTMaterialTypes.DUST.all().forEach(m -> {
            if (m.has(MaterialTags.HAS_CUSTOM_SMELTING)) return;
            if (DIRECT_SMELT_INTO.getMapping(m).has(GTMaterialTypes.INGOT)) {
                addSmeltingRecipe(consumer, provider, GTMaterialTypes.DUST, GTMaterialTypes.INGOT, 1, m, DIRECT_SMELT_INTO.getMapping(m));
            }
            if (DIRECT_SMELT_INTO.getMapping(m).has(GTMaterialTypes.NUGGET)) {
                addSmeltingRecipe(consumer, provider, GTMaterialTypes.DUST_TINY, GTMaterialTypes.NUGGET, 1, m, DIRECT_SMELT_INTO.getMapping(m));;
            }
            if (DIRECT_SMELT_INTO.getMapping(m).has(CHUNK)) {
                addSmeltingRecipe(consumer, provider, GTMaterialTypes.DUST_SMALL, CHUNK, 1, m, DIRECT_SMELT_INTO.getMapping(m));;
            }
        });
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> consumer, GTRecipeProvider provider, MaterialType<?> input, MaterialTypeItem<?> output, int amount, Material in){
        addSmeltingRecipe(consumer, provider, input, output, amount, in, in);
    }

    public static void addSmeltingRecipe(Consumer<FinishedRecipe> consumer, GTRecipeProvider provider, MaterialType<?> input, MaterialTypeItem<?> output, int amount, Material in, Material out){
        GTCookingRecipeBuilder.blastingRecipe(RecipeIngredient.of(input.getMaterialTag(in), 1), new ItemStack(output.get(out), MaterialTags.SMELTING_MULTI.getInt(in) * amount), 2.0F, 100)
                .addCriterion("has_material_" + in.getId(), provider.hasSafeItem(output.getMaterialTag(out)))
                .build(consumer, provider.fixLoc(Ref.ID, in.getId().concat("_" + input.getId() + "_to_" + output.getId())));
        GTCookingRecipeBuilder.smeltingRecipe(RecipeIngredient.of(input.getMaterialTag(in), 1), new ItemStack(output.get(out), MaterialTags.SMELTING_MULTI.getInt(in) * amount), 2.0F, 200)
                .addCriterion("has_material_" + in.getId(), provider.hasSafeItem(output.getMaterialTag(out)))
                .build(consumer, provider.fixLoc(Ref.ID, in.getId().concat("_" + input.getId() + "_to_" + output.getId() + "_smelting")));
    }
}

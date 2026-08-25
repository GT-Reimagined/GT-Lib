package org.gtreimagined.gtlib.integration.recipeviewer.emi;

import brachy.modularui.integration.emi.recipe.ModularUIEmiCategory;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibRecipeViewerPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.StoneVein;
import org.gtreimagined.gtlib.integration.recipeviewer.emi.recipe.RecipeMapRecipe;
import org.gtreimagined.gtlib.integration.recipeviewer.emi.recipe.SmallOreRecipe;
import org.gtreimagined.gtlib.integration.recipeviewer.emi.recipe.StoneVeinRecipe;
import org.gtreimagined.gtlib.integration.recipeviewer.emi.recipe.VeinRecipe;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.int4;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOreData;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerData;
import org.gtreimagined.gtlib.worldgen.vein.VeinData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EmiEntrypoint
public class GTLibEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry emiRegistry) {
        if (FMLEnvironment.production) return;
        List<ItemLike> list = new ArrayList<>();
        GTLibRecipeViewerPlugin.getItemsToHide().forEach(c -> c.accept(list));
        if (!list.isEmpty()) {
            emiRegistry.removeEmiStacks(s -> s.getKey() instanceof Item i && list.contains(i));
        }
        List<Fluid> fluidList = new ArrayList<>();
        GTLibRecipeViewerPlugin.getFluidsToHide().forEach(c -> c.accept(fluidList));
        List<Item> buckets = fluidList.stream().map(Fluid::getBucket).toList();
        if (!fluidList.isEmpty()){
            emiRegistry.removeEmiStacks(s -> s.getKey() instanceof Fluid f && fluidList.contains(f));
            emiRegistry.removeEmiStacks(s -> s.getKey() instanceof Item i && buckets.contains(i));
        }

        GTLibRecipeViewerPlugin.getREGISTRY().forEach((id, tuple) -> {
            GuiProperties gui = tuple.gui;
            int4 area = gui.getArea();
            Tier tier = tuple.map.getGuiTier() != null ? tuple.map.getGuiTier() : tuple.tier;
            EmiRecipeCategory mainCategory = new ModularUIEmiCategory(tuple.map.getLoc(),
                    createIcon(tuple.map.getIcon(), tuple.workstations.isEmpty() ? null : tuple.workstations.get(0)));
            emiRegistry.addCategory(mainCategory);
            Map<String, EmiRecipeCategory> subCategories = new HashMap<>();
            if (!tuple.map.getSubCategories().isEmpty()){
                tuple.map.getSubCategories().forEach((s, subCategory) -> {
                    ResourceLocation subCategoryId = new ResourceLocation(tuple.map.getDomain(), s);
                    EmiRecipeCategory subEmiCategory = new ModularUIEmiCategory(subCategoryId,
                            createIcon(subCategory.icon().get(), null));
                    subCategories.put(subCategoryId.toString(), subEmiCategory);
                    emiRegistry.addCategory(subEmiCategory);
                });
            }
            tuple.workstations.forEach(s -> {
                ItemLike item = RegistryUtils.getItemFromID(s);
                if (item == Items.AIR) return;
                emiRegistry.addWorkstation(mainCategory, EmiStack.of(item));
                if (!subCategories.isEmpty()){
                    subCategories.values().forEach(c -> emiRegistry.addWorkstation(c, EmiStack.of(item)));
                }
            });
            if (tuple.map.getSubCategories().isEmpty()) {
                List<IRecipe> recipes = GTLibRecipeViewerPlugin.getRecipes(tuple.map, emiRegistry.getRecipeManager());
                recipes.forEach(r -> {
                    emiRegistry.addRecipe(new RecipeMapRecipe(mainCategory, r, gui, tier));
                });
            } else {
                List<IRecipe> recipes = GTLibRecipeViewerPlugin.getRecipes(tuple.map, emiRegistry.getRecipeManager());
                List<IRecipe> mainRecipes = new ArrayList<>();
                Map<String, List<IRecipe>> recipeMap = new HashMap<>();
                for (IRecipe recipe : recipes) {
                    boolean found = false;
                    for (var entry : tuple.map.getSubCategories().entrySet()) {
                        if (entry.getValue().predicate().test(recipe)) {
                            found = true;
                            recipeMap.computeIfAbsent(entry.getKey(), (s) -> new ArrayList<>()).add(recipe);
                            break;
                        }
                    }
                    if (!found) {
                        mainRecipes.add(recipe);
                    }
                }
                mainRecipes.forEach(r -> {
                    emiRegistry.addRecipe(new RecipeMapRecipe(mainCategory, r, gui, tier));
                });
                for (var entry : recipeMap.entrySet()) {
                    EmiRecipeCategory recipeCategory = subCategories.get(tuple.map.getDomain() + ":" + entry.getKey());
                    entry.getValue().forEach(r -> {
                        emiRegistry.addRecipe(new RecipeMapRecipe(recipeCategory, r, gui, tier));
                    });
                }
            }
        });
        emiRegistry.addCategory(SmallOreRecipe.CATEGORY);
        emiRegistry.addCategory(VeinRecipe.CATEGORY);
        emiRegistry.addCategory(StoneVeinRecipe.CATEGORY);
        SmallOreData.INSTANCE.getVeins().values().stream().map(SmallOreRecipe::new).forEach(emiRegistry::addRecipe);
        VeinData.INSTANCE.getVeins().values().stream().map(VeinRecipe::new).forEach(emiRegistry::addRecipe);
        Object2IntMap<StoneType> veinTotalWeights = new Object2IntOpenHashMap<>();
        StoneLayerData.INSTANCE.getVeins().forEach((r, l) -> {
            if (l.type() == null) return;
            int currentWeight = veinTotalWeights.getOrDefault(l.type(), 0);
            veinTotalWeights.put(l.type(), currentWeight + l.weight());
        });
        StoneLayerData.INSTANCE.getVeins().forEach((r, v) -> {
            if (!veinTotalWeights.containsKey(v.type())) return;
            v.ores().forEach(o -> {
                emiRegistry.addRecipe(new StoneVeinRecipe(new StoneVein(v, o, veinTotalWeights.getOrDefault(v.type(), 0))));
            });
        });
    }

    private EmiRenderable createIcon(Object icon, ResourceLocation iconId){
        EmiRenderable renderable = EmiStack.of(Data.DEBUG_SCANNER);
        if (icon != null) {
            if (icon instanceof ItemStack itemStack) {
                renderable = EmiStack.of(itemStack);
            }
            if (icon instanceof ItemLike item) {
                renderable = EmiStack.of(item);
            }
            if (icon instanceof ResourceLocation resourceLocation) {
                renderable = new EmiTexture(resourceLocation, 0, 0, 16, 16, 16, 16, 16, 16);
            }
        } else {
            Item item = iconId == null ? Data.DEBUG_SCANNER : RegistryUtils.getItemFromID(iconId);
            if (item == Items.AIR) item = Data.DEBUG_SCANNER;
            renderable = EmiStack.of(item);
        }
        return renderable;
    }

    public static void showRecipes(ResourceLocation... locations){
        List<ResourceLocation> locations1 = List.of(locations);
        EmiApi.getRecipeManager().getCategories().stream().filter(c -> locations1.contains(c.getId())).forEach(EmiApi::displayRecipeCategory);
    }
}

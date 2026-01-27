package org.gtreimagined.gtlib.integration.emi;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiRenderable;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.gui.GuiData;
import org.gtreimagined.gtlib.integration.emi.recipe.RecipeMapRecipe;
import org.gtreimagined.gtlib.integration.jei.category.RecipeMapCategory;
import org.gtreimagined.gtlib.integration.xei.GTLibXEIPlugin;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.map.SubCategory;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.int4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EmiEntrypoint
public class GTLibEmiPlugin implements EmiPlugin {
    @Override
    public void register(EmiRegistry emiRegistry) {
        if (FMLEnvironment.production) return;
        List<ItemLike> list = new ArrayList<>();
        GTLibXEIPlugin.getItemsToHide().forEach(c -> c.accept(list));
        if (!list.isEmpty()) {
            emiRegistry.removeEmiStacks(s -> s.getKey() instanceof Item i && list.contains(i));
        }
        List<Fluid> fluidList = new ArrayList<>();
        GTLibXEIPlugin.getFluidsToHide().forEach(c -> c.accept(fluidList));
        List<Item> buckets = fluidList.stream().map(Fluid::getBucket).toList();
        if (!fluidList.isEmpty()){
            emiRegistry.removeEmiStacks(s -> s.getKey() instanceof Fluid f && fluidList.contains(f));
            emiRegistry.removeEmiStacks(s -> s.getKey() instanceof Item i && buckets.contains(i));
        }

        GTLibXEIPlugin.getREGISTRY().forEach((id, tuple) -> {
            GuiData gui = tuple.gui;
            int4 area = gui.getArea();
            Tier tier = tuple.map.getGuiTier() != null ? tuple.map.getGuiTier() : tuple.tier;
            EmiRecipeCategory mainCategory = new EmiRecipeCategory(tuple.map.getLoc(),
                    createIcon(tuple.map.getIcon(), tuple.workstations.isEmpty() ? null : tuple.workstations.get(0)));
            emiRegistry.addCategory(mainCategory);
            Map<String, EmiRecipeCategory> subCategories = new HashMap<>();
            if (!tuple.map.getSubCategories().isEmpty()){
                tuple.map.getSubCategories().forEach((s, subCategory) -> {
                    ResourceLocation subCategoryId = new ResourceLocation(Ref.SHARED_ID, s);
                    EmiRecipeCategory subEmiCategory = new EmiRecipeCategory(subCategoryId,
                            createIcon(subCategory.icon().get(), null));
                    subCategories.put(s, subEmiCategory);
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
                List<IRecipe> recipes = GTLibXEIPlugin.getRecipes(tuple.map, emiRegistry.getRecipeManager());
                recipes.forEach(r -> {
                    emiRegistry.addRecipe(new RecipeMapRecipe(mainCategory, r, gui, tier));
                });
            } else {
                List<IRecipe> recipes = GTLibXEIPlugin.getRecipes(tuple.map, emiRegistry.getRecipeManager());
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
                    EmiRecipeCategory recipeCategory = subCategories.get(entry.getKey());
                    entry.getValue().forEach(r -> {
                        emiRegistry.addRecipe(new RecipeMapRecipe(recipeCategory, r, gui, tier));
                    });
                }
            }
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
}

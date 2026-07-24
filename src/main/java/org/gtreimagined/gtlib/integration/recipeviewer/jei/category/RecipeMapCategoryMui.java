package org.gtreimagined.gtlib.integration.recipeviewer.jei.category;

import brachy.modularui.integration.jei.recipe.ModularUIRecipeCategory;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.ItemLike;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.integration.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.widgets.RecipeWidget;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.SubCategory;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.Utils;
import org.jetbrains.annotations.Nullable;

public class RecipeMapCategoryMui extends ModularUIRecipeCategory<IRecipe> {
    protected final RecipeType<IRecipe> type;
    protected Component title;
    protected IDrawable icon;
    private RecipeMapCategoryMui(IRecipeMap map, RecipeType<IRecipe> type, GuiProperties gui, Tier defaultTier){
        super(r -> new RecipeWidget(r, map, gui, map.getGuiTier() == null ? defaultTier : map.getGuiTier()), Recipe::getId);
        this.type = type;
    }

    public RecipeMapCategoryMui(IRecipeMap map, RecipeType<IRecipe> type, GuiProperties gui, Tier defaultTier, ResourceLocation iconId){
        this(map, type, gui, defaultTier);
        title = map.getDisplayName();
        initIcon(map.getIcon(), iconId);
    }

    public RecipeMapCategoryMui(IRecipeMap map, RecipeType<IRecipe> type, GuiProperties gui, Tier defaultTier, ResourceLocation subCategoryId, SubCategory subCategory){
        this(map, type, gui, defaultTier);
        title = Utils.translatable(subCategory.langKey());
        initIcon(subCategory.icon().get(), null);
    }

    private void initIcon(Object icon, ResourceLocation iconId){
        if (icon != null) {
            if (icon instanceof ItemStack itemStack) {
                this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, itemStack);
            }
            if (icon instanceof ItemLike item) {
                this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item));
            }
            if (icon instanceof ResourceLocation resourceLocation){
                this.icon = GTLibJEIPlugin.guiHelper.drawableBuilder(resourceLocation, 0, 0, 16, 16).setTextureSize(16, 16).build();
            }
        } else {
            Item item = iconId == null ? Data.DEBUG_SCANNER : RegistryUtils.getItemFromID(iconId);
            if (item == Items.AIR) item = Data.DEBUG_SCANNER;
            this.icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(item, 1));
        }
    }


    @Override
    public RecipeType<IRecipe> getRecipeType() {
        return type;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return icon;
    }
}

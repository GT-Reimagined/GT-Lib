package org.gtreimagined.gtlib.integration.recipeviewer.jei;

import com.google.common.collect.Sets;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

@SuppressWarnings("removal")
public class MultiMachineInfoCategory implements IRecipeCategory<MultiMachineInfoPage> {
    private static IGuiHelper guiHelper;
    private final IDrawable background;
    private final IDrawable icon;
    private static final Set<MultiMachineInfoPage> MULTI_MACHINES_PAGES = Sets.newHashSet();
    private static final RecipeType<MultiMachineInfoPage> RECIPE_TYPE = RecipeType.create(Ref.SHARED_ID, "multi_machine_info", MultiMachineInfoPage.class);

    public MultiMachineInfoCategory() {
        this.background = guiHelper.createBlankDrawable(176, 150);
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Data.DEBUG_SCANNER, 1));
    }

    public static void setGuiHelper(IGuiHelper guiHelper) {
        MultiMachineInfoCategory.guiHelper = guiHelper;
    }
    
    public static void addMultiMachine(MultiMachineInfoPage page) {
        MULTI_MACHINES_PAGES.add(page);
    }

    public static void registerRecipes(IRecipeRegistration registry) {
        registry.addRecipes(RECIPE_TYPE, MULTI_MACHINES_PAGES.stream().toList());
    }

    /*@Override
    public boolean handleClick(@NotNull MultiMachineInfoPage recipe, double mouseX, double mouseY, int mouseButton) {
        return recipe.handleClick(mouseX, mouseY, mouseButton);
    }*/


    @Override
    public void draw(MultiMachineInfoPage recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        //recipe.drawInfo(matrixStack, (int)mouseX, (int)mouseY);
    }

    @NotNull
    @Override
    public Component getTitle() {
        return Utils.literal( "Multi Machines Title");
    }

    @NotNull
    @Override
    public IDrawable getBackground() {
        return background;
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public RecipeType<MultiMachineInfoPage> getRecipeType() {
        return RECIPE_TYPE;
    }

    /*@Override
    public void setIngredients(@NotNull MultiMachineInfoPage recipe, @NotNull IIngredients ingredients) {
        recipe.setIngredients(ingredients);
    }*/
    
    /*@NotNull
    @Override
    public List<Component> getTooltipStrings(@NotNull MultiMachineInfoPage recipe, double mouseX, double mouseY) {
        return recipe.getTooltipStrings(mouseX, mouseY);
    }*/

    @Override
    public void setRecipe(IRecipeLayoutBuilder recipeLayout, MultiMachineInfoPage recipe, IFocusGroup iFocusGroup) {
        //recipe.setRecipeLayout(recipeLayout, guiHelper);
    }
}

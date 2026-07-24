package org.gtreimagined.gtlib.integration.recipeviewer.jei;

import brachy.modularui.drawable.GuiTextures;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.integration.xei.StoneVein;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.text.DecimalFormat;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.ORE;
import static org.gtreimagined.gtlib.integration.recipeviewer.jei.RecipeMapCategory.JEI_OFFSET_X;
import static org.gtreimagined.gtlib.integration.recipeviewer.jei.RecipeMapCategory.JEI_OFFSET_Y;

public class StoneVeinCategory implements IRecipeCategory<StoneVein> {
    IDrawable icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.IRON_ORE.getDefaultInstance());
    IDrawable background = GTLibJEIPlugin.guiHelper.drawableBuilder(GuiTextures.MC_BACKGROUND.location(), 4, 3, 170, 60).setTextureSize(195, 136).addPadding(0, 60, 0,0).build();
    public static final RecipeType<StoneVein> STONE_VEINS = new RecipeType<>(new ResourceLocation(Ref.ID, "stone_veins"), StoneVein.class);
    public StoneVeinCategory() {

    }

    @Override
    public Component getTitle() {
        return Utils.translatable("jei.category.gtlib.stone_veins");
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public RecipeType<StoneVein> getRecipeType() {
       return STONE_VEINS;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, StoneVein recipe, IFocusGroup focuses) {

        Material material = recipe.ore().material();//i == 0 ? recipe.primary() : i == 1 ? recipe.secondary() : i == 2 ? recipe.between() : recipe.sporadic();
        builder.addSlot(RecipeIngredientRole.OUTPUT, 1, 1)
                .addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ORE.get().get(material, VanillaStoneTypes.STONE).asItem()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 19, 1)
                .addIngredient(VanillaTypes.ITEM_STACK, new ItemStack(recipe.stoneLayer().block()));
        GTLibJEIPlugin.addDimensionSlots(builder, recipe.stoneLayer().dimensions());
    }

    @Override
    public void draw(StoneVein recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        int x = JEI_OFFSET_X;
        int y = JEI_OFFSET_Y + 3;
        DecimalFormat format = new DecimalFormat("###.####");
        String fullId = recipe.stoneLayer().getLoc().getPath();
        renderString(graphics, "Stone Layer Name: " + Utils.lowerUnderscoreToUpperSpaced(fullId), Minecraft.getInstance().font, 0, 18, 0x000000, x, y, false);
        renderString(graphics, "Stone: " + recipe.stoneLayer().block().getName().getString(), Minecraft.getInstance().font, 0, 28, 0x000000, x, y, false);
        renderString(graphics, "Ore: " + recipe.ore().material().getDisplayNameString() + " Ore", Minecraft.getInstance().font, 0, 38, 0x000000, x, y, false);
        renderString(graphics, "StoneLayerChance: " + format.format(((double) recipe.stoneLayer().weight() / recipe.totalWeight()) * 100) + "%", Minecraft.getInstance().font, 0, 58, 0x000000, x, y, false);
        renderString(graphics, "MinY: " + recipe.ore().minY() + " MaxY: " + recipe.ore().maxY(), Minecraft.getInstance().font, 0, 68, 0x000000, x, y, false);
        renderString(graphics, "Ore Chance per Stone: " + format.format(((double)recipe.ore().chance() / Ref.U) * 100) + "%", Minecraft.getInstance().font, 0, 78, 0x000000, x, y, false);
        renderString(graphics, "Generated world:", Minecraft.getInstance().font, 0, 88, 0x000000, x, y, false);

    }

    void renderString(GuiGraphics graphics, String string, Font font, float x, float y, int color, int guiOffsetX, int guiOffsetY, boolean shadow) {
        graphics.drawString(font, string, (guiOffsetX + x), guiOffsetY + y, color, shadow);
    }
}

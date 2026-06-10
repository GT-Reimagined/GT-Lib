package org.gtreimagined.gtlib.integration.jei.category;

import brachy.modularui.drawable.GuiTextures;
import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.integration.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.vein.Vein;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.ORE;
import static org.gtreimagined.gtlib.integration.jei.category.RecipeMapCategory.JEI_OFFSET_X;
import static org.gtreimagined.gtlib.integration.jei.category.RecipeMapCategory.JEI_OFFSET_Y;

public class VeinCategory implements IRecipeCategory<Vein> {
    IDrawable icon = GTLibJEIPlugin.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.IRON_ORE.getDefaultInstance());
    IDrawable background = GTLibJEIPlugin.guiHelper.drawableBuilder(GuiTextures.MC_BACKGROUND.location(), 4, 3, 170, 60).setTextureSize(195, 136).addPadding(0, 60, 0,0).build();
    public static final RecipeType<Vein> VEINS = new RecipeType<>(new ResourceLocation(Ref.ID, "veins"), Vein.class);
    public VeinCategory() {

    }

    @Override
    public Component getTitle() {
        return Utils.translatable("jei.category.gtlib.veins");
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
    public RecipeType<Vein> getRecipeType() {
       return VEINS;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, Vein recipe, IFocusGroup focuses) {
        for (int i = 0; i < 4; i++) {
            Material material = i == 0 ? recipe.primary() : i == 1 ? recipe.secondary() : i == 2 ? recipe.between() : recipe.sporadic();
            builder.addSlot(RecipeIngredientRole.OUTPUT, 1 + (i * 18), 1)
                    .addIngredients(VanillaTypes.ITEM_STACK, GTAPI.all(StoneType.class).stream()
                            .filter(s -> s.doesGenerateOre() && s != VanillaStoneTypes.BEDROCK)
                            .map(s -> ORE.get().get(material, s).asBlock())
                            .map(ItemStack::new).toList());
        }
        GTLibJEIPlugin.addDimensionSlots(builder, recipe.dimensions());
    }

    @Override
    public void draw(Vein recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX, double mouseY) {
        int x = JEI_OFFSET_X;
        int y = JEI_OFFSET_Y + 3;
        String fullId = recipe.getLoc().getPath();
        renderString(graphics, "Vein Name: " + Utils.lowerUnderscoreToUpperSpaced(fullId), Minecraft.getInstance().font, 0, 18, 0x000000, x, y, false);
        renderString(graphics, "Primary: " + recipe.primary().getDisplayNameString() + " Ore", Minecraft.getInstance().font, 0, 38, 0x000000, x, y, false);
        renderString(graphics, "Secondary: " + recipe.secondary().getDisplayNameString() + " Ore", Minecraft.getInstance().font, 0, 48, 0x000000, x, y, false);
        renderString(graphics, "Between: " + recipe.between().getDisplayNameString() + " Ore", Minecraft.getInstance().font, 0, 58, 0x000000, x, y, false);
        renderString(graphics, "Sporadic: " + recipe.sporadic().getDisplayNameString() + " Ore", Minecraft.getInstance().font, 0, 68, 0x000000, x, y, false);
        renderString(graphics, "MinY: " + recipe.minY() + " MaxY: " + recipe.maxY(), Minecraft.getInstance().font, 0, 78, 0x000000, x, y, false);
        renderString(graphics, "Weight: " + recipe.weight(), Minecraft.getInstance().font, 100, 78, 0x000000, x, y, false);
        renderString(graphics, "Generated world:", Minecraft.getInstance().font, 0, 88, 0x000000, x, y, false);

    }

    void renderString(GuiGraphics graphics, String string, Font font, float x, float y, int color, int guiOffsetX, int guiOffsetY, boolean shadow) {
        graphics.drawString(font, string, (guiOffsetX + x), guiOffsetY + y, color, shadow);
    }
}

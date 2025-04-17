package org.gtreimagined.gtlib.integration.jei.category;

import com.mojang.blaze3d.vertex.PoseStack;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.resources.ResourceKey;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockDimensionMarker;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
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
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.ORE;
import static org.gtreimagined.gtlib.integration.jei.category.RecipeMapCategory.JEI_OFFSET_X;
import static org.gtreimagined.gtlib.integration.jei.category.RecipeMapCategory.JEI_OFFSET_Y;

public class VeinCategory implements IRecipeCategory<Vein> {
    IDrawable icon = RecipeMapCategory.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.IRON_ORE.getDefaultInstance());
    IDrawable background = RecipeMapCategory.guiHelper.drawableBuilder(new ResourceLocation(Ref.ID, "textures/gui/background/machine_basic.png"), 3, 3, 170, 60).addPadding(0, 60, 0,0).build();
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
    public ResourceLocation getUid() {
        return new ResourceLocation(Ref.ID, "veins");
    }

    @Override
    public Class<? extends Vein> getRecipeClass() {
        return Vein.class;
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
        int i = 0;
        List<Block> markers = new ArrayList<>();
        for (ResourceLocation dimension : recipe.dimensions().stream().map(ResourceKey::location).toList()) {
            int y = i / 9;
            int x = i % 9;
            Block dimensionMarker = GTAPI.get(BlockDimensionMarker.class, dimension.getPath() + "_marker", Ref.ID);
            ItemStack world;
            if (dimensionMarker != null){
                if (markers.contains(dimensionMarker)) {
                    continue;
                }
                markers.add(dimensionMarker);
                world = new ItemStack(dimensionMarker);
            } else {
                world = new ItemStack(Items.BARRIER).setHoverName(Utils.literal(dimension.toString()));
            }
            builder.addSlot(RecipeIngredientRole.INPUT, 1 + (x * 18), 102 + (y * 18)).addIngredients(VanillaTypes.ITEM_STACK, List.of(world));
            i++;
        }
    }

    @Override
    public void draw(Vein recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        int x = JEI_OFFSET_X;
        int y = JEI_OFFSET_Y + 3;
        String fullId = recipe.getLoc().getPath();
        renderString(stack, "Vein Name: " + Utils.lowerUnderscoreToUpperSpaced(fullId), Minecraft.getInstance().font, 0, 18, 0x000000, x, y, false);
        renderString(stack, "Primary: " + recipe.primary().getDisplayNameString() + " Ore", Minecraft.getInstance().font, 0, 38, 0x000000, x, y, false);
        renderString(stack, "Secondary: " + recipe.secondary().getDisplayNameString() + " Ore", Minecraft.getInstance().font, 0, 48, 0x000000, x, y, false);
        renderString(stack, "Between: " + recipe.between().getDisplayNameString() + " Ore", Minecraft.getInstance().font, 0, 58, 0x000000, x, y, false);
        renderString(stack, "Sporadic: " + recipe.sporadic().getDisplayNameString() + " Ore", Minecraft.getInstance().font, 0, 68, 0x000000, x, y, false);
        renderString(stack, "MinY: " + recipe.minY() + " MaxY: " + recipe.maxY(), Minecraft.getInstance().font, 0, 78, 0x000000, x, y, false);
        renderString(stack, "Weight: " + recipe.weight(), Minecraft.getInstance().font, 100, 78, 0x000000, x, y, false);
        renderString(stack, "Generated world:", Minecraft.getInstance().font, 0, 88, 0x000000, x, y, false);

    }

    void renderString(PoseStack stack, String string, Font render, float x, float y, int color, int guiOffsetX, int guiOffsetY, boolean shadow) {
        if (shadow) {
            render.drawShadow(stack, string, (guiOffsetX + x), guiOffsetY + y, color);
        } else {
            render.draw(stack, string, (guiOffsetX + x), guiOffsetY + y, color);
        }
    }
}

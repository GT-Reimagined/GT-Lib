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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockDimensionMarker;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;

import java.util.ArrayList;
import java.util.List;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.ORE_SMALL;
import static org.gtreimagined.gtlib.integration.jei.category.RecipeMapCategory.JEI_OFFSET_X;
import static org.gtreimagined.gtlib.integration.jei.category.RecipeMapCategory.JEI_OFFSET_Y;

public class SmallOreCategory implements IRecipeCategory<SmallOre> {
    IDrawable icon = RecipeMapCategory.guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, Items.IRON_ORE.getDefaultInstance());
    IDrawable background = RecipeMapCategory.guiHelper.drawableBuilder(new ResourceLocation(Ref.ID, "textures/gui/background/machine_basic.png"), 3, 3, 170, 60).addPadding(0, 60, 0,0).build();
    public static final RecipeType<SmallOre> SMALL_ORES = new RecipeType<>(new ResourceLocation(Ref.ID, "small_ores"), SmallOre.class);
    public SmallOreCategory() {

    }

    @Override
    public Component getTitle() {
        return Utils.translatable("jei.category.gtlib.small_ores");
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
    public RecipeType<SmallOre> getRecipeType() {
       return SMALL_ORES;
    }

    @Override
    public ResourceLocation getUid() {
        return new ResourceLocation(Ref.ID, "small_ores");
    }

    @Override
    public Class<? extends SmallOre> getRecipeClass() {
        return SmallOre.class;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, SmallOre recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 1, 1)
                .addIngredients(VanillaTypes.ITEM_STACK, GTAPI.all(StoneType.class).stream()
                        .filter(s -> s.doesGenerateOre() && s != VanillaStoneTypes.BEDROCK)
                        .map(s -> ORE_SMALL.get().get(recipe.material(), s).asBlock())
                        .map(ItemStack::new).toList());

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
    public void draw(SmallOre recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        int x = JEI_OFFSET_X;
        int y = JEI_OFFSET_Y + 3;
        String fullId = recipe.getLoc().getPath();
        renderString(stack, "Vein Name: " + Utils.lowerUnderscoreToUpperSpaced(fullId), Minecraft.getInstance().font, 0, 18, 0x000000, x, y);
        renderString(stack, "MinY: " + (recipe.minY() == Integer.MIN_VALUE ? "N/A" : recipe.minY()) + " MaxY: " + (recipe.maxY() == Integer.MAX_VALUE ? "N/A" : recipe.maxY()), Minecraft.getInstance().font, 0, 28, 0x000000, x, y);
        renderString(stack, "Amount per chunk: " + recipe.amountPerChunk(), Minecraft.getInstance().font, 0, 38, 0x000000, x, y);
        renderString(stack, "Drops: ", Minecraft.getInstance().font, 0, 58, 0x000000, x, y);
        renderString(stack, "Generated world:", Minecraft.getInstance().font, 0, 88, 0x000000, x, y);

    }

    void renderString(PoseStack stack, String string, Font render, float x, float y, int color, int guiOffsetX, int guiOffsetY) {
        render.draw(stack, string, (guiOffsetX + x), guiOffsetY + y, color);
    }
}

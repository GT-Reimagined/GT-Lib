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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.integration.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;

import java.util.ArrayList;
import java.util.List;

import static org.gtreimagined.gtlib.data.GTMaterialTypes.*;
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

        List<List<ItemStack>> drops = new ArrayList<>();
        List<ItemStack> stoneDusts = new ArrayList<>();
        GTAPI.all(StoneType.class).stream().filter(s -> s.doesGenerateOre() && s != VanillaStoneTypes.BEDROCK).forEach(s -> {
            if (s.getMaterial().has(DUST)){
                stoneDusts.add(DUST.get(s.getMaterial(), 1));
            }
        });
        if (!stoneDusts.isEmpty()) {
            drops.add(stoneDusts);
        }
        if (recipe.material().has(GEM_EXQUISITE)){
            drops.add(List.of(GEM_EXQUISITE.get(recipe.material(), 1)));
        }
        if (recipe.material().has(GEM_FLAWLESS)){
            drops.add(List.of(GEM_FLAWLESS.get(recipe.material(), 1)));
        }
        if (recipe.material().has(GEM)){
            drops.add(List.of(GEM.get(recipe.material(), 1)));
        }
        if (recipe.material().has(GEM_FLAWED)){
            drops.add(List.of(GEM_FLAWED.get(recipe.material(), 1)));
        }
        if (recipe.material().has(GEM_CHIPPED)){
            drops.add(List.of(GEM_CHIPPED.get(recipe.material(), 1)));
        }
        if (recipe.material().has(CRUSHED)){
            drops.add(List.of(CRUSHED.get(recipe.material(), 1)));
        }
        if (recipe.material().has(DUST_IMPURE)){
            drops.add(List.of(DUST_IMPURE.get(recipe.material(), 1)));
        }
        if (recipe.material().has(DUST) && !recipe.material().has(CRUSHED) && !recipe.material().has(DUST_IMPURE) && !recipe.material().has(GEM)){
            drops.add(List.of(DUST.get(recipe.material(), 1)));
        }
        for (int i = 0; i < 8 && i < drops.size(); i++) {
            int x = i % 4;
            int y = i / 4;
            builder.addSlot(RecipeIngredientRole.OUTPUT, 42 + (x * 18), 55 + (y * 18)).addIngredients(VanillaTypes.ITEM_STACK, drops.get(i));
        }
        GTLibJEIPlugin.addDimensionSlots(builder, recipe.dimensions());
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

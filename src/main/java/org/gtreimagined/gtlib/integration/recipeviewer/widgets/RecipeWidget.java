package org.gtreimagined.gtlib.integration.recipeviewer.widgets;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.CompositeProgress;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import brachy.modularui.widgets.TextWidget;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.integration.xei.renderer.IRecipeInfoRenderer;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.mui.BarDir;
import org.gtreimagined.gtlib.mui.widgets.GTProgressWidget;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.util.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeWidget extends ParentWidget<RecipeWidget> {
    public RecipeWidget(IRecipe recipe, IRecipeMap map, GuiProperties gui, Tier guiTier){
        IRecipeInfoRenderer infoRenderer = map.getInfoRenderer();
        this.size(170, 80 + (infoRenderer.getRows() <= 0 ? 0 : 7 + (10 * infoRenderer.getRows())));
        BarDir direction = gui.getMachineData().getDir();
        UITexture texture = gui.getMachineData().getProgressTexture(guiTier);
        ProgressWidget progressWidget = new ProgressWidget()
                //.syncHandler("progress")
                .pos(gui.getMachineData().getProgressPos().x + 6, gui.getMachineData().getProgressPos().y + 6);
        this.child(progressWidget);
        if (!direction.isCircular()) {
            progressWidget.texture(texture, direction.toRegularDirection());
        } else {
            progressWidget.progress(CompositeProgress.circularLike4Slice(
                    texture.getSubArea(0.0f, 0.0f, 1f, 0.5f),
                    texture.getSubArea(0f, 0.5f,1f, 1f),
                    direction.toCircularDirection()
            ));
        }
        List<SlotData<?>> slots;
        if (recipe.hasInputItems()){
            slots = gui.getSlots().getSlots(SlotType.IT_IN, guiTier);
            if (!slots.isEmpty()){
                ParentWidget<?> itemStackGroup = new ParentWidget<>().sizeRel(1f);
                int s = 0;
                List<List<ItemStack>> inputs = recipe.getInputItems().stream().map(t -> Arrays.asList(t.getItems())).toList();
                if (!inputs.isEmpty()){
                    int slotCount = Math.min(slots.size(), inputs.size());
                    for (; s < slotCount; s++) {
                        final int ss = s;
                        List<ItemStack> input = inputs.get(s);
                        itemStackGroup.child(RecipeViewerSlotWidget.create()
                                .recipeSlotRole(RecipeSlotRole.INPUT)
                                .pos(slots.get(s).getJeiX(), slots.get(s).getJeiY())
                                .tooltipBuilder(r -> {
                                    if (input.isEmpty()){
                                        r.addLine(Utils.literal("Empty Tag"));
                                        return;
                                    }
                                    if (recipe.getInputItems().get(ss) instanceof RecipeIngredient ri) {
                                        if (ri.ignoreConsume()) {
                                            r.addLine(Utils.literal("Does not get consumed in the process.").withStyle(ChatFormatting.WHITE));
                                        }
                                        if (ri.ignoreNbt()) {
                                            r.addLine(Utils.literal("Ignores NBT.").withStyle(ChatFormatting.WHITE));
                                        }
                                        Ingredient i = recipe.getInputItems().get(ss);
                                        if (RecipeMap.isIngredientSpecial(i)) {
                                            r.addLine(Utils.literal("Special ingredient. Class name: ").withStyle(ChatFormatting.GRAY).append(Utils.literal(i.getClass().getSimpleName()).withStyle(ChatFormatting.GOLD)));
                                        }
                                    }
                                    if (recipe.hasInputChances()) {
                                        if (recipe.getInputChances()[ss] < 10000) {
                                            r.addLine(Utils.literal("Consumption Chance: " + ((float)recipe.getInputChances()[ss] / 100) + "%").withStyle(ChatFormatting.WHITE));
                                        }
                                    }
                                })
                                .value(new ItemStackList(input.isEmpty() ? List.of(new ItemStack(Data.DEBUG_SCANNER)) : input)));
                    }
                }
                this.child(itemStackGroup);
            }
        }


        if (recipe.hasOutputItems()) {
            slots = gui.getSlots().getSlots(SlotType.IT_OUT, guiTier);
            if (!slots.isEmpty()) {
                ParentWidget<?> itemStackGroup = new ParentWidget<>().sizeRel(1f);
                List<ItemStack> outputs = Arrays.stream(recipe.getOutputItems(false)).toList();
                int slotCount = Math.min(slots.size(), outputs.size());
                for (int s = 0; s < slotCount; s++) {
                    final int ss = s;
                    RecipeViewerSlotWidget<?> widget = RecipeViewerSlotWidget.create()
                            .recipeSlotRole(RecipeSlotRole.OUTPUT)
                            .pos(slots.get(s).getJeiX(), slots.get(s).getJeiY())
                            .tooltipBuilder(r -> {
                                if (recipe.hasOutputChances()) {
                                    if (recipe.getOutputChances()[ss] < 10000) {
                                        r.addLine(Utils.literal("Output Chance: " + ((float)recipe.getOutputChances()[ss] / 100) + "%").withStyle(ChatFormatting.WHITE));
                                    }
                                }
                            })
                            .value(outputs.get(s));
                    if (recipe.hasOutputChances()){
                        if (recipe.getOutputChances()[s] < 10000){
                            widget.backgroundOverlay(new ChanceOverlay(Utils.literal("Output Chance: " + ((float)recipe.getOutputChances()[ss] / 100) + "%").withStyle(ChatFormatting.WHITE),
                                    slots.get(s).getJeiX(), slots.get(s).getJeiY()));
                        }
                    }
                }
                this.child(itemStackGroup);
            }
        }
    }
}

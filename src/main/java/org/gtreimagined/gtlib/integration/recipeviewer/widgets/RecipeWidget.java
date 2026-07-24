package org.gtreimagined.gtlib.integration.recipeviewer.widgets;

import brachy.modularui.drawable.UITexture;
import brachy.modularui.drawable.progress.CompositeProgress;
import brachy.modularui.integration.recipeviewer.RecipeSlotRole;
import brachy.modularui.integration.recipeviewer.RecipeViewerSlotWidget;
import brachy.modularui.integration.recipeviewer.entry.fluid.FluidStackList;
import brachy.modularui.integration.recipeviewer.entry.item.ItemStackList;
import brachy.modularui.screen.RichTooltip;
import brachy.modularui.screen.viewport.ModularGuiContext;
import brachy.modularui.theme.WidgetThemeEntry;
import brachy.modularui.widget.ParentWidget;
import brachy.modularui.widgets.ProgressWidget;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.ingredient.IRecipeSlotView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.integration.recipeviewer.renderer.IRecipeInfoRenderer;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.mui.BarDir;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.util.FluidUtils;
import org.gtreimagined.gtlib.util.Utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeWidget extends ParentWidget<RecipeWidget> {
    int progress = 0;
    int maxProgress = 50;
    public RecipeWidget(IRecipe recipe, IRecipeMap map, GuiProperties gui, Tier guiTier){
        IRecipeInfoRenderer infoRenderer = map.getInfoRenderer();
        this.size(170, 80 + (infoRenderer.getRows() <= 0 ? 0 : 7 + (10 * infoRenderer.getRows())));
        BarDir direction = gui.getMachineData().getDir();
        UITexture texture = gui.getMachineData().getProgressTexture(guiTier);
        ProgressWidget progressWidget = new ProgressWidget()
                .clientValue(() -> (double)progress / maxProgress)
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
        if (recipe.hasInputFluids()) {
            slots = gui.getSlots().getSlots(SlotType.FL_IN, guiTier);
            if (!slots.isEmpty()) {
                ParentWidget<?> fluidStackGroup = new ParentWidget<>().sizeRel(1f);
                List<FluidIngredient> fluids = recipe.getInputFluids();
                int slotCount = Math.min(slots.size(), fluids.size());
                for (int s = 0; s < slotCount; s++) {
                    final int ss = s;
                    RecipeViewerSlotWidget<?> widget = RecipeViewerSlotWidget.create()
                            .recipeSlotRole(RecipeSlotRole.INPUT)
                            .pos(slots.get(s).getJeiX(), slots.get(s).getJeiY())
                            .tooltip(r -> {
                                FluidStack stack = fluids.get(ss).getStacks()[0];
                                createFluidTooltip(r, stack);
                            })
                            .value(FluidStackList.of(Arrays.asList(fluids.get(s).getStacks())));
                    fluidStackGroup.child(widget);
                }
                this.child(fluidStackGroup);
            }
        }
        if (recipe.hasOutputFluids()) {
            slots = gui.getSlots().getSlots(SlotType.FL_OUT, guiTier);
            if (!slots.isEmpty()) {
                ParentWidget<?> fluidStackGroup = new ParentWidget<>().sizeRel(1f);
                FluidStack[] fluids = recipe.getOutputFluids();
                int slotCount = Math.min(slots.size(), fluids.length);
                for (int s = 0; s < slotCount; s++) {
                    final int ss = s;
                    RecipeViewerSlotWidget<?> widget = RecipeViewerSlotWidget.create()
                            .recipeSlotRole(RecipeSlotRole.OUTPUT)
                            .pos(slots.get(s).getJeiX(), slots.get(s).getJeiY())
                            .tooltip(r -> {
                                FluidStack stack = fluids[ss];
                                createFluidTooltip(r, stack);
                            })
                            .value(fluids[s]);
                    fluidStackGroup.child(widget);
                }
                this.child(fluidStackGroup);
            }
        }
    }


    private void createFluidTooltip(RichTooltip richTooltip, FluidStack stack) {
        Component component = null;
/*
        for (Component comp : list) {
            if (comp.getContents() instanceof TranslatableContents translatable){
                if (translatable.getKey().equals("jei.tooltip.recipe.by")){
                    component = comp;
                    break;
                }
            }
        }
        if (list.size() > 2) list.remove(2);
        if (list.size() > 1) list.remove(1);
*/
        int mb = stack.getAmount();
        richTooltip.addLine(Utils.translatable("gtlib.tooltip.fluid.amount", mb + " L").withStyle(ChatFormatting.BLUE));
        richTooltip.addLine(Utils.translatable("gtlib.tooltip.fluid.temp", FluidUtils.getFluidTemperature(stack.getFluid())).withStyle(ChatFormatting.RED));
        String liquid = !FluidUtils.isFluidGaseous(stack.getFluid()) ? "liquid" : "gas";
        richTooltip.addLine(Utils.translatable("gtlib.tooltip.fluid." + liquid).withStyle(ChatFormatting.GREEN));
        //if (Utils.hasNoConsumeTag(ing.getDisplayedIngredient().get().getIngredient(ForgeTypes.FLUID_STACK).get()))
        //    list.add(Utils.literal("Does not get consumed in the process").withStyle(ChatFormatting.WHITE));
        //if (component != null) list.add(component);
    }

    @Override
    public void draw(ModularGuiContext context, WidgetThemeEntry<?> widgetTheme) {
        super.draw(context, widgetTheme);
        progress++;
        if (progress >= maxProgress){
            progress = 0;
        }
    }
}

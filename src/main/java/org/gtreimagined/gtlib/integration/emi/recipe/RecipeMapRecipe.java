package org.gtreimagined.gtlib.integration.emi.recipe;

import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.TankWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.Data;
import org.gtreimagined.gtlib.gui.GuiData;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.integration.emi.GTEMIFluidIngredient;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.ingredient.FluidIngredient;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipeMapRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final IRecipe recipe;
    private final List<EmiIngredient> inputs = new ArrayList<>();
    private final List<EmiStack> outputs = new ArrayList<>();
    GuiData gui;
    Tier guiTier;
    int fluidInputOffset;
    int fluidOutputOffset;
    ResourceLocation id;

    public RecipeMapRecipe(EmiRecipeCategory category, IRecipe recipe, GuiData gui, Tier guiTier){
        this.category = category;
        this.recipe = recipe;
        this.gui = gui;
        this.guiTier = guiTier;
        this.recipe.getInputItems().forEach(i -> inputs.add(EmiIngredient.of(i)));
        fluidInputOffset = inputs.size();
        this.recipe.getInputFluids().forEach(f -> inputs.add(new GTEMIFluidIngredient(f)));
        if (this.recipe.hasOutputItems()){
            for (ItemStack outputItem : this.recipe.getOutputItems(false)) {
                outputs.add(EmiStack.of(outputItem));
            }
        }
        fluidOutputOffset = outputs.size();
        if (recipe.hasOutputFluids()){
            for (FluidStack stack : recipe.getOutputFluids()){
                outputs.add(ForgeEmiStack.of(stack));
            }
        }
        id = recipe.getTags().contains("emi_proxy") ? new ResourceLocation(recipe.getId().getNamespace(), "/" + recipe.getId().getPath()) : recipe.getId();
    }
    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return id;
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return inputs;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return outputs;
    }

    @Override
    public int getDisplayWidth() {
        return gui.getArea().z;
    }

    @Override
    public int getDisplayHeight() {
        return gui.getArea().w;
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        List<SlotData<?>> slots;
        int groupIndex = 0, slotCount;
        int offsetX = gui.getArea().x, offsetY = gui.getArea().y;
        int inputItems = 0, inputFluids = 0;
        if (recipe.hasInputItems()) {
            slots = gui.getSlots().getSlots(SlotType.IT_IN, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                int s = 0;
                if (!inputs.isEmpty()) {
                    int recipeSlotCount = Math.min(slotCount, fluidInputOffset);
                    for (; s < slotCount; s++) {
                        SlotWidget slot;
                        if (s < recipeSlotCount){
                            slot = widgetHolder.addSlot(inputs.get(s), slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1));
                        } else {
                            widgetHolder.addSlot(slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1));
                            continue;
                        }
                        if (recipe.getInputItems().size() > s && recipe.getInputItems().get(s) instanceof RecipeIngredient ri) {
                            if (ri.ignoreConsume()){
                                slot.appendTooltip(Utils.literal("Does not get consumed in the process.").withStyle(ChatFormatting.WHITE));
                            }
                            if (ri.ignoreNbt()) {
                                slot.appendTooltip(Utils.literal("Ignores NBT.").withStyle(ChatFormatting.WHITE));
                            }
                            if (RecipeMap.isIngredientSpecial(ri)) {
                                slot.appendTooltip(Utils.literal("Special ingredient. Class name: ").withStyle(ChatFormatting.GRAY).append(Utils.literal(ri.getClass().getSimpleName()).withStyle(ChatFormatting.GOLD)));
                            }

                        }
                        if (recipe.hasInputChances()) {
                            if (recipe.getInputChances()[s] < 10000) {
                                slot.appendTooltip(Utils.literal("Consumption Chance: " + ((float)recipe.getInputChances()[s] / 100) + "%").withStyle(ChatFormatting.WHITE));
                            }
                        }
                    }
                }
            }
        }
        if (recipe.hasOutputItems()) {
            slots = gui.getSlots().getSlots(SlotType.IT_OUT, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                int recipeSlotCount = Math.min(slotCount, fluidOutputOffset);
                for (int s = 0; s < slotCount; s++) {
                    SlotWidget slot;
                    if (s < recipeSlotCount){
                        slot = widgetHolder.addSlot(outputs.get(s), slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1)).recipeContext(this);
                    } else {
                        widgetHolder.addSlot(slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1)).recipeContext(this);
                        continue;
                    }
                    if (recipe.hasOutputChances() && recipe.getOutputChances()[s] < 10000){
                        slot.appendTooltip(Utils.literal("Output Chance: " + ((float) recipe.getOutputChances()[s] / 100) + "%").withStyle(ChatFormatting.WHITE));
                    }
                }
            }
        }

        if (recipe.hasInputFluids()){
            slots = gui.getSlots().getSlots(SlotType.FL_IN, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                for (int s = 0; s < slotCount; s++) {
                    int offsetIndex = s + fluidInputOffset;
                    SlotWidget slot;
                    widgetHolder.addTexture(slots.get(s).getTexture(), slots.get(s).getX() - (offsetX), slots.get(s).getY() - (offsetY), 18, 18, 0, 0, 18, 18, 18, 18);
                    if (offsetIndex < inputs.size()){

                        slot = widgetHolder.addTank(inputs.get(offsetIndex), slots.get(s).getX() - (offsetX), slots.get(s).getY() - (offsetY), 18, 18, recipe.getInputFluids().get(s).getAmount()).drawBack(false);
                    } else {
                        //widgetHolder.addSlot(slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1)).recipeContext(this);
                        continue;
                    }
                }
            }
        }

        if (recipe.hasOutputFluids()){
            slots = gui.getSlots().getSlots(SlotType.FL_OUT, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                for (int s = 0; s < slotCount; s++) {
                    int offsetIndex = s + fluidOutputOffset;
                    SlotWidget slot;
                    widgetHolder.addTexture(slots.get(s).getTexture(), slots.get(s).getX() - (offsetX), slots.get(s).getY() - (offsetY), 18, 18, 0, 0, 18, 18, 18, 18);
                    if (offsetIndex < outputs.size()){

                        slot = widgetHolder.addTank(outputs.get(offsetIndex), slots.get(s).getX() - (offsetX), slots.get(s).getY() - (offsetY), 18, 18, recipe.getOutputFluids()[s].getAmount()).drawBack(false).recipeContext(this);
                    } else {
                        //widgetHolder.addSlot(slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1)).recipeContext(this);
                        continue;
                    }
                }
            }
        }

        /*if (recipe.hasInputFluids()) {
            slots = gui.getSlots().getSlots(SlotType.FL_IN, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                List<FluidIngredient> fluids = recipe.getInputFluids();
                slotCount = Math.min(slotCount, fluids.size());
                for (int s = 0; s < slotCount; s++) {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1));
                    slot.addIngredients(ForgeTypes.FLUID_STACK, Arrays.asList(fluids.get(s).getStacks()));
                    slot.setFluidRenderer((int)fluids.get(s).getAmount(), true, 16, 16);
                    int finalS = s;
                    slot.addTooltipCallback((ing, list) -> {
                        FluidStack stack = fluids.get(finalS).getStacks()[0];
                        createFluidTooltip(ing, list, stack);
                    });
                    inputFluids++;
                }
            }
        }*/
        /*if (recipe.hasOutputFluids()) {
            slots = gui.getSlots().getSlots(SlotType.FL_OUT, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                FluidStack[] fluids = recipe.getOutputFluids();
                slotCount = Math.min(slotCount, fluids.length);
                for (int s = 0; s < slotCount; s++) {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1));
                    slot.setFluidRenderer(fluids[s].getAmount(), true, 16, 16);
                    slot.addIngredients(ForgeTypes.FLUID_STACK, Collections.singletonList(fluids[s]));
                    int finalS = s;
                    slot.addTooltipCallback((ing, list) -> {
                        FluidStack stack = fluids[finalS];
                        createFluidTooltip(ing, list, stack);
                    });
                }
            }
        }*/
    }
}

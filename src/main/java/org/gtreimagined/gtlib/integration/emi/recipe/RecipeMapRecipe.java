package org.gtreimagined.gtlib.integration.emi.recipe;

import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.SlotWidget;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.SlotData;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.integration.emi.GTEMIFluidIngredient;
import org.gtreimagined.gtlib.integration.emi.GTFluidEmiStack;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.ingredient.RecipeIngredient;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.util.Utils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class RecipeMapRecipe implements EmiRecipe {
    private final EmiRecipeCategory category;
    private final IRecipe recipe;
    private final List<EmiIngredient> inputs = new ArrayList<>();
    private final List<EmiStack> outputs = new ArrayList<>();
    GuiProperties gui;
    Tier guiTier;
    int fluidInputOffset;
    int fluidOutputOffset;
    ResourceLocation id;
    IRecipeMap map;

    public RecipeMapRecipe(EmiRecipeCategory category, IRecipe recipe, GuiProperties gui, Tier guiTier){
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
                outputs.add(new GTFluidEmiStack(stack.getFluid(), stack.getTag(), stack.getAmount()));
            }
        }
        this.map = GTAPI.get(RecipeMap.class, recipe.getMapLoc());
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
        return gui.getArea().w + (map.getInfoRenderer().getRows() <= 0 ? 0 : 7 + (10 *map.getInfoRenderer().getRows()));
    }

    @Override
    public void addWidgets(WidgetHolder widgetHolder) {
        List<SlotData<?>> slots;
        int slotCount;
        int offsetX = gui.getArea().x, offsetY = gui.getArea().y;
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
                            slot = widgetHolder.addSlot(inputs.get(s), slots.get(s).getJeiX() - (offsetX - 1), slots.get(s).getJeiY() - (offsetY - 1));
                        } else {
                            widgetHolder.addSlot(slots.get(s).getJeiX() - (offsetX - 1), slots.get(s).getJeiY() - (offsetY - 1));
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
                        slot = widgetHolder.addSlot(outputs.get(s), slots.get(s).getJeiX() - (offsetX - 1), slots.get(s).getJeiY() - (offsetY - 1)).recipeContext(this);
                    } else {
                        widgetHolder.addSlot(slots.get(s).getJeiX() - (offsetX - 1), slots.get(s).getJeiY() - (offsetY - 1)).recipeContext(this);
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
                    widgetHolder.addTexture(slots.get(s).getBaseTexture().location(), slots.get(s).getJeiX() - (offsetX), slots.get(s).getJeiY() - (offsetY), 18, 18, 0, 0, 18, 18, 18, 18);
                    if (slots.get(s).getOverlayTexture() != null){
                        widgetHolder.addTexture(slots.get(s).getOverlayTexture().location(), slots.get(s).getJeiX() - (offsetX), slots.get(s).getJeiY() - (offsetY), 18, 18, 0, 0, 18, 18, 18, 18);
                    }
                    if (offsetIndex < inputs.size()){
                        widgetHolder.addTank(inputs.get(offsetIndex), slots.get(s).getJeiX() - (offsetX), slots.get(s).getJeiY() - (offsetY), 18, 18, recipe.getInputFluids().get(s).getAmount()).drawBack(false);
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
                    widgetHolder.addTexture(slots.get(s).getBaseTexture().location(), slots.get(s).getJeiX() - (offsetX), slots.get(s).getJeiY() - (offsetY), 18, 18, 0, 0, 18, 18, 18, 18);
                    if (slots.get(s).getOverlayTexture() != null){
                        widgetHolder.addTexture(slots.get(s).getOverlayTexture().location(), slots.get(s).getJeiX() - (offsetX), slots.get(s).getJeiY() - (offsetY), 18, 18, 0, 0, 18, 18, 18, 18);
                    }
                    if (offsetIndex < outputs.size()){
                        widgetHolder.addTank(outputs.get(offsetIndex), slots.get(s).getJeiX() - (offsetX), slots.get(s).getJeiY() - (offsetY), 18, 18, recipe.getOutputFluids()[s].getAmount()).drawBack(false).recipeContext(this);
                    }
                }
            }
        }
    }
}

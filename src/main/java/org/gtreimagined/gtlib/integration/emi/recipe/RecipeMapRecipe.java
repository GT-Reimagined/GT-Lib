package org.gtreimagined.gtlib.integration.emi.recipe;

import dev.emi.emi.api.forge.ForgeEmiStack;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.recipe.RecipeIngredientRole;
import net.minecraft.ChatFormatting;
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
    }
    @Override
    public EmiRecipeCategory getCategory() {
        return category;
    }

    @Override
    public @Nullable ResourceLocation getId() {
        return recipe.getId();
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
        List<ItemStack> outputs = recipe.hasOutputItems() ? Arrays.stream(recipe.getOutputItems(false)).toList() : Collections.emptyList();
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
                    slotCount = Math.min(slotCount, fluidInputOffset);
                    for (; s < slotCount; s++) {
                        widgetHolder.addSlot(inputs.get(s), slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1));
                        /*List<ItemStack> input = inputs.get(s);
                        if (input.isEmpty()) {
                            List<ItemStack> st = new ObjectArrayList<>(1);
                            st.add(new ItemStack(Data.DEBUG_SCANNER, 1).setHoverName(Utils.literal("Empty tag")));
                            slot.addIngredients(VanillaTypes.ITEM_STACK, st);
                        } else {
                            slot.addIngredients(VanillaTypes.ITEM_STACK, input);
                            final int ss = s;
                            slot.addTooltipCallback((ing, list) -> {
                                if (recipe.getInputItems().get(ss) instanceof RecipeIngredient ri) {
                                    if (ri.ignoreConsume()) {
                                        list.add(Utils.literal("Does not get consumed in the process.").withStyle(ChatFormatting.WHITE));
                                    }
                                    if (ri.ignoreNbt()) {
                                        list.add(Utils.literal("Ignores NBT.").withStyle(ChatFormatting.WHITE));
                                    }
                                    Ingredient i = recipe.getInputItems().get(ss);
                                    if (RecipeMap.isIngredientSpecial(i)) {
                                        list.add(Utils.literal("Special ingredient. Class name: ").withStyle(ChatFormatting.GRAY).append(Utils.literal(i.getClass().getSimpleName()).withStyle(ChatFormatting.GOLD)));
                                    }
                                }
                                if (recipe.hasInputChances()) {
                                    if (recipe.getInputChances()[ss] < 10000) {
                                        list.add(Utils.literal("Consumption Chance: " + ((float)recipe.getInputChances()[ss] / 100) + "%").withStyle(ChatFormatting.WHITE));
                                    }
                                }
                            });
                            inputItems++;
                        }*/
                    }
                }
            }
        }
        /*if (recipe.hasOutputItems()) {
            slots = gui.getSlots().getSlots(SlotType.IT_OUT, guiTier);
            slotCount = slots.size();
            if (slotCount > 0) {
                slotCount = Math.min(slotCount, outputs.size());
                for (int s = 0; s < slotCount; s++) {
                    IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, slots.get(s).getX() - (offsetX - 1), slots.get(s).getY() - (offsetY - 1));
                    slot.addIngredient(VanillaTypes.ITEM_STACK, outputs.get(s));
                    final int ss = s;
                    slot.addTooltipCallback((ing, list) -> {
                        if (recipe.hasOutputChances()) {
                            if (recipe.getOutputChances()[ss] < 10000) {
                                list.add(Utils.literal("Output Chance: " + ((float)recipe.getOutputChances()[ss] / 100) + "%").withStyle(ChatFormatting.WHITE));
                            }
                        }
                    });
                }
            }
        }*/

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

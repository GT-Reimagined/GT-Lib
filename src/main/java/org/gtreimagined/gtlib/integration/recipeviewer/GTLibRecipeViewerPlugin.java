package org.gtreimagined.gtlib.integration.recipeviewer;

import brachy.modularui.ModularUI.Mods;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Getter;
import net.minecraft.world.item.crafting.RecipeManager;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.gui.GuiProperties;
import org.gtreimagined.gtlib.gui.slot.ISlotProvider;
import org.gtreimagined.gtlib.integration.recipeviewer.emi.GTLibEmiPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.GTLibJEIPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.REIUtils;
import org.gtreimagined.gtlib.machine.BlockMachine;
import org.gtreimagined.gtlib.machine.Tier;
import org.gtreimagined.gtlib.machine.types.BasicMultiMachine;
import org.gtreimagined.gtlib.machine.types.Machine;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.structure.Pattern;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.gtreimagined.gtlib.gui.SlotType.*;
import static org.gtreimagined.gtlib.machine.MachineFlag.RECIPE;

public class GTLibRecipeViewerPlugin {
    private static final List<Consumer<List<ItemLike>>> ITEMS_TO_HIDE = new ArrayList<>();
    private static final List<Consumer<List<Fluid>>> FLUIDS_TO_HIDE = new ArrayList<>();
    @Getter
    private static final Map<ResourceLocation, List<Consumer<List<Item>>>> WORKSTATIONS = new Object2ObjectOpenHashMap<>();
    @Getter
    private static final Map<BlockMachine, List<Pattern>> STRUCTURES = new Object2ObjectOpenHashMap<>();
    
    public static final GuiProperties BACKUP_MAP_GUI = new GuiProperties(Ref.ID, "backup_map").setSlots(ISlotProvider.DEFAULT()
            .add(IT_IN, 17, 16).add(IT_IN, 35, 16).add(IT_IN, 53, 16).add(IT_IN, 17, 34).add(IT_IN, 35, 34)
            .add(IT_IN, 53, 34).add(IT_OUT, 107, 16).add(IT_OUT, 125, 16).add(IT_OUT, 143, 16).add(IT_OUT, 107, 34)
            .add(IT_OUT, 125, 34).add(IT_OUT, 143, 34).add(FL_IN, 17, 63).add(FL_IN, 35, 63).add(FL_IN, 53, 63)
            .add(FL_OUT, 107, 63).add(FL_OUT, 125, 63).add(FL_OUT, 143, 63));
    public static class RegistryValue {
        public IRecipeMap map;
        public GuiProperties gui;
        public Tier tier;
        public List<ResourceLocation> workstations = new ArrayList<>();

        public RegistryValue(IRecipeMap map, GuiProperties gui, Tier tier) {
            this.map = map;
            this.gui = gui;
            this.tier = tier;
        }

        public RegistryValue addWorkstation(ResourceLocation supplier){
            if (supplier != null && !workstations.contains(supplier)) {
                workstations.add(supplier);
            }
            return this;
        }
    }

    @Getter
    private static final Object2ObjectMap<ResourceLocation, RegistryValue> REGISTRY = new Object2ObjectLinkedOpenHashMap<>();

    public static void registerMissingMaps(){
        GTAPI.all(RecipeMap.class).forEach(r -> {
            if (!REGISTRY.containsKey(r.getLoc())){
                registerCategory(r, BACKUP_MAP_GUI, Tier.LV, null, false);
            }
        });
    }

    public static List<IRecipe> getRecipes(IRecipeMap recipeMap, RecipeManager manager){
        if (manager == null) return Collections.emptyList();
        List<IRecipe> recipes = new ArrayList<>(manager.getAllRecipesFor(recipeMap.getRecipeType()).stream().filter(r -> r.getMapId().equals(recipeMap.getLoc().toString()) && !r.isHidden()).toList());
        if (recipeMap.getProxy() != null && recipeMap instanceof RecipeMap<?> map) {
            List<net.minecraft.world.item.crafting.Recipe<?>> proxyRecipes = (List<net.minecraft.world.item.crafting.Recipe<?>>) manager.getAllRecipesFor(recipeMap.getProxy().loc());
            proxyRecipes.forEach(recipe -> {
                IRecipe recipe1 = recipeMap.getProxy().handler().apply(recipe, map.RB().tags("emi_proxy"));
                if (recipe1 != null && !recipe1.isHidden()){
                    recipes.add(recipe1);
                }
            });
        }
        return recipes;
    }

    public static void registerCategory(IRecipeMap map, GuiProperties gui, Tier tier, ResourceLocation model, boolean override) {
        if (REGISTRY.containsKey(new ResourceLocation(map.getDomain(), map.getId())) && !override) {
            GTLib.LOGGER.info("Attempted duplicate category registration: " + map.getId());
            return;
        }
        REGISTRY.put(new ResourceLocation(map.getDomain(), map.getId()), new RegistryValue(map, map.getGui() == null ? gui : map.getGui(), tier).addWorkstation(model));//new Tuple<>(map, new Tuple<>(gui, tier)));
    }

    public static boolean containsCategory(IRecipeMap map){
        return REGISTRY.containsKey(map.getLoc());
    }

    public static void registerCategoryWorkstation(IRecipeMap map, ResourceLocation model){
        RegistryValue value = REGISTRY.get(map.getLoc());
        if (value != null) {
            value.addWorkstation(model);
        }
    }

    public static void registerPatternForJei(BasicMultiMachine<?> machine, List<Pattern> patternList){
        machine.getTiers().forEach(t -> {
            registerPatternForJei(machine, t, patternList);
        });
    }
    public static void registerPatternForJei(BasicMultiMachine<?> machine, Tier tier, List<Pattern> patternList){
        STRUCTURES.put(machine.getBlockState(tier), patternList);
    }

    public static String intToSuperScript(long i){
        String intString = String.valueOf(i);
        StringBuilder builder = new StringBuilder();
        for (char c : intString.toCharArray()) {
            builder.append(charToSuperScript(c));
        }
        return builder.toString();
    }

    private static String charToSuperScript(char c){
        return switch (c){
            case '0' -> "⁰";
            case '1' -> "¹";
            case '2' -> "²";
            case '3' -> "³";
            case '4' -> "⁴";
            case '5' -> "⁵";
            case '6' -> "⁶";
            case '7' -> "⁷";
            case '8' -> "⁸";
            case '9' -> "⁹";
            default -> String.valueOf(c);
        };
    }

    public static void showCategory(Machine<?> type, Tier tier) {
        if (!type.has(RECIPE)) return;
        IRecipeMap map = type.getRecipeMap(tier);
        if (map == null) return; //incase someone adds tier specific recipe maps without a fallback
        List<ResourceLocation> categories = new ArrayList<>();
        categories.add(map.getLoc());
        if (!map.getSubCategories().isEmpty()){
            map.getSubCategories().keySet().forEach(s -> categories.add(new ResourceLocation(Ref.SHARED_ID, s)));
        }
        showCategories(categories.toArray(ResourceLocation[]::new));
    }

    public static void showCategories(ResourceLocation... categories){
        if (Mods.EMI.isLoaded()){
            GTLibEmiPlugin.showRecipes(categories);
        } else if (Mods.REI.isLoaded()){
            REIUtils.showCategories(categories);
        } else if (Mods.JEI.isLoaded()){
            GTLibJEIPlugin.showCategories(categories);
        }
    }


    public static <T> void addModDescriptor(List<Component> tooltip, T t) {
        if (GTAPI.isModLoaded(Ref.MOD_JEI) && !GTAPI.isModLoaded(Ref.MOD_REI)){
            GTLibJEIPlugin.addModDescriptor(tooltip, t);
        } else if (GTAPI.isModLoaded(Ref.MOD_REI)){
            REIUtils.addModDescriptor(tooltip, t);
        }
    }

    public static void addItemsToHide(ItemLike... items){
        addItemsToHide(l -> {
            l.addAll(Arrays.asList(items));
        });
    }

    public static void addItemsToHide(Consumer<List<ItemLike>> listConsumer){
        ITEMS_TO_HIDE.add(listConsumer);
    }

    public static void addFluidsToHide(Fluid... fluids){
        addFluidsToHide(l -> {
            l.addAll(Arrays.asList(fluids));
        });
    }


    public static void addFluidsToHide(Consumer<List<Fluid>> listConsumer){
        FLUIDS_TO_HIDE.add(listConsumer);
    }

    public static void addWorkstations(ResourceLocation category, Item... items){
        addWorkstations(category, l -> l.addAll(Arrays.asList(items)));
    }

    public static void addWorkstations(ResourceLocation category, Consumer<List<Item>> listConsumer){
        WORKSTATIONS.computeIfAbsent(category, r -> new ArrayList<>()).add(listConsumer);
    }

    public static List<Consumer<List<ItemLike>>> getItemsToHide() {
        return ITEMS_TO_HIDE;
    }

    public static List<Consumer<List<Fluid>>> getFluidsToHide() {
        return FLUIDS_TO_HIDE;
    }

}

package org.gtreimagined.gtlib.integration.recipeviewer.jei;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLib;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.block.BlockDimensionMarker;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.category.MultiMachineInfoCategory;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.category.RecipeMapCategory;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.category.SmallOreCategory;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.category.StoneVeinCategory;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.category.VeinCategory;
import org.gtreimagined.gtlib.integration.recipeviewer.jei.extension.JEIMaterialRecipeExtension;
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibRecipeViewerPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.StoneVein;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.material.MaterialRecipe;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOreData;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerData;
import org.gtreimagined.gtlib.worldgen.vein.VeinData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.gtreimagined.tesseract.api.forge.TesseractCaps;
import org.gtreimagined.tesseract.api.eu.IEnergyHandler;
import org.gtreimagined.tesseract.api.eu.IEnergyItem;
import org.gtreimagined.tesseract.api.wrapper.ItemStackWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@SuppressWarnings("removal")
@JeiPlugin
public class GTLibJEIPlugin implements IModPlugin {
    public static final Map<String, RecipeType<IRecipe>> RECIPE_TYPES = new Object2ObjectOpenHashMap<>();
    @Getter
    private static IJeiRuntime runtime;
    private static IJeiHelpers helpers;
    public static IGuiHelper guiHelper;

    public GTLibJEIPlugin() {
        GTLib.LOGGER.info("Creating GTAPI's JEI Plugin");
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Ref.ID, "jei");
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        guiHelper = runtime.getJeiHelpers().getGuiHelper();
        if (GTAPI.isModLoaded(Ref.MOD_REI) || (GTAPI.isModLoaded(Ref.MOD_EMI) && !FMLEnvironment.production)) return;
        //Remove fluid "blocks".
        List<ItemLike> list = new ArrayList<>();
        GTLibRecipeViewerPlugin.getItemsToHide().forEach(c -> c.accept(list));
        if (!list.isEmpty()) {
            runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, list.stream().map(i -> i.asItem().getDefaultInstance()).toList());
        }
        List<Fluid> fluidList = new ArrayList<>();
        GTLibRecipeViewerPlugin.getFluidsToHide().forEach(c -> c.accept(fluidList));
        // wish there was a better way to do this
        if (!fluidList.isEmpty()){
            runtime.getIngredientManager().removeIngredientsAtRuntime(ForgeTypes.FLUID_STACK,  fluidList.stream().map(f -> new FluidStack(f, 1)).toList());
            runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM_STACK, fluidList.stream().map(i -> i.getBucket().getDefaultInstance()).toList());
        }
        //runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, GTAPI.all(BlockSurfaceRock.class).stream().map(b -> new ItemStack(b, 1)).filter(t -> !t.isEmpty()).collect(Collectors.toList()));
        //runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, GTAPI.all(BlockOre.class).stream().filter(b -> b.getStoneType() != Data.STONE).map(b -> new ItemStack(b, 1)).collect(Collectors.toList()));
        //runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, Data.MACHINE_INVALID.getTiers().stream().map(t -> Data.MACHINE_INVALID.getItem(t).getDefaultInstance()).collect(Collectors.toList()));
    }



    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if (GTAPI.isModLoaded(Ref.MOD_REI)) return;
        List<ItemLike> list = new ArrayList<>();
        GTLibRecipeViewerPlugin.getItemsToHide().forEach(c -> c.accept(list));
        GTAPI.all(Item.class).forEach(i -> {
            if (list.contains(i)) return;
            if (i instanceof IEnergyItem energyItem && energyItem.canCreate(new ItemStackWrapper(i.getDefaultInstance()))) {
                registration.registerSubtypeInterpreter(i, (s, c) -> {
                    if (c == UidContext.Recipe) return "";
                    long energy = s.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(IEnergyHandler::getEnergy).orElse(0L);
                    long capacity = s.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(IEnergyHandler::getCapacity).orElse(0L);
                    return "e:" + energy + "/" + capacity;
                });
            }
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (GTAPI.isModLoaded(Ref.MOD_REI)) return;
        guiHelper = registry.getJeiHelpers().getGuiHelper();
        MultiMachineInfoCategory.setGuiHelper(registry.getJeiHelpers().getGuiHelper());
        if (helpers == null) helpers = registry.getJeiHelpers();
        Set<ResourceLocation> registeredMachineCats = new ObjectOpenHashSet<>();

        GTLibRecipeViewerPlugin.getREGISTRY().forEach((id, tuple) -> {
            if (!registeredMachineCats.contains(tuple.map.getLoc())) {
                RecipeType<IRecipe> type = new RecipeType<>(tuple.map.getLoc(), IRecipe.class);
                RECIPE_TYPES.put(type.getUid().toString(), type);
                registry.addRecipeCategories(new RecipeMapCategory(tuple.map, type, tuple.gui, tuple.tier, tuple.workstations.isEmpty() ? null : tuple.workstations.get(0)));
                registeredMachineCats.add(tuple.map.getLoc());
                if (!tuple.map.getSubCategories().isEmpty()){
                    tuple.map.getSubCategories().forEach((s, subCategory) -> {
                        ResourceLocation subCategoryId = new ResourceLocation(Ref.SHARED_ID, s);
                        if (!registeredMachineCats.contains(subCategoryId)) {
                            RecipeType<IRecipe> subType = new RecipeType<>(subCategoryId, IRecipe.class);
                            RECIPE_TYPES.put(subType.getUid().toString(), subType);
                            registeredMachineCats.add(subCategoryId);
                            registry.addRecipeCategories(new RecipeMapCategory(tuple.map, subType, tuple.gui, tuple.tier, subCategoryId, subCategory));
                        }
                    });
                }
            }
        });

        // multi machine
        registry.addRecipeCategories(new MultiMachineInfoCategory());
        registry.addRecipeCategories(new VeinCategory());
        registry.addRecipeCategories(new SmallOreCategory());
        registry.addRecipeCategories(new StoneVeinCategory());
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (GTAPI.isModLoaded(Ref.MOD_REI)) return;
        if (helpers == null) helpers = registration.getJeiHelpers();
        GTLibRecipeViewerPlugin.getREGISTRY().forEach((id, tuple) -> {
            if (tuple.map.getSubCategories().isEmpty()) {
                registration.addRecipes(RECIPE_TYPES.get(id.toString()), GTLibRecipeViewerPlugin.getRecipes(tuple.map, getRecipeManager()));
            } else {
                List<IRecipe> recipes = GTLibRecipeViewerPlugin.getRecipes(tuple.map, getRecipeManager());
                List<IRecipe> mainRecipes = new ArrayList<>();
                Map<String, List<IRecipe>> recipeMap = new HashMap<>();
                for (IRecipe recipe : recipes) {
                    boolean found = false;
                    for (var entry : tuple.map.getSubCategories().entrySet()) {
                        if (entry.getValue().predicate().test(recipe)){
                            found = true;
                            recipeMap.computeIfAbsent(entry.getKey(), (s) -> new ArrayList<>()).add(recipe);
                            break;
                        }
                    }
                    if (!found) {
                        mainRecipes.add(recipe);
                    }
                }
                registration.addRecipes(RECIPE_TYPES.get(id.toString()), mainRecipes);
                for (var entry : recipeMap.entrySet()) {
                    registration.addRecipes(RECIPE_TYPES.get(Ref.SHARED_ID + ":" + entry.getKey()), entry.getValue());
                }
            }
        });
        registration.addRecipes(VeinCategory.VEINS, VeinData.INSTANCE.getVeins().values().stream().toList());
        registration.addRecipes(SmallOreCategory.SMALL_ORES, SmallOreData.INSTANCE.getVeins().values().stream().toList());
        Object2IntMap<StoneType> veinTotalWeights = new Object2IntOpenHashMap<>();
        for (var layer : StoneLayerData.INSTANCE.getVeins().entrySet()) {
            if (layer.getValue().type() == null) continue;
            int currentWeight = veinTotalWeights.getOrDefault(layer.getValue().type(), 0);
            veinTotalWeights.put(layer.getValue().type(), currentWeight + layer.getValue().weight());
        }
        List<StoneVein> stoneVeins = new ArrayList<>();
        StoneLayerData.INSTANCE.getVeins().forEach((r, v) -> {
            if (!veinTotalWeights.containsKey(v.type())) return;
            v.ores().forEach(o -> {
                stoneVeins.add(new StoneVein(v, o, veinTotalWeights.getOrDefault(v.type(), 0)));
            });
        });
        registration.addRecipes(StoneVeinCategory.STONE_VEINS, stoneVeins);
        MultiMachineInfoCategory.registerRecipes(registration);
    }

    private RecipeManager getRecipeManager(){
        if (FMLEnvironment.dist.isDedicatedServer()){
            return ServerLifecycleHooks.getCurrentServer().getRecipeManager();
        } else {
            if (getWorld() == null) return null;
            return getWorld().getRecipeManager();
        }
    }

    @OnlyIn(Dist.CLIENT)
    ClientLevel getWorld(){
        return Minecraft.getInstance().level;
    }

    public static void showCategories(ResourceLocation... categories) {
        if (runtime != null) {
            List<RecipeType<?>> list = new ArrayList<>();
            for (ResourceLocation r : ImmutableList.copyOf(categories)) {
                RecipeType<IRecipe> iRecipeRecipeType = RECIPE_TYPES.get(r.toString());
                list.add(iRecipeRecipeType);
            }
            runtime.getRecipesGui().showTypes(list);
        }
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        if (GTAPI.isModLoaded(Ref.MOD_REI)) return;
        registration.getCraftingCategory().addCategoryExtension(MaterialRecipe.class, JEIMaterialRecipeExtension::new);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {

    }

    public static <T> void addModDescriptor(List<Component> tooltip, T t) {
        if (t == null || helpers == null) return;
        String text = helpers.getModIdHelper().getFormattedModNameForModId(getRuntime().getIngredientManager().getIngredientHelper((Object) t).getDisplayModId(t));
        tooltip.add(Utils.literal(text));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (GTAPI.isModLoaded(Ref.MOD_REI)) return;
        GTLibRecipeViewerPlugin.getREGISTRY().forEach((id, tuple) -> {
            if (tuple.workstations.isEmpty()) return;
            tuple.workstations.forEach(s -> {
                ItemLike item = RegistryUtils.getItemFromID(s);
                if (item == Items.AIR) return;
                registration.addRecipeCatalyst(new ItemStack(item), RECIPE_TYPES.get(tuple.map.getLoc().toString()));
                if (!tuple.map.getSubCategories().isEmpty()){
                    tuple.map.getSubCategories().keySet().forEach(s1 -> registration.addRecipeCatalyst(new ItemStack(item), RECIPE_TYPES.get(new ResourceLocation(Ref.SHARED_ID, s1).toString())));
                }
            });
        });
        GTLibRecipeViewerPlugin.getWORKSTATIONS().forEach((r, l) -> {
            List<Item> list = new ArrayList<>();
            l.forEach(l2 -> l2.accept(list));
            list.forEach(i -> {
                registration.addRecipeCatalyst(new ItemStack(i), RecipeType.create(r.getNamespace(), r.getPath(), Recipe.class));
            });
        });
    }


    public static void addDimensionSlots(IRecipeLayoutBuilder builder, List<ResourceKey<Level>> dimensions) {
        int i = 0;
        List<Block> markers = new ArrayList<>();
        for (ResourceLocation dimension : dimensions.stream().map(ResourceKey::location).toList()) {
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

    public static void uses(FluidStack val, boolean USE) {
        GTLibJEIPlugin.getRuntime().getRecipesGui().show(new IFocus<FluidStack>() {
            @Override
            public ITypedIngredient<FluidStack> getTypedValue() {
                return new ITypedIngredient<>() {
                    @Override
                    public IIngredientType<FluidStack> getType() {
                        return ForgeTypes.FLUID_STACK;
                    }

                    @Override
                    public FluidStack getIngredient() {
                        return val;
                    }

                    @Override
                    public <V> Optional<V> getIngredient(IIngredientType<V> ingredientType) {
                        if (ingredientType == ForgeTypes.FLUID_STACK) return ((Optional<V>) Optional.of(val));
                        return Optional.empty();
                    }
                };
            }

            @Override
            public RecipeIngredientRole getRole() {
                return USE ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT;
            }

            @Override
            public <T> Optional<IFocus<T>> checkedCast(IIngredientType<T> ingredientType) {
                return Optional.empty();
            }

        });
    }
}

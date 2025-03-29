package org.gtreimagined.gtlib.integration.jei;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.forge.ForgeTypes;
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
import org.gtreimagined.gtlib.Antimatter;
import org.gtreimagined.gtlib.AntimatterAPI;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.integration.jei.category.MultiMachineInfoCategory;
import org.gtreimagined.gtlib.integration.jei.category.RecipeMapCategory;
import org.gtreimagined.gtlib.integration.jei.category.VeinCategory;
import org.gtreimagined.gtlib.integration.jei.extension.JEIMaterialRecipeExtension;
import org.gtreimagined.gtlib.integration.jeirei.AntimatterJEIREIPlugin;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.map.IRecipeMap;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.recipe.material.MaterialRecipe;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.Utils;
import org.gtreimagined.gtlib.worldgen.AntimatterWorldGenerator;
import org.gtreimagined.gtlib.worldgen.vein.WorldGenVeinLayer;
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
import tesseract.api.forge.TesseractCaps;
import tesseract.api.gt.IEnergyItem;
import tesseract.api.gt.IGTNode;
import tesseract.api.wrapper.ItemStackWrapper;

import java.util.ArrayList;
import java.util.Collections;
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

    public GTLibJEIPlugin() {
        Antimatter.LOGGER.info("Creating AntimatterAPI's JEI Plugin");
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(Ref.ID, "jei");
    }

    @Override
    public void onRuntimeAvailable(@NotNull IJeiRuntime jeiRuntime) {
        if (AntimatterAPI.isModLoaded(Ref.MOD_REI)) return;
        runtime = jeiRuntime;
        //Remove fluid "blocks".
        List<ItemLike> list = new ArrayList<>();
        AntimatterJEIREIPlugin.getItemsToHide().forEach(c -> c.accept(list));
        if (!list.isEmpty()) {
            runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, list.stream().map(i -> i.asItem().getDefaultInstance()).toList());
        }
        List<Fluid> fluidList = new ArrayList<>();
        AntimatterJEIREIPlugin.getFluidsToHide().forEach(c -> c.accept(fluidList));
        // wish there was a better way to do this
        if (!fluidList.isEmpty()){
            runtime.getIngredientManager().removeIngredientsAtRuntime(ForgeTypes.FLUID_STACK,  fluidList.stream().map(f -> new FluidStack(f, 1)).toList());
            runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, fluidList.stream().map(i -> i.getBucket().getDefaultInstance()).toList());
        }
        //runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, AntimatterAPI.all(BlockSurfaceRock.class).stream().map(b -> new ItemStack(b, 1)).filter(t -> !t.isEmpty()).collect(Collectors.toList()));
        //runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, AntimatterAPI.all(BlockOre.class).stream().filter(b -> b.getStoneType() != Data.STONE).map(b -> new ItemStack(b, 1)).collect(Collectors.toList()));
        //runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, Data.MACHINE_INVALID.getTiers().stream().map(t -> Data.MACHINE_INVALID.getItem(t).getDefaultInstance()).collect(Collectors.toList()));
    }



    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        if (AntimatterAPI.isModLoaded(Ref.MOD_REI)) return;
        List<ItemLike> list = new ArrayList<>();
        AntimatterJEIREIPlugin.getItemsToHide().forEach(c -> c.accept(list));
        AntimatterAPI.all(Item.class).forEach(i -> {
            if (list.contains(i)) return;
            if (i instanceof IEnergyItem energyItem && energyItem.canCreate(new ItemStackWrapper(i.getDefaultInstance()))) {
                registration.registerSubtypeInterpreter(i, (s, c) -> {
                    if (c == UidContext.Recipe) return "";
                    long energy = s.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(IGTNode::getEnergy).orElse(0L);
                    long capacity = s.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM).map(IGTNode::getCapacity).orElse(0L);
                    return "e:" + energy + "/" + capacity;
                });
            }
        });
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (AntimatterAPI.isModLoaded(Ref.MOD_REI)) return;
        RecipeMapCategory.setGuiHelper(registry.getJeiHelpers().getGuiHelper());
        MultiMachineInfoCategory.setGuiHelper(registry.getJeiHelpers().getGuiHelper());
        if (helpers == null) helpers = registry.getJeiHelpers();
        Set<ResourceLocation> registeredMachineCats = new ObjectOpenHashSet<>();

        AntimatterJEIREIPlugin.getREGISTRY().forEach((id, tuple) -> {
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
    }

    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        if (AntimatterAPI.isModLoaded(Ref.MOD_REI)) return;
        if (helpers == null) helpers = registration.getJeiHelpers();
        AntimatterJEIREIPlugin.getREGISTRY().forEach((id, tuple) -> {
            if (tuple.map.getSubCategories().isEmpty()) {
                registration.addRecipes(RECIPE_TYPES.get(id.toString()), getRecipes(tuple.map));
            } else {
                Antimatter.LOGGER.info(tuple.map.getId());
                List<IRecipe> recipes = getRecipes(tuple.map);
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
        registration.addRecipes(VeinCategory.VEIN_LAYERS, AntimatterWorldGenerator.all(WorldGenVeinLayer.class));
        MultiMachineInfoCategory.registerRecipes(registration);
    }

    private List<IRecipe> getRecipes(IRecipeMap recipeMap){
        RecipeManager manager = getRecipeManager();
        if (manager == null) return Collections.emptyList();
        List<IRecipe> recipes = new ArrayList<>(manager.getAllRecipesFor(recipeMap.getRecipeType()).stream().filter(r -> r.getMapId().equals(recipeMap.getId()) && !r.isHidden()).toList());
        if (recipeMap.getProxy() != null && recipeMap instanceof RecipeMap<?> map) {
            List<net.minecraft.world.item.crafting.Recipe<?>> proxyRecipes = (List<net.minecraft.world.item.crafting.Recipe<?>>) manager.getAllRecipesFor(recipeMap.getProxy().loc());
            proxyRecipes.forEach(recipe -> {
                IRecipe recipe1 = recipeMap.getProxy().handler().apply(recipe, map.RB());
                if (recipe1 != null && !recipe1.isHidden()){
                    recipes.add(recipe1);
                }
            });
        }
        return recipes;
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
            runtime.getRecipesGui().showCategories(ImmutableList.copyOf(categories));
        }
    }

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        if (AntimatterAPI.isModLoaded(Ref.MOD_REI)) return;
        registration.getCraftingCategory().addCategoryExtension(MaterialRecipe.class, JEIMaterialRecipeExtension::new);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        AntimatterJEIREIPlugin.getREGISTRY().forEach((id, tuple) -> {
            registration.addRecipeTransferHandler(new MachineTransferHandler(tuple.map.getLoc()));
        });
    }

    public static <T> void addModDescriptor(List<Component> tooltip, T t) {
        if (t == null || helpers == null) return;
        String text = helpers.getModIdHelper().getFormattedModNameForModId(getRuntime().getIngredientManager().getIngredientHelper((Object) t).getDisplayModId(t));
        tooltip.add(Utils.literal(text));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (AntimatterAPI.isModLoaded(Ref.MOD_REI)) return;
        AntimatterJEIREIPlugin.getREGISTRY().forEach((id, tuple) -> {
            if (tuple.workstations.isEmpty()) return;
            tuple.workstations.forEach(s -> {
                ItemLike item = RegistryUtils.getItemFromID(s);
                if (item == Items.AIR) return;
                registration.addRecipeCatalyst(new ItemStack(item), tuple.map.getLoc());
                if (!tuple.map.getSubCategories().isEmpty()){
                    tuple.map.getSubCategories().keySet().forEach(s1 -> registration.addRecipeCatalyst(new ItemStack(item), new ResourceLocation(Ref.SHARED_ID, s1)));
                }
            });
        });
        AntimatterJEIREIPlugin.getWORKSTATIONS().forEach((r, l) -> {
            List<Item> list = new ArrayList<>();
            l.forEach(l2 -> l2.accept(list));
            list.forEach(i -> {
                registration.addRecipeCatalyst(new ItemStack(i), RecipeType.create(r.getNamespace(), r.getPath(), Recipe.class));
            });
        });
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

            @Override
            public Mode getMode() {
                return USE ? Mode.INPUT : Mode.OUTPUT;
            }

        });
    }
}

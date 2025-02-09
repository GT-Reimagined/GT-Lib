package muramasa.antimatter.integration.jei;

import com.google.common.collect.ImmutableList;
import earth.terrarium.botarium.common.fluid.base.FluidHolder;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import lombok.Getter;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import muramasa.antimatter.Antimatter;
import muramasa.antimatter.AntimatterAPI;
import muramasa.antimatter.Ref;
import muramasa.antimatter.integration.jei.category.MultiMachineInfoCategory;
import muramasa.antimatter.integration.jei.category.RecipeMapCategory;
import muramasa.antimatter.integration.jei.extension.JEIMaterialRecipeExtension;
import muramasa.antimatter.integration.jeirei.AntimatterJEIREIPlugin;
import muramasa.antimatter.machine.Tier;
import muramasa.antimatter.machine.types.Machine;
import muramasa.antimatter.recipe.IRecipe;
import muramasa.antimatter.recipe.map.IRecipeMap;
import muramasa.antimatter.recipe.map.RecipeMap;
import muramasa.antimatter.recipe.material.MaterialRecipe;
import muramasa.antimatter.util.AntimatterPlatformUtils;
import muramasa.antimatter.util.Utils;
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
import org.jetbrains.annotations.NotNull;
import tesseract.TesseractCapUtils;
import tesseract.api.gt.IEnergyItem;
import tesseract.api.gt.IGTNode;
import tesseract.api.wrapper.ItemStackWrapper;
import xyz.wagyourtail.unimined.expect.annotation.Environment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static muramasa.antimatter.machine.MachineFlag.RECIPE;

@SuppressWarnings("removal")
@JeiPlugin
public class AntimatterJEIPlugin implements IModPlugin {
    public static final Map<String, RecipeType<IRecipe>> RECIPE_TYPES = new Object2ObjectOpenHashMap<>();
    @Getter
    private static IJeiRuntime runtime;
    private static IJeiHelpers helpers;

    public AntimatterJEIPlugin() {
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
            runtime.getIngredientManager().removeIngredientsAtRuntime(JEIPlatformHelper.INSTANCE.getFluidIngredientObjectType(), (Collection) fluidList.stream().map(f -> JEIPlatformHelper.INSTANCE.getFluidObject(FluidHolder.of(f))).toList());
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
                    long energy = TesseractCapUtils.INSTANCE.getEnergyHandlerItem(s).map(IGTNode::getEnergy).orElse(0L);
                    long capacity = TesseractCapUtils.INSTANCE.getEnergyHandlerItem(s).map(IGTNode::getCapacity).orElse(0L);
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
                Antimatter.LOGGER.info(mainRecipes.stream().map(r -> r.getId().toString()).toList());
                registration.addRecipes(RECIPE_TYPES.get(id.toString()), mainRecipes);
                for (var entry : recipeMap.entrySet()) {
                    Antimatter.LOGGER.info(entry.getValue().stream().map(r -> r.getId().toString()).toList());
                    registration.addRecipes(RECIPE_TYPES.get(Ref.SHARED_ID + ":" + entry.getKey()), entry.getValue());
                }
            }
        });
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
        if (AntimatterAPI.getSIDE().isServer()){
            return AntimatterPlatformUtils.INSTANCE.getCurrentServer().getRecipeManager();
        } else {
            if (getWorld() == null) return null;
            return getWorld().getRecipeManager();
        }
    }

    @Environment(Environment.EnvType.CLIENT)
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
        Object o = t;
        if (t instanceof FluidHolder holder) o = JEIPlatformHelper.INSTANCE.getFluidObject(holder);
        String text = helpers.getModIdHelper().getFormattedModNameForModId(getRuntime().getIngredientManager().getIngredientHelper(o).getDisplayModId(o));
        tooltip.add(Utils.literal(text));
    }

    @Override
    public void registerRecipeCatalysts(@NotNull IRecipeCatalystRegistration registration) {
        if (AntimatterAPI.isModLoaded(Ref.MOD_REI)) return;
        AntimatterJEIREIPlugin.getREGISTRY().forEach((id, tuple) -> {
            if (tuple.workstations.isEmpty()) return;
            tuple.workstations.forEach(s -> {
                ItemLike item = AntimatterPlatformUtils.INSTANCE.getItemFromID(s);
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
}

package org.gtreimagined.gtlib.integration.recipeviewer.rei;

import dev.architectury.fluid.FluidStack;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import me.shedaniel.rei.api.client.entry.filtering.base.BasicFilteringRule;
import me.shedaniel.rei.api.client.plugins.REIClientPlugin;
import me.shedaniel.rei.api.client.registry.category.CategoryRegistry;
import me.shedaniel.rei.api.client.registry.display.DisplayRegistry;
import me.shedaniel.rei.api.client.registry.entry.CollapsibleEntryRegistry;
import me.shedaniel.rei.api.common.entry.EntryStack;
import me.shedaniel.rei.api.common.entry.settings.EntrySettingsAdapterRegistry;
import me.shedaniel.rei.api.common.entry.type.VanillaEntryTypes;
import me.shedaniel.rei.api.common.plugins.PluginManager;
import me.shedaniel.rei.api.common.registry.ReloadStage;
import me.shedaniel.rei.forge.REIPluginClient;
import me.shedaniel.rei.plugin.common.BuiltinPlugin;
import org.gtreimagined.gtlib.GTAPI;
import org.gtreimagined.gtlib.GTLibConfig;
import org.gtreimagined.gtlib.Ref;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibRecipeViewerPlugin;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.category.RecipeMapCategory;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.category.SmallOreCategory;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.category.VeinCategory;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.display.RecipeMapDisplay;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.display.SmallOreDisplay;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.display.VeinDisplay;
import org.gtreimagined.gtlib.integration.recipeviewer.rei.extension.REIMaterialRecipeExtension;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.material.MaterialTypeBlock;
import org.gtreimagined.gtlib.material.MaterialTypeItem;
import org.gtreimagined.gtlib.ore.CobbleStoneType;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.recipe.IRecipe;
import org.gtreimagined.gtlib.recipe.map.RecipeMap;
import org.gtreimagined.gtlib.util.RegistryUtils;
import org.gtreimagined.gtlib.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOreData;
import org.gtreimagined.gtlib.worldgen.vein.VeinData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

@REIPluginClient()
public class GTLibREIClientPlugin implements REIClientPlugin {
    @Override
    public String getPluginProviderName() {
        return Ref.ID + ":rei";
    }

    @Override
    public void registerCollapsibleEntries(CollapsibleEntryRegistry registry) {
        if (!GTLibConfig.ADD_REI_GROUPS.get()) return;

        GTAPI.all(MaterialType.class).stream().filter(t -> t instanceof MaterialTypeItem<?> || t instanceof MaterialTypeBlock<?>).forEach(t -> {
            if (t.get() instanceof MaterialTypeBlock.IOreGetter getter){
                GTAPI.all(StoneType.class, s -> {
                    if (s != VanillaStoneTypes.STONE && !GTLibConfig.SHOW_ALL_ORES.get() && t != GTMaterialTypes.BEARING_ROCK) return;
                    if (t == GTMaterialTypes.BEARING_ROCK && !GTLibConfig.SHOW_ROCKS.get()) return;
                    List<EntryStack<ItemStack>> entries = t.all().stream().map(m -> EntryStack.of(VanillaEntryTypes.ITEM, getter.get((Material) m, s).asStack())).toList();
                    registry.group(new ResourceLocation(Ref.SHARED_ID, t.getId() + "_" + s.getId()), Utils.translatable(Ref.ID + ".rei.group." + t.getId() + "." + s.getId()), entries);
                });
                if (t != GTMaterialTypes.BEARING_ROCK){
                    return;
                }
            }
            if (GTLibConfig.GROUP_ORES_ONLY.get()) return;
            Function<Material, ItemStack> func = null;
            if (t instanceof MaterialTypeItem<?> typeItem){
                func = m -> typeItem.get(m, 1);
            }
            if (t instanceof MaterialTypeBlock<?> typeBlock){
                Object func2 = typeBlock.get();
                if (func2 instanceof MaterialTypeBlock.IBlockGetter getter){
                    func = m -> getter.get(m).asStack();
                }
            }
            if (func == null) return;
            Function<Material, ItemStack> finalFunc = func;
            List<EntryStack<ItemStack>> entries = t.all().stream().map(m -> EntryStack.of(VanillaEntryTypes.ITEM, finalFunc.apply((Material) m))).toList();
            registry.group(new ResourceLocation(Ref.SHARED_ID, t.getId()), Utils.translatable(Ref.ID + ".rei.group." + t.getId()), entries);
        });
        if (GTLibConfig.GROUP_ORES_ONLY.get()) return;
        GTAPI.all(StoneType.class, s -> {
            if (s instanceof CobbleStoneType cobble){
                List<EntryStack<ItemStack>> entries = cobble.getBlocks().values().stream().map(b -> EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(b.asItem()))).toList();
                registry.group(new ResourceLocation(Ref.SHARED_ID, s.getId()), Utils.translatable(Ref.ID + ".rei.group." + s.getId()), entries);
            }
        });
    }

    @Override
    public void registerBasicEntryFiltering(BasicFilteringRule<?> rule) {
        List<ItemLike> list = new ArrayList<>();
        GTLibRecipeViewerPlugin.getItemsToHide().forEach(c -> c.accept(list));
        list.forEach(i -> rule.hide(EntryStack.of(VanillaEntryTypes.ITEM, i.asItem().getDefaultInstance())));
        List<Fluid> fluidList = new ArrayList<>();
        GTLibRecipeViewerPlugin.getFluidsToHide().forEach(c -> c.accept(fluidList));
        fluidList.forEach(f -> {
            rule.hide(EntryStack.of(VanillaEntryTypes.FLUID, FluidStack.create(f, 1)));
            rule.hide(EntryStack.of(VanillaEntryTypes.ITEM, f.getBucket().getDefaultInstance()));
        });
    }

    @Override
    public void registerCategories(CategoryRegistry registry) {
        Set<ResourceLocation> registeredMachineCats = new ObjectOpenHashSet<>();

        GTLibRecipeViewerPlugin.getREGISTRY().forEach((id, tuple) -> {
            if (!registeredMachineCats.contains(tuple.map.getLoc())) {
                RecipeMapCategory category = new RecipeMapCategory(tuple.map, tuple.workstations.isEmpty() ? null : tuple.workstations.get(0));
                registry.add(category);
                if (!tuple.workstations.isEmpty()){
                    tuple.workstations.forEach(s -> {
                        ItemLike item = RegistryUtils.getItemFromID(s);
                        if (item == Items.AIR) return;
                        registry.addWorkstations(category.getCategoryIdentifier(), EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(item)));
                    });
                }
                for (var e : tuple.map.getSubCategories().entrySet()){
                    RecipeMapCategory subCategory = new RecipeMapCategory(new ResourceLocation(tuple.map.getDomain(), e.getKey()), e.getValue());
                    registry.add(subCategory);
                    if (!tuple.workstations.isEmpty()){
                        tuple.workstations.forEach(s -> {
                            ItemLike item = RegistryUtils.getItemFromID(s);
                            if (item == Items.AIR) return;
                            registry.addWorkstations(subCategory.getCategoryIdentifier(), EntryStack.of(VanillaEntryTypes.ITEM, new ItemStack(item)));
                        });
                    }
                }
                registeredMachineCats.add(tuple.map.getLoc());
            }
        });
        registry.add(new SmallOreCategory());
        registry.add(new VeinCategory());
        REIUtils.EXTRA_CATEGORIES.forEach(c -> c.accept(registry));
    }

    @Override
    public void registerDisplays(DisplayRegistry registry) {
        // regular recipes
        GTLibRecipeViewerPlugin.getREGISTRY().values().forEach(t -> {
            var m = t.map;
            if (m instanceof RecipeMap<?> rm){
                if (m.getProxy() != null){
                    registry.registerRecipeFiller(net.minecraft.world.item.crafting.Recipe.class, m.getProxy().loc(), r -> {
                        IRecipe recipe = m.getProxy().handler().apply(r, rm.RB());
                        if (recipe == null) return null;
                        if (recipe.isHidden()) return null;
                        return new RecipeMapDisplay(recipe, m, t.gui, t.tier);
                    });
                } else {
                    registry.registerRecipeFiller(IRecipe.class, type -> RecipeMap.getRecipeTypes().contains(type), r -> !r.isHidden() && r.getMapLoc().equals(m.getLoc()), r -> {
                        ResourceLocation categoryId = m.getLoc();
                        if (!m.getSubCategories().isEmpty()){
                            for (var e : m.getSubCategories().entrySet()){
                                if (e.getValue().predicate().test(r)){
                                    categoryId = new ResourceLocation(m.getDomain(), e.getKey());
                                    break;
                                }
                            }
                        }
                        return new RecipeMapDisplay(r, m, t.gui, t.tier, categoryId);
                    });
                }
            }
        });
        SmallOreData.INSTANCE.getVeins().values().stream().map(SmallOreDisplay::new).forEach(registry::add);
        VeinData.INSTANCE.getVeins().values().stream().map(VeinDisplay::new).forEach(registry::add);
        REIUtils.EXTRA_DISPLAYS.forEach(c -> c.accept(registry));
    }

    @Override
    public void postStage(PluginManager<REIClientPlugin> manager, ReloadStage stage) {
        if (stage != ReloadStage.END || !manager.equals(PluginManager.getClientInstance())) return;
        CategoryRegistry.getInstance().get(BuiltinPlugin.CRAFTING).registerExtension(new REIMaterialRecipeExtension());
    }

    @Override
    public void registerEntrySettingsAdapters(EntrySettingsAdapterRegistry registry) {
        REIClientPlugin.super.registerEntrySettingsAdapters(registry);
    }
}

package org.gtreimagined.gtlib.integration.recipeviewer.jei

import com.google.common.collect.ImmutableList
import it.unimi.dsi.fastutil.objects.Object2IntMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import mezz.jei.api.IModPlugin
import mezz.jei.api.JeiPlugin
import mezz.jei.api.constants.VanillaTypes
import mezz.jei.api.forge.ForgeTypes
import mezz.jei.api.helpers.IGuiHelper
import mezz.jei.api.helpers.IJeiHelpers
import mezz.jei.api.ingredients.subtypes.UidContext
import mezz.jei.api.recipe.RecipeType
import mezz.jei.api.registration.*
import mezz.jei.api.runtime.IJeiRuntime
import net.minecraft.client.Minecraft
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.material.Fluid
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.api.distmarker.OnlyIn
import net.minecraftforge.fluids.FluidStack
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.server.ServerLifecycleHooks
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.GTAPI.isModLoaded
import org.gtreimagined.gtlib.GTLib
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibRecipeViewerPlugin
import org.gtreimagined.gtlib.integration.recipeviewer.StoneVein
import org.gtreimagined.gtlib.integration.recipeviewer.jei.category.*
import org.gtreimagined.gtlib.integration.recipeviewer.jei.extension.JEIMaterialRecipeExtension
import org.gtreimagined.gtlib.ore.StoneType
import org.gtreimagined.gtlib.recipe.IRecipe
import org.gtreimagined.gtlib.recipe.material.MaterialRecipe
import org.gtreimagined.gtlib.util.RegistryUtils
import org.gtreimagined.gtlib.util.literal
import org.gtreimagined.gtlib.worldgen.smallore.SmallOreData
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerData
import org.gtreimagined.gtlib.worldgen.vein.VeinData
import org.gtreimagined.tesseract.api.eu.IEnergyItem
import org.gtreimagined.tesseract.api.forge.TesseractCaps
import org.gtreimagined.tesseract.api.wrapper.ItemStackWrapper
import thedarkcolour.kotlinforforge.forge.runForDist
import thedarkcolour.kotlinforforge.forge.sidedDelegate
import java.util.function.Consumer
import java.util.function.Function

@JeiPlugin
class GTLibJEIPlugin : IModPlugin {
    init {
        GTLib.LOGGER.info("Creating GTAPI's JEI Plugin")
    }

    override fun getPluginUid(): ResourceLocation {
        return ResourceLocation(Ref.ID, "jei")
    }

    override fun onRuntimeAvailable(jeiRuntime: IJeiRuntime) {
        runtime = jeiRuntime
        guiHelper = runtime!!.jeiHelpers.guiHelper
        if (isModLoaded(Ref.MOD_REI) || (isModLoaded(Ref.MOD_EMI) && !FMLEnvironment.production)) return
        //Remove fluid "blocks".
        val list: MutableList<ItemLike> = ArrayList()
        GTLibRecipeViewerPlugin.getItemsToHide().forEach { it.accept(list) }
        if (list.isNotEmpty()) {
            runtime!!.ingredientManager.removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                list.stream().map { i -> i.asItem().defaultInstance }.toList()
            )
        }
        val fluidList: MutableList<Fluid> = ArrayList()
        GTLibRecipeViewerPlugin.getFluidsToHide().forEach { it.accept(fluidList) }
        // wish there was a better way to do this
        if (fluidList.isNotEmpty()) {
            runtime!!.ingredientManager.removeIngredientsAtRuntime(
                ForgeTypes.FLUID_STACK,
                fluidList.stream().map { f: Fluid? -> FluidStack(f, 1) }.toList()
            )
            runtime!!.ingredientManager.removeIngredientsAtRuntime(
                VanillaTypes.ITEM_STACK,
                fluidList.stream().map { i: Fluid? -> i!!.bucket.defaultInstance }.toList()
            )
        }
        //runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, GTAPI.all(BlockSurfaceRock.class).stream().map(b -> new ItemStack(b, 1)).filter(t -> !t.isEmpty()).collect(Collectors.toList()));
        //runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, GTAPI.all(BlockOre.class).stream().filter(b -> b.getStoneType() != Data.STONE).map(b -> new ItemStack(b, 1)).collect(Collectors.toList()));
        //runtime.getIngredientManager().removeIngredientsAtRuntime(VanillaTypes.ITEM, Data.MACHINE_INVALID.getTiers().stream().map(t -> Data.MACHINE_INVALID.getItem(t).getDefaultInstance()).collect(Collectors.toList()));
    }


    override fun registerItemSubtypes(registration: ISubtypeRegistration) {
        if (isModLoaded(Ref.MOD_REI)) return
        val list: MutableList<ItemLike?> = ArrayList()
        GTLibRecipeViewerPlugin.getItemsToHide()
            .forEach(Consumer { c: Consumer<MutableList<ItemLike?>?>? -> c!!.accept(list) })
        GTAPI.all<Item>().forEach { i ->
            if (list.contains(i)) return@forEach
            if (i is IEnergyItem && i.canCreate(ItemStackWrapper(i.defaultInstance))) {
                registration.registerSubtypeInterpreter(i) { s, c ->
                    if (c == UidContext.Recipe) return@registerSubtypeInterpreter ""
                    val energy = s.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM)
                        .map { it.getEnergy() }.orElse(0L)
                    val capacity = s.getCapability(TesseractCaps.ENERGY_HANDLER_CAPABILITY_ITEM)
                            .map { it.getCapacity() }.orElse(0L)
                    "e:$energy/$capacity"
                }
            }
        }
    }

    override fun registerCategories(registry: IRecipeCategoryRegistration) {
        if (isModLoaded(Ref.MOD_REI)) return
        guiHelper = registry.jeiHelpers.guiHelper
        MultiMachineInfoCategory.setGuiHelper(registry.jeiHelpers.guiHelper)
        if (helpers == null) helpers = registry.jeiHelpers
        val registeredMachineCats: MutableSet<ResourceLocation?> = ObjectOpenHashSet<ResourceLocation?>()

        GTLibRecipeViewerPlugin.REGISTRY
            .forEach { (_, tuple) ->
                val (map, gui, tier, workstations) = tuple
                if (!registeredMachineCats.contains(map.loc)) {
                    val type = RecipeType(map.loc, IRecipe::class.java)
                    RECIPE_TYPES[type.uid.toString()] = type
                    registry.addRecipeCategories(RecipeMapCategory(map, type, gui, tier,
                            if (workstations.isEmpty()) null else workstations[0]))
                    registeredMachineCats.add(map.loc)
                    if (map.getSubCategories().isNotEmpty()) {
                        map.getSubCategories().forEach { (s, subCategory) ->
                            val subCategoryId = ResourceLocation(map.domain, s)
                            if (!registeredMachineCats.contains(subCategoryId)) {
                                val subType = RecipeType(subCategoryId, IRecipe::class.java)
                                RECIPE_TYPES[subType.uid.toString()] = subType
                                registeredMachineCats.add(subCategoryId)
                                registry.addRecipeCategories(
                                    RecipeMapCategory(map, subType, gui, tier, subCategoryId, subCategory)
                                )
                            }
                        }
                    }
                }
            }

        // multi machine
        registry.addRecipeCategories(MultiMachineInfoCategory())
        registry.addRecipeCategories(VeinCategory())
        registry.addRecipeCategories(SmallOreCategory())
        registry.addRecipeCategories(StoneVeinCategory())
    }

    override fun registerRecipes(registration: IRecipeRegistration) {
        if (isModLoaded(Ref.MOD_REI)) return
        if (helpers == null) helpers = registration.jeiHelpers
        GTLibRecipeViewerPlugin.REGISTRY
            .forEach { (id, tuple) ->
                val (map, _, _, _) = tuple
                if (map.getSubCategories().isEmpty()) {
                    registration.addRecipes(RECIPE_TYPES[id.toString()]!!, GTLibRecipeViewerPlugin.getRecipes(map, this.recipeManager))
                } else {
                    val recipes = GTLibRecipeViewerPlugin.getRecipes(map, this.recipeManager)
                    val mainRecipes: MutableList<IRecipe> = ArrayList()
                    val recipeMap: MutableMap<String, MutableList<IRecipe>> = HashMap()
                    for (recipe in recipes) {
                        var found = false
                        for ((key, value) in map.getSubCategories()) {
                            if (value.predicate.test(recipe)) {
                                found = true
                                recipeMap.computeIfAbsent(key) { ArrayList() }
                                    .add(recipe)
                                break
                            }
                        }
                        if (!found) {
                            mainRecipes.add(recipe)
                        }
                    }
                    registration.addRecipes(RECIPE_TYPES[id.toString()]!!, mainRecipes)
                    for ((key, value) in recipeMap) {
                        registration.addRecipes(RECIPE_TYPES[id.namespace + ":" + key]!!, value)
                    }
                }
            }
        registration.addRecipes(VeinCategory.VEINS, VeinData.veins.values.stream().toList())
        registration.addRecipes(SmallOreCategory.SMALL_ORES, SmallOreData.veins.values.stream().toList())
        val veinTotalWeights: Object2IntMap<StoneType> = Object2IntOpenHashMap()
        for ((_, value) in StoneLayerData.veins) {
            if (value.type == null) continue
            val currentWeight = veinTotalWeights.getOrDefault(value.type, 0)
            veinTotalWeights.put(value.type, currentWeight + value.weight)
        }
        val stoneVeins: MutableList<StoneVein> = ArrayList()
        StoneLayerData.veins.forEach { (_, v) ->
            if (!veinTotalWeights.containsKey(v.type)) return@forEach
            v.ores.forEach { o ->
                stoneVeins.add(StoneVein(v, o, veinTotalWeights.getOrDefault(v.type, 0)))
            }
        }
        registration.addRecipes(StoneVeinCategory.STONE_VEINS, stoneVeins)
        MultiMachineInfoCategory.registerRecipes(registration)
    }

    private val recipeManager: RecipeManager?
        get() {
            return runForDist({ Minecraft.getInstance().level?.recipeManager}) { ServerLifecycleHooks.getCurrentServer().recipeManager }
        }

    override fun registerVanillaCategoryExtensions(registration: IVanillaCategoryExtensionRegistration) {
        if (isModLoaded(Ref.MOD_REI)) return
        registration.craftingCategory.addCategoryExtension(MaterialRecipe::class.java, Function(::JEIMaterialRecipeExtension))
    }

    override fun registerRecipeTransferHandlers(registration: IRecipeTransferRegistration) {
    }

    override fun registerRecipeCatalysts(registration: IRecipeCatalystRegistration) {
        if (isModLoaded(Ref.MOD_REI)) return
        GTLibRecipeViewerPlugin.REGISTRY
            .forEach { (_, tuple) ->
                val (map, _, _, workstations) = tuple
                if (workstations.isEmpty()) return@forEach
                workstations.forEach { s ->
                    val item: ItemLike = RegistryUtils.getItemFromID(s)
                    if (item === Items.AIR) return@forEach
                    registration.addRecipeCatalyst(ItemStack(item), RECIPE_TYPES[map.loc.toString()])
                    if (tuple.map.getSubCategories().isNotEmpty()) {
                        tuple.map.getSubCategories().keys.forEach { s1 ->
                            registration.addRecipeCatalyst(ItemStack(item),
                                RECIPE_TYPES[ResourceLocation(map.domain, s1).toString()])
                        }
                    }
                }
            }
        GTLibRecipeViewerPlugin.WORKSTATIONS
            .forEach { (r, l) ->
                val list: MutableList<Item> = ArrayList()
                l.forEach { it.accept(list) }
                list.forEach { i ->
                    registration.addRecipeCatalyst(ItemStack(i), RecipeType.create(r!!.namespace, r.path, Recipe::class.java))
                }
            }
    }

    companion object {
        val RECIPE_TYPES: MutableMap<String, RecipeType<IRecipe>> =
            Object2ObjectOpenHashMap()

        private var runtime: IJeiRuntime? = null
        private var helpers: IJeiHelpers? = null
        @JvmField
        var guiHelper: IGuiHelper? = null

        @JvmStatic
        fun showCategories(vararg categories: ResourceLocation?) {
            if (runtime != null) {
                val list: MutableList<RecipeType<*>?> = ArrayList()
                for (r in ImmutableList.copyOf<ResourceLocation>(categories)) {
                    val iRecipeRecipeType: RecipeType<IRecipe> = RECIPE_TYPES[r.toString()] ?: run {
                        GTLib.LOGGER.warn("No recipe type found for $r")
                        continue
                    }
                    list.add(iRecipeRecipeType)
                }
                runtime!!.recipesGui.showTypes(list)
            }
        }

        @JvmStatic
        fun <T> addModDescriptor(tooltip: MutableList<Component?>, t: T?) {
            val runtime = this.runtime
            if (t == null || helpers == null || runtime == null) return
            val text: String = helpers!!.modIdHelper.getFormattedModNameForModId(
                runtime.ingredientManager.getIngredientHelper(t as Any)
                    .getDisplayModId(t)
            )
            tooltip.add(literal(text))
        }
    }
}

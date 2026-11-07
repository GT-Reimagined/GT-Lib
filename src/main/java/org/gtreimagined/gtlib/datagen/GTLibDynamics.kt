package org.gtreimagined.gtlib.datagen

import com.google.common.collect.Sets
import com.google.gson.Gson
import com.google.gson.JsonSerializer
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.ccbluex.fastutil.invoke
import net.devtech.arrp.api.RuntimeResourcePack
import net.devtech.arrp.json.loot.JCondition
import net.devtech.arrp.json.models.JTextures
import net.devtech.arrp.util.UnsafeByteArrayOutputStream
import net.minecraft.advancements.Advancement
import net.minecraft.data.recipes.FinishedRecipe
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.packs.PackResources
import net.minecraft.world.Container
import net.minecraft.world.item.crafting.Recipe
import net.minecraft.world.item.crafting.RecipeManager
import net.minecraft.world.item.crafting.RecipeType
import net.minecraft.world.level.storage.loot.Deserializers
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.ModLoader
import net.minecraftforge.fml.loading.FMLEnvironment
import net.minecraftforge.fml.loading.FMLPaths
import org.gtreimagined.gtlib.GTAPI
import org.gtreimagined.gtlib.GTAPI.isModLoaded
import org.gtreimagined.gtlib.GTLib
import org.gtreimagined.gtlib.GTLibConfig
import org.gtreimagined.gtlib.Ref
import org.gtreimagined.gtlib.datagen.json.JGTLibModel
import org.gtreimagined.gtlib.datagen.providers.GTBlockLootProvider
import org.gtreimagined.gtlib.datagen.providers.GTLanguageProvider
import org.gtreimagined.gtlib.datagen.providers.GTRecipeProvider
import org.gtreimagined.gtlib.datagen.providers.GTTagProvider
import org.gtreimagined.gtlib.event.GTCraftingEvent
import org.gtreimagined.gtlib.event.GTLoaderEvent
import org.gtreimagined.gtlib.event.GTProvidersEvent
import org.gtreimagined.gtlib.event.GTWorldGenEvent
import org.gtreimagined.gtlib.integration.kubejs.KubeJSRegistrar
import org.gtreimagined.gtlib.recipe.IRecipe
import org.gtreimagined.gtlib.recipe.loader.IRecipeRegistrate
import org.gtreimagined.gtlib.recipe.map.IRecipeMap
import org.gtreimagined.gtlib.recipe.map.RecipeBuilder
import org.gtreimagined.gtlib.recipe.map.RecipeMap
import org.gtreimagined.gtlib.recipe.map.RecipeMap.Companion.recipeTypes
import org.gtreimagined.gtlib.registration.IGTRegistrar
import org.gtreimagined.gtlib.worldgen.bedrockore.BedrockVein
import org.gtreimagined.gtlib.worldgen.smallore.SmallOre
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayer
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerCollision
import org.gtreimagined.gtlib.worldgen.vanillaore.VanillaVein
import org.gtreimagined.gtlib.worldgen.vein.Vein
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.Any
import kotlin.Boolean
import kotlin.ByteArray
import kotlin.Int
import kotlin.Long
import kotlin.RuntimeException
import kotlin.plus

object GTLibDynamics {
    @JvmField
    val DYNAMIC_RESOURCE_PACK: RuntimeResourcePack = RuntimeResourcePack.create(ResourceLocation(Ref.ID, "dynamic"))
    @JvmField
    val RUNTIME_DATA_PACK: RuntimeResourcePack = RuntimeResourcePack.create(ResourceLocation(Ref.ID, "data"), 8)

    val GSON: Gson = Deserializers.createLootTableSerializer()
        .setPrettyPrinting()
        .disableHtmlEscaping()
        .registerTypeAdapter(
            Advancement.Builder::class.java,
            JsonSerializer { src: Advancement.Builder, _, _ -> src.serializeToJson() })
        .registerTypeAdapter(
            FinishedRecipe::class.java,
            JsonSerializer { src: FinishedRecipe, _, _ -> src.serializeRecipe() })
        .registerTypeAdapter(JGTLibModel::class.java, JGTLibModel.JGTLibModelSerializer())
        .registerTypeAdapter(JTextures::class.java, JTextures.Serializer())
        .registerTypeAdapter(JCondition::class.java, JCondition.Serializer())
        .create()
    private var initialized = false

    val RECIPE_IDS: MutableSet<ResourceLocation> = Sets.newHashSet()

    @JvmField
    val FINISHED_RECIPE_CONSUMER: Consumer<FinishedRecipe> = Consumer { f ->
        if (RECIPE_IDS.add(f.id)) {
            GTDynamicDataPack.addRecipe(f)
        } else {
            if (FMLEnvironment.production) {
                GTLib.LOGGER.warn("Recipe duplicated: ${f.id}")
            } else {
                GTLib.LOGGER.catching(RuntimeException("Recipe duplicated: ${f.id}"))
            }
        }
    }

    private val PROVIDERS: Object2ObjectOpenHashMap<String, MutableList<Supplier<IGTLibProvider>>> = Object2ObjectOpenHashMap()

    @JvmStatic
    fun addResourcePacks(function: Consumer<PackResources>) {
        function.accept(DYNAMIC_RESOURCE_PACK)
    }

    @JvmStatic
    fun setInitialized() {
        initialized = true
    }

    @JvmStatic
    fun addDataPacks(function: Consumer<PackResources>) {
        if (initialized) {
            onResourceReload(FMLEnvironment.dist.isDedicatedServer)
        }
        function.accept(RUNTIME_DATA_PACK)
        function.accept(GTDynamicDataPack("gtlib:recipes", GTAPI.all<IGTRegistrar>().stream()
                    .map { it.dataPackDomains }
                    .flatMap {  it.stream() }.collect(Collectors.toSet())))
    }

    /**
     * Providers and Dynamic Resource Pack Section
     */
    @JvmStatic
    fun clientProvider(domain: String, providerFunc: Supplier<IGTLibProvider>) {
        PROVIDERS.computeIfAbsent(domain){ ObjectArrayList() }.add(providerFunc)
    }

    @JvmStatic
    fun runDataProvidersDynamically() {
        GTBlockLootProvider.init()
        val ev = GTProvidersEvent(GTLib)
        ModLoader.get().postEvent(ev)
        val providers: MutableCollection<IGTLibProvider> = ev.providers
        val time: Long = System.currentTimeMillis()
        val async: Stream<IGTLibProvider> =
            providers.stream().filter { it.async() }.parallel()
        val sync: Stream<IGTLibProvider> = providers.stream().filter { !it.async() }
        Stream.concat(async, sync).forEach { it.run() }
        providers.forEach { it.onCompletion() }
        GTTagProvider.afterCompletion()
        GTBlockLootProvider.afterCompletion()
        GTLib.LOGGER.info("Time to run data providers: ${System.currentTimeMillis() - time} ms.")
        if (GTLibConfig.EXPORT_DEFAULT_DATA_AND_ASSETS.get()) {
            RUNTIME_DATA_PACK.dump(FMLPaths.CONFIGDIR.get().parent.resolve("dumped"))
        }
    }

    @JvmStatic
    fun runAssetProvidersDynamically(server: Boolean) {
        val providers: List<IGTLibProvider> = PROVIDERS.object2ObjectEntrySet().stream()
            .flatMap { v -> v.value.stream().map { it.get() } }.filter { !server || it is GTLanguageProvider }.toList()
        val time: Long = System.currentTimeMillis()
        val async: Stream<IGTLibProvider> =
            providers.stream().filter { it.async() }.parallel()
        val sync: Stream<IGTLibProvider> = providers.stream().filter { !it.async() }
        Stream.concat(async, sync).forEach { it.run() }
        providers.forEach { it.onCompletion() }
        GTLanguageProvider.postCompletion()
        GTLib.LOGGER.info("Time to run asset providers: ${System.currentTimeMillis() - time} ms.")
        if (GTLibConfig.EXPORT_DEFAULT_DATA_AND_ASSETS.get()) {
            DYNAMIC_RESOURCE_PACK.dump(FMLPaths.CONFIGDIR.get().parent.resolve("dumped"))
        }
    }

    /**
     * Collects all gt lib registered recipes, pushing them to @rec.
     *
     * @param rec consumer for IFinishedRecipe.
     */
    @JvmStatic
    fun collectRecipes(provider: GTRecipeProvider, rec: Consumer<FinishedRecipe>) {
        val event = GTCraftingEvent(GTLib)
        ModLoader.get().postEvent(event)
        for (loader in event.loaders) {
            loader.loadRecipes(rec, provider)
        }
    }

    @JvmStatic
    fun onRecipeManagerBuild(objectIn: Consumer<FinishedRecipe>) {
        GTLib.LOGGER.info("GTLib recipe manager running..")
        collectRecipes(GTRecipeProvider(Ref.ID, "provider"), objectIn)
        GTLib.LOGGER.info("GTLib recipe manager done..")
    }

    @JvmStatic
    fun onRecipeCompile(server: Boolean, manager: RecipeManager) {
        GTLib.LOGGER.info("Compiling GT recipes")
        var time: Long = System.nanoTime()

        for (m in GTAPI.all<RecipeMap<*>>()) {
            val proxy = m.proxy
            if (proxy != null) {
                val recipes: MutableList<Recipe<*>> = manager.getAllRecipesFor(m.proxy!!.loc as RecipeType<Recipe<Container>>) as MutableList<Recipe<*>>
                recipes.forEach { recipe ->
                    val r: IRecipe? = proxy.handler(recipe, m.RB())
                    if (r != null) m.compileRecipe(r)
                }
            }
        }

        for (recipeType: RecipeType<out IRecipe> in recipeTypes) {
            val recipes: MutableList<out IRecipe> = manager.getAllRecipesFor(recipeType)
            val map: MutableMap<String, MutableList<IRecipe>> = recipes.stream().collect(
                Collectors.groupingBy(
                    Function { it.getMapId() })
            )

            for ((key, value) in map) {
                val rmap: IRecipeMap? = GTAPI.get<IRecipeMap>(ResourceLocation(key))
                if (rmap != null) {
                    value.stream().forEach { recipe -> rmap.compileRecipe(recipe) }
                    //entry.getValue().forEach(rmap::add);
                    //rmap.compile(manager);
                }
            }
        }


        time = System.nanoTime() - time
        val size: Int = GTAPI.all<IRecipeMap>().stream()
            .mapToInt { it.getRecipes(false).size }.sum()

        GTLib.LOGGER.info("Time to compile GT recipes: (ms) ${(time) / (1000 * 1000)}")
        GTLib.LOGGER.info("No. of GT recipes: $size")
        GTLib.LOGGER.info("Average loading time / recipe: (µs) ${(if (size > 0) time / size else time) / 1000}")

        /*
         * GTAPI.all(RecipeMap.class, t -> {
         * GTLib.LOGGER.info("Recipe map " + t.getId() + " compiled " +
         * t.getRecipes(false).size() + " recipes."); });
         */
        // Invalidate old tag getter.
        // TagUtils.resetSupplier();
    }

    /**
     * Reloads dynamic assets during resource reload.
     */
    @JvmStatic
    fun onResourceReload(serverEvent: Boolean) {
        val provider = GTRecipeProvider(Ref.ID, "provider")
        GTDynamicDataPack.clearServer()
        RECIPE_IDS.clear()
        collectRecipes(provider, FINISHED_RECIPE_CONSUMER)
        GTAPI.all<RecipeMap<*>> { it.reset() }
        val filter: MutableSet<ResourceLocation>
        if (isModLoaded(Ref.MOD_KJS)) {
            filter = KubeJSRegistrar.getFilter(serverEvent)
        } else {
            filter = mutableSetOf()
        }
        val loaders: MutableMap<ResourceLocation, IRecipeRegistrate.IRecipeLoader> = Object2ObjectOpenHashMap(30)
        MinecraftForge.EVENT_BUS.post(
            GTLoaderEvent(GTLib) { a, b, c ->
                if (filter.contains(ResourceLocation(a, b))) return@GTLoaderEvent
                if (loaders.put(ResourceLocation(a, b), c) != null) {
                    GTLib.LOGGER.warn("Duplicate recipe loader: ${ResourceLocation(a, b)}")
                }
            }
        )
        val veins: MutableList<Vein> = ObjectArrayList()
        val stoneLayers: MutableList<StoneLayer> = ObjectArrayList()
        val smallOres: MutableList<SmallOre> = ObjectArrayList()
        val vanillaVeins: MutableList<VanillaVein> = ObjectArrayList()
        val bedrockVeins: MutableList<BedrockVein> = ObjectArrayList()
        val collisions: MutableList<StoneLayerCollision> = ObjectArrayList()
        var runRegular = true
        if (isModLoaded(Ref.MOD_KJS) && serverEvent) {
            runRegular = KubeJSRegistrar.postWorldgenEvent(veins, stoneLayers, collisions)
        }
        if (runRegular) {
            val ev = GTWorldGenEvent(GTLib)
            MinecraftForge.EVENT_BUS.post(ev)
            veins.addAll(ev.VEINS)
            smallOres.addAll(ev.SMALL_ORES)
            stoneLayers.addAll(ev.STONE_LAYERS)
            vanillaVeins.addAll(ev.VANILLA_ORES)
            bedrockVeins.addAll(ev.BEDROCK_VEINS)
            collisions.addAll(ev.COLLISIONS)
        }
        for (vein in veins) {
            GTDynamicDataPack.addWorldgenObject(vein)
        }
        for (stoneLayer in stoneLayers) {
            GTDynamicDataPack.addWorldgenObject(stoneLayer)
        }
        for (collision in collisions) {
            GTDynamicDataPack.addWorldgenObject(collision)
        }
        for (smallOre in smallOres) {
            GTDynamicDataPack.addWorldgenObject(smallOre)
        }
        for (vanillaVein in vanillaVeins) {
            GTDynamicDataPack.addWorldgenObject(vanillaVein)
        }
        for (vein in bedrockVeins) {
            GTDynamicDataPack.addWorldgenObject(vein)
        }
        loaders.forEach { (r, l) ->
            RecipeBuilder.setCurrentModId(r.namespace)
            l.init()
            RecipeBuilder.setCurrentModId(Ref.SHARED_ID)
        }
        GTLib.LOGGER.info(/* message = */ "Amount of GTLib Recipe Loaders registered: " + loaders.size)
    }

    @JvmStatic
    fun getTagLoc(identifier: String, tagId: ResourceLocation): ResourceLocation {
        return ResourceLocation(tagId.namespace, "$identifier/${tagId.path}")
    }

    @JvmStatic
    fun serialize(obj: Any): ByteArray {
        val ubaos = UnsafeByteArrayOutputStream()
        val writer = OutputStreamWriter(ubaos)
        GSON.toJson(obj, writer)
        try {
            writer.close()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return ubaos.bytes
    }

    @JvmStatic
    fun fix(identifier: ResourceLocation, prefix: String, append: String): ResourceLocation {
        return ResourceLocation(identifier.namespace, "$prefix/${identifier.path}.$append")
    } /*
     * public static void runBackgroundProviders() {
     * GTLib.LOGGER.info("Running DummyTagProviders...");
     * Ref.BACKGROUND_GEN.addProviders(DummyTagProviders.DUMMY_PROVIDERS); try {
     * Ref.BACKGROUND_GEN.run(); } catch (IOException e) { e.printStackTrace(); } }
     */
}

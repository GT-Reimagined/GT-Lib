package org.gtreimagined.gtlib

import brachy.modularui.factory.GuiManager
import com.terraformersmc.terraform.utils.TerraformFuelRegistry
import net.devtech.arrp.ARRP
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.ItemLike
import net.minecraft.world.level.biome.Biome
import net.minecraft.world.level.biome.Biomes
import net.minecraftforge.api.distmarker.Dist
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.AddReloadListenerEvent
import net.minecraftforge.fml.DistExecutor
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.gtreimagined.gtlib.client.GTLibModelManager
import org.gtreimagined.gtlib.common.event.ARRPEvents
import org.gtreimagined.gtlib.cover.ICover
import org.gtreimagined.gtlib.data.GTLibBlocks
import org.gtreimagined.gtlib.data.GTLibMaterials
import org.gtreimagined.gtlib.data.GTMaterialTypes
import org.gtreimagined.gtlib.data.GTTools
import org.gtreimagined.gtlib.data.VanillaStoneTypes
import org.gtreimagined.gtlib.datagen.GTLibDynamics
import org.gtreimagined.gtlib.datagen.GTLoot
import org.gtreimagined.gtlib.datagen.loaders.MaterialRecipes
import org.gtreimagined.gtlib.datagen.loaders.StoneRecipes
import org.gtreimagined.gtlib.datagen.providers.GTBlockLootProvider
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider
import org.gtreimagined.gtlib.datagen.providers.GTBlockTagProvider
import org.gtreimagined.gtlib.datagen.providers.GTFluidTagProvider
import org.gtreimagined.gtlib.datagen.providers.GTItemModelProvider
import org.gtreimagined.gtlib.datagen.providers.GTItemTagProvider
import org.gtreimagined.gtlib.datagen.providers.GTLanguageProvider
import org.gtreimagined.gtlib.datagen.providers.GTTagProvider
import org.gtreimagined.gtlib.event.GTCraftingEvent
import org.gtreimagined.gtlib.event.GTProvidersEvent
import org.gtreimagined.gtlib.fluid.GTFluid
import org.gtreimagined.gtlib.gui.SlotTypes
import org.gtreimagined.gtlib.gui.event.GuiEvents
import org.gtreimagined.gtlib.integration.Integrations
import org.gtreimagined.gtlib.integration.kubejs.KubeJSRegistrar
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibRecipeViewerPlugin
import org.gtreimagined.gtlib.item.interaction.CauldronInteractions
import org.gtreimagined.gtlib.machine.MachineState
import org.gtreimagined.gtlib.material.Material
import org.gtreimagined.gtlib.material.MaterialTags
import org.gtreimagined.gtlib.material.MaterialTypeBlock
import org.gtreimagined.gtlib.material.MaterialTypeItem
import org.gtreimagined.gtlib.material.SubTag
import org.gtreimagined.gtlib.mui.GTGuiThemes
import org.gtreimagined.gtlib.mui.factory.CoverUIFactory
import org.gtreimagined.gtlib.network.GTLibNetwork
import org.gtreimagined.gtlib.ore.BlockOre
import org.gtreimagined.gtlib.ore.StoneType
import org.gtreimagined.gtlib.proxy.ClientHandler
import org.gtreimagined.gtlib.proxy.CommonHandler
import org.gtreimagined.gtlib.proxy.IProxyHandler
import org.gtreimagined.gtlib.proxy.ServerHandler
import org.gtreimagined.gtlib.recipe.Recipe
import org.gtreimagined.gtlib.recipe.RecipeBuilders
import org.gtreimagined.gtlib.recipe.container.MirroredShapedRecipe
import org.gtreimagined.gtlib.recipe.ingredient.IngredientSerializer
import org.gtreimagined.gtlib.recipe.ingredient.PropertyIngredient
import org.gtreimagined.gtlib.recipe.material.MaterialSerializer
import org.gtreimagined.gtlib.recipe.serializer.MachineRecipeSerializer
import org.gtreimagined.gtlib.registration.RegistrationEvent
import org.gtreimagined.gtlib.tool.IGTTool
import org.gtreimagined.gtlib.util.TagUtils
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator
import org.gtreimagined.gtlib.worldgen.bedrockore.BedrockVeinData
import org.gtreimagined.gtlib.worldgen.smallore.SmallOreData
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerData
import org.gtreimagined.gtlib.worldgen.vanillaore.VanillaVeinData
import org.gtreimagined.gtlib.worldgen.vein.VeinData
import thedarkcolour.kotlinforforge.forge.MOD_BUS
import java.awt.image.BufferedImage
import java.io.IOException
import java.util.function.Supplier
import java.util.stream.Collectors
import javax.imageio.ImageIO

@Mod(Ref.ID)
object GTLib : GTMod() {
    @JvmField
    val LOGGER: Logger = LogManager.getLogger(Ref.ID)
    @JvmField
    var PROXY: IProxyHandler


    init {
        LOGGER.info("Loading GTLib")
        PROXY = DistExecutor.unsafeRunForDist({ Supplier { ClientHandler() } }, { Supplier { ServerHandler() } })
        // change in new Forge
        if (GTAPI.isModLoaded(Ref.MOD_KJS)) {
            KubeJSRegistrar()
        }
        GTLibDynamics.clientProvider(Ref.ID) { GTBlockStateProvider(Ref.ID, Ref.NAME + " BlockStates") }
        GTLibDynamics.clientProvider(Ref.ID) { GTItemModelProvider(Ref.ID, Ref.NAME + " Item Models") }
        GTLibDynamics.clientProvider(Ref.SHARED_ID) { GTBlockStateProvider(Ref.SHARED_ID, "GT Shared BlockStates") }
        GTLibDynamics.clientProvider(Ref.SHARED_ID) { GTItemModelProvider(Ref.SHARED_ID, "GT Shared Item Models") }
        GTLibDynamics.clientProvider(Ref.ID) { GTLanguageProvider(Ref.ID, Ref.NAME + " en_us Localization", "en_us") }
        GTLibDynamics.clientProvider(Ref.SHARED_ID) {
            GTLanguageProvider(Ref.SHARED_ID, Ref.NAME + " en_us Localization (Shared)", "en_us") }
        GTAPI.init()
        GTCreativeTabs.init()
        GTLibNetwork.register()
        GTLibConfig.createConfig()
        GuiManager.registerFactory(CoverUIFactory.INSTANCE)
        /* Lifecycle events */
        val eventBus = MOD_BUS
        eventBus.addListener(this::modConstructionEvent)
        eventBus.addListener(this::clientSetup)
        eventBus.addListener(this::commonSetup)
        eventBus.addListener(this::serverSetup)
        eventBus.addListener(this::loadComplete)

        eventBus.addListener(this::addCraftingLoaders)
        eventBus.addListener(this::providers)
        MinecraftForge.EVENT_BUS.addListener(this::onServerReloadListeners)
        ARRP.EVENT_BUS.register(ARRPEvents::class.java)
    }

    fun onServerReloadListeners(event: AddReloadListenerEvent) {
        event.addListener(VanillaVeinData.INSTANCE)
        event.addListener(VeinData.INSTANCE)
        event.addListener(SmallOreData.INSTANCE)
        event.addListener(BedrockVeinData.INSTANCE)
        event.addListener(StoneLayerData.INSTANCE)
    }

    fun addCraftingLoaders(ev: GTCraftingEvent) {
        ev.addLoader(StoneRecipes::loadRecipes)
        ev.addLoader(MaterialRecipes::init)
    }

    fun providers(ev: GTProvidersEvent) {
        var provider : GTBlockTagProvider? = null;
        ev.addProvider {
            provider = GTBlockTagProvider(Ref.ID, Ref.NAME + " Block Tags", false)
            provider
        }
        ev.addProvider {
            GTFluidTagProvider(
                Ref.SHARED_ID,
                "GT Shared Fluid Tags", false
            )
        }
        ev.addProvider {
            GTItemTagProvider(Ref.ID, Ref.NAME + " Item Tags", false, provider)
        }
        ev.addProvider { GTBlockLootProvider(Ref.ID, Ref.NAME + " Loot generator") }
        ev.addProvider {
            object : GTTagProvider<Biome>(Registries.BIOME, Ref.ID, Ref.NAME + " Biome Tags", "worldgen/biome", null) {
                override fun processTags(domain: String?) {
                    this.tag(TagUtils.getBiomeTag(ResourceLocation("is_desert"))).add(Biomes.DESERT)
                    this.tag(TagUtils.getBiomeTag(ResourceLocation("is_plains"))).add(Biomes.PLAINS)
                    this.tag(TagUtils.getBiomeTag(ResourceLocation("is_savanna")))
                        .add(Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA)
                    this.tag(TagUtils.getBiomeTag(ResourceLocation("is_swamp"))).add(Biomes.SWAMP)
                }
            }
        }
        if (GTAPI.isModLoaded(Ref.MOD_KJS)) {
            KubeJSRegistrar.providerEvent(ev)
        }
    }

    override fun onRegistrationEvent(event: RegistrationEvent, side: Dist) {
        if (event == RegistrationEvent.DATA_INIT) {
            Recipe.init()
            GTLoot.RandomWeightLootFunction.init()
            SlotTypes.init()
            RecipeBuilders.init()
            MachineState.init()
            GTLibMaterials.init()
            GTMaterialTypes.init()
            GTTools.init(side)
            VanillaStoneTypes.init()
            GTLibBlocks.init()
            Data.init(side)
            ICover.init()
            SubTag.init()
            GTLibWorldGenerator.preinit()
            GuiEvents.init()
            MaterialSerializer.init()
            MirroredShapedRecipe.init()
            MachineRecipeSerializer.init()
            IngredientSerializer.init()
            PropertyIngredient.Serializer.init()
        } else if (event == RegistrationEvent.WORLDGEN_INIT) {
            GTLibWorldGenerator.init()
            GTTools.postInit()
        } else if (event == RegistrationEvent.DATA_READY) {
            CauldronInteractions.init()
            if (GTAPI.isModLoaded(Ref.MOD_JEI) || GTAPI.isModLoaded(Ref.MOD_REI)) {
                GTLibRecipeViewerPlugin.registerMissingMaps()
            }
            GTLibRecipeViewerPlugin.addItemsToHide { l: MutableList<ItemLike> ->
                if (!GTLibConfig.SHOW_ALL_ORES.get()) {
                    GTAPI.all(StoneType::class.java) { s ->
                        if (s !== VanillaStoneTypes.STONE && s !== VanillaStoneTypes.SAND && s.doesGenerateOre()) {
                            GTMaterialTypes.ORE.all().forEach { m ->
                                val ore = GTMaterialTypes.ORE.get().get(m, s).asBlock()
                                if (ore is BlockOre) {
                                    l.add(ore)
                                }
                            }
                            GTMaterialTypes.SMALL_ORE.all().forEach { m ->
                                val ore = GTMaterialTypes.SMALL_ORE.get().get(m, s).asBlock()
                                if (ore is BlockOre) {
                                    l.add(ore)
                                }
                            }
                        }
                    }
                }
                GTAPI.all(MaterialTypeItem::class.java) { t ->
                    if (!t.hidden()) return@all
                    val stacks = t.all().stream().map { m -> t.get(m) }
                        .collect(Collectors.toList())
                    if (stacks.isEmpty()) return@all
                    l.addAll(stacks)
                }
                GTAPI.all(IGTTool::class.java).stream()
                    .filter { t -> t.getGTToolType() === GTTools.WRENCH_ALT }
                    .forEach { tool -> l.add(tool.item) }
                GTAPI.all(GTFluid::class.java)
                    .forEach { t -> l.add(t.getFluidBlock()) }
            }
            GTAPI.all(Material::class.java).forEach { m ->
                val map = MaterialTags.FURNACE_FUELS.getMap(m)
                map?.forEach { (t, i) ->
                    if (t is MaterialTypeItem<*>) {
                        TerraformFuelRegistry.addFuel(t.get(m), i)
                    } else if (t is MaterialTypeBlock<*>) {
                        val getter = t.get()
                        if (getter is MaterialTypeBlock.IBlockGetter) {
                            TerraformFuelRegistry.addFuel(getter.get(m).asItem(), i)
                        }
                    }
                }
            }
        } else if (event == RegistrationEvent.CLIENT_DATA_INIT) {
            GTLibModelManager.init()
        }
    }

    @JvmStatic
    fun readImage(imagePath: String): BufferedImage? {
        val inputStream = GTLib::class.java.getResourceAsStream("/assets/${Ref.ID}/textures/$imagePath.png")
        val result: Result<BufferedImage?> = runCatching {
            ImageIO.read(inputStream)
        }
        return result.fold({it}){
            when(it){
                is IOException -> LOGGER.error("Unexpected IO Exception Occurred", it)
                is Exception -> LOGGER.error("Unexpected Exception Occurred", it)
            }
            null
        }
    }

    override fun getId(): String {
        return Ref.ID
    }


    private fun modConstructionEvent(event: FMLConstructModEvent) {
        GTGuiThemes.registerThemes()
    }

    private fun clientSetup(e: FMLClientSetupEvent) {
        ClientHandler.setup()
        GTAPI.onRegistration(RegistrationEvent.DATA_READY)
        GTLibDynamics.runDataProvidersDynamically()
        e.enqueueWork {
            GTAPI.getClientDeferredQueue().ifPresent { t ->
                for (r in t) {
                    try {
                        r.run()
                    } catch (ex: Exception) {
                        LOGGER.warn("Caught error during client setup: ${ex.message}")
                    }
                }
            }
        }
    }

    private fun commonSetup(e: FMLCommonSetupEvent) {
        CommonHandler.setup()
        GTLibDynamics.setInitialized()
        LOGGER.info("GTLib Data Processing has Finished. All Data Objects can now be Modified!")
        e.enqueueWork {
            GTAPI.getCommonDeferredQueue().ifPresent { t ->
                for (r in t) {
                    try {
                        r.run()
                    } catch (ex: Exception) {
                        LOGGER.warn("Caught error during common setup: ${ex.message}")
                    }
                }
            }
        }

        val modEventBus = MOD_BUS
        modEventBus.addListener(Integrations::enqueueIMC)
    }

    private fun serverSetup(e: FMLDedicatedServerSetupEvent) {
        ServerHandler.setup()
        GTAPI.onRegistration(RegistrationEvent.DATA_READY)
        GTLibDynamics.runDataProvidersDynamically()
        e.enqueueWork {
            GTAPI.getServerDeferredQueue().ifPresent { t ->
                for (r in t) {
                    try {
                        r.run()
                    } catch (ex: Exception) {
                        LOGGER.warn("Caught error during server setup: ${ex.message}")
                    }
                }
            }
        }
    }

    private fun loadComplete(event: FMLLoadCompleteEvent) {
    }
}

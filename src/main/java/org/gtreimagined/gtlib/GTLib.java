package org.gtreimagined.gtlib;

import brachy.modularui.factory.GuiManager;
import com.terraformersmc.terraform.utils.TerraformFuelRegistry;
import net.minecraft.core.registries.Registries;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.gtreimagined.gtlib.block.BlockDimensionMarker;
import org.gtreimagined.gtlib.client.GTLibModelManager;
import org.gtreimagined.gtlib.common.event.ARRPEvents;
import org.gtreimagined.gtlib.cover.ICover;
import org.gtreimagined.gtlib.data.GTTools;
import org.gtreimagined.gtlib.data.GTMaterialTypes;
import org.gtreimagined.gtlib.data.GTLibMaterials;
import org.gtreimagined.gtlib.data.VanillaStoneTypes;
import org.gtreimagined.gtlib.data.GTLibBlocks;
import org.gtreimagined.gtlib.datagen.GTLibDynamics;
import org.gtreimagined.gtlib.datagen.GTLoot;
import org.gtreimagined.gtlib.datagen.loaders.MaterialRecipes;
import org.gtreimagined.gtlib.datagen.loaders.StoneRecipes;
import org.gtreimagined.gtlib.datagen.providers.GTBlockLootProvider;
import org.gtreimagined.gtlib.datagen.providers.GTBlockStateProvider;
import org.gtreimagined.gtlib.datagen.providers.GTBlockTagProvider;
import org.gtreimagined.gtlib.datagen.providers.GTFluidTagProvider;
import org.gtreimagined.gtlib.datagen.providers.GTItemModelProvider;
import org.gtreimagined.gtlib.datagen.providers.GTItemTagProvider;
import org.gtreimagined.gtlib.datagen.providers.GTLanguageProvider;
import org.gtreimagined.gtlib.datagen.providers.GTTagProvider;
import org.gtreimagined.gtlib.event.GTCraftingEvent;
import org.gtreimagined.gtlib.event.GTProvidersEvent;
import org.gtreimagined.gtlib.fluid.GTFluid;
import org.gtreimagined.gtlib.gui.SlotType;
import org.gtreimagined.gtlib.gui.event.GuiEvents;
import org.gtreimagined.gtlib.integration.Integrations;
import org.gtreimagined.gtlib.integration.recipeviewer.GTLibXEIPlugin;
import org.gtreimagined.gtlib.integration.kubejs.KubeJSRegistrar;
import org.gtreimagined.gtlib.item.interaction.CauldronInteractions;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.material.MaterialTypeBlock;
import org.gtreimagined.gtlib.material.MaterialTypeItem;
import org.gtreimagined.gtlib.material.SubTag;
import org.gtreimagined.gtlib.mui.GTGuiThemes;
import org.gtreimagined.gtlib.mui.factory.CoverUIFactory;
import org.gtreimagined.gtlib.network.GTLibNetwork;
import org.gtreimagined.gtlib.ore.BlockOre;
import org.gtreimagined.gtlib.ore.StoneType;
import org.gtreimagined.gtlib.proxy.ClientHandler;
import org.gtreimagined.gtlib.proxy.CommonHandler;
import org.gtreimagined.gtlib.proxy.IProxyHandler;
import org.gtreimagined.gtlib.proxy.ServerHandler;
import org.gtreimagined.gtlib.recipe.Recipe;
import org.gtreimagined.gtlib.recipe.RecipeBuilders;
import org.gtreimagined.gtlib.recipe.container.MirroredShapedRecipe;
import org.gtreimagined.gtlib.recipe.ingredient.IngredientSerializer;
import org.gtreimagined.gtlib.recipe.ingredient.PropertyIngredient;
import org.gtreimagined.gtlib.recipe.material.MaterialSerializer;
import org.gtreimagined.gtlib.recipe.serializer.MachineRecipeSerializer;
import org.gtreimagined.gtlib.registration.RegistrationEvent;
import org.gtreimagined.gtlib.tool.IGTTool;
import org.gtreimagined.gtlib.util.TagUtils;
import org.gtreimagined.gtlib.worldgen.GTLibWorldGenerator;
import net.devtech.arrp.ARRP;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.gtreimagined.gtlib.worldgen.bedrockore.BedrockVeinData;
import org.gtreimagined.gtlib.worldgen.smallore.SmallOreData;
import org.gtreimagined.gtlib.worldgen.stonelayer.StoneLayerData;
import org.gtreimagined.gtlib.worldgen.vanillaore.VanillaVeinData;
import org.gtreimagined.gtlib.worldgen.vein.VeinData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mod(Ref.ID)
public class GTLib extends GTMod {

    public static GTLib INSTANCE;
    public static final Logger LOGGER = LogManager.getLogger(Ref.ID);
    public static IProxyHandler PROXY;

    static {
        // GTAPI.runBackgroundProviders();
    }

    public GTLib() {
        super();

        LOGGER.info("Loading GTLib");
        INSTANCE = this;
        PROXY = DistExecutor.unsafeRunForDist(() -> ClientHandler::new, () -> ServerHandler::new);
        // change in new Forge
        if (GTAPI.isModLoaded(Ref.MOD_KJS)){
            new KubeJSRegistrar();
        }
        GTLibDynamics.clientProvider(Ref.ID,
                () -> new GTBlockStateProvider(Ref.ID, Ref.NAME.concat(" BlockStates")));
        GTLibDynamics.clientProvider(Ref.ID,
                () -> new GTItemModelProvider(Ref.ID, Ref.NAME.concat(" Item Models")));
        GTLibDynamics.clientProvider(Ref.SHARED_ID,
                () -> new GTBlockStateProvider(Ref.SHARED_ID, "GT Shared BlockStates"));
        GTLibDynamics.clientProvider(Ref.SHARED_ID,
                () -> new GTItemModelProvider(Ref.SHARED_ID, "GT Shared Item Models"));
        GTLibDynamics.clientProvider(Ref.ID,
                () -> new GTLanguageProvider(Ref.ID, Ref.NAME.concat(" en_us Localization"), "en_us"));
        GTLibDynamics.clientProvider(Ref.SHARED_ID,
                () -> new GTLanguageProvider(Ref.SHARED_ID, Ref.NAME.concat(" en_us Localization (Shared)"), "en_us"));
        GTAPI.init();
        GTCreativeTabs.init();
        GTLibNetwork.register();
        GTLibConfig.createConfig();
        GuiManager.registerFactory(CoverUIFactory.INSTANCE);
        /* Lifecycle events */
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::modConstructionEvent);
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::serverSetup);
        eventBus.addListener(this::loadComplete);

        eventBus.addListener(this::addCraftingLoaders);
        eventBus.addListener(this::providers);
        MinecraftForge.EVENT_BUS.addListener(this::onServerReloadListeners);
        ARRP.EVENT_BUS.register(ARRPEvents.class);
    }

    public void onServerReloadListeners(AddReloadListenerEvent event){
        event.addListener(VanillaVeinData.INSTANCE);
        event.addListener(VeinData.INSTANCE);
        event.addListener(SmallOreData.INSTANCE);
        event.addListener(BedrockVeinData.INSTANCE);
        event.addListener(StoneLayerData.INSTANCE);
    }

    public void addCraftingLoaders(GTCraftingEvent ev) {
        ev.addLoader(StoneRecipes::loadRecipes);
        ev.addLoader(MaterialRecipes::init);
    }

    public void providers(GTProvidersEvent ev) {
        final GTBlockTagProvider[] p = new GTBlockTagProvider[1];
        ev.addProvider(() -> {
            p[0] = new GTBlockTagProvider(Ref.ID, Ref.NAME.concat(" Block Tags"), false);
            return p[0];
        });
        ev.addProvider(() -> new GTFluidTagProvider(Ref.SHARED_ID,
                "GT Shared Fluid Tags", false));
        ev.addProvider(() -> new GTItemTagProvider(Ref.ID, Ref.NAME.concat(" Item Tags"),
                false, p[0]));
        ev.addProvider(() -> new GTBlockLootProvider(Ref.ID, Ref.NAME.concat(" Loot generator")));
        ev.addProvider(() -> new GTTagProvider<Biome>(Registries.BIOME, Ref.ID, Ref.NAME.concat(" Biome Tags"), "worldgen/biome", null) {
            @Override
            protected void processTags(String domain) {
                this.tag(TagUtils.getBiomeTag(new ResourceLocation("is_desert"))).add(Biomes.DESERT);
                this.tag(TagUtils.getBiomeTag(new ResourceLocation("is_plains"))).add(Biomes.PLAINS);
                this.tag(TagUtils.getBiomeTag(new ResourceLocation("is_savanna"))).add(Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA);
                this.tag(TagUtils.getBiomeTag(new ResourceLocation("is_swamp"))).add(Biomes.SWAMP);
            }
        });
        if (GTAPI.isModLoaded(Ref.MOD_KJS)) {
            KubeJSRegistrar.providerEvent(ev);
        }
    }

    @Override
    public void onRegistrationEvent(RegistrationEvent event, Dist side) {
        if (event == RegistrationEvent.DATA_INIT) {
            Recipe.init();
            GTLoot.RandomWeightLootFunction.init();
            SlotType.init();
            RecipeBuilders.init();
            MachineState.init();
            GTLibMaterials.init();
            GTMaterialTypes.init();
            GTTools.init(side);
            VanillaStoneTypes.init();
            GTLibBlocks.init();
            Data.init(side);
            ICover.init();
            SubTag.init();
            GTLibWorldGenerator.preinit();
            GuiEvents.init();
            MaterialSerializer.init();
            MirroredShapedRecipe.init();
            MachineRecipeSerializer.init();
            IngredientSerializer.init();
            PropertyIngredient.Serializer.init();
        } else if (event == RegistrationEvent.WORLDGEN_INIT) {
            GTLibWorldGenerator.init();
            GTTools.postInit();
        } else if (event == RegistrationEvent.DATA_READY) {
            CauldronInteractions.init();
            if (GTAPI.isModLoaded(Ref.MOD_JEI) || GTAPI.isModLoaded(Ref.MOD_REI)){
                GTLibXEIPlugin.registerMissingMaps();
            }
            GTLibXEIPlugin.addItemsToHide(l -> {
                if (!GTLibConfig.SHOW_ALL_ORES.get()){
                    GTAPI.all(StoneType.class, s -> {
                        if (s != VanillaStoneTypes.STONE && s != VanillaStoneTypes.SAND && s.doesGenerateOre()){
                            GTMaterialTypes.ORE.all().forEach(m -> {
                                Block ore = GTMaterialTypes.ORE.get().get(m, s).asBlock();
                                if (ore instanceof BlockOre){
                                    l.add(ore);
                                }
                            });
                            GTMaterialTypes.SMALL_ORE.all().forEach(m -> {
                                Block ore = GTMaterialTypes.SMALL_ORE.get().get(m, s).asBlock();
                                if (ore instanceof BlockOre){
                                    l.add(ore);
                                }
                            });
                        }
                    });

                }
                GTAPI.all(MaterialTypeItem.class, t -> {
                    if (!t.hidden()) return;
                    List<ItemLike> stacks = (List<ItemLike>) t.all().stream().map(obj -> t.get((Material)obj)).collect(Collectors.toList());
                    if (stacks.isEmpty()) return;
                    l.addAll(stacks);
                });
                GTAPI.all(IGTTool.class).stream().filter(t -> t.getGTToolType() == GTTools.WRENCH_ALT).forEach(tool -> l.add(tool.getItem()));
                GTAPI.all(GTFluid.class).forEach(t -> l.add(t.getFluidBlock()));
                GTAPI.all(BlockDimensionMarker.class).forEach(b -> l.add(b.asItem()));
            });
            GTAPI.all(Material.class).forEach(m -> {
                Map<MaterialType<?>, Integer> map = MaterialTags.FURNACE_FUELS.getMap(m);
                if (map != null){
                    map.forEach((t, i) -> {
                        if (t instanceof MaterialTypeItem<?> typeItem){
                            TerraformFuelRegistry.addFuel(typeItem.get(m), i);
                        } else if (t instanceof MaterialTypeBlock<?> typeBlock && typeBlock.get() instanceof MaterialTypeBlock.IBlockGetter blockGetter){
                            TerraformFuelRegistry.addFuel(blockGetter.get(m).asItem(), i);
                        }
                    });
                }

            });
        } else if (event == RegistrationEvent.CLIENT_DATA_INIT){
            GTLibModelManager.init();
        }
    }

    public static BufferedImage readImage(String imagePath) throws IOException {
        InputStream in = GTLib.class.getResourceAsStream("/assets/" + Ref.ID + "/textures/" + imagePath + ".png");
        return ImageIO.read(in);
    }

    @Override
    public String getId() {
        return Ref.ID;
    }


    private void modConstructionEvent(FMLConstructModEvent event){
        GTGuiThemes.registerThemes();
    }

    private void clientSetup(final FMLClientSetupEvent e) {
        ClientHandler.setup();
        GTAPI.onRegistration(RegistrationEvent.DATA_READY);
        GTLibDynamics.runDataProvidersDynamically();
        e.enqueueWork(() -> GTAPI.getClientDeferredQueue().ifPresent(t -> {
            for (Runnable r : t) {
                try {
                    r.run();
                } catch (Exception ex) {
                    LOGGER.warn("Caught error during client setup: " + ex.getMessage());
                }
            }
        }));
    }

    private void commonSetup(final FMLCommonSetupEvent e) {
        CommonHandler.setup();
        GTLibDynamics.setInitialized();
        LOGGER.info("GTLib Data Processing has Finished. All Data Objects can now be Modified!");
        e.enqueueWork(() -> GTAPI.getCommonDeferredQueue().ifPresent(t -> {
            for (Runnable r : t) {
                try {
                    r.run();
                } catch (Exception ex) {
                    LOGGER.warn("Caught error during common setup: " + ex.getMessage());
                }
            }
        }));

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(Integrations::enqueueIMC);
    }

    private void serverSetup(final FMLDedicatedServerSetupEvent e) {
        ServerHandler.setup();
        GTAPI.onRegistration(RegistrationEvent.DATA_READY);
        GTLibDynamics.runDataProvidersDynamically();
        e.enqueueWork(() -> GTAPI.getServerDeferredQueue().ifPresent(t -> {
            for (Runnable r : t) {
                try {
                    r.run();
                } catch (Exception ex) {
                    LOGGER.warn("Caught error during server setup: " + ex.getMessage());
                }
            }
        }));
    }

    private void loadComplete(FMLLoadCompleteEvent event) {
    }
}

package org.gtreimagined.gtlib;

import com.terraformersmc.terraform.utils.TerraformFuelRegistry;
import org.gtreimagined.gtlib.block.BlockDimensionMarker;
import org.gtreimagined.gtlib.client.GTLibModelManager;
import org.gtreimagined.gtlib.client.ClientData;
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
import org.gtreimagined.gtlib.integration.jeirei.AntimatterJEIREIPlugin;
import org.gtreimagined.gtlib.integration.kubejs.KubeJSRegistrar;
import org.gtreimagined.gtlib.item.interaction.CauldronInteractions;
import org.gtreimagined.gtlib.machine.MachineState;
import org.gtreimagined.gtlib.material.Material;
import org.gtreimagined.gtlib.material.MaterialTags;
import org.gtreimagined.gtlib.material.MaterialType;
import org.gtreimagined.gtlib.material.MaterialTypeBlock;
import org.gtreimagined.gtlib.material.MaterialTypeItem;
import org.gtreimagined.gtlib.material.SubTag;
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
import org.gtreimagined.gtlib.worldgen.AntimatterWorldGenerator;
import net.devtech.arrp.ARRP;
import net.minecraft.data.BuiltinRegistries;
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

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mod(Ref.ID)
public class Antimatter extends AntimatterMod {

    public static Antimatter INSTANCE;
    public static final Logger LOGGER = LogManager.getLogger(Ref.ID);
    public static IProxyHandler PROXY;

    static {
        // AntimatterAPI.runBackgroundProviders();
    }

    public Antimatter() {
        super();

        LOGGER.info("Loading Antimatter");
        INSTANCE = this;
        PROXY = DistExecutor.unsafeRunForDist(() -> ClientHandler::new, () -> ServerHandler::new);
        // change in new Forge
        if (AntimatterAPI.isModLoaded(Ref.MOD_KJS)){
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
        AntimatterAPI.init();
        GTLibNetwork.register();
        AntimatterConfig.createConfig();
        /* Lifecycle events */
        IEventBus eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::clientSetup);
        eventBus.addListener(this::commonSetup);
        eventBus.addListener(this::serverSetup);
        eventBus.addListener(this::loadComplete);

        eventBus.addListener(this::addCraftingLoaders);
        eventBus.addListener(this::providers);
        ARRP.EVENT_BUS.register(ARRPEvents.class);
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
        ev.addProvider(() -> new GTTagProvider<Biome>(BuiltinRegistries.BIOME, Ref.ID, Ref.NAME.concat(" Biome Tags"), "worldgen/biome") {
            @Override
            protected void processTags(String domain) {
                this.tag(TagUtils.getBiomeTag(new ResourceLocation("is_desert"))).add(Biomes.DESERT);
                this.tag(TagUtils.getBiomeTag(new ResourceLocation("is_plains"))).add(Biomes.PLAINS);
                this.tag(TagUtils.getBiomeTag(new ResourceLocation("is_savanna"))).add(Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.WINDSWEPT_SAVANNA);
                this.tag(TagUtils.getBiomeTag(new ResourceLocation("is_swamp"))).add(Biomes.SWAMP);
            }
        });
        KubeJSRegistrar.providerEvent(ev);
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
            AntimatterWorldGenerator.preinit();
            GuiEvents.init();
            MaterialSerializer.init();
            MirroredShapedRecipe.init();
            MachineRecipeSerializer.init();
            IngredientSerializer.init();
            PropertyIngredient.Serializer.init();
        } else if (event == RegistrationEvent.WORLDGEN_INIT) {
            AntimatterWorldGenerator.init();
            GTTools.postInit();
        } else if (event == RegistrationEvent.DATA_READY) {
            CauldronInteractions.init();
            if (AntimatterAPI.isModLoaded(Ref.MOD_JEI) || AntimatterAPI.isModLoaded(Ref.MOD_REI)){
                AntimatterJEIREIPlugin.registerMissingMaps();
            }
            AntimatterJEIREIPlugin.addItemsToHide(l -> {
                if (!AntimatterConfig.SHOW_ALL_ORES.get()){
                    AntimatterAPI.all(StoneType.class, s -> {
                        if (s != VanillaStoneTypes.STONE && s != VanillaStoneTypes.SAND && s.doesGenerateOre()){
                            GTMaterialTypes.ORE.all().forEach(m -> {
                                Block ore = GTMaterialTypes.ORE.get().get(m, s).asBlock();
                                if (ore instanceof BlockOre){
                                    l.add(ore);
                                }
                            });
                            GTMaterialTypes.ORE_SMALL.all().forEach(m -> {
                                Block ore = GTMaterialTypes.ORE_SMALL.get().get(m, s).asBlock();
                                if (ore instanceof BlockOre){
                                    l.add(ore);
                                }
                            });
                        }
                    });

                }
                if (!AntimatterConfig.SHOW_ROCKS.get()){
                    GTMaterialTypes.BEARING_ROCK.all().forEach(m -> {
                        AntimatterAPI.all(StoneType.class, s -> {
                            if (s.doesGenerateOre()) {
                                l.add(GTMaterialTypes.BEARING_ROCK.get().get(m, s).asBlock());
                            }
                        });
                    });
                }
                AntimatterAPI.all(MaterialTypeItem.class, t -> {
                    if (!t.hidden()) return;
                    List<ItemLike> stacks = (List<ItemLike>) t.all().stream().map(obj -> t.get((Material)obj)).collect(Collectors.toList());
                    if (stacks.isEmpty()) return;
                    l.addAll(stacks);
                });
                AntimatterAPI.all(IGTTool.class).stream().filter(t -> t.getAntimatterToolType() == GTTools.WRENCH_ALT).forEach(tool -> l.add(tool.getItem()));
                AntimatterAPI.all(GTFluid.class).forEach(t -> l.add(t.getFluidBlock()));
                AntimatterAPI.all(BlockDimensionMarker.class).forEach(b -> l.add(b.asItem()));
            });
            AntimatterAPI.all(Material.class).forEach(m -> {
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
            ClientData.init();
            if (AntimatterConfig.OVERRIDE_BASALT_TEXTURE.get()){
                try {
                    GTLibDynamics.DYNAMIC_RESOURCE_PACK.addTexture(new ResourceLocation("block/basalt_top"), readImage("block/stone/basalt/stone"));
                    GTLibDynamics.DYNAMIC_RESOURCE_PACK.addTexture(new ResourceLocation("block/basalt_side"), readImage("block/stone/basalt/stone"));
                    GTLibDynamics.DYNAMIC_RESOURCE_PACK.addTexture(new ResourceLocation("block/smooth_basalt"), readImage("block/stone/basalt/smooth"));
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private static BufferedImage readImage(String imagePath) throws IOException {
        InputStream in = Antimatter.class.getResourceAsStream("/assets/" + Ref.ID + "/textures/" + imagePath + ".png");
        return ImageIO.read(in);
    }

    @Override
    public String getId() {
        return Ref.ID;
    }


    private void clientSetup(final FMLClientSetupEvent e) {
        ClientHandler.setup();
        AntimatterAPI.onRegistration(RegistrationEvent.DATA_READY);
        GTLibDynamics.runDataProvidersDynamically();
        e.enqueueWork(() -> AntimatterAPI.getClientDeferredQueue().ifPresent(t -> {
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
        e.enqueueWork(() -> AntimatterAPI.getCommonDeferredQueue().ifPresent(t -> {
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
        AntimatterAPI.onRegistration(RegistrationEvent.DATA_READY);
        GTLibDynamics.runDataProvidersDynamically();
        e.enqueueWork(() -> AntimatterAPI.getServerDeferredQueue().ifPresent(t -> {
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

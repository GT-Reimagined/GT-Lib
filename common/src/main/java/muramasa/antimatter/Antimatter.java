package muramasa.antimatter;

import com.terraformersmc.terraform.utils.TerraformFuelRegistry;
import muramasa.antimatter.client.AntimatterModelManager;
import muramasa.antimatter.client.ClientData;
import muramasa.antimatter.common.event.ARRPEvents;
import muramasa.antimatter.cover.ICover;
import muramasa.antimatter.data.AntimatterDefaultTools;
import muramasa.antimatter.data.AntimatterMaterialTypes;
import muramasa.antimatter.data.AntimatterMaterials;
import muramasa.antimatter.data.AntimatterStoneTypes;
import muramasa.antimatter.datagen.AntimatterDynamics;
import muramasa.antimatter.datagen.AntimatterLoot;
import muramasa.antimatter.datagen.loaders.MaterialRecipes;
import muramasa.antimatter.datagen.loaders.StoneRecipes;
import muramasa.antimatter.datagen.providers.AntimatterBlockLootProvider;
import muramasa.antimatter.datagen.providers.AntimatterBlockStateProvider;
import muramasa.antimatter.datagen.providers.AntimatterBlockTagProvider;
import muramasa.antimatter.datagen.providers.AntimatterFluidTagProvider;
import muramasa.antimatter.datagen.providers.AntimatterItemModelProvider;
import muramasa.antimatter.datagen.providers.AntimatterItemTagProvider;
import muramasa.antimatter.datagen.providers.AntimatterLanguageProvider;
import muramasa.antimatter.datagen.providers.AntimatterTagProvider;
import muramasa.antimatter.event.AntimatterCraftingEvent;
import muramasa.antimatter.event.AntimatterProvidersEvent;
import muramasa.antimatter.fluid.AntimatterFluid;
import muramasa.antimatter.gui.SlotType;
import muramasa.antimatter.gui.event.GuiEvents;
import muramasa.antimatter.integration.Integrations;
import muramasa.antimatter.integration.jeirei.AntimatterJEIREIPlugin;
import muramasa.antimatter.integration.kubejs.KubeJSRegistrar;
import muramasa.antimatter.item.interaction.CauldronInteractions;
import muramasa.antimatter.machine.MachineState;
import muramasa.antimatter.material.Material;
import muramasa.antimatter.material.MaterialTags;
import muramasa.antimatter.material.MaterialType;
import muramasa.antimatter.material.MaterialTypeBlock;
import muramasa.antimatter.material.MaterialTypeItem;
import muramasa.antimatter.material.SubTag;
import muramasa.antimatter.network.AntimatterNetwork;
import muramasa.antimatter.ore.BlockOre;
import muramasa.antimatter.ore.StoneType;
import muramasa.antimatter.proxy.ClientHandler;
import muramasa.antimatter.proxy.CommonHandler;
import muramasa.antimatter.proxy.IProxyHandler;
import muramasa.antimatter.proxy.ServerHandler;
import muramasa.antimatter.recipe.Recipe;
import muramasa.antimatter.recipe.RecipeBuilders;
import muramasa.antimatter.recipe.container.ContainerItemShapedRecipe;
import muramasa.antimatter.recipe.container.ContainerItemShapelessRecipe;
import muramasa.antimatter.recipe.ingredient.IngredientSerializer;
import muramasa.antimatter.recipe.ingredient.PropertyIngredient;
import muramasa.antimatter.recipe.material.MaterialSerializer;
import muramasa.antimatter.recipe.serializer.MachineRecipeSerializer;
import muramasa.antimatter.registration.RegistrationEvent;
import muramasa.antimatter.tool.IAntimatterTool;
import muramasa.antimatter.util.TagUtils;
import muramasa.antimatter.worldgen.AntimatterWorldGenerator;
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
        AntimatterDynamics.clientProvider(Ref.ID,
                () -> new AntimatterBlockStateProvider(Ref.ID, Ref.NAME.concat(" BlockStates")));
        AntimatterDynamics.clientProvider(Ref.ID,
                () -> new AntimatterItemModelProvider(Ref.ID, Ref.NAME.concat(" Item Models")));
        AntimatterDynamics.clientProvider(Ref.SHARED_ID,
                () -> new AntimatterBlockStateProvider(Ref.SHARED_ID, "Antimatter Shared BlockStates"));
        AntimatterDynamics.clientProvider(Ref.SHARED_ID,
                () -> new AntimatterItemModelProvider(Ref.SHARED_ID, "Antimatter Shared Item Models"));
        AntimatterDynamics.clientProvider(Ref.ID,
                () -> new AntimatterLanguageProvider(Ref.ID, Ref.NAME.concat(" en_us Localization"), "en_us"));
        AntimatterDynamics.clientProvider(Ref.SHARED_ID,
                () -> new AntimatterLanguageProvider(Ref.SHARED_ID, Ref.NAME.concat(" en_us Localization (Shared)"), "en_us"));
        AntimatterAPI.init();
        AntimatterNetwork.register();
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

    public void addCraftingLoaders(AntimatterCraftingEvent ev) {
        ev.addLoader(StoneRecipes::loadRecipes);
        ev.addLoader(MaterialRecipes::init);
    }

    public void providers(AntimatterProvidersEvent ev) {
        final AntimatterBlockTagProvider[] p = new AntimatterBlockTagProvider[1];
        ev.addProvider(() -> {
            p[0] = new AntimatterBlockTagProvider(Ref.ID, Ref.NAME.concat(" Block Tags"), false);
            return p[0];
        });
        ev.addProvider(() -> new AntimatterFluidTagProvider(Ref.SHARED_ID,
                "Antimatter Shared Fluid Tags", false));
        ev.addProvider(() -> new AntimatterItemTagProvider(Ref.ID, Ref.NAME.concat(" Item Tags"),
                false, p[0]));
        ev.addProvider(() -> new AntimatterBlockLootProvider(Ref.ID, Ref.NAME.concat(" Loot generator")));
        ev.addProvider(() -> new AntimatterTagProvider<Biome>(BuiltinRegistries.BIOME, Ref.ID, Ref.NAME.concat(" Biome Tags"), "worldgen/biome") {
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
            AntimatterLoot.RandomWeightLootFunction.init();
            SlotType.init();
            RecipeBuilders.init();
            MachineState.init();
            AntimatterMaterials.init();
            AntimatterMaterialTypes.init();
            AntimatterDefaultTools.init(side);
            AntimatterStoneTypes.init();
            Data.init(side);
            ICover.init();
            SubTag.init();
            AntimatterWorldGenerator.preinit();
            GuiEvents.init();
            MaterialSerializer.init();
            ContainerItemShapedRecipe.init();
            ContainerItemShapelessRecipe.init();
            MachineRecipeSerializer.init();
            IngredientSerializer.init();
            PropertyIngredient.Serializer.init();
        } else if (event == RegistrationEvent.WORLDGEN_INIT) {
            AntimatterWorldGenerator.init();
            AntimatterDefaultTools.postInit();
        } else if (event == RegistrationEvent.DATA_READY) {
            CauldronInteractions.init();
            if (AntimatterAPI.isModLoaded(Ref.MOD_JEI) || AntimatterAPI.isModLoaded(Ref.MOD_REI)){
                AntimatterJEIREIPlugin.registerMissingMaps();
            }
            AntimatterJEIREIPlugin.addItemsToHide(l -> {
                if (!AntimatterConfig.SHOW_ALL_ORES.get()){
                    AntimatterAPI.all(StoneType.class, s -> {
                        if (s != AntimatterStoneTypes.STONE && s != AntimatterStoneTypes.SAND && s.doesGenerateOre()){
                            AntimatterMaterialTypes.ORE.all().forEach(m -> {
                                Block ore = AntimatterMaterialTypes.ORE.get().get(m, s).asBlock();
                                if (ore instanceof BlockOre){
                                    l.add(ore);
                                }
                            });
                            AntimatterMaterialTypes.ORE_SMALL.all().forEach(m -> {
                                Block ore = AntimatterMaterialTypes.ORE_SMALL.get().get(m, s).asBlock();
                                if (ore instanceof BlockOre){
                                    l.add(ore);
                                }
                            });
                        }
                    });

                }
                if (!AntimatterConfig.SHOW_ROCKS.get()){
                    AntimatterMaterialTypes.BEARING_ROCK.all().forEach(m -> {
                        AntimatterAPI.all(StoneType.class, s -> {
                            if (s.doesGenerateOre()) {
                                l.add(AntimatterMaterialTypes.BEARING_ROCK.get().get(m, s).asBlock());
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
                AntimatterAPI.all(IAntimatterTool.class).stream().filter(t -> t.getAntimatterToolType() == AntimatterDefaultTools.WRENCH_ALT).forEach(tool -> l.add(tool.getItem()));
                AntimatterAPI.all(AntimatterFluid.class).forEach(t -> l.add(t.getFluidBlock()));
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
            AntimatterModelManager.init();
            ClientData.init();
            if (AntimatterConfig.OVERRIDE_BASALT_TEXTURE.get()){
                try {
                    AntimatterDynamics.DYNAMIC_RESOURCE_PACK.addTexture(new ResourceLocation("block/basalt_top"), readImage("block/stone/basalt/stone"));
                    AntimatterDynamics.DYNAMIC_RESOURCE_PACK.addTexture(new ResourceLocation("block/basalt_side"), readImage("block/stone/basalt/stone"));
                    AntimatterDynamics.DYNAMIC_RESOURCE_PACK.addTexture(new ResourceLocation("block/smooth_basalt"), readImage("block/stone/basalt/smooth"));
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
        AntimatterDynamics.runDataProvidersDynamically();
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
        AntimatterDynamics.setInitialized();
        LOGGER.info("AntimatterAPI Data Processing has Finished. All Data Objects can now be Modified!");
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
        AntimatterDynamics.runDataProvidersDynamically();
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
